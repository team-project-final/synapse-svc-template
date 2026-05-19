# synapse-platform-svc — W2 skeleton

> **한 줄 정의**: "W1 위에 횡단 관심사(`global/`) + JWT 인증 + 환경별 프로파일을 얹은 단계."
> 4개 도메인은 그대로지만, 그 도메인들이 공통으로 쓰는 기반이 생겼습니다.

이 문서는 W1을 끝낸 신입/주니어가 **"왜 갑자기 `global/` 패키지가 필요한가"**를 이해하도록 설계되었습니다.

---

## 🎯 이 단계의 목표

W2를 마치면 여러분은:
- [x] 횡단 관심사(cross-cutting concern)가 무엇인지 안다
- [x] `ApiResponse<T>`로 응답을 통일하는 이유를 안다
- [x] `BusinessException`을 던지면 어떻게 자동 처리되는지 흐름을 안다
- [x] JWT 토큰이 발급되고 검증되는 전체 흐름을 따라갈 수 있다
- [x] `application-{local,dev,prod}.yml`로 환경을 분리할 줄 안다

---

## 🧭 큰 그림: 왜 횡단 관심사를 분리하나요?

### ❌ 안 분리하면 — 4개 컨트롤러가 같은 일을 4번씩

W1까지만 작성된 상태에서, **모든 도메인에 인증과 예외 처리를 넣어야 한다면** 이렇게 됩니다:

```java
// auth/controller/AuthController.java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest req) {
    try {
        // JWT 검증, 권한 체크 (코드 20줄)
        TokenResponse result = authService.login(req);
        return ResponseEntity.ok(Map.of("success", true, "data", result));
    } catch (InvalidCredentialsException e) {
        return ResponseEntity.status(401).body(Map.of("success", false, "error", e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(500).body(...);
    }
}

// billing/controller/BillingController.java
@PostMapping("/charge")
public ResponseEntity<?> charge(@RequestBody ChargeRequest req) {
    try {
        // 똑같은 JWT 검증, 권한 체크 (또 20줄!)
        ...
    } catch (...) { ... }
}

// audit, notification — 같은 패턴 두 번 더
```

**4개 도메인 × 동일한 try-catch = 80줄의 중복.** 응답 포맷 한 번 바꾸려면 4개 파일 수정. 한 군데만 실수해도 일관성 깨짐.

### ✅ 횡단 관심사 분리

W2는 이런 중복을 **한 곳(`global/`)에 모아놓고, 4개 도메인은 모르게** 만듭니다:

```java
// 4개 컨트롤러는 이렇게 깔끔해집니다
@PostMapping("/login")
public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
    return ApiResponse.ok(authService.login(req));   // 끝.
}
```

- 인증 → `JwtAuthFilter`가 요청 들어올 때 자동 검증
- 예외 → `GlobalExceptionHandler`가 모든 컨트롤러 예외를 가로채서 일관된 응답으로 변환
- 응답 포맷 → `ApiResponse<T>`가 표준 봉투

이게 **횡단 관심사(Cross-Cutting Concern)**의 분리입니다. 도메인 코드는 비즈니스에만 집중하고, 공통 기반은 한 곳에 모이는 패턴.

---

## 📂 W2에서 추가된 구조

```
src/main/java/com/synapse/platform/
├── PlatformApplication.java
├── auth/ audit/ billing/ notification/   ← W1 그대로 (auth만 ApiResponse 적용 데모)
└── global/                                ← NEW (이 단계의 핵심)
    ├── config/                            ← Bean 설정 모음
    │   ├── SecurityConfig.java               JWT 필터 체인 + PasswordEncoder
    │   └── RedisConfig.java                  StringRedisTemplate Bean
    ├── exception/                         ← 통일 예외 처리
    │   ├── ErrorCode.java                    enum: 코드/메시지/HTTP 상태
    │   ├── BusinessException.java            던지기만 하면 자동 처리됨
    │   └── GlobalExceptionHandler.java       @RestControllerAdvice
    ├── response/                          ← 통일 응답 포맷
    │   └── ApiResponse<T>                    {success, data, error, timestamp}
    ├── security/                          ← JWT 인증 인프라
    │   ├── JwtTokenProvider.java             토큰 발급/파싱
    │   └── JwtAuthFilter.java                요청마다 토큰 검증
    └── util/                              ← 도메인 독립 유틸 (현재 비어있음)
```

