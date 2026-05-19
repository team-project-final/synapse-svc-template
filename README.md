# synapse-platform-svc — W3 skeleton

> **한 줄 정의**: "도메인끼리 직접 호출하는 대신 Kafka 이벤트로 통신하는 단계."
> 이 단계 이후로는 `BillingService`가 `AuthService`를 import하는 일이 **불가능**해야 합니다.

이 문서는 **Kafka를 처음 보는 신입/주니어**가 "왜 멀쩡한 메서드 호출 대신 이벤트라는 걸 쓰나"를 이해하도록 작성되었습니다.

---

## 🎯 이 단계의 목표

W3을 마치면 여러분은:
- [x] 동기 호출(메서드 호출)과 이벤트(메시지 발행)의 차이를 안다
- [x] Producer / Consumer / Topic / Consumer Group이 뭔지 안다
- [x] 토픽 네이밍 컨벤션을 이해하고 새 토픽을 만들 수 있다
- [x] `@KafkaListener`로 이벤트를 받고, `KafkaTemplate`으로 발행하는 코드를 쓸 수 있다
- [x] auth가 `UserRegistered`를 발행 → notification이 받아 환영 알림 보내는 흐름을 설명할 수 있다

---

## 🧭 큰 그림: 왜 직접 호출 대신 이벤트?

### ❌ 직접 호출 방식의 문제

회원가입 직후 환영 메일을 보낸다고 합시다. 직관적으로는:

```java
// auth/service/AuthService.java
@Service
public class AuthService {
    private final NotificationService notificationService;   // 다른 도메인 의존!

    public void register(SignupRequest req) {
        User user = userRepo.save(...);
        notificationService.sendWelcomeEmail(user.getEmail());   // 직접 호출
    }
}
```

이게 왜 나쁜지 5가지:

1. **강결합 (Tight Coupling)** — `auth`가 `notification`을 알아야 함. notification API가 바뀌면 auth도 수정.
2. **장애 전파** — notification 서버가 죽으면 회원가입 전체가 실패. "메일 못 보냈으니 가입도 안 됩니다."
3. **느림** — auth는 메일 발송이 끝날 때까지 응답을 못 함. 1초 → 3초 → 사용자 이탈.
4. **확장 불가** — 다음에 audit도 회원가입을 로깅하고 싶다면? `auth.register()`에 또 `auditService.log()` 추가. 회원가입 추가 동작마다 auth 수정.
5. **테스트 어려움** — `AuthService` 단위 테스트에 NotificationService mock 필요.

### ✅ 이벤트 방식

auth는 "회원가입이 일어났다"는 **사실(이벤트)**만 발행:

```java
// auth/kafka/producer/UserEventPublisher.java
@Component
public class UserEventPublisher {
    public void publishUserRegistered(UserRegistered event) {
        kafkaTemplate.send("synapse.platform.auth.user-registered.v1", event.userId().toString(), event);
    }
}
```

누가 그 이벤트를 받든 auth는 **관심 없음**. 알아서 구독:

```java
// notification/kafka/consumer/UserRegisteredConsumer.java
@Component
public class UserRegisteredConsumer {
    @KafkaListener(topics = "synapse.platform.auth.user-registered.v1", groupId = "...")
    public void onUserRegistered(UserRegistered event) {
        notificationService.sendWelcome(event.userId(), event.email());
    }
}
```

5가지 문제 모두 해결:

1. **느슨한 결합** — auth는 notification 클래스를 모름. Kafka만 알면 됨.
2. **장애 격리** — notification이 죽어도 회원가입은 성공. 메시지는 Kafka에 쌓여 있다가 부활 후 처리.
3. **빠름** — auth는 발행만 하고 즉시 응답.
4. **확장 자유** — audit·gamification·marketing도 같은 토픽 구독만 추가. auth 코드 0줄 수정.
5. **테스트 간단** — Producer mock만, 다른 도메인 mock 불필요.

이게 **이벤트 기반 아키텍처(Event-Driven Architecture)**의 핵심.

---

## 📂 W3에서 추가된 구조

