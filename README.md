# synapse-platform-svc — W3 skeleton

> **추가**: 도메인별 `kafka/` 패키지 (producer/consumer) + Kafka 인프라 설정.
> 도메인 간 직접 호출은 **0건**. `auth → notification` 환영 알림 = 이벤트 경유.

## 패키지 구조 (W3)

```
src/main/java/com/synapse/platform/
├── PlatformApplication.java
├── auth/
│   ├── controller/ service/ repository/ entity/ dto/
│   └── kafka/                                        ← NEW
│       └── producer/UserEventPublisher
├── audit/
│   ├── controller/ service/ repository/ entity/ dto/
│   └── kafka/                                        ← NEW
│       └── consumer/AuditEventConsumer               (topicPattern: synapse.*)
├── billing/
│   ├── controller/ service/ repository/ entity/ dto/
│   └── kafka/                                        ← NEW
│       ├── producer/BillingEventPublisher
│       └── consumer/PaymentCompletedConsumer         (외부 PG 콜백)
├── notification/
│   ├── controller/ service/ repository/ entity/ dto/
│   └── kafka/                                        ← NEW
│       └── consumer/
│           ├── UserRegisteredConsumer                (auth → 환영 알림)
│           └── NotificationRequestedConsumer         (광역 알림 요청)
└── global/
    ├── config/
    │   ├── KafkaConfig.java                          ← NEW (producer/consumer 팩토리)
    │   ├── SecurityConfig.java
    │   └── RedisConfig.java
    └── kafka/event/                                  ← NEW (임시 stub)
        ├── UserRegistered.java
        ├── BillingChargeRequested.java
        ├── PaymentCompleted.java
        └── NotificationRequested.java
```

## 이벤트 토픽 컨벤션

`synapse.{service}.{domain}.{event-name}.v{version}` — 도메인 간 통신은 이 네이밍.

| 토픽 | Publisher | Consumer |
|---|---|---|
| `synapse.platform.auth.user-registered.v1` | auth | audit, notification |
| `synapse.platform.billing.charge-requested.v1` | billing | audit, (외부 PG) |
| `synapse.notification.requested.v1` | (광역) | notification |
| `synapse.external.pg.payment-completed.v1` | PG 외부 | billing |
| `synapse.*` (패턴 매칭) | (모든 도메인) | audit |

## shared-events 마이그레이션 예정

`global/kafka/event/`의 record들은 임시 stub.
synapse-shared 멀티모듈 publish 후 `com.synapse.shared.event.*`로 교체 (build.gradle.kts 주석 참조).

## W2 → W3 변화 요약

| 항목 | W2 | W3 |
|---|---|---|
| 도메인 간 통신 | 미정의 (사실상 직접 호출 가능) | Kafka 이벤트 only |
| 의존성 | + security + jjwt + redis | + spring-kafka + spring-kafka-test |
| 설정 | 4 프로파일 | + `spring.kafka.*` |
| 도메인 패키지 | controller/service/repository/entity/dto | + `kafka/{producer,consumer}/` |

## 다음 주차

- `skeleton/platform/w4` — 각 도메인을 `api/application/domain/infrastructure`로 재구성 + ArchUnit으로 강제
