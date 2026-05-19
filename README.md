# synapse-platform-svc — W4 skeleton (최종)

> **한 줄 정의**: "각 도메인을 `api/application/domain/infrastructure` 4-계층으로 재구성하고, ArchUnit으로 규칙을 자동 강제하는 단계."
> 사람이 컨벤션을 지키지 않아도, **테스트가 자동으로 위반을 잡아냅니다.**

이 문서는 **헥사고날 아키텍처를 처음 보는 신입/주니어**가 "왜 평평하던 패키지가 갑자기 4단으로 쌓이나"를 이해하도록 작성되었습니다.

---

## 🎯 이 단계의 목표

W4를 마치면 여러분은:
- [x] 라이트 헥사고날(Light Hexagonal)과 풀 헥사고날의 차이를 안다
- [x] `api / application / domain / infrastructure` 4계층의 책임을 안다
- [x] **Port**와 **Adapter**가 뭔지, 왜 인터페이스를 끼우는지 안다
- [x] `domain/policy/`가 왜 분리되어야 하는지 안다
- [x] ArchUnit 7가지 룰의 의미와 위반 예시를 안다
- [x] CI에서 ArchUnit이 PR을 막는 이유를 설명할 수 있다

---

## 🧭 큰 그림: 왜 또 패키지 구조를 바꾸나요?

### ❌ W3 구조의 한계

W3까지의 도메인 구조:
```
auth/
├── controller/     ← HTTP만
├── service/        ← 비즈니스 로직 + 비밀번호 룰 + 토큰 발급 + ...
├── repository/     ← JPA 직접
├── entity/         ← JPA Entity
├── dto/
└── kafka/          ← Producer + Consumer
```

문제:
1. **`service/`가 너무 많은 일을 함** — 비즈니스 룰, JPA 호출, Kafka 발행 모두 섞임. 단위 테스트 시 Spring 컨텍스트 + DB + Kafka 모두 필요.
2. **JPA·Kafka 교체가 사실상 불가능** — service가 `UserRepository extends JpaRepository`를 직접 의존. JPA를 MyBatis로 바꾸려면 service까지 수정.
3. **도메인 룰이 service에 흩어짐** — "비밀번호는 10자 이상 + 대소문자 + 숫자" 같은 룰이 `AuthService.login()` 안에 박혀 있음. 룰을 단위 테스트하려면 Spring 띄워야 함.

### ✅ 라이트 헥사고날의 해법

각 도메인을 **목적별 4계층**으로 다시 나눕니다:

```
auth/
├── api/                    ← "외부에서 들어오는 입구" (HTTP)
├── application/            ← "유즈케이스 + 의존 인터페이스"
│   └── port/                  outbound port — DB, 메시징 추상화
├── domain/                 ← "비즈니스 본질" (JPA 어노테이션은 허용)
│   └── policy/                도메인 룰 (외부 의존 0)
└── infrastructure/         ← "외부 시스템 연결"
    ├── persistence/           JPA 구현 (port의 adapter)
    └── messaging/             Kafka 구현 (port의 adapter)
```

핵심 변화:
- **`application/`은 `port/` 인터페이스만 의존** → DB·Kafka 구현이 바뀌어도 application은 무변.
- **`domain/policy/`는 외부 의존 0** → 룰 단위 테스트가 순수 Java만으로 가능.
- **`infrastructure/`는 port를 구현하는 adapter** → 구현 교체 = adapter만 갈아끼우기.

### 라이트 vs 풀 헥사고날의 차이

| 측면 | 풀 헥사고날 (Strict) | 라이트 헥사고날 (W4) |
|---|---|---|
| `domain/`에 JPA 어노테이션 | ❌ 금지 (Persistence Entity와 도메인 객체 분리) | ✅ 허용 (실용성 우선) |
| Inbound port (UseCase 인터페이스) | ✅ 정의 (`AuthUseCase`) | ❌ 생략 (`AuthService` 직접) |
| Mapper (Domain ↔ Persistence Entity) | ✅ 필수 | ❌ 동일 클래스 사용 |
| 학습 비용 | 높음 | 낮음 |
| 확장 시 추가 작업 | 적음 | DB 변경 시 미세 충격 가능 |