```
src/main/java/com/synapse/platform/
├── PlatformApplication.java
├── auth/
│   ├── controller/ service/ repository/ entity/ dto/   ← W1~W2 그대로
│   └── kafka/                                          ← NEW
│       └── producer/UserEventPublisher                  (UserRegistered 발행)
├── audit/
│   ├── controller/ service/ ...
│   └── kafka/
│       └── consumer/AuditEventConsumer                  (topicPattern "synapse.*")
├── billing/
│   ├── controller/ service/ ...
│   └── kafka/
│       ├── producer/BillingEventPublisher               (ChargeRequested 발행)
│       └── consumer/PaymentCompletedConsumer             (외부 PG 콜백 수신)
├── notification/
│   ├── controller/ service/ ...
│   └── kafka/
│       └── consumer/
│           ├── UserRegisteredConsumer                   (auth → 환영 알림)
│           └── NotificationRequestedConsumer            (광역 알림 요청)
└── global/
    ├── config/
    │   ├── KafkaConfig.java                             ← NEW (factory + EnableKafka)
    │   ├── SecurityConfig.java
    │   └── RedisConfig.java
    └── kafka/event/                                     ← NEW (임시 stub)
        ├── UserRegistered.java
        ├── BillingChargeRequested.java
        ├── PaymentCompleted.java
        └── NotificationRequested.java
```

### 핵심 설계 선택

| 결정 | 이유 |
|---|---|
| `kafka/`를 도메인 안에 둠 | "이 도메인이 어떤 이벤트를 발행/수신하는가"가 한눈에 보임. |
| `producer/`와 `consumer/`를 분리 | 발행 코드와 수신 코드는 책임이 다름. 한 도메인이 양쪽일 수 있음 (billing). |
| 이벤트 클래스는 `global/kafka/event/` | 여러 도메인이 공유하므로 도메인 안에 두면 안 됨. **임시**로 여기 두고, 추후 `synapse-shared` 라이브러리로 이전. |

---

## 🔄 핵심 개념 빠르게 — Kafka 입문

### 1. **Topic (토픽)**: "이벤트의 종류 = 채널"

`synapse.platform.auth.user-registered.v1`은 토픽 이름. 발행자도 수신자도 이 이름으로 약속.

### 2. **Producer (생산자)**: 토픽에 메시지를 던지는 쪽

`KafkaTemplate.send(topic, key, value)`. key는 같은 사용자의 이벤트가 같은 파티션에 들어가도록 보장(순서 유지).

### 3. **Consumer (소비자)**: 토픽을 구독해서 메시지를 받는 쪽

`@KafkaListener(topics = ..., groupId = ...)`. groupId가 핵심 ↓

### 4. **Consumer Group (소비자 그룹)**: 같은 일을 분담하는 컨슈머 묶음

같은 `groupId`를 가진 컨슈머들은 **한 메시지를 한 번만** 처리 (작업 분담).
다른 `groupId`는 **각각 독립적으로** 같은 메시지를 받음 (브로드캐스트 효과).

예시:
- `audit` 컨슈머의 `groupId = "synapse-platform-audit"` → audit 인스턴스가 3개 있어도 한 메시지는 1번만 audit됨.
- `notification`의 `groupId = "synapse-platform-notification"` → audit와 별개라 동일 메시지를 또 받음.

### 5. **Offset (오프셋)**: "어디까지 읽었는지" 북마크

Consumer가 종료됐다가 다시 떠도 마지막 읽은 위치부터 재개.

### 6. **Partition (파티션)**: 토픽을 쪼갠 단위

- 같은 key의 메시지는 항상 같은 파티션 → 순서 보장.
- 다른 키는 다른 파티션 → 병렬 처리.

---

## 📛 토픽 네이밍 컨벤션

```
synapse.{service}.{domain}.{event-name}.v{version}
```

- `synapse.` = 회사/제품 prefix.
- `{service}` = `platform`, `knowledge`, `engagement`, `learning`, `external`.
- `{domain}` = `auth`, `billing`, `pg`, …
- `{event-name}` = **kebab-case**, **과거형 동사**. `user-registered`, `payment-completed`. "무슨 일이 일어났다"라는 사실.
- `v{N}` = 스키마 버전. **breaking change 시 v2 신설**, v1과 병행 운영 후 deprecate.

| 토픽 | Publisher | Consumer | 의미 |
|---|---|---|---|
| `synapse.platform.auth.user-registered.v1` | auth | audit, notification | "회원가입 완료됨" |
| `synapse.platform.billing.charge-requested.v1` | billing | audit, PG | "결제 요청됨" |
| `synapse.notification.requested.v1` | (광역) | notification | "알림 발송 요청" |
| `synapse.external.pg.payment-completed.v1` | PG 외부 | billing | "결제 외부 처리 완료" |
| `synapse.*` (패턴) | (모든) | audit | "모든 이벤트 = 감사 대상" |

> 🚫 **금지 패턴**: `send-email-to-user`, `do-something`, `request-charge` 같은 **명령형 토픽명**. 이벤트는 "사실"이지 "명령"이 아닙니다. 명령형이 필요하면 `*.command.*` 네임스페이스로 분리.

