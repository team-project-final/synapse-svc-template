# synapse-platform-svc — W1 skeleton

> **한 줄 정의**: "도메인 4개를 폴더로 분리한 가장 단순한 Spring Boot 서비스." 아직 횡단 기능(인증·예외 처리·이벤트)은 없습니다.

이 문서는 **신입/주니어 개발자**가 처음 이 코드베이스를 봤을 때 길을 잃지 않도록 작성되었습니다. 시니어가 옆에서 설명해 주는 톤으로 읽어주세요.

---

## 🎯 이 단계의 목표

W1을 마치면 여러분은:
- [x] 도메인(domain)이 무엇인지 안다
- [x] Spring Boot 프로젝트의 기본 5-계층(Controller/Service/Repository/Entity/DTO)을 안다
- [x] `./gradlew bootRun`으로 앱을 띄우고 4개 엔드포인트에 요청을 보낼 수 있다
- [x] 도메인 간에 코드를 어떻게 분리해야 하는지 감을 잡는다

**W1에서는 아직 하지 않는 것들**: 인증, 통일 응답 포맷, 예외 처리, Kafka, 헥사고날, ArchUnit. 이건 W2~W4에서 단계적으로 추가됩니다. 한꺼번에 다 보면 머리 아프니까 한 층씩 쌓아갑시다.

---

## 🧭 큰 그림: 왜 도메인을 먼저 나누나요?

### ❌ 안티패턴 — 계층(layer)만으로 나누면?

신입이 흔히 보는 구조는 이런 식입니다:

```
src/main/java/com/synapse/platform/
├── controller/
│   ├── AuthController.java
│   ├── AuditController.java
│   ├── BillingController.java
│   └── NotificationController.java
├── service/
│   ├── AuthService.java
│   ├── AuditService.java
│   ├── BillingService.java
│   └── NotificationService.java
└── repository/
    ├── UserRepository.java
    ├── AuditLogRepository.java
    └── ...
```

작은 프로젝트에선 괜찮아 보이지만, 도메인이 4개로 늘어나면 곧 다음 문제가 생깁니다:

1. **`controller/` 폴더가 너무 커진다** — 30개 컨트롤러가 한 폴더에 있으면 IDE에서 찾기 힘들다.
2. **도메인 간 의존성이 안 보인다** — `BillingService`가 `AuthService`를 import하는 게 자연스러워 보인다 (실제로는 절대 그러면 안 됨).
3. **도메인 하나 분리해서 다른 서비스로 옮기기 힘들다** — 폴더가 사방에 흩어져 있어서.

### ✅ 도메인 우선 분리

이 프로젝트는 **도메인 먼저, 계층 나중** 패턴을 씁니다:

```
src/main/java/com/synapse/platform/
├── auth/              ← 도메인 (auth와 관련된 모든 것이 여기)
│   ├── controller/
│   ├── service/
│   └── ...
├── audit/             ← 도메인
│   ├── controller/
│   └── ...
├── billing/           ← 도메인
└── notification/      ← 도메인
```

장점:
- **응집도(Cohesion)가 높다** — auth 작업할 땐 `auth/` 폴더 하나만 보면 됨.
- **도메인 분리가 시각적으로 강제된다** — `BillingService`가 `import com.synapse.platform.auth.service.AuthService;` 하면 누가 봐도 어색함.
- **나중에 마이크로서비스로 분리하기 쉽다** — `billing/`을 통째로 새 레포로 이동.

---

## 📂 패키지 구조 (W1)

```
synapse-platform-svc/
├── build.gradle.kts            ← Gradle 빌드 스크립트 (Kotlin DSL)
├── settings.gradle.kts          ← 프로젝트명 정의
├── gradle.properties            ← JVM 옵션, 병렬 빌드 설정
├── Dockerfile                   ← 컨테이너 이미지 빌드 정의
└── src/
    ├── main/
    │   ├── java/com/synapse/platform/
    │   │   ├── PlatformApplication.java         ← @SpringBootApplication 진입점
    │   │   │
    │   │   ├── auth/                            ← 도메인 1: 인증
    │   │   │   ├── controller/AuthController       HTTP 요청 받음
    │   │   │   ├── service/AuthService             비즈니스 로직
    │   │   │   ├── repository/UserRepository       DB 접근
    │   │   │   ├── entity/User                     DB 테이블 매핑
    │   │   │   └── dto/
    │   │   │       ├── request/LoginRequest         외부 → 서버 데이터
    │   │   │       └── response/TokenResponse       서버 → 외부 데이터
    │   │   │
    │   │   ├── audit/         ← 도메인 2: 감사 로그
    │   │   ├── billing/       ← 도메인 3: 과금
    │   │   └── notification/  ← 도메인 4: 알림
    │   │
    │   └── resources/
    │       └── application.yml   ← 설정 (DB, 포트 등)
    │
    └── test/
        └── java/com/synapse/platform/
            └── PlatformApplicationTests.java     ← Spring 컨텍스트 로딩 테스트
```