**라이트로 시작 → 정말 필요해지면 풀로** 전환이 정석. 미리 풀 헥사고날 가면 팀이 지칩니다.

---

## 📂 W4 패키지 구조 (4개 도메인 동일 패턴)

```
src/main/java/com/synapse/platform/auth/
├── api/                                          ← Inbound (HTTP)
│   ├── AuthController.java
│   └── dto/
│       ├── request/LoginRequest.java
│       └── response/TokenResponse.java
│
├── application/                                  ← UseCase + outbound port
│   ├── AuthService.java                            UseCase 구현
│   └── port/                                       outbound 인터페이스
│       ├── UserPort.java                            영속성 추상
│       └── EventPort.java                           메시징 추상
│
├── domain/                                       ← 비즈니스 모델
│   ├── User.java                                   엔티티 (JPA OK)
│   └── policy/                                     도메인 룰 (외부 의존 0)
│       └── PasswordPolicy.java
│
└── infrastructure/                               ← Adapter (외부 연결)
    ├── persistence/
    │   ├── UserJpaRepository.java                  Spring Data, package-private
    │   └── UserPersistenceAdapter.java             implements UserPort
    └── messaging/
        └── UserEventKafkaAdapter.java              implements EventPort
```

4개 도메인 전체:

```
com.synapse.platform/
├── PlatformApplication.java
├── auth/        api/ application/{port/} domain/{policy/} infrastructure/{persistence/, messaging/}
├── audit/       api/ application/{port/} domain/{policy/} infrastructure/{persistence/, messaging/}
├── billing/     api/ application/{port/} domain/{policy/} infrastructure/{persistence/, messaging/}
├── notification/api/ application/{port/} domain/{policy/} infrastructure/{persistence/, messaging/}
└── global/      config/ exception/ response/ security/ util/ kafka/event/   (W2~W3 그대로)
```

---

## 🧱 각 계층의 책임

### 1️⃣ `api/` — 외부에서 들어오는 입구

- HTTP 요청을 받음. 컨트롤러 + DTO.
- 비즈니스 로직 금지. `application`에 위임만.
- 변경 영향: HTTP 스펙이 바뀔 때만 수정 (REST → GraphQL 마이그레이션 등).

```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;     // application 계층 의존 OK

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }
}
```

### 2️⃣ `application/` — UseCase + 의존 인터페이스

- "무엇을 할지" 조립 (use case). 트랜잭션 경계.
- DB·Kafka 같은 외부 시스템은 **인터페이스(`port`)로만 의존**.
- `application/port/`는 outbound port (출력 방향): "나는 이런 기능이 필요하다"는 선언.

```java
@Service
public class AuthService {
    private final UserPort userPort;            // ← port 인터페이스만
    private final EventPort eventPort;          // ← port 인터페이스만
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public TokenResponse login(LoginRequest request) {
        User user = userPort.findByEmail(request.email())
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        return new TokenResponse(tokenProvider.issueAccessToken(user.getId().toString()), "...");
    }
}
```

```java
// application/port/UserPort.java
public interface UserPort {
    Optional<User> findByEmail(String email);
    User save(User user);
}
```

### 3️⃣ `domain/` — 비즈니스 본질

- 엔티티(데이터 모델)와 도메인 룰의 집.
- 라이트 헥사고날에서는 JPA 어노테이션 허용. (풀에서는 금지)
- 다른 계층 import 절대 금지. **가장 안쪽 원**.

```java
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(...) private Long id;
    private String email;
    private String passwordHash;
    // 도메인 메서드 — 데이터만 다루고 외부 호출 안 함
    public void changeEmail(String newEmail) { ... }
}
```

#### `domain/policy/` — 외부 의존 0의 순수 룰