---

## 📨 이벤트 시나리오 따라가기

### 시나리오: 회원가입 → 환영 알림 (auth → notification)

```
┌─────────────────────────────────────────────────────────────────────────┐
│ 1) Client                                                                │
│    POST /api/v1/auth/register {email, password}                          │
└────────────────────┬────────────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────────────────┐
│ 2) auth/controller/AuthController                                        │
│    → authService.register(req)                                           │
└────────────────────┬────────────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────────────────┐
│ 3) auth/service/AuthService                                              │
│    a) userRepository.save(new User(...))                                 │
│    b) userEventPublisher.publishUserRegistered(                          │
│         new UserRegistered(userId, email, Instant.now())                 │
│       )                                                                   │
│    c) return 즉시 200 OK                                                  │
└────────────────────┬────────────────────────────────────────────────────┘
                     ↓
        ┌─────────────────────────────────┐
        │ Kafka                            │
        │ topic = synapse.platform.auth.   │
        │         user-registered.v1       │
        │ key   = "12345" (userId)         │
        │ value = UserRegistered { ... }   │
        └────────────┬────────────────────┘
                     ↓ (구독자들이 알아서 가져감)
        ┌─────────────────────────────────────────────┐
        ├──→ audit/kafka/consumer/AuditEventConsumer  │ (groupId: audit)
        │     auditService.record(...)                 │
        │                                              │
        └──→ notification/kafka/consumer/              │ (groupId: notification)
              UserRegisteredConsumer                   │
              notificationService.sendWelcome(...)     │
        └─────────────────────────────────────────────┘
```

**핵심 관찰**:
- AuthService는 **NotificationService도 AuditService도 모름**.
- audit/notification은 **AuthService도 서로도 모름**. 각자 Kafka만 봄.
- 새 도메인(예: marketing)이 이 이벤트를 구독하고 싶으면 `@KafkaListener` 하나만 추가.

---

## 🛠 코드 자세히 보기

### Producer (auth/kafka/producer/UserEventPublisher.java)

```java
@Component
public class UserEventPublisher {
    public static final String TOPIC_USER_REGISTERED = "synapse.platform.auth.user-registered.v1";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserRegistered(UserRegistered event) {
        kafkaTemplate.send(TOPIC_USER_REGISTERED, event.userId().toString(), event);
        //                  ↑ topic                ↑ key (파티션 결정)     ↑ value (JSON 직렬화됨)
    }
}
```

**왜 key를 userId로?** 같은 유저의 후속 이벤트(`UserEmailChanged`, `UserDeleted`)가 같은 파티션에 가서 **순서가 보장됨**.

### Consumer (notification/kafka/consumer/UserRegisteredConsumer.java)

```java
@Component
public class UserRegisteredConsumer {
    @KafkaListener(
        topics = "synapse.platform.auth.user-registered.v1",
        groupId = "synapse-platform-notification"   // ← 이 도메인의 작업 분담 단위
    )
    public void onUserRegistered(UserRegistered event) {
        notificationService.sendWelcome(event.userId(), event.email());
    }
}
```

### 광역 Consumer (audit/kafka/consumer/AuditEventConsumer.java)

```java
@KafkaListener(topicPattern = "synapse\\..*", groupId = "synapse-platform-audit")
public void onAnyEvent(Object event) {
    auditService.record(event.getClass().getSimpleName(), 0L);
}
```

- `topicPattern`은 정규식. `synapse.`로 시작하는 **모든** 토픽 구독.
- 새 토픽이 생겨도 audit는 자동으로 받음.

### KafkaConfig (global/config/KafkaConfig.java) 핵심

```java
@Configuration
@EnableKafka     // ← @KafkaListener가 작동하려면 필수
public class KafkaConfig {
    // Producer 설정 — JSON 직렬화, ACK 정책
    @Bean ProducerFactory<String, Object> producerFactory() { ... acks=all ... }
    @Bean KafkaTemplate<String, Object> kafkaTemplate() { ... }

    // Consumer 설정 — JSON 역직렬화, offset 정책
    @Bean ConsumerFactory<String, Object> consumerFactory() {
        ...
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.synapse.*");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    }
}
```

**`acks=all`**: producer는 모든 replica가 메시지를 받았다고 확인할 때까지 대기 → 메시지 손실 방지.
**`earliest`**: 새 컨슈머가 처음 떴을 때 가장 오래된 메시지부터 읽기.

---

## ⚙️ 이벤트 클래스 — `global/kafka/event/`

```java
public record UserRegistered(
    Long userId,
    String email,
    Instant registeredAt
) {}
```

