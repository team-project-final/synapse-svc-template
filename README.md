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

## 🚀 W2에서 추가되는 것들 — 각 항목이 **왜** 필요한가

"표면적으로 무엇이 추가되나"를 넘어, **W1 상태에서 어떤 통증이 그 추가를 요구하는가**를 봐야 합니다. 같은 패턴이 W2→W3, W3→W4에서도 반복됩니다: **현재의 통증이 다음 단계를 부른다.**

### 1️⃣ `ApiResponse<T>` — 통일 응답 포맷

**W1에서 무엇이 아픈가?**

W1의 컨트롤러들은 응답이 제각각입니다:

```java
// auth — DTO 직접 반환
@PostMapping("/login")
public TokenResponse login(...) { ... }

// audit — List 직접 반환
@GetMapping
public List<AuditLogResponse> list() { ... }
```

응답 JSON도 도메인마다 다른 모양:
```json
// auth/login 성공 응답
{"accessToken":"...","refreshToken":"..."}

// audit/logs 성공 응답
[{"id":1,...}, {"id":2,...}]

// 그리고 에러가 나면? Spring 기본 포맷
{"timestamp":"...","status":500,"error":"...","path":"..."}
```

**프론트엔드는 매번 분기**해야 합니다: "이번 응답은 객체? 배열? 에러 포맷은 또 다르네?" 도메인이 10개로 늘면 클라이언트 코드가 if/else 지옥.

**W2의 해법**: 모든 응답을 같은 봉투로 감쌈.

```json
{"success":true,  "data":{"accessToken":"...","refreshToken":"..."}, "timestamp":"..."}
{"success":true,  "data":[{"id":1,...}], "timestamp":"..."}
{"success":false, "error":{"code":"A001","message":"..."}, "timestamp":"..."}
```

프론트는 `response.success`로 분기만 하면 끝. 도메인이 100개여도 동일.

> 💡 추가 효과: API 문서가 "응답에 항상 `success`, `data`, `error`, `timestamp`가 있다"는 한 줄로 끝남.

---

### 2️⃣ `global/exception/` — 통일 예외 처리

**W1에서 무엇이 아픈가?**

위 응답 통일을 W1 코드에 직접 넣으려면 모든 컨트롤러에 try-catch가 필요합니다:

```java
// auth/controller/AuthController.java (W1에 통일 응답을 억지로 넣으면)
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest req) {
    try {
        TokenResponse data = authService.login(req);
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    } catch (InvalidCredentialsException e) {
        return ResponseEntity.status(401)
            .body(Map.of("success", false, "error", Map.of("code","A001","message",e.getMessage())));
    } catch (Exception e) {
        return ResponseEntity.status(500)
            .body(Map.of("success", false, "error", Map.of("code","C999","message","서버 오류")));
    }
}
```

4개 컨트롤러 × 평균 3개 엔드포인트 = **12번 같은 try-catch 복붙**. 한 군데만 실수해도 일관성 깨짐. 에러 코드 정의도 사방에 흩어짐.

**W2의 해법**: 컨트롤러는 그냥 던지고, `@RestControllerAdvice`가 가로채서 일관 처리.

```java
// 컨트롤러 — 깔끔
@PostMapping("/login")
public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
    return ApiResponse.ok(authService.login(req));
}

// 서비스 — 그냥 던지면 됨
throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);

// global/exception/GlobalExceptionHandler — 한 곳에서 처리
@ExceptionHandler(BusinessException.class)
public ResponseEntity<ApiResponse<Void>> handle(BusinessException e) { ... }
```

`ErrorCode` enum이 모든 에러를 카탈로그화 → 코드 prefix(`A___`/`B___`/`N___`)로 도메인 구분.

> 💡 예외 응답을 한 줄 바꾸려면? `GlobalExceptionHandler` 1곳 수정. W1 방식이면 12곳 수정.

---

### 3️⃣ `global/security/` + `SecurityConfig` — JWT 인증

**W1에서 무엇이 아픈가?**

지금 `GET /api/v1/audit/logs`는 **누구나 호출 가능**합니다. 운영 절대 불가.

W1에서 인증을 넣으려면 모든 컨트롤러 메서드 시작 부분에:
```java
@GetMapping
public List<AuditLogResponse> list(@RequestHeader("Authorization") String header) {
    if (!validateToken(header)) throw new UnauthorizedException();
    return auditService.findAll();
}
```

또 12번 복붙. 토큰 검증 로직 바꾸려면 12곳 수정.

**W2의 해법**: Spring Security Filter Chain이 모든 요청을 가로채 토큰 검증.

```
요청 → JwtAuthFilter → SecurityContext에 인증 정보 저장 → 컨트롤러 (인증된 상태)
                  ↓ (토큰 없거나 무효)
                401 자동 응답 (컨트롤러 도달 안 함)
```

컨트롤러 코드는 W1과 동일 — 인증을 신경 쓰지 않음.

**왜 JWT인가?** (다른 선택과 비교)
| 방식 | 장점 | 단점 |
|---|---|---|
| 세션 (서버 메모리) | 즉시 무효화 | 서버 스케일아웃 시 세션 공유 필요 |
| 세션 (Redis) | 스케일 OK | DB 라운드트립 |
| **JWT** | stateless, 서버 부담 0 | 무효화 어려움(리프레시 토큰으로 보완) |