```java
public final class PasswordPolicy {
    private static final int MIN_LENGTH = 10;
    public static boolean isValid(String raw) {
        // 외부 의존성 없는 순수 Java
    }
}
```

**왜 분리?**
- 룰을 단위 테스트할 때 Spring 안 띄움 → 빠름.
- "비밀번호 정책이 뭐였지?"를 한 파일로 즉시 찾음.
- 룰 변경 시 인프라/API 코드 안 봐도 됨.

### 4️⃣ `infrastructure/` — Adapter

- port를 **구현**해서 실제 외부 시스템에 연결.
- JPA, Kafka, Redis, 외부 HTTP API 등 인프라 의존성이 여기 모임.
- application은 이 패키지 import 금지 (ArchUnit이 강제).

#### `infrastructure/persistence/`

```java
// 외부에서 못 보게 package-private
interface UserJpaRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

@Component
class UserPersistenceAdapter implements UserPort {
    private final UserJpaRepository jpaRepository;

    @Override public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email);
    }
    @Override public User save(User user) { return jpaRepository.save(user); }
}
```

> 💡 `UserJpaRepository`는 패키지 외부에서 볼 수 없습니다 (package-private). application은 `UserPort`만 봅니다. **JPA를 MyBatis로 바꾸려면 이 패키지만 갈아끼우면 됨**.

#### `infrastructure/messaging/`

```java
@Component
class UserEventKafkaAdapter implements EventPort {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override public void publishUserRegistered(UserRegistered event) {
        kafkaTemplate.send(TOPIC, event.userId().toString(), event);
    }
}
```

W3의 `UserEventPublisher` → W4에서는 `UserEventKafkaAdapter`로 이름 변경(역할 명시). Consumer도 마찬가지로 messaging 안에.

---

## 🔄 데이터 흐름: W3 vs W4 비교

### W3 흐름

```
HTTP → AuthController → AuthService → UserRepository (JpaRepository 직접)
                                    → UserEventPublisher (KafkaTemplate 직접)
```

문제: AuthService가 JPA와 Kafka 클래스를 직접 알아야 함.

### W4 흐름

```
HTTP → AuthController (api)
         ↓
       AuthService (application)
         ↓ (UserPort 인터페이스)
       UserPersistenceAdapter (infrastructure.persistence)
         ↓
       UserJpaRepository → DB

       AuthService (application)
         ↓ (EventPort 인터페이스)
       UserEventKafkaAdapter (infrastructure.messaging)
         ↓
       KafkaTemplate → Kafka
```

AuthService는 `UserPort`, `EventPort` 인터페이스만 본다. 구현 교체 = adapter 클래스 바꾸기.

---

## 🛡 ArchUnit — 컨벤션을 코드로 강제

W3까지는 "도메인 끼리 import 금지"가 **문서상 약속**이었습니다. 누군가 실수로 위반해도 컴파일됨.

W4는 ArchUnit 테스트로 **위반 시 빌드 실패**:

```bash
./gradlew test
```

`src/test/java/com/synapse/platform/arch/PlatformArchitectureTest.java`가 7개 룰을 검증.

### 7가지 룰 의미 + 위반 예시

#### 룰 1. 도메인 슬라이스 간 직접 의존 금지

```java
slices().matching("..platform.(*)..")
    .should().notDependOnEachOther()
    .ignoreDependency(global, anyClass)
```

**위반 예시**:
```java
// auth/application/AuthService.java
import com.synapse.platform.billing.application.BillingService;   // ❌
```
빌드 실패. 도메인 통신은 `global/kafka/event` 경유만.

#### 룰 2. `domain/`은 다른 계층 import 금지

**위반 예시**:
```java
// domain/User.java
import com.synapse.platform.auth.application.AuthService;   // ❌
import com.synapse.platform.auth.api.dto.request.LoginRequest;   // ❌
```
도메인은 가장 안쪽 — 무엇도 의존하지 않습니다.

#### 룰 3. `application/`은 `api/`·`infrastructure/` import 금지 (`port/` 예외)