- record로 작성 → 불변, 직렬화 친화적.
- **W3에서는 임시 stub.** synapse-shared 멀티모듈 publish 후 `com.synapse.shared.event.UserRegistered`로 교체할 예정. (build.gradle.kts 주석 참조)
- 이전 시 모든 producer/consumer import 한 줄씩 변경.

### 마이그레이션 체크리스트 (synapse-shared publish 후)

1. `synapse-shared:shared-events 1.0.0` GitHub Packages에 publish
2. `build.gradle.kts`의 `implementation("com.synapse:shared-events:1.0.0")` 주석 해제
3. 모든 producer/consumer/service의 `com.synapse.platform.global.kafka.event.*` → `com.synapse.shared.event.*` 일괄 치환
4. `global/kafka/event/` 패키지 삭제

---

## 🐳 Kafka 로컬 실행

W3 단계에서는 Kafka가 떠 있어야 producer/consumer가 동작합니다. 가장 간단한 방법:

```yaml
# docker-compose.yml (예시 — 실제 파일은 syn 레포 또는 별도 작성)
services:
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    ports:
      - "9092:9092"
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@localhost:9093
      CLUSTER_ID: synapse-local
```

```bash
docker compose up -d kafka
./gradlew bootRun
```

토픽 자동 생성을 원치 않으면 (운영 권장):
```bash
docker exec -it kafka kafka-topics.sh --create \
  --topic synapse.platform.auth.user-registered.v1 \
  --partitions 3 --replication-factor 1 \
  --bootstrap-server localhost:9092
```

---

## 🔄 W2 → W3 변화 요약

| 항목 | W2 | W3 |
|---|---|---|
| 도메인 간 통신 | 미정의 (직접 호출 가능) | **Kafka 이벤트 only** |
| 의존성 | + security + jjwt + redis | + spring-kafka + spring-kafka-test |
| 설정 | 4 프로파일 | + `spring.kafka.*` |
| 도메인 패키지 | controller/service/repository/entity/dto | + `kafka/{producer,consumer}/` |
| `global/` | config/exception/response/security/util | + `global/config/KafkaConfig` + `global/kafka/event/` (임시) |
| 응답 시간 | 동기 처리 시간 합 | producer는 즉시 응답 |

---

## ⚠️ W3에서 자주 하는 실수

### 1. 명령형 토픽명

```
❌ synapse.notification.send-welcome-email.v1     ("보내라" = 명령)
✅ synapse.platform.auth.user-registered.v1       ("일어났다" = 사실)
```

이벤트는 **과거 사실**입니다. 누가 듣고 무엇을 할지는 receiver 마음.

### 2. 컨슈머에서 예외를 그냥 던지기

```java
// ❌ — 예외 던지면 동일 메시지 무한 재시도 (offset 안 넘어감) → 컨슈머 lag 폭증
@KafkaListener(...)
public void on(Event e) {
    if (...) throw new RuntimeException("나쁨");
}
```

해결:
- 비즈니스 예외 → try-catch 후 DLQ(Dead Letter Queue) 토픽으로 전송
- 일시 장애 → Spring Retry / `RetryableTopic` 어노테이션
- 영구 실패 → 로깅 후 ack (다음으로 진행)

### 3. Idempotency(멱등성) 미고려

같은 메시지가 두 번 와도 결과가 같아야 합니다. (네트워크 재시도, 컨슈머 재시작 등)

```java
// ❌ — 이벤트 2번 받으면 알림도 2번
public void on(UserRegistered e) {
    notificationService.sendWelcome(e.userId(), e.email());
}

// ✅ — 처리 기록 확인
public void on(UserRegistered e) {
    if (deliveryLog.alreadyProcessed("UserRegistered", e.userId())) return;
    notificationService.sendWelcome(e.userId(), e.email());
    deliveryLog.markProcessed("UserRegistered", e.userId());
}
```

### 4. 동기 호출 흐름을 그대로 이벤트로 옮기기

❌ 안티패턴 (request-response over Kafka):
```
A → "B에게 처리 요청" 이벤트 발행
A → B의 응답 이벤트 폴링/대기
```

이러면 Kafka의 강점(비동기·디커플링)이 사라집니다. 동기 응답이 정말 필요하면 REST를, 아니면 진짜 비동기로.

### 5. shared-events 없이 이벤트 클래스 양쪽에 복붙

`UserRegistered` 클래스를 auth가 정의하고, notification이 똑같은 클래스를 자기 패키지에 복붙 → 필드 하나 추가하면 한쪽만 변경되어 역직렬화 실패.