---

## 🧱 각 폴더의 역할 (5계층의 정석)

### 1. `controller/` — HTTP 요청을 받는 사람

- 책임: URL 매핑, 요청 파라미터 파싱, 응답 반환.
- **여기엔 비즈니스 로직을 쓰면 안 됨.** 받자마자 `service`로 넘기세요.
- 예시: `AuthController.login()` → `authService.login(request)` 호출.

```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);   // 위임만!
    }
}
```

### 2. `service/` — 비즈니스 로직의 주인공

- 책임: "무엇을 할지" 결정. 트랜잭션 경계도 여기.
- DB가 필요하면 `repository`에 위임, 외부 API 호출도 여기.
- 예시: `AuthService.login()`은 "유저 조회 → 비밀번호 검증 → 토큰 발급" 흐름을 조립.

```java
@Service
public class AuthService {
    private final UserRepository userRepository;

    public TokenResponse login(LoginRequest request) {
        // 비즈니스 로직 (W2 이후 채워짐)
    }
}
```

### 3. `repository/` — DB 접근만 책임

- Spring Data JPA가 `JpaRepository`를 상속하면 자동으로 구현체를 만들어줌.
- **여기엔 비즈니스 로직 절대 금지.** 단순 CRUD만.

```java
public interface UserRepository extends JpaRepository<User, Long> {
    // findById, save, deleteById 등 자동 제공
    // Optional<User> findByEmail(String email);   ← 이런 식으로 쿼리 추가
}
```

### 4. `entity/` — DB 테이블 = Java 클래스

- `@Entity`가 붙은 클래스 1개 = DB 테이블 1개.
- 필드 = 컬럼.
- Setter는 가급적 만들지 마세요 (불변 객체 지향).

```java
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String passwordHash;
    // ...
}
```

### 5. `dto/` — 외부와 주고받는 데이터 봉투

- DTO = Data Transfer Object. **API 입출력 전용** 클래스.
- `request/` = 클라이언트가 보내는 데이터, `response/` = 서버가 응답으로 주는 데이터.
- **entity와 dto를 분리해야 합니다.** 안 그러면 DB 컬럼명 바꾸자마자 API가 깨짐.

```java
// 입력
public record LoginRequest(String email, String password) {}

// 출력
public record TokenResponse(String accessToken, String refreshToken) {}
```

> Java 16+의 `record`는 불변 DTO 작성에 딱 맞습니다. 생성자·getter·equals·hashCode 자동 생성.

---

## 🔄 데이터 흐름 따라가기 (HTTP 요청 → 응답)

`POST /api/v1/auth/login`이 들어오면:

```
1. Spring이 AuthController.login()을 호출
   └─ @RequestBody로 JSON을 LoginRequest로 자동 변환

2. AuthController가 AuthService.login(request)에 위임
   └─ controller는 HTTP만 알면 됨 — 비즈니스 로직 모름

3. AuthService가 UserRepository.findByEmail(...)로 유저 조회
   └─ service는 DB가 어떻게 생겼는지 알 필요 없음, repository에 맡김

4. UserRepository가 JPA를 통해 users 테이블에 SELECT 쿼리 실행
   └─ JpaRepository가 자동으로 처리

5. service가 비밀번호 검증, 토큰 발급 → TokenResponse 생성

6. controller가 TokenResponse를 반환 → Spring이 JSON으로 직렬화 → 응답
```

**핵심**: 각 계층은 아래 계층만 알면 되고, 위 계층은 모릅니다. (단방향 의존)

---

## 🌐 4개 도메인 소개