**위반 예시**:
```java
// application/AuthService.java
import com.synapse.platform.auth.infrastructure.persistence.UserJpaRepository;   // ❌
import com.synapse.platform.auth.api.dto.response.TokenResponse;   // 실은 OK — dto는 application도 사용
```
service는 인터페이스(`UserPort`)만 의존. 구현 클래스는 모름.

> `application/port/`만 예외 — port가 외부와 연결되는 정의 지점이므로.

#### 룰 4. `api/`는 `infrastructure/` import 금지

**위반 예시**:
```java
// api/AuthController.java
import com.synapse.platform.auth.infrastructure.persistence.UserJpaRepository;   // ❌
// 컨트롤러가 JpaRepository 직접 호출
```
컨트롤러는 application(service)만 호출.

#### 룰 5. `domain.policy/`는 외부 의존성 0

```java
classes().that().resideInAPackage("..domain.policy..")
    .should().onlyDependOnClassesThat().resideInAnyPackage("java..", "..domain.policy..", "..domain..")
```

**위반 예시**:
```java
// domain/policy/PasswordPolicy.java
import org.springframework.stereotype.Component;   // ❌
import jakarta.persistence.Entity;                  // ❌
```
정책은 순수 Java만. Spring·JPA 어노테이션 금지.

#### 룰 6. `JpaRepository`는 `infrastructure.persistence`에만

```java
classes().that().areAssignableTo(JpaRepository.class)
    .should().resideInAPackage("..infrastructure.persistence..")
```

**위반 예시**:
```java
// application/port/UserPort.java
interface UserPort extends JpaRepository<User, Long> { ... }   // ❌
// port가 JpaRepository 상속하면 application이 JPA에 결합
```

#### 룰 7. `@KafkaListener`는 `infrastructure.messaging`에만

```java
classes().that().areAnnotatedWith(KafkaListener.class)
    .should().resideInAPackage("..infrastructure.messaging..")
```

**위반 예시**:
```java
// application/AuthService.java
@KafkaListener(topics = "...")   // ❌
public void onUserRegistered(UserRegistered e) { ... }
```
service는 인프라 어노테이션을 모름.

### ArchUnit 룰 추가하는 법

```java
@Test
void controller_must_have_RestController_annotation() {
    classes()
        .that().resideInAPackage("..api..")
        .and().haveSimpleNameEndingWith("Controller")
        .should().beAnnotatedWith(RestController.class)
        .check(CLASSES);
}
```

`PlatformArchitectureTest.java`에 메서드 추가하고 PR 올리면 됨. 팀 합의로 룰을 키워가세요.

---

## 🔄 W3 → W4 변화 요약

| 항목 | W3 | W4 |
|---|---|---|
| 도메인 패키지 | `controller / service / repository / entity / dto / kafka` | `api / application / domain / infrastructure` |
| 영속성 | `repository/`에 `JpaRepository` 직접 | `application/port` + `infrastructure/persistence/{Repo, Adapter}` |
| 메시징 | `kafka/{producer, consumer}/` | `application/port` + `infrastructure/messaging/` |
| 룰 위치 | service 안에 혼재 | `domain/policy/`에 격리 |
| 강제 메커니즘 | 컨벤션 (문서) | **ArchUnit 7룰로 자동 강제** |
| 단위 테스트 | service 테스트 = Spring 컨텍스트 필요 | policy/도메인은 순수 JUnit |
| 의존성 추가 | spring-kafka | + archunit-junit5 1.3.0 |

---

## ⚠️ W4에서 자주 하는 실수

### 1. `application/`이 `infrastructure` 클래스를 직접 사용

```java
// ❌
@Service
public class AuthService {
    private final UserPersistenceAdapter adapter;   // 구현 클래스를 직접
}

// ✅
public class AuthService {
    private final UserPort userPort;   // 인터페이스만
}
```

스프링 DI는 어차피 `UserPort` 타입으로 자동 주입해 줍니다(Bean이 1개면). 컨크리트 클래스 import할 이유 없음.

### 2. `domain/`에 Spring 어노테이션