그리고 `src/main/resources/`에:

```
resources/
├── application.yml          ← 모든 프로파일 공통 (앱 이름, JWT, actuator)
├── application-local.yml    ← H2, 로컬 Redis
├── application-dev.yml      ← 개발용 Postgres/Redis
└── application-prod.yml     ← 프로덕션 (시크릿은 환경변수)
```

---

## 🧱 각 `global/` 하위 패키지 자세히

### 1️⃣ `global/response/ApiResponse<T>` — 응답을 봉투로 감싸기

**왜?** 클라이언트가 "성공인가, 실패인가, 데이터가 뭔가"를 일관된 구조로 받게 하려고.

```java
public record ApiResponse<T>(
    boolean success,
    T data,
    ApiError error,
    Instant timestamp
) {
    public static <T> ApiResponse<T> ok(T data) { ... }
    public static <T> ApiResponse<T> fail(String code, String message) { ... }
}
```

**Before (W1)**:
```json
{"accessToken":"abc","refreshToken":"def"}     // 200 OK
{"timestamp":"...", "status":500, ...}          // 500 (Spring 기본)
```

**After (W2)**:
```json
{ "success": true,  "data": {"accessToken":"abc","refreshToken":"def"}, "timestamp": "..." }
{ "success": false, "error": {"code":"A001","message":"이메일 또는 비밀번호..."}, "timestamp": "..." }
```

→ 프론트엔드는 항상 `response.success`로 분기하면 됨. 응답 포맷이 도메인별로 다를 일이 없음.

### 2️⃣ `global/exception/` — 예외를 던지면 자동 변환

`ErrorCode`는 enum으로 모든 에러 코드를 한 곳에 정의:

```java
public enum ErrorCode {
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A001", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVOICE_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "청구서를 찾을 수 없습니다."),
    // ...
}
```

코드 prefix 약속: `A___` = auth, `B___` = billing, `N___` = notification, `C___` = 공통.

서비스 코드에서는 그냥 던집니다:

```java
@Service
public class AuthService {
    public TokenResponse login(LoginRequest req) {
        User user = userRepo.findByEmail(req.email())
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        // ...
    }
}
```

그러면 `GlobalExceptionHandler`가 가로채서:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handle(BusinessException e) {
        return ResponseEntity.status(e.getErrorCode().getStatus())
            .body(ApiResponse.fail(e.getErrorCode().getCode(), e.getMessage()));
    }
}
```

자동으로 `401 Unauthorized + {"success":false,"error":{"code":"A001",...}}` 응답이 됩니다. **컨트롤러에 try-catch 없음.**

> 💡 `@RestControllerAdvice`는 "모든 컨트롤러를 둘러싼 인터셉터" 같은 역할. 컨트롤러에서 던진 예외를 가로챕니다.

### 3️⃣ `global/security/` + `global/config/SecurityConfig` — JWT 인증

**JWT가 뭔가요?**
- JSON Web Token. 사용자 정보를 서명된 문자열로 만들어 클라이언트에 발급.
- 서버는 토큰을 보관하지 않음(stateless). 토큰 자체로 검증.
- 형태: `xxxxx.yyyyy.zzzzz` (header.payload.signature)

**흐름**:

```
[로그인]
  Client ──POST /api/v1/auth/login (email,password)─→ AuthController
                                                      │
                                                      ↓
                                       AuthService.login()
                                          → 비밀번호 검증 (BCrypt)
                                          → JwtTokenProvider.issueAccessToken(userId)
                                          → "eyJhbG..." 토큰 생성
                                       
  Client ←─{"accessToken":"eyJhbG..."}── AuthController