**해법**: W3는 임시로 `global/kafka/event/`에 모음. 영구적으로는 `synapse-shared` 라이브러리에서 공유.

### 6. JSON 역직렬화 trust package 누락

```java
props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.synapse.*");
```

이걸 빼면 `IllegalArgumentException: The class is not in the trusted packages` 발생. 보안 기본값이라 명시 필요.

---

## ▶️ 실행 + 검증

### 1. Kafka 띄우기

```bash
docker compose up -d kafka
```

### 2. 앱 실행

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

### 3. 이벤트 발행/수신 확인

```bash
# 1) 등록 데모 호출 (코드에 추가 필요 — 현재는 service.registerDemo로 호출 가능한 형태)
# 2) Kafka 토픽 내용 확인
docker exec -it kafka kafka-console-consumer.sh \
  --topic synapse.platform.auth.user-registered.v1 \
  --from-beginning \
  --bootstrap-server localhost:9092

# 3) 로그에서 consumer 처리 확인
# "UserRegistered → 환영 알림: userId=..."
# "Audit captured event: type=UserRegistered"
```

---

## 🔭 다음 주차 미리보기

| 브랜치 | 추가되는 것 |
|---|---|
| `skeleton/platform/w4` | 각 도메인을 `api/application/{port}/domain/{policy}/infrastructure/{persistence,messaging}`로 재구성 + ArchUnit 7룰로 자동 강제 |

W4에서는 W3의 `kafka/`가 `infrastructure/messaging/`으로, `repository/`가 `infrastructure/persistence/`로 이동합니다. 단순 폴더 이동이 아니라 **Port/Adapter 패턴** 도입.

---

## 🚀 W4에서 추가되는 것들 — 각 항목이 **왜** 필요한가

W3까지 오면서 도메인은 분리됐고 통신도 깔끔해졌습니다. 그런데 한 도메인 안을 들여다보면 service가 **너무 많은 일**을 하고 있습니다. W4는 한 도메인 내부의 책임을 재정리합니다.

### 0️⃣ 출발점: W3에서 service가 얼마나 무거워졌는지

W3 시점의 `AuthService`를 봅시다:

```java
@Service
public class AuthService {
    private final UserRepository userRepository;           // JPA에 결합
    private final UserEventPublisher userEventPublisher;   // Kafka에 결합
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public TokenResponse login(LoginRequest req) {
        // 1) JPA 호출
        User user = userRepository.findByEmail(req.email()).orElseThrow(...);
        // 2) 비즈니스 룰 (비밀번호 정책)
        if (req.password().length() < 10) throw ...;
        if (!hasUpperLowerDigit(req.password())) throw ...;
        // 3) 인프라(BCrypt)
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) throw ...;
        // 4) Kafka 발행
        userEventPublisher.publishLoginSuccess(...);
        // 5) 토큰 발급
        return new TokenResponse(tokenProvider.issueAccessToken(...), ...);
    }
}
```

**5가지 책임**을 한 service가 짊어집니다. 결과:

| 통증 | 구체적 결과 |
|---|---|
| **단위 테스트 무거움** | `AuthService` 테스트에 Spring 컨텍스트 + H2 + Embedded Kafka 모두 필요. 한 테스트 5초+. |
| **인프라 교체 불가능** | JPA → MyBatis, Kafka → RabbitMQ로 바꾸려면 모든 service 수정 |
| **룰의 위치 모호** | "비밀번호 정책이 뭐였지?" → AuthService 다 뒤져야. 다른 service에도 비슷한 룰 흩어짐 |
| **컨벤션 사문화** | "도메인 간 import 금지"가 문서일 뿐, 누가 위반해도 빌드 통과 |

W4는 이 5가지를 한꺼번에 해결합니다.

---

### 1️⃣ 도메인 패키지 재구성: `controller/service/...` → `api/application/domain/infrastructure/`

**W3의 폴더 구조**:
```
auth/
├── controller/  ← HTTP 입구
├── service/     ← 로직 + JPA + Kafka + 룰 (너무 많음)
├── repository/  ← JPA 직접
├── entity/      ← 도메인 + JPA
├── dto/
└── kafka/       ← 메시징
```

**왜 이대로면 안 되나?** 이름이 **계층의 역할**을 드러내지 않습니다. `repository/`가 "DB 접근"이라는 건 알지만, 그게 인프라인지 application의 일부인지 모호. service는 어느 선까지가 자기 책임인지 모름.

