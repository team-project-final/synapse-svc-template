# synapse-platform-svc — W2 skeleton

> **추가**: `global/` 횡단 관심사 (config·exception·response·security·util) + 프로파일 분리 (local/dev/prod) + Spring Security + JWT 기본 설정.

## 패키지 구조 (W2)

```
src/main/java/com/synapse/platform/
├── PlatformApplication.java
├── auth/ audit/ billing/ notification/      ← W1 그대로 (auth만 ApiResponse 적용 데모)
└── global/                                  ← NEW
    ├── config/
    │   ├── SecurityConfig.java               JWT 필터 체인 + PasswordEncoder Bean
    │   └── RedisConfig.java                  StringRedisTemplate Bean
    ├── exception/
    │   ├── ErrorCode.java                    enum: 도메인별 코드 (A001~, B001~, N001~)
    │   ├── BusinessException.java
    │   └── GlobalExceptionHandler.java       @RestControllerAdvice
    ├── response/
    │   └── ApiResponse<T>                    {success, data, error, timestamp}
    ├── security/
    │   ├── JwtTokenProvider.java             jjwt 0.12.x API
    │   └── JwtAuthFilter.java                OncePerRequestFilter
    └── util/                                 도메인 독립 유틸 전용 (현재 비어있음)
```

## W1 → W2 변화 요약

| 항목 | W1 | W2 |
|---|---|---|
| 응답 포맷 | 도메인 DTO 직접 반환 | `ApiResponse<T>` 래핑 |
| 예외 처리 | 도메인 컨트롤러마다 try-catch | `GlobalExceptionHandler` 일원화 |
| 인증 | 없음 | JWT + `JwtAuthFilter` + `SecurityConfig` |
| 설정 | `application.yml` 1개 | common + local/dev/prod 4개 |
| 의존성 | web/jpa/validation | + security + jjwt + redis |

## 프로파일

```bash
# 로컬 (H2)
./gradlew bootRun

# 개발 환경 (Postgres + Redis 컨테이너)
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun

# 프로덕션
SPRING_PROFILES_ACTIVE=prod java -jar build/libs/synapse-platform-svc.jar
```

## 다음 주차

- `skeleton/platform/w3` — 도메인별 `kafka/` 추가, shared-events 의존
- `skeleton/platform/w4` — `api/application/domain/infrastructure` + ArchUnit