```java
// ❌
@Entity
@Component   // ← 도메인이 Spring을 알게 됨
public class User { ... }
```
JPA 어노테이션(`@Entity`)은 라이트에서 허용하지만, `@Component`·`@Service`는 application/infrastructure만.

### 3. `port/`가 한 번에 너무 많은 메서드

```java
// ❌ — Fat port (인터페이스 메서드 30개)
public interface UserPort {
    Optional<User> findById(...);
    List<User> findAll(...);
    Page<User> search(...);
    long countByStatus(...);
    // ... 25개 더
}
```

port는 **application이 실제 사용하는 메서드만**. ISP(Interface Segregation Principle). 필요해지면 분리 (UserReadPort, UserWritePort).

### 4. `JpaRepository`를 public으로 노출

```java
// ❌
public interface UserJpaRepository extends JpaRepository<User, Long> { ... }

// ✅ — package-private
interface UserJpaRepository extends JpaRepository<User, Long> { ... }
```
public이면 외부에서 직접 사용 가능 → port 패턴 우회 가능.

### 5. ArchUnit 위반을 "예외"로 빼고 잊기

```java
// ❌
.ignoreDependency(EVERYTHING, EVERYTHING)   // 룰을 무력화
```

위반이 정당하다면 룰 자체를 다시 설계하거나 코드를 수정하세요. 예외는 의도가 명확할 때(`global/`)만.

### 6. `domain/policy/`에 비즈니스 객체 의존

```java
// ❌
public class ChargePolicy {
    public boolean isChargeable(Invoice invoice) {   // Invoice는 도메인 클래스
        return invoice.getAmount().compareTo(MIN) > 0;
    }
}
```
이건 도메인 객체 의존이라 ArchUnit 룰 5에서는 OK (룰은 `..domain..` 패키지 허용). 다만 더 순수하게 가려면:
```java
public class ChargePolicy {
    public static boolean isChargeable(BigDecimal amount, String currency) { ... }   // primitive만
}
```

### 7. infrastructure에서 도메인 룰 다시 작성

```java
// ❌ — 룰이 두 곳에
class UserPersistenceAdapter {
    public User save(User user) {
        if (user.getEmail().length() > 100) throw new ...;   // 룰이 인프라에!
        return jpaRepository.save(user);
    }
}
```
룰은 `domain/policy/` 또는 도메인 엔티티 안에만.

---

## ▶️ 실행 + 검증

### 1. 앱 띄우기

```bash
./gradlew bootRun
```

W2의 JWT 인증, W3의 Kafka 이벤트가 모두 동작합니다. 단, W4 코드는 실제로 동작하려면 DB·Kafka 모두 필요 (로컬 docker-compose 권장).

### 2. ArchUnit만 빠르게 검증

```bash
./gradlew test --tests "*ArchitectureTest"
```

7개 룰이 모두 통과하면 ✅ 출력. 위반 시 어떤 클래스가 무엇을 어디서 import했는지 상세히 출력.

### 3. 위반 시뮬레이션으로 ArchUnit 확인

`AuthService.java`에 의도적으로 위반 코드를 넣고 테스트:

```java
import com.synapse.platform.billing.application.BillingService;   // ← 룰 1 위반
```

실행:
```bash
./gradlew test --tests "*ArchitectureTest"
```

실패 메시지:
```
Architecture Violation [...] auth depends on billing in (AuthService.java:N)
```

확인 후 원복.

### 4. CI 통합 (PR gate)

```yaml
# .github/workflows/ci.yml (예시)
- name: ArchUnit
  run: ./gradlew test --tests "*ArchitectureTest"
```

PR이 룰을 어기면 머지 불가. 컨벤션이 사문화되지 않음.

---

## 🔭 다음 단계 (이 템플릿 외부)

W4는 **template 마지막 단계**. 실제 시작은 여기서부터:

1. **synapse-shared 멀티모듈 publish**
   - `global/kafka/event/`의 stub → `com.synapse.shared.event.*`로 교체
   - GitHub Packages에 1.0.0 publish
   - 모든 *-svc가 `implementation("com.synapse:shared-events:1.0.0")` 사용