**W4의 폴더 구조**:
```
auth/
├── api/             ← "외부 입구": HTTP. 컨트롤러 + DTO만.
├── application/     ← "유즈케이스": service + outbound port 인터페이스
│   └── port/         outbound 인터페이스 (다음 항목)
├── domain/          ← "비즈니스 본질": 엔티티 + 룰
│   └── policy/       도메인 룰 격리 (다음 다음 항목)
└── infrastructure/  ← "외부 시스템 연결": JPA·Kafka 구현
    ├── persistence/
    └── messaging/
```

각 폴더 이름이 **책임의 추상도**를 표현합니다. 깊이 들어갈수록 본질(domain), 얕으면 외부(api/infrastructure). 신입이 패키지명만 봐도 "여기엔 무엇이 와야 하는지" 감 잡힘.

> 💡 **"또 이름 바꾸기 짜증나는데?"** — 단순 rename이 아닙니다. 다음 항목들이 진짜 변화. 폴더 구조는 변화의 표지일 뿐.

---

### 2️⃣ `application/port/` — Outbound Port (인터페이스)

**W3의 문제 — service가 인프라 구체 클래스 직접 의존**:

```java
public class AuthService {
    private final UserRepository userRepository;        // ← Spring Data JPA 구체
    private final UserEventPublisher userEventPublisher; // ← KafkaTemplate 구체
```

JPA를 MyBatis로 바꾸고 싶다? AuthService 수정. Kafka를 RabbitMQ로? 또 수정. **service가 인프라 변경에 결합**되어 있음.

**W4의 해법 — 인터페이스(port)만 의존**:

```java
// application/port/UserPort.java — 인터페이스 정의
public interface UserPort {
    Optional<User> findByEmail(String email);
    User save(User user);
}

// application/AuthService.java — port만 의존
public class AuthService {
    private final UserPort userPort;        // 인터페이스
    private final EventPort eventPort;      // 인터페이스
```

```java
// infrastructure/persistence/UserPersistenceAdapter.java — port 구현
@Component
class UserPersistenceAdapter implements UserPort {
    private final UserJpaRepository jpaRepo;   // 내부 디테일
    // ...
}
```

**의존 방향이 뒤집힘** (Dependency Inversion):
- W3: `application → infrastructure` (service가 JPA를 알아야 함)
- W4: `application ← infrastructure` (infrastructure가 application의 port를 구현)

이게 **헥사고날의 본질**. service는 "내가 필요한 건 이런 모양"이라는 인터페이스만 정의, 구현은 자유로움.

**효과**:
- JPA → MyBatis: `UserPersistenceAdapter`만 교체 (또는 `MyBatisAdapter` 추가). application 수정 0.
- 단위 테스트: `UserPort`의 in-memory mock을 만들면 DB 없이 service 테스트 가능.

> 💡 **왜 inbound port(예: `AuthUseCase` 인터페이스)는 안 만드나?** 라이트 헥사고날의 단순화. controller가 `AuthService` 구체 클래스를 직접 호출. 추상화의 이득 < 코드 부풀음. 진짜 필요해지면 풀로 진화.

---

### 3️⃣ `domain/policy/` — 비즈니스 룰의 격리

**W3의 문제 — 룰이 service 안에 흩어짐**:

```java
public class AuthService {
    public void register(...) {
        // 비밀번호 정책 (룰)
        if (password.length() < 10) throw ...;
        if (!Pattern.matches(".*[A-Z].*", password)) throw ...;
        if (!Pattern.matches(".*[0-9].*", password)) throw ...;
        // 본 로직 시작...
    }

    public void changePassword(...) {
        // 같은 룰을 또 검사 (또는 빼먹음)
        if (password.length() < 10) throw ...;
        // ...
    }
}
```

**3가지 문제**:
1. "비밀번호 정책이 뭐였지?"를 알려면 service들을 다 뒤져야 함.
2. 룰을 단위 테스트하려면 Spring 띄워야 함 (service에 박혀 있으니).
3. 룰을 바꾸려면 여러 service 동시 수정. 빼먹기 쉬움.

**W4의 해법 — `domain/policy/`에 격리**:

```java
// domain/policy/PasswordPolicy.java — 외부 의존성 0의 순수 Java
public final class PasswordPolicy {
    public static boolean isValid(String raw) {
        if (raw == null) return false;
        if (raw.length() < 10 || raw.length() > 72) return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char c : raw.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            // ...
        }
        return hasUpper && hasLower && hasDigit;
    }
}
```

```java
// application/AuthService.java — 룰 한 줄로
if (!PasswordPolicy.isValid(req.password())) throw new BusinessException(...);
```