[이후 모든 보호 API 호출]
  Client ──GET /api/v1/billing/...
            Authorization: Bearer eyJhbG...
                  ↓
            JwtAuthFilter (OncePerRequestFilter)
              → 헤더에서 토큰 추출
              → JwtTokenProvider.parse(token) 검증
              → SecurityContextHolder에 인증 정보 저장
                  ↓
            SecurityConfig.filterChain()
              → /api/v1/auth/** = permitAll, 그 외 = authenticated
                  ↓
            BillingController.charge() 실행
```

**SecurityConfig 핵심**:

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(disable)
        .sessionManagement(STATELESS)             // JWT니까 세션 안 씀
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/auth/**", "/actuator/health").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
}
```

`PasswordEncoder`는 `BCryptPasswordEncoder` Bean으로 등록 → AuthService에서 주입받아 사용.

> ⚠️ **`synapse.jwt.secret`은 반드시 32바이트 이상**. `application.yml`의 기본값은 개발용. 프로덕션은 환경변수로 주입.

### 4️⃣ `global/config/RedisConfig` — Redis 연결

W2에서는 빈 껍데기만. W3 이후 토큰 블랙리스트, 캐싱 등에 활용 예정.

### 5️⃣ `global/util/` — 도메인 독립 유틸

지금은 비어있지만, 날짜 포맷·문자열 마스킹·해시 헬퍼처럼 **도메인 의존 없는 유틸**만 들어갑니다. `billing` 전용 헬퍼는 `billing/` 내부에 두세요.

---

## ⚙️ 프로파일 시스템

### 왜 4개로 나누나요?

| 파일 | 언제 쓰이나 | 핵심 차이 |
|---|---|---|
| `application.yml` | 모든 환경 공통 | 앱 이름, JWT 설정, Actuator 노출 범위 |
| `application-local.yml` | 개발자 로컬 | H2 인메모리 DB, 로컬 Redis, DEBUG 로그 |
| `application-dev.yml` | 개발 서버 (k8s dev namespace 등) | 컨테이너 Postgres, INFO 로그, `ddl-auto: validate` |
| `application-prod.yml` | 프로덕션 | 환경변수 시크릿, Hikari pool 튜닝, WARN 로그 |

### 실행 방법

```bash
# 로컬 (기본 — application.yml의 active: local)
./gradlew bootRun

# 개발 서버용 빌드
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun

# 프로덕션 (시크릿은 환경변수 필수)
DB_URL=jdbc:postgresql://... DB_USERNAME=... DB_PASSWORD=... \
  SPRING_PROFILES_ACTIVE=prod \
  java -jar build/libs/synapse-platform-svc-0.2.0-SNAPSHOT.jar
```

### `ddl-auto`의 함정 — 절대 헷갈리지 마세요

| 값 | 동작 | 언제? |
|---|---|---|
| `create-drop` | 시작할 때 테이블 생성, 종료 시 삭제 | local 테스트만 |
| `create` | 매번 테이블 재생성 (데이터 날아감) | 위험. 거의 안 씀. |
| `update` | 스키마 자동 변경 | dev 초기. 운영에선 금지. |
| `validate` | 스키마가 entity와 일치하는지만 검증 | dev/staging 권장 |
| `none` | 아무것도 안 함 | **prod 필수**. 마이그레이션은 Flyway/Liquibase로. |

---

## 🔄 W1 → W2 변화 요약

| 항목 | W1 | W2 |
|---|---|---|
| 응답 포맷 | 도메인 DTO 직접 반환 | `ApiResponse<T>` 래핑 |
| 예외 처리 | (없음 — try-catch 필요) | `GlobalExceptionHandler` 일원화 |
| 입력 검증 | 없음 | `@Valid` + Bean Validation (`@NotBlank`, `@Email` 등) |
| 인증 | 없음 | JWT + `JwtAuthFilter` + `SecurityConfig` |
| 비밀번호 | 평문 (스텁) | `BCryptPasswordEncoder` |
| 설정 | `application.yml` 1개 | common + local/dev/prod 4개 |
| 의존성 | web/jpa/validation | + security + jjwt 0.12.x + spring-data-redis |

---

## ⚠️ W2에서 자주 하는 실수

### 1. `BusinessException`을 던지면서 핸들러 없이 try-catch

```java
// ❌ 컨트롤러에서 다시 catch — 핸들러가 일을 못 함
try {
    return ApiResponse.ok(authService.login(req));
} catch (BusinessException e) {
    return ApiResponse.fail(e.getErrorCode().getCode(), e.getMessage());
}

// ✅ 그냥 던지세요. 핸들러가 알아서 처리.
return ApiResponse.ok(authService.login(req));
```

### 2. 도메인 코드에서 `global/`을 너무 광범위하게 import

`global/`은 횡단 기반이지만, 도메인이 모든 걸 의존하면 결국 같은 문제가 됩니다.

- ✅ `BusinessException`, `ErrorCode`, `ApiResponse` → OK
- ✅ `JwtTokenProvider`, `PasswordEncoder` → OK (auth 도메인만)
- ❌ `global/util/`을 모든 도메인이 super-helper로 만들기 → `util` 비대화

**원칙**: `global/`은 "여러 도메인이 정말 공통으로 쓰는 것만". 그 도메인만 쓰면 도메인 내부에 두세요.

### 3. JWT 시크릿을 yml에 하드코딩

```yaml
# ❌
synapse:
  jwt:
    secret: "my-super-secret-key-do-not-leak-this"

# ✅
synapse:
  jwt:
    secret: ${SYNAPSE_JWT_SECRET}    # 환경변수로 주입
```

GitHub에 푸시한 순간 끝. **시크릿 스캐닝**에 걸립니다.

### 4. `@Valid` 빼먹기

```java
// ❌ — Validation 어노테이션이 DTO에 있어도 작동 안 함
public ApiResponse<TokenResponse> login(@RequestBody LoginRequest req) { ... }

// ✅
public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest req) { ... }
```

`@Valid` 없으면 `@NotBlank`, `@Email`이 무시됩니다. `MethodArgumentNotValidException` 발생도 안 함 = 핸들러 무용지물.

### 5. `RestControllerAdvice`에서 너무 광범위한 catch

```java
// ❌ — 모든 예외를 한 코드로 묶으면 디버깅 지옥
@ExceptionHandler(Exception.class)
public ApiResponse<Void> handleAll(Exception e) {
    return ApiResponse.fail("ERROR", "오류 발생");
}

// ✅ — 구체적 예외 먼저, Exception은 마지막에 (반드시 로그)
@ExceptionHandler(Exception.class)
public ApiResponse<Void> handleUnknown(Exception e) {
    log.error("Unhandled exception", e);
    return ApiResponse.fail(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage());
}
```

---

## ▶️ 실행 + 검증

### 1. 앱 띄우기 (로컬)

```bash
./gradlew bootRun
```

### 2. 인증 흐름 테스트

```bash
# 1) 로그인 시도 → 실패 응답이 통일 포맷으로 옴
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"pass1234"}'
# → {"success":false,"error":{"code":"A001","message":"W3 스텁 — ..."},"timestamp":"..."}

# 2) 보호 엔드포인트에 토큰 없이 접근 → 401
curl http://localhost:8080/api/v1/audit/logs
# → 401 Unauthorized

# 3) 헬스체크 (permitAll)
curl http://localhost:8080/actuator/health
# → {"status":"UP"}
```

### 3. 입력 검증 동작 확인

```bash
# 이메일 형식 위반 → 400 + 통일 에러
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"not-an-email","password":""}'
# → {"success":false,"error":{"code":"C001","message":"email: must be a well-formed email address"}, ...}
```

---

## 🔭 다음 주차 미리보기

| 브랜치 | 추가되는 것 |
|---|---|
| `skeleton/platform/w3` | 도메인별 `kafka/{producer,consumer}/`, 이벤트 토픽 컨벤션 |
| `skeleton/platform/w4` | 각 도메인을 `api/application/domain/infrastructure`로 재구성 + ArchUnit |

---

## 🚀 W3에서 추가되는 것들 — 각 항목이 **왜** 필요한가

W2에서는 횡단 관심사를 한 곳에 모았습니다. 하지만 **도메인끼리 어떻게 통신하는지**는 여전히 미정의입니다. W3은 이 빈자리를 채웁니다.

### 1️⃣ 도메인별 `kafka/{producer,consumer}/` 패키지 — 이벤트 통신 채널

**W2에서 무엇이 아픈가?**

회원가입 직후 환영 메일을 보내야 한다고 합시다. 가장 직관적인 코드:

```java
// auth/service/AuthService.java
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final NotificationService notificationService;   // ← 다른 도메인 의존!
    private final AuditService auditService;                  // ← 또 다른 도메인 의존!

    public TokenResponse register(SignupRequest req) {
        User user = userRepository.save(...);

        // 1) 환영 메일
        notificationService.sendWelcomeEmail(user.getEmail());
        // 2) 감사 로그
        auditService.record("USER_REGISTERED", user.getId());
        // 3) 다음 달엔 마케팅도 추가될 예정...

        return new TokenResponse(...);
    }
}
```

**5가지 통증**:

| 통증 | 구체적 결과 |
|---|---|
| **강결합** | `BillingService`/`NotificationService` 시그니처가 바뀌면 AuthService도 수정 |
| **장애 전파** | NotificationService가 죽으면 회원가입 전체가 500. "메일 못 보냈으니 가입도 안 됩니다." |
| **응답 지연** | 메일 발송이 끝날 때까지 사용자는 로딩 화면. 1초 → 3초 → 이탈 |
| **확장 불가** | 다음에 마케팅도 회원가입을 감지하려면? `auth.register()`에 또 한 줄 추가. 회원가입 부수효과마다 auth 수정 |
| **테스트 부담** | `AuthService` 단위 테스트에 NotificationService·AuditService mock 모두 필요 |

**W3의 해법**: auth는 "회원가입 일어남"이라는 사실(이벤트)만 발행, 나머지는 알아서 구독.

```java
// auth — 사실 발행만
public void register(SignupRequest req) {
    User user = userRepository.save(...);
    userEventPublisher.publishUserRegistered(new UserRegistered(user.getId(), user.getEmail(), Instant.now()));
}

// notification — 자기 일은 자기가
@KafkaListener(topics = "synapse.platform.auth.user-registered.v1")
public void on(UserRegistered e) { notificationService.sendWelcome(...); }

// audit — 자기 일은 자기가
@KafkaListener(topicPattern = "synapse\\..*")
public void on(Object e) { auditService.record(...); }
```

5가지 통증 모두 해결: AuthService는 누가 듣는지 모름. 메일 서버 죽어도 가입 성공. 마케팅이 추가돼도 auth 코드 0줄 수정.

> 💡 **왜 도메인 안에 `kafka/`를 두나?** "이 도메인이 어떤 이벤트를 발행/수신하는가"가 한눈에 보임. `kafka/` 글로벌 폴더에 모으면 결국 도메인이 흩어짐.

---

### 2️⃣ `global/config/KafkaConfig` — Producer/Consumer 팩토리

**W2에서 무엇이 아픈가?**

Producer/Consumer를 도메인마다 직접 만들면:

```java
// auth/kafka/UserEventPublisher.java (W2식으로 직접 구성)
public class UserEventPublisher {
    private final KafkaProducer<String, Object> producer;

    public UserEventPublisher() {
        Properties props = new Properties();
        props.put(BOOTSTRAP_SERVERS_CONFIG, "...");      // 4개 도메인이
        props.put(KEY_SERIALIZER_CLASS_CONFIG, ...);     // 같은 설정을
        props.put(VALUE_SERIALIZER_CLASS_CONFIG, ...);   // 4번 복붙
        props.put(ACKS_CONFIG, "all");
        this.producer = new KafkaProducer<>(props);
    }
}
```

부트스트랩 서버 주소 한 번 바꾸려면 4곳 수정. ACK 정책, 직렬화 방식 변경도 마찬가지.

**W3의 해법**: `KafkaConfig`가 `KafkaTemplate`/`ConsumerFactory` Bean을 등록 → 도메인은 주입만.

```java
// auth/kafka/producer/UserEventPublisher.java
@Component
public class UserEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;   // 주입받음
    // 설정은 KafkaConfig가 한 곳에서 관리
}
```

`@EnableKafka`도 여기에. 이게 없으면 `@KafkaListener`가 작동하지 않습니다.

---

### 3️⃣ `global/kafka/event/*` — 이벤트 클래스 임시 저장소

**W2에서 무엇이 아픈가?**

`UserRegistered` 이벤트를 auth가 정의하면:

```java
// auth/event/UserRegistered.java
package com.synapse.platform.auth.event;
public record UserRegistered(Long userId, String email, Instant at) {}
```

notification이 이걸 받으려면 **auth 패키지를 import**해야 함:

```java
// notification/kafka/consumer/UserRegisteredConsumer.java
import com.synapse.platform.auth.event.UserRegistered;   // ❌ 도메인 격리 위반!
```

이러면 도메인 분리 의미가 사라집니다. notification이 auth 변경에 결합됨.

**W3의 임시 해법**: 모든 이벤트 클래스를 `global/kafka/event/`에 모음.

```java
package com.synapse.platform.global.kafka.event;
public record UserRegistered(Long userId, String email, Instant at) {}
```

auth와 notification 모두 `global/`만 의존 → 서로를 import하지 않음.

**왜 "임시"인가?** 진짜 해법은 별도 라이브러리. `synapse-shared` 멀티모듈을 publish하면 `com.synapse.shared.event.UserRegistered`로 옮기고 `global/kafka/event/`는 삭제 예정. (build.gradle.kts에 주석으로 표시되어 있음)

> 💡 별도 라이브러리가 정답인 이유: knowledge-svc/engagement-svc 등 **다른 마이크로서비스도 같은 이벤트 스키마**가 필요. 한 서비스 안에 묻어두면 공유 불가.

---

### 4️⃣ 토픽 네이밍 컨벤션 — `synapse.{service}.{domain}.{event}.v{N}`

**W2에서 무엇이 아픈가?**

여러 서비스가 같은 Kafka 클러스터를 쓰는데 네이밍 규칙이 없으면:

```
auth-events             ← 어느 서비스 거? 어느 도메인?
user_registered          ← snake_case + kebab-case 혼용
sendWelcomeEmail         ← 명령형이라 "사실"인지 헷갈림
notification-v2          ← v1 어디 갔지?
```

운영 중 토픽 100개 넘어가면 누가 누구의 이벤트를 듣는지 추적 불가.

**W3의 해법**: 엄격한 컨벤션.

```
synapse.{service}.{domain}.{event-name}.v{version}
       ↓        ↓        ↓              ↓
   prefix   서비스   도메인     사실(과거형)   스키마 버전
```

예시: `synapse.platform.auth.user-registered.v1`

이름만 봐도 "platform 서비스의 auth 도메인에서 회원가입이 발생했다(v1 스키마)"가 읽힘. 그리고:

- **버전(`v1`, `v2`)** — Avro 스키마가 호환 안 되게 변경되면 새 버전 신설. v1과 병행 운영 후 deprecate.
- **과거형(`user-registered`)** — "일어났다"는 사실. 명령형(`send-email`)은 금지 — 명령은 수신자가 알아서 결정.

---

### 5️⃣ Consumer Group — 동일 메시지의 분배 vs 브로드캐스트

**W2에서 무엇이 아픈가?**

회원가입 이벤트 1개가 발생했는데:
- audit는 한 번 기록 → 1번 처리
- notification은 환영 메일 발송 → 1번 처리
- (마케팅이 들어오면) 캠페인 매칭 → 1번 처리

같은 이벤트지만 도메인마다 **각각** 처리해야 합니다. 동시에 한 도메인 안에 인스턴스가 3개라면 **한 도메인 안에서는 1번만** 처리되어야 합니다 (중복 메일 방지).

이걸 직접 구현하면 락·중복 체크·DLQ까지 끝없는 코드.

**W3의 해법**: Consumer Group이 자동 처리.

```java
// audit (인스턴스 3개) — 같은 groupId
@KafkaListener(topics = "...", groupId = "synapse-platform-audit")

// notification (인스턴스 5개) — 다른 groupId
@KafkaListener(topics = "...", groupId = "synapse-platform-notification")
```

같은 `groupId`끼리는 메시지 분담 (한 메시지 1번만), 다른 `groupId`는 독립 (각각 받음). Kafka가 무료로 제공.

---

### 6️⃣ `spring-kafka` 의존성 + 테스트 도구

W3 기능 구현용:

| 의존성 | 어디서 쓰나 |
|---|---|
| `org.springframework.kafka:spring-kafka` | KafkaTemplate, @KafkaListener, @EnableKafka |
| `org.springframework.kafka:spring-kafka-test` | EmbeddedKafka로 통합 테스트 (실 Kafka 없이) |

**왜 라이브러리?** Apache Kafka의 raw Java 클라이언트는 너무 저수준. Spring 추상화가 ack 관리·재시도·역직렬화 trust package 등을 표준화해줍니다.

---

### 🔁 패턴 재확인: 통증 → 추가

W2 → W3도 동일한 패턴:

```
W2의 상태:                              W3의 해법:
도메인 간 호출이 "그냥 가능"        →   직접 호출 자체를 막고 이벤트로
강결합·장애 전파·확장 부담            →   디커플링·격리·자유로운 구독자 추가
```

> 그리고 **W3→W4**도 같은 패턴: "service가 너무 많은 일(JPA 호출 + Kafka 발행 + 비즈니스 룰)을 짊어져 무겁다" → 라이트 헥사고날로 책임 분리. 자세한 건 W3 README의 마지막 섹션에서.

---

## 📚 W2에서 새로 등장한 용어

| 용어 | 의미 |
|---|---|
| **횡단 관심사 (Cross-Cutting Concern)** | 여러 도메인이 공통으로 필요한 기능. 인증, 로깅, 예외 처리, 응답 포맷 등. |
| **`@RestControllerAdvice`** | 모든 컨트롤러를 감싸서 예외 처리·응답 변환을 일원화. |
| **`@ExceptionHandler`** | 특정 예외 타입을 처리하는 메서드. |
| **JWT (JSON Web Token)** | 서명된 토큰으로 사용자 신원을 증명. stateless 인증. |
| **BCrypt** | 비밀번호 해시 함수. 같은 입력도 매번 다른 해시 생성(salt). |
| **`PasswordEncoder`** | Spring Security의 비밀번호 해시 추상화. `BCryptPasswordEncoder`가 기본. |
| **`OncePerRequestFilter`** | 한 요청에 한 번만 실행되는 필터 (forward/redirect 시 중복 방지). |
| **`SecurityContextHolder`** | 현재 인증된 사용자 정보를 저장하는 ThreadLocal 컨테이너. |
| **`@Valid`** | 컨트롤러 파라미터에 붙이면 DTO의 검증 어노테이션이 작동. |
| **Bean Validation** | `@NotBlank`, `@Email`, `@Positive` 등 표준 검증. JSR-303/380. |
| **프로파일 (Profile)** | 환경별 설정 묶음. `application-{프로파일}.yml`. |
| **`ddl-auto`** | JPA가 시작 시 스키마를 어떻게 다룰지. `create-drop`/`validate`/`none`. |
| **Hikari** | Spring Boot 기본 connection pool. `prod`에서 풀 크기 튜닝 필수. |
| **Actuator** | Spring Boot의 운영 엔드포인트(health, metrics, info 등). |

---

## 🆘 막힐 때

- `Cannot resolve symbol BusinessException` → 패키지 경로 확인. `com.synapse.platform.global.exception.*`.
- 401 떠서 디버깅 못 함 → `SecurityConfig`의 `permitAll()` 목록에 임시 추가하고 작업 후 원복.
- JWT 검증 실패 → `synapse.jwt.secret`이 32바이트 이상인지 확인.
- `@Valid`인데 에러 메시지가 영어로 나옴 → 메시지 번들(`ValidationMessages.properties`)을 한국어로 추가 (W2에는 미포함).