| 도메인 | 책임 | 대표 엔드포인트 |
|---|---|---|
| **auth** | 사용자 인증, 토큰 발급/갱신 | `POST /api/v1/auth/login` |
| **audit** | 모든 도메인의 활동을 감사 로그로 보관 | `GET /api/v1/audit/logs` |
| **billing** | 결제, 청구서 관리 | `POST /api/v1/billing/charge` |
| **notification** | 이메일/SMS/푸시 발송 | `POST /api/v1/notifications` |

이 4개 도메인은 **같은 레포(synapse-platform-svc)**에 묶여 있습니다. "모듈리스(Modular Monolith)" 패턴 — 폴리레포 단점인 운영 부담을 줄이면서, 단일 모놀리스의 단점인 도메인 혼재를 막습니다.

---

## ▶️ 실행하기

### 1. 앱 띄우기

```bash
./gradlew bootRun
```

기본 포트: `8080`. 첫 실행 시 H2 인메모리 DB가 뜹니다.

### 2. 엔드포인트 호출

```bash
# auth — 로그인 (스텁이라 가짜 토큰 반환)
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"pass"}'
# → {"accessToken":"stub-access-token","refreshToken":"stub-refresh-token"}

# audit — 로그 목록 (빈 배열)
curl http://localhost:8080/api/v1/audit/logs
# → []

# billing — 결제 요청 (스텁)
curl -X POST http://localhost:8080/api/v1/billing/charge \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"amount":10000,"currency":"KRW"}'

# notification — 알림 발송 (스텁)
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"channel":"EMAIL","payload":"hi"}'
```

### 3. 테스트 실행

```bash
./gradlew test
```

현재는 `PlatformApplicationTests.contextLoads()` 1개만 — Spring 컨텍스트가 정상적으로 뜨는지만 검증합니다.

---

## 🚫 W1에서 의도적으로 안 하는 것들

| 안 한 것 | 왜 안 하나? | 어디서 추가되나? |
|---|---|---|
| 통일 응답 포맷(`ApiResponse<T>`) | 일단 도메인 분리에 집중 | **W2** `global/response/` |
| 예외 처리 | 컨트롤러마다 try-catch 안 쓰려면 횡단 처리가 필요 | **W2** `global/exception/` |
| JWT 인증 | Spring Security가 무거우니 다음에 | **W2** `global/security/` |
| 환경별 설정 분리 | 일단 H2로 로컬만 | **W2** `application-{local,dev,prod}.yml` |
| Kafka 이벤트 통신 | 도메인 간 직접 호출이 막힐 때 도입 | **W3** 도메인별 `kafka/` |
| 헥사고날 아키텍처 | 계층이 늘어나면 도입 | **W4** `api/application/domain/infrastructure` |
| ArchUnit 룰 강제 | 컨벤션이 익숙해진 뒤 강제 | **W4** `src/test/.../arch/` |

각 단계는 **앞 단계의 문제가 실제로 느껴질 때** 도입해야 가치가 있습니다. 처음부터 W4로 시작하면 추상화에 짓눌립니다.

---

## ⚠️ W1에서 자주 하는 실수

### 1. controller에 비즈니스 로직 작성

```java
// ❌ 나쁜 예
@PostMapping("/login")
public TokenResponse login(@RequestBody LoginRequest request) {
    User user = userRepository.findByEmail(request.email()).orElseThrow();
    if (!password.matches(user.getPasswordHash())) throw ...;   // 컨트롤러에서 비즈니스 룰!
    return new TokenResponse(...);
}

// ✅ 좋은 예
@PostMapping("/login")
public TokenResponse login(@RequestBody LoginRequest request) {
    return authService.login(request);   // service에 위임
}
```

### 2. 도메인 간 직접 import

```java
// ❌ 절대 금지
package com.synapse.platform.billing.service;
import com.synapse.platform.auth.service.AuthService;   // billing이 auth를 import!

@Service
public class BillingService {
    private final AuthService authService;   // 도메인 결합 → 향후 분리 불가
}
```

**규칙**: `billing/` 하위 코드는 `auth/`, `audit/`, `notification/` 하위 클래스를 **import하면 안 됨**. 다른 도메인의 정보가 필요하다면 W3에서 Kafka 이벤트로 받아오세요.