**효과**:
- 정책 조회: `PasswordPolicy` 한 파일 보면 끝.
- 단위 테스트: 순수 JUnit, Spring 0초 부팅. 100ms 이내.
- 정책 변경: `PasswordPolicy` 한 곳만 수정.
- 정책이 도메인 객체에 가까이 있어 발견 가능.

**ArchUnit 룰 5번이 이걸 강제**: `domain/policy/`는 Spring·JPA·다른 도메인 import 일체 금지. 정책 안에 `@Component`나 `@Entity`가 들어가는 순간 빌드 실패.

---

### 4️⃣ `infrastructure/persistence/` — JPA를 가두기

**W3의 문제 — `UserRepository extends JpaRepository`가 public**:

```java
// auth/repository/UserRepository.java
public interface UserRepository extends JpaRepository<User, Long> { ... }
```

이러면 **어디서든** UserRepository를 직접 호출 가능. 컨트롤러도, 다른 도메인도(컨벤션 위반이지만 컴파일은 됨).

**W4의 해법 — JPA repository는 package-private**:

```java
// infrastructure/persistence/UserJpaRepository.java
interface UserJpaRepository extends JpaRepository<User, Long> { ... }
//        ↑ public 없음 = package-private
```

```java
// infrastructure/persistence/UserPersistenceAdapter.java — 같은 패키지에서만 보임
@Component
class UserPersistenceAdapter implements UserPort {
    private final UserJpaRepository jpaRepo;   // 같은 패키지라 접근 가능
}
```

application은 `UserJpaRepository`라는 클래스 자체가 안 보임. **JPA를 패키지 안에 가둬놓은 효과**.

**ArchUnit 룰 6번이 이걸 강제**: `JpaRepository` 상속 클래스는 `infrastructure.persistence` 패키지에만 존재 가능.

---

### 5️⃣ `infrastructure/messaging/` — Kafka도 인프라

**W3의 위치**:
```
auth/kafka/producer/UserEventPublisher.java
notification/kafka/consumer/UserRegisteredConsumer.java
```

**W3의 문제**:
- `kafka/`라는 이름이 "이게 어느 계층인지" 안 알려줌. 인프라인 것 같긴 한데 service 옆에 있어서 동등해 보임.
- `@KafkaListener`가 도메인 service 옆에 있으면 누구든 service에 `@KafkaListener` 메서드를 추가하고 싶어짐 → 인프라 결합.

**W4의 해법 — `infrastructure/messaging/`으로 이동 + 이름도 Adapter로**:

```
auth/infrastructure/messaging/UserEventKafkaAdapter.java    (implements EventPort)
notification/infrastructure/messaging/UserRegisteredKafkaConsumer.java
```

`Publisher` → `KafkaAdapter`로 이름을 바꿔서 "이건 port의 Kafka 구현체"임을 명시.

**ArchUnit 룰 7번이 이걸 강제**: `@KafkaListener`는 `infrastructure.messaging` 패키지에만.

---

### 6️⃣ ArchUnit — 컨벤션을 코드로 강제 (사문화 방지)

**W3까지 가장 큰 문제**: 모든 컨벤션이 **문서 약속**일 뿐.

```java
// 누군가 PR에서 이걸 작성
package com.synapse.platform.billing.application;
import com.synapse.platform.auth.application.AuthService;   // ❌ 도메인 격리 위반

@Service
public class BillingService {
    private final AuthService authService;   // 컴파일됨. 빌드 성공.
}
```

리뷰어가 놓치면 머지. 6개월 후 도메인 간 import가 100개 → 추출 불가능.

**W4의 해법 — ArchUnit 7개 룰을 테스트로 작성, CI에서 빌드 fail**:

```bash
./gradlew test --tests "*ArchitectureTest"
```

위 위반 코드는 다음 메시지로 fail:
```
Architecture Violation [...] auth depends on billing in (BillingService.java:N)
```

CI에 `test`를 PR gate로 걸면 위반 PR은 머지 불가. **컨벤션이 코드가 됨**.

**7가지 룰**:
1. 도메인 슬라이스 간 직접 의존 금지
2. `domain/`은 다른 계층 import 금지
3. `application/`은 `api/`·`infrastructure/` import 금지 (port 예외)
4. `api/`는 `infrastructure/` import 금지
5. `domain/policy/`는 외부 의존성 0
6. `JpaRepository`는 `infrastructure.persistence`에만
7. `@KafkaListener`는 `infrastructure.messaging`에만

각 룰은 위에서 본 W3 통증과 1:1 대응:
- 룰 1 ↔ 도메인 격리 위반
- 룰 2~4 ↔ 계층 의존 방향 위반
- 룰 5 ↔ 룰 격리 위반
- 룰 6, 7 ↔ 인프라 결합 위반

