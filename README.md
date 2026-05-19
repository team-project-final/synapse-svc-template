# synapse-engagement-svc — W4 skeleton (최종)

> **추가**: 라이트 헥사고날 + ArchUnit. **engagement의 핵심: `gamification/domain/policy/`에 룰 격리.**

---

## 📂 W4 구조

```
com.synapse.engagement/
├── EngagementApplication.java
│
├── community/
│   ├── api/{PostController, dto/}
│   ├── application/{PostService, port/{PostPort, CommentPort, EventPort}}
│   ├── domain/{Post, Comment, policy/PostValidationPolicy}
│   └── infrastructure/
│       ├── persistence/{PostJpaRepository, CommentJpaRepository, CommunityPersistenceAdapter}
│       └── messaging/CommunityEventKafkaAdapter
│
├── gamification/
│   ├── api/{GamificationController, dto/}
│   ├── application/{GamificationService, port/{PointPort, BadgePort}}
│   ├── domain/
│   │   ├── Point.java, Badge.java
│   │   └── policy/                                ← engagement의 핵심
│   │       ├── PointPolicy.java                    활동 → 점수 매핑
│   │       └── BadgeAchievementPolicy.java         누적 → 뱃지 단계
│   └── infrastructure/
│       ├── persistence/{PointJpaRepository, BadgeJpaRepository, GamificationPersistenceAdapter}
│       └── messaging/                              fan-in 컨슈머 4개
│           ├── UserRegisteredKafkaConsumer         platform 이벤트
│           ├── CommentCreatedKafkaConsumer         community 이벤트
│           ├── CardReviewedKafkaConsumer           learning 이벤트
│           └── NoteCreatedKafkaConsumer            knowledge 이벤트
│
└── global/   ← W2~W3 그대로
```

---

## 🎯 engagement의 W4 핵심: `domain/policy/` 격리

다른 서비스의 policy는 "검증 룰" 수준이지만, gamification의 policy는 **비즈니스 자체**:

### `PointPolicy` — 활동 → 점수 매핑

```java
public final class PointPolicy {
    public static long pointsFor(String reason, int rawValue) {
        return switch (reason) {
            case REASON_COMMENT -> 10;
            case REASON_NOTE_WRITE -> 20;
            case REASON_CARD_REVIEW -> rawValue * 5L;
            case REASON_FIRST_LOGIN -> 100;
            default -> 0;
        };
    }
}
```

운영 중 "댓글 점수 10→15로 바꿔주세요"가 자주 들어옵니다. 이 한 파일만 수정. Spring 안 띄움. 단위 테스트 100ms.

### `BadgeAchievementPolicy` — 누적 → 뱃지

```java
private static final List<Threshold> THRESHOLDS = List.of(
    new Threshold(100,      "STARTER"),
    new Threshold(1_000,    "ENTHUSIAST"),
    new Threshold(10_000,   "EXPERT"),
    new Threshold(100_000,  "LEGEND")
);
```

새 뱃지 추가도 이 한 줄. **운영진이 직접 수정할 수도 있을 만큼 단순**.

### 컨슈머는 policy를 통해서만 점수 부여

```java
// CardReviewedKafkaConsumer
gamificationService.awardForEvent(event.userId(), PointPolicy.REASON_CARD_REVIEW, event.quality());

// GamificationService
public void awardForEvent(Long userId, String reason, int rawValue) {
    long amount = PointPolicy.pointsFor(reason, rawValue);   // ← policy 위임
    pointPort.save(new Point(userId, amount, reason));
}
```

컨슈머·서비스에는 점수 매핑이 **하드코딩되지 않음**. 점수 조정 = policy 한 파일.

---

## 🛡 ArchUnit 7룰 (engagement 표준)

| # | 룰 | 비고 |
|---|---|---|
| 1 | 도메인 슬라이스 격리 (community ↛ gamification) | global 예외 |
| 2 | domain → 다른 계층 의존 금지 | |
| 3 | application → api/infrastructure 의존 금지 (port 예외) | |
| 4 | api → infrastructure 의존 금지 | |
| 5 | **domain.policy 외부 의존 0 — engagement에선 가장 중요한 룰** | |
| 6 | JpaRepository = infrastructure.persistence만 | |
| 7 | @KafkaListener = infrastructure.messaging만 | |

knowledge처럼 "controller-less 예외"는 불필요 (community/gamification 모두 컨트롤러 보유).

---

## 🔄 W3 → W4 변화 요약

| 항목 | W3 | W4 |
|---|---|---|
| 점수 매핑 | 컨슈머마다 하드코딩 (`awardPoint(uid, 10, ...)`) | `PointPolicy.pointsFor()` 한 곳에 |
| Persistence | JpaRepository 직접 service에서 | port 인터페이스 + Adapter |
| 의존 방향 | application → infrastructure | application ← infrastructure |
| 강제 | 컨벤션 | ArchUnit 7룰 |

자세한 통증 → 해법: [platform/w3 README의 W4 항목 섹션](https://github.com/team-project-final/synapse-svc-template/blob/skeleton/platform/w3/README.md).

---

## 🎓 engagement가 W4에서 가장 잘 보여주는 것

**"비즈니스 룰을 어디에 두는가"의 모범 사례.**

- 점수·뱃지 변경이 잦은 도메인 = policy 격리의 가치가 가장 큼
- 단위 테스트가 Spring 없이 가능 → CI에서 점수 룰 수정 PR이 1초 안에 검증
- 운영진/PM이 코드를 읽기 쉬움 → `PointPolicy.java` 한 장만 보여줘도 점수 체계 이해

policy 격리가 의미 없는 도메인(단순 CRUD)에서 이 패턴을 쓰면 오버엔지니어링.
policy 격리가 핵심인 도메인(gamification)에서는 이 패턴이 자연스러움.

---

## 다음 단계 (template 외부)

W4가 마지막 — 실제 synapse-engagement-svc 레포로 옮기고:
1. synapse-shared 의존성 활성화 → `global/kafka/event/` 삭제
2. PointPolicy를 YAML로 외부화 검토 (운영진 직접 수정 가능하게)
3. 리더보드 SQL aggregation 구현
