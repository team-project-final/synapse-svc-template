# synapse-engagement-svc — W3 skeleton

> **추가**: gamification이 **4-way fan-in 컨슈머**로 변신. 외부 3개 서비스 + 자체 community 이벤트를 통합 수신.

---

## 🎯 fan-in 구조 — engagement만의 특수성

```
                            ┌──── synapse.platform.auth.user-registered.v1
                            │     (platform 서비스)
                            │
                            ├──── synapse.engagement.community.comment-created.v1
                            │     (자체 community 도메인)
                            │
   gamification consumer ◀──┤
   (4개 @KafkaListener)     ├──── synapse.learning.card.card-reviewed.v1
                            │     (learning 서비스)
                            │
                            └──── synapse.knowledge.note.created.v1
                                  (knowledge 서비스)
```

gamification은 **4개 외부 이벤트 소스**를 동시에 듣고, 각각 다른 점수·뱃지 정책을 적용. 이게 fan-in의 본질.

> 💡 **왜 한 도메인에 컨슈머가 4개나?** "활동 기반 보상"이라는 단일 책임이지만, 활동 소스가 여러 곳. **책임은 하나**, **입력만 여럿**. 컨슈머 분리는 토픽이 다르기 때문일 뿐.

---

## 📂 W3 추가 구조

```
com.synapse.engagement/
├── community/
│   ├── controller/ service/ repository/ entity/ dto/
│   └── kafka/                                          ← NEW
│       └── producer/CommunityEventPublisher             CommentCreated 발행
│
├── gamification/
│   ├── controller/ service/ repository/ entity/ dto/
│   └── kafka/                                          ← NEW
│       └── consumer/                                    fan-in 4개!
│           ├── UserRegisteredConsumer                   platform → EARLY_ADOPTER
│           ├── CommentCreatedConsumer                   community → 댓글 포인트
│           ├── CardReviewedConsumer                     learning → 복습 포인트
│           └── NoteCreatedConsumer                      knowledge → 작성 포인트
│
└── global/
    ├── config/{KafkaConfig (NEW), ...}
    └── kafka/event/                                    ← NEW (4개 이벤트 stub)
        ├── UserRegistered.java (외부 → 임시 stub)
        ├── CommentCreated.java
        ├── CardReviewed.java
        └── NoteCreated.java
```

---

## 📛 토픽 일람 (engagement 관점)

| 토픽 | 방향 | 발행자/수신자 | 보상 |
|---|---|---|---|
| `synapse.engagement.community.comment-created.v1` | OUT | community 발행 | (다른 서비스도 구독 가능) |
| `synapse.platform.auth.user-registered.v1` | IN | gamification 수신 | EARLY_ADOPTER 뱃지 |
| `synapse.learning.card.card-reviewed.v1` | IN | gamification 수신 | 복습 quality × 5점 |
| `synapse.knowledge.note.created.v1` | IN | gamification 수신 | 20점 |

**OUT 1 / IN 3 = 수신이 발행보다 많은 도메인.** 다른 서비스들과 정반대 비율.

---

## 🔁 W2 → W3 변화 요약

| 항목 | W2 | W3 |
|---|---|---|
| gamification 동작 | API 호출 시에만 (스텁) | 4개 외부 이벤트로 자동 트리거 |
| community 발행 | (없음) | CommentCreated 발행 |
| shared-events 의존 | 없음 | 4개 이벤트 클래스 (스텁) |
| 컨슈머 groupId | (없음) | `synapse-engagement-gamification` (4개 토픽 한 그룹) |

> 💡 **모든 fan-in 컨슈머가 같은 groupId** — engagement-svc 인스턴스가 N개 있어도 한 메시지는 한 인스턴스만 처리.

자세한 통증 → 해법은 [platform/w3 README](https://github.com/team-project-final/synapse-svc-template/blob/skeleton/platform/w3/README.md).

---

## 🚀 W4에서 추가되는 것들 — engagement 특수

### gamification의 `domain/policy/`가 핵심

다른 도메인에서 policy는 "검증 룰" 수준이지만, gamification에서는 **점수 매핑 정책 자체**가 핵심 비즈니스:

```java
// W4: domain/policy/PointPolicy.java
public final class PointPolicy {
    public static long forCommentCreated() { return 10; }
    public static long forCardReviewed(int quality) { return quality * 5L; }
    public static long forNoteCreated() { return 20; }
}

// domain/policy/BadgeAchievementPolicy.java
public final class BadgeAchievementPolicy {
    public static String evaluate(long totalPoints, int commentCount, int noteCount) {
        if (totalPoints >= 10000) return "LEGEND";
        // ...
    }
}
```

**왜 격리?** 운영하면서 "댓글 점수 10→15로 바꾸자"가 자주 일어남. 정책 한 파일만 수정하면 끝. 인프라/API 코드 안 봐도 됨. 단위 테스트도 Spring 없이.

### W4 변화 요약

| 항목 | 일반 |
|---|---|
| api / application / domain / infrastructure 재구성 | 다른 svc와 동일 |
| `domain/policy/` 격리 강조 | **engagement는 룰이 비즈니스 자체** |
| ArchUnit | 7룰 표준 (controller-less 예외 불필요) |

자세한 통증 → 해법: [platform/w3 README의 W4 항목 섹션](https://github.com/team-project-final/synapse-svc-template/blob/skeleton/platform/w3/README.md).

---

## 🔭 다음

- `skeleton/engagement/w4` — 라이트 헥사고날 + ArchUnit + gamification policy 격리