2. **다른 *-svc에도 W4 적용**
   - knowledge-svc, engagement-svc, learning-svc 동일 구조로 마이그레이션
   - 각 서비스의 ArchUnit 룰도 함께 추가

3. **CI에 ArchUnit gate 활성화**
   - PR마다 `./gradlew test` 실행
   - 룰 위반 시 머지 차단

4. **syn 레포 BACKEND_STRUCTURE.md**
   - 이 W4 구조를 공식 컨벤션 문서로
   - 새 도메인 추가 시 참고용

5. **풀 헥사고날로 진화 (선택)**
   - JPA Entity와 도메인 모델 분리
   - Mapper 도입
   - 도메인 복잡도가 높아질 때 검토

---

## 📚 W4에서 새로 등장한 용어

| 용어 | 의미 |
|---|---|
| **헥사고날 아키텍처 (Hexagonal Architecture)** | "Ports and Adapters" 별칭. 도메인 보호 + 인프라 교체 가능성을 위한 패턴. Alistair Cockburn (2005). |
| **클린 아키텍처 (Clean Architecture)** | Uncle Bob의 동심원 패턴. 헥사고날의 사촌. 본질 동일. |
| **Port** | "도메인이 외부에 요구하는 기능 인터페이스". Outbound(나가는) port가 일반적. |
| **Adapter** | port를 구현해서 실제 외부 시스템과 연결. JPA Adapter, Kafka Adapter 등. |
| **UseCase** | application 계층의 한 시나리오. 보통 service 메서드 하나에 해당. |
| **Inbound Port** | 외부에서 application으로 들어오는 인터페이스. 풀 헥사고날에서 정의, 라이트는 생략. |
| **Outbound Port** | application이 외부에 요청하는 인터페이스 (UserPort, EventPort). |
| **DIP (Dependency Inversion Principle)** | "구체가 추상에 의존, 추상이 구체에 의존하지 않음". Port/Adapter의 본질. |
| **ISP (Interface Segregation Principle)** | "필요한 메서드만 가진 작은 인터페이스". Fat port를 분리하는 근거. |
| **Aggregate** | 도메인 모델에서 함께 변경되는 객체 묶음 (DDD). W4에선 단순화. |
| **Domain Policy** | 비즈니스 룰의 한 단위. 외부 의존 없음. |
| **ArchUnit** | JUnit 위에서 패키지·계층·의존 룰을 검증하는 라이브러리. |
| **Slice** | ArchUnit이 도메인 단위로 자르는 한 조각 (`auth/`, `billing/`). |
| **Package-private** | `public`·`protected`·`private` 어느 것도 안 붙은 가시성. 같은 패키지 안에서만 보임. |

---

## 🆘 막힐 때

- ArchUnit이 의도와 다르게 fail → 메시지에 위반 위치(파일:line)가 있음. 거기서부터.
- "이 룰 너무 빡빡함" → 팀과 합의 후 룰 자체를 완화하거나 `ignoreDependency` 추가. 단, 이유를 코멘트로 남기기.
- Bean이 안 떠서 `NoSuchBeanDefinition` → adapter에 `@Component` 빠진 경우 99%. 인터페이스(port)는 `@Component` 안 붙임, 구현(adapter)에만.
- `UserJpaRepository`를 직접 호출하고 싶음 → port 추가가 정답. 인프라 직접 호출은 W4 아키텍처가 막는 것.

---

## 🎓 추가 학습 자료

- Alistair Cockburn, *Hexagonal Architecture* (2005) — 원전
- Uncle Bob, *Clean Architecture* — 동심원 시각화
- ArchUnit 공식 가이드 — https://www.archunit.org/userguide/html/000_Index.html
- "Modular Monolith" — Simon Brown, Kamil Grzybek 블로그 시리즈
- synapse-svc-template W1~W3 README — 점진적 진화의 이유 복습용
