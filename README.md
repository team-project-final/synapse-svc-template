# synapse-engagement-svc — W1 skeleton

> **한 줄 정의**: "community(게시판/댓글) + gamification(포인트/뱃지) 2 도메인. W3에서 gamification이 **fan-in 컨슈머**로 진가 발휘."

---

## 🎯 이 단계의 목표

- [x] community/gamification 2 도메인 split
- [x] gamification이 왜 다른 서비스들의 이벤트를 듣는 fan-in 패턴인지 감 잡기

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
