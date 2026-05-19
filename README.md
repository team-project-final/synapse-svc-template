# synapse-engagement-svc — W2 skeleton

> **추가**: `global/` 횡단 관심사 + JWT + 프로파일.

## 📂 구조

```
com.synapse.engagement/
├── EngagementApplication.java
├── community/ gamification/    ← W1 그대로
└── global/                     ← NEW
    ├── config/{SecurityConfig, RedisConfig}
    ├── exception/{ErrorCode (COM___, GAM___), BusinessException, GlobalExceptionHandler}
    ├── response/ApiResponse<T>
    ├── security/{JwtTokenProvider, JwtAuthFilter}
    └── util/
```

## engagement 특이점

### gamification 보호 — 점수 변경은 외부에 노출 X

`SecurityConfig`에서 점수 조회만 일부 permitAll(leaderboard 등), 점수 변경 API는 **컨트롤러 자체가 없음** (W3에서 Kafka 이벤트만 트리거).

```java
.requestMatchers("/actuator/health", "/api/v1/gamification/leaderboard").permitAll()
.anyRequest().authenticated()
```

리더보드는 게스트도 보지만, `users/{id}/score`/`badges`는 인증 필요.

## 🚀 W3에서 추가되는 것들

community/W2 README와 동일 패턴. **gamification 특수**:

W3에서 gamification에 4개 Kafka 컨슈머가 추가됩니다 — 단일 도메인이 **4개 외부 이벤트 소스를 동시 구독**하는 fan-in 구조.

| 외부 이벤트 (W3) | 트리거되는 보상 |
|---|---|
| `synapse.platform.auth.user-registered.v1` | "EARLY_ADOPTER" 뱃지 |
| `synapse.engagement.community.comment-created.v1` | 댓글 카운트 → "COMMENTER_X" 뱃지 |
| `synapse.learning.card.card-reviewed.v1` | 복습 카운트 → "STUDENT_X" 뱃지 |
| `synapse.knowledge.note.created.v1` | 노트 카운트 → "WRITER_X" 뱃지 |

community는 1개 producer만 (다른 서비스/도메인이 댓글 카운트를 듣기 위해). 발행자보다 **수신자가 훨씬 많은 도메인**이라는 특수성.

자세한 통증 → 해법은 [platform/w2의 W3 항목 섹션](https://github.com/team-project-final/synapse-svc-template/blob/skeleton/platform/w2/README.md) 참고.

## 🔭 다음 주차

- `skeleton/engagement/w3` — 4-way fan-in 컨슈머 + community producer
- `skeleton/engagement/w4` — 라이트 헥사고날 + ArchUnit (gamification policy 격리 강조)