마이크로서비스 환경 = 보통 JWT 선택. 무효화는 W2의 Redis에 블랙리스트로 보완.

---

### 4️⃣ `global/config/RedisConfig` — Redis 연결

**W1에서 무엇이 아픈가?**

W1은 Redis가 없습니다. W2 JWT 시스템을 운영 수준으로 만들려면:
- **리프레시 토큰 저장** — 메모리에 두면 서버 재시작 시 모든 사용자 강제 로그아웃
- **토큰 블랙리스트** — 로그아웃한 토큰을 만료 전까지 거부할 저장소 필요
- **레이트 리미팅** — "분당 로그인 5회 초과 차단" 같은 카운터

이 모든 게 Redis(빠른 KV 스토어)가 필요한 이유.

**W2의 해법**: `StringRedisTemplate` Bean 등록. 사용 자체는 W2~W3에서 본격화.

---

### 5️⃣ 프로파일 분리 (`application-{local,dev,prod}.yml`)

**W1에서 무엇이 아픈가?**

W1의 `application.yml`은 H2 인메모리 DB로 고정:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:platform
    driver-class-name: org.h2.Driver
```

이 상태로 dev 서버에 배포하려면? 매번 yml 수정 → 빌드 → 푸시. **개발자 PC에선 H2, 서버에선 Postgres**라는 차이를 빌드 산출물 안에 표현할 방법이 없음.

W1의 더 큰 문제: **비밀번호가 yml에 들어가는 순간 GitHub로 새어 나감**. 시크릿 스캐너가 PR을 차단.

**W2의 해법**: 환경별 yml 분리 + 시크릿은 환경변수.

```yaml
# application-prod.yml
spring:
  datasource:
    url: ${DB_URL}              # 환경변수에서 주입
    password: ${DB_PASSWORD}    # GitHub에 절대 안 올라감
```

```bash
# 실행 환경 선택
SPRING_PROFILES_ACTIVE=prod java -jar app.jar
```

**`ddl-auto`도 환경별로 달라야 함**: local=`create-drop`, dev=`validate`, prod=`none` (운영에선 마이그레이션 도구로). 단일 yml로는 표현 불가.

---

### 6️⃣ Bean Validation (`@Valid`, `@NotBlank`, `@Email`)

**W1에서 무엇이 아픈가?**

W1의 `LoginRequest`는 검증이 없음:
```java
public record LoginRequest(String email, String password) {}
```

사용자가 `{"email":"","password":null}` 보내면? AuthService 안에 들어가서 NullPointerException. **검증을 서비스에서** 하면:
```java
public TokenResponse login(LoginRequest req) {
    if (req.email() == null || req.email().isBlank()) throw new ...;
    if (!isEmail(req.email())) throw new ...;
    if (req.password() == null || req.password().length() < 8) throw new ...;
    // 본 로직은 5줄 아래에...
}
```

도메인 로직의 절반이 검증으로 도배됨. 12개 엔드포인트 × 평균 5개 필드 = 60줄의 검증 boilerplate.

**W2의 해법**: 어노테이션으로 선언적 검증.

```java
public record LoginRequest(
    @NotBlank @Email String email,
    @NotBlank String password
) {}

// 컨트롤러에 @Valid만 붙이면 자동 검증, 위반 시 MethodArgumentNotValidException
public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest req) { ... }
```

위반은 `GlobalExceptionHandler`가 자동 처리 (위 ②번). 서비스 코드는 본 로직만.

---

### 7️⃣ 의존성 추가 — `spring-security`, `jjwt`, `spring-data-redis`

위 6개 항목의 구현에 필요한 라이브러리들. **단순 의존성 추가가 아니라 W2의 모든 기능이 이걸 깔고 동작합니다.**

| 의존성 | 어디서 쓰나 |
|---|---|
| `spring-boot-starter-security` | SecurityConfig, JwtAuthFilter (filter chain) |
| `jjwt-api/impl/jackson 0.12.x` | JwtTokenProvider (토큰 발급/파싱) |
| `spring-boot-starter-data-redis` | StringRedisTemplate (리프레시 토큰, 블랙리스트) |
| `spring-security-test` | 보호 엔드포인트 단위 테스트 (`@WithMockUser`) |

---

### 🔁 패턴: "통증 → 추가" 사이클

W1 → W2의 7가지 항목을 보면 모두 같은 패턴:

```
W1에서 N번 복붙 → 일관성 깨짐 + 변경 비용 N배   ←  통증
                ↓
W2에서 한 곳에 모음 → 모든 도메인이 자동 혜택   ←  해법
```

이 패턴이 W2→W3, W3→W4에서도 반복됩니다.

- **W3**: "도메인 간 직접 호출이 강결합·장애전파를 부름" → Kafka 이벤트로 디커플링
- **W4**: "service가 JPA·Kafka·룰을 모두 짊어져 무거움" → 라이트 헥사고날로 책임 분리

각 단계는 **앞 단계의 통증을 실제로 느꼈을 때** 도입해야 합니다. 미리 다 박으면 추상화의 무게에 짓눌립니다.

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