### 3. entity를 그대로 응답으로 내보내기

```java
// ❌ 나쁜 예
@GetMapping("/{id}")
public User getUser(@PathVariable Long id) {   // entity가 응답
    return userRepository.findById(id).orElseThrow();
}
```

문제: DB 컬럼 추가/삭제가 즉시 API 변경. `passwordHash`까지 외부에 노출. → **반드시 DTO로 변환해서 응답.**

### 4. setter 남용

```java
// ❌
User user = userRepository.findById(1L).get();
user.setEmail("new@example.com");
userRepository.save(user);

// ✅ — 도메인 메서드로 의도를 드러내기
user.changeEmail("new@example.com");   // 검증 로직도 메서드 안에
```

### 5. `application.yml`에 비밀번호/시크릿 하드코딩

W2에서 `application-prod.yml`을 분리하지만, **시크릿은 절대 yml에 직접 쓰지 않습니다.** 환경변수(`${DB_PASSWORD}`)로 주입.

---

## 🔭 다음 주차 미리보기

| 브랜치 | 추가되는 것 | 한 줄 요약 |
|---|---|---|
| `skeleton/platform/w2` | `global/` (config·exception·response·security·util) | 횡단 관심사 분리, JWT, 프로파일 |
| `skeleton/platform/w3` | 도메인별 `kafka/` | 도메인 간 통신을 이벤트로 |
| `skeleton/platform/w4` | `api/application/domain/infrastructure` + ArchUnit | 라이트 헥사고날 + 자동 강제 |

각 브랜치로 `git checkout`해서 차이를 직접 보세요. 누적 분기이므로 `git diff skeleton/platform/w1..skeleton/platform/w2`로 추가분만 비교 가능합니다.

---

## 📚 용어 사전 (신입용)

| 용어 | 의미 |
|---|---|
| **도메인(Domain)** | 비즈니스 영역. "auth(인증)", "billing(과금)"처럼 한 가지 책임. |
| **모듈리스(Modular Monolith)** | 한 레포에 여러 도메인을 두지만 폴더로 강하게 분리한 구조. 마이크로서비스 직전 단계. |
| **계층(Layer)** | controller/service/repository처럼 책임을 나눈 가로 구분. |
| **DTO** | Data Transfer Object. API 입출력용 데이터 클래스. |
| **Entity** | DB 테이블과 1:1 매핑된 클래스. `@Entity` 어노테이션. |
| **JPA** | Java Persistence API. Java 객체 ↔ DB 테이블 매핑 표준. Hibernate가 구현체. |
| **Spring Data JPA** | JPA를 더 편하게 쓰는 Spring 라이브러리. `JpaRepository` 인터페이스 상속만 하면 CRUD 자동 생성. |
| **Bean** | Spring이 관리하는 객체. `@Service`, `@Component` 등이 붙으면 자동 등록. |
| **DI (Dependency Injection)** | 객체가 직접 `new` 하지 않고 외부에서 주입받는 방식. 생성자 매개변수로 받는 게 정석. |
| **`@RestController`** | HTTP 요청을 받는 컨트롤러. 반환값이 자동으로 JSON 직렬화. |
| **`@RequestBody`** | HTTP 요청 본문(JSON)을 Java 객체로 자동 변환. |
| **`record`** | Java 16+의 불변 데이터 클래스 문법. 한 줄로 DTO 작성. |
| **H2** | 인메모리 DB. 로컬 개발/테스트용. 앱 끄면 데이터 사라짐. |
| **`bootRun`** | Gradle 태스크. `./gradlew bootRun`으로 Spring Boot 앱 실행. |

---

## 🆘 막힐 때

- 코드가 안 컴파일됨 → 패키지 import 누락이 90%. IDE의 자동 import 단축키 (`Ctrl+Shift+O` IntelliJ).
- bootRun에서 포트 충돌 → `application.yml`의 `server.port` 변경 or `lsof -i :8080`으로 점유 프로세스 확인.
- H2 Console로 DB 보고 싶음 → `application.yml`에 `spring.h2.console.enabled=true` 추가 후 `http://localhost:8080/h2-console`.
- 더 깊이 알고 싶음 → [Spring Boot 공식 가이드](https://spring.io/guides), 사내 `syn` 레포의 컨벤션 문서.