---

### 7️⃣ `archunit-junit5` 의존성

```kotlin
testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
```

테스트 시점에만 필요. 운영 바이너리 크기에 영향 0.

---

### 🔁 W3 → W4 통증→해법 매트릭스 (한 장 요약)

| W3 통증 | W4 해법 |
|---|---|
| service가 JPA·Kafka·룰을 모두 짊어져 무거움 | 책임 분리 (port + adapter + policy) |
| 단위 테스트가 Spring·DB·Kafka 모두 요구 | port mock + policy 순수 JUnit |
| 인프라(JPA/Kafka) 교체가 service까지 수정 | adapter만 갈아끼우면 됨 |
| 비즈니스 룰이 service에 흩어짐 | `domain/policy/`에 격리 |
| `UserRepository`가 public이라 어디서든 호출 가능 | `UserJpaRepository` package-private + adapter |
| `kafka/`가 service 옆에 있어 결합 유혹 | `infrastructure/messaging/`으로 격리 |
| 컨벤션이 문서일 뿐, 위반해도 빌드 통과 | ArchUnit 7룰이 CI에서 강제 |

---

### W4 이후엔?

W4는 template의 마지막 단계지만, **실제 운영은 거기서부터 시작**입니다. README의 "다음 단계 (이 템플릿 외부)" 섹션 참고:

1. **synapse-shared 멀티모듈 publish** — `global/kafka/event/` stub을 진짜 공유 라이브러리로
2. **다른 *-svc에 W4 적용** — knowledge/engagement/learning도 동일 구조로
3. **CI에 ArchUnit gate 활성화** — 룰 위반 시 PR merge 차단
4. **풀 헥사고날로 점진 진화 (선택)** — 도메인 복잡도가 높아지면 JPA Entity와 도메인 모델 분리

---

## 📚 W3에서 새로 등장한 용어

| 용어 | 의미 |
|---|---|
| **Event-Driven Architecture (EDA)** | 컴포넌트가 직접 호출 대신 이벤트로 상호작용하는 아키텍처 스타일. |
| **Producer** | Kafka 토픽에 메시지를 발행하는 쪽. |
| **Consumer** | Kafka 토픽을 구독해서 메시지를 받는 쪽. |
| **Topic** | 메시지가 흐르는 채널. 이름으로 식별. |
| **Partition** | 토픽을 쪼갠 단위. 같은 key는 같은 파티션 → 순서 보장. |
| **Consumer Group** | 같은 `groupId`를 가진 컨슈머 묶음. 메시지를 분담. |
| **Offset** | 컨슈머가 어디까지 읽었는지의 위치. |
| **`KafkaTemplate`** | Spring Kafka의 producer 헬퍼. `.send(topic, key, value)`. |
| **`@KafkaListener`** | Spring Kafka의 consumer 어노테이션. |
| **`@EnableKafka`** | `@KafkaListener`가 작동하게 하는 마법. KafkaConfig에 필수. |
| **`topicPattern`** | 정규식으로 토픽 매칭 (광역 구독). |
| **acks=all** | producer가 모든 replica의 ack를 기다림. 손실 방지. |
| **auto-offset-reset** | 새 컨슈머의 시작 위치 (`earliest` / `latest`). |
| **Idempotency (멱등성)** | 같은 작업을 여러 번 해도 결과가 같음. 분산 시스템 핵심. |
| **DLQ (Dead Letter Queue)** | 처리 실패 메시지를 옮기는 별도 토픽. |
| **Schema Registry** | Avro 스키마를 중앙 관리하는 서비스. shared-events 도입 후 필요. |
| **At-least-once** | 메시지가 **최소 한 번** 전달됨. 중복 가능. (Kafka 기본) |
| **Exactly-once** | 정확히 한 번. 트랜잭션 producer + 멱등 consumer 조합. |

---

## 🆘 막힐 때

- 컨슈머가 메시지를 못 받음 → 토픽이 생성됐는지(`kafka-topics.sh --list`), `groupId`가 맞는지 확인.
- 같은 메시지가 계속 옴 → 컨슈머에서 예외 던지고 있을 가능성. ack를 처리하도록 수정.
- `Failed to send` → `bootstrap-servers` 주소 확인, 방화벽, Kafka 컨테이너 상태.
- 역직렬화 에러 → `TRUSTED_PACKAGES`, 양쪽 이벤트 클래스 필드 일치, JSON 직렬화 가능한 타입인지.
- 토픽 자동 생성을 막고 싶음 → broker `auto.create.topics.enable=false`, 토픽은 수동 생성 또는 운영 도구로.
