# synapse-engagement-svc — W1 skeleton

> **한 줄 정의**: "community(게시판/댓글) + gamification(포인트/뱃지) 2 도메인. W3에서 gamification이 **fan-in 컨슈머**로 진가 발휘."

---

## 🎯 이 단계의 목표

- [x] community/gamification 2 도메인 split
- [x] gamification이 왜 다른 서비스들의 이벤트를 듣는 fan-in 패턴인지 감 잡기

---

## 🧭 큰 그림: 왜 도메인을 먼저 나누나요?

### ❌ 안티패턴 — 계층(layer)만으로 나누면?

```
src/main/java/com/synapse/engagement/
├── controller/   PostController, GamificationController
├── service/      PostService, GamificationService
├── repository/   PostRepository, CommentRepository, PointRepository, BadgeRepository
└── entity/       Post, Comment, Point, Badge
```

도메인 2개, entity 4개로 시작하지만:
1. **`repository/` 폴더에 4개 섞임** — 어느 도메인 것인지 한눈에 안 옴.
2. **도메인 격리 위반 유혹** — `GamificationService`가 `PostRepository`를 직접 호출하기 쉬움 (실은 절대 안 됨 — 댓글 카운트는 Kafka 이벤트로 받아야).
3. **gamification 분리 어려움** — 향후 별도 서비스로 추출 시 사방에 흩어진 폴더 추적 비용.

### ✅ 도메인 우선 분리 (이 프로젝트 채택)

```
src/main/java/com/synapse/engagement/
├── community/        ← 도메인 (Post, Comment 모두 여기)
│   ├── controller/ service/ repository/ entity/ dto/
└── gamification/     ← 도메인 (Point, Badge 모두 여기)
    ├── controller/ service/ repository/ entity/ dto/
```

- **응집도 높음** — gamification 작업 시 `gamification/` 폴더 하나만.
- **격리 시각화** — `GamificationService`가 `import com.synapse.engagement.community.*` 하면 누가 봐도 어색.
- **fan-in 강조** — W3에서 gamification이 외부 4개 서비스 이벤트를 받는데, 직접 호출 유혹을 처음부터 차단.

---

## 🧱 5계층의 정석

| 계층 | 역할 | 예시 (community) |
|---|---|---|
| **controller/** | HTTP 요청 받음 → service에 위임. 비즈니스 로직 금지. | `PostController.createPost()` → `postService.createPost(req)` |
| **service/** | "무엇을 할지" 조립. 트랜잭션 경계. | `PostService.createComment()`가 Comment entity 생성 + repository 저장 |
| **repository/** | DB 접근만. JpaRepository 자동 CRUD. | `PostRepository extends JpaRepository<Post, Long>` |
| **entity/** | DB 테이블 매핑. `@Entity`. | `Post { id, authorId, title, body }`, `Comment { id, postId, authorId, body }` |
| **dto/** | API 입출력 봉투. **entity 노출 금지**. | `CreatePostRequest`, `PostResponse` |

각 계층은 **아래만** 의존, 위는 모름 (단방향). gamification은 점수 변경 API를 외부에 노출하지 않아 controller가 read-only — controller도 도메인 책임에 맞게 좁힐 수 있는 예시.

---

## 🧭 2개 도메인의 성격

| 도메인 | 성격 | HTTP API |
|---|---|---|
| **community** | CRUD — 게시글, 댓글 | `POST/GET /api/v1/community/posts`, `POST .../comments` |
| **gamification** | **Read-only API + 광역 Event 소비** | `GET /api/v1/gamification/users/{id}/score`, `/badges`, `/leaderboard` |

### gamification의 진짜 모습

gamification은 표면적으로 "포인트·뱃지 조회 API"지만, 본질은:

```
[W3에서 추가될 흐름]
                  ┌─ platform/UserRegistered    → "회원가입" 첫 가입 뱃지
                  ├─ community/CommentCreated  → "댓글 N개" 뱃지
                  ├─ learning/CardReviewed      → "복습 연속" 뱃지
                  └─ knowledge/NoteCreated      → "노트 N개" 뱃지
                          ↓
                  gamification 컨슈머 (fan-in)
                          ↓
                  Point/Badge 저장
                          ↓
                  GET /api/v1/gamification/users/{id}/score  ← 조회만 HTTP
```

**fan-in 패턴**: 한 도메인이 여러 외부 서비스의 이벤트를 동시 구독해서 통합하는 패턴. **shared-events 의존이 가장 두꺼운 도메인**.

### W1에서는 어떻게?

```java
// W1 — 수동 호출용 메서드만 존재
gamificationService.awardPoint(userId, 100, "FIRST_POST");
gamificationService.awardBadge(userId, "EARLY_ADOPTER");
```

다른 도메인이 직접 호출하면 컨벤션 위반. W1은 단위 테스트로만 검증 가능 (자기 자신).

---

## 📂 패키지 구조 (W1)

```
src/main/java/com/synapse/engagement/
├── EngagementApplication.java
│
├── community/
│   ├── controller/PostController          POST/GET /posts + POST /posts/{id}/comments
│   ├── service/PostService                Post + Comment 양쪽 처리
│   ├── repository/                         PostRepository + CommentRepository
│   ├── entity/                             Post, Comment
│   └── dto/{request,response}/
│
└── gamification/
    ├── controller/GamificationController  조회 전용 (score, badges, leaderboard)
    ├── service/GamificationService         조회 + awardPoint/awardBadge (W3에서 컨슈머가 호출)
    ├── repository/                         PointRepository + BadgeRepository
    ├── entity/                             Point, Badge
    └── dto/response/                       UserScoreResponse, BadgeResponse
```

---

## ▶️ 실행

```bash
./gradlew bootRun
```

기본 포트 `8082` (platform=8080, knowledge=8081 충돌 방지).

```bash
# community
curl -X POST http://localhost:8082/api/v1/community/posts \
  -H "Content-Type: application/json" \
  -d '{"authorId":1,"title":"Hello","body":"World"}'

# 댓글
curl -X POST http://localhost:8082/api/v1/community/posts/1/comments \
  -H "Content-Type: application/json" \
  -d '{"authorId":2,"body":"Nice"}'

# gamification (W1에선 빈 응답 — 이벤트 수신 인프라가 W3에서 추가됨)
curl http://localhost:8082/api/v1/gamification/users/1/score
```

---

## 🔭 다음 주차

- `skeleton/engagement/w2` — `global/` (config·exception·response·security·util)
- `skeleton/engagement/w3` — **gamification fan-in** (3 외부 서비스 이벤트 구독)
- `skeleton/engagement/w4` — 라이트 헥사고날 + ArchUnit (gamification policy 격리 강조)

---

## 🚀 W2에서 추가되는 것들

platform/W1 README의 "W2에서 추가되는 것들" 섹션과 동일 — 도메인이 2개라 통증 강도는 약간 낮지만 패턴은 같습니다.

engagement 특수: gamification은 **외부 사용자에게 점수 조작 API를 노출하지 않습니다.** 점수 증감은 항상 이벤트 기반. 컨트롤러는 조회 전용. W2에서 `SecurityConfig`로도 보장 (조회 엔드포인트만 노출).
