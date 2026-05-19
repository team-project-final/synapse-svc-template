# Spring Boot 4.0 마이그레이션 노트

> **적용 버전**: Spring Boot **4.0.6** + Gradle **9.5.1** + Java **21**
> **적용 일**: 2026-05-19
> **영향 범위**: 16개 skeleton 브랜치 모두

---

## 환경 호환성 확인

| 도구 | 버전 | 호환성 |
|---|---|---|
| Java | 21 (toolchain) | ✅ SB4 minimum |
| Gradle | 9.5.1 (wrapper) | ✅ SB4 Gradle plugin 요구사항 (8.14+ 또는 9.x) |
| Spring Boot | 4.0.6 | — |
| Spring Framework | 7.x (BOM 관리) | — |
| Spring Security | 7.x (BOM 관리) | — |
| Jackson | 3.x (BOM 관리) | — |
| Hibernate | 7.x (BOM 관리) | — |
| JUnit | 6 (BOM 관리) | — |
| Jakarta EE | 11 | — |

---

## 3.x → 4.x 주요 변경과 현재 코드 영향

### ✅ 영향 없음 (확인됨)

| 영역 | 코드 | 상태 |
|---|---|---|
| `@RestController`, `@Service`, `@Component`, `@Configuration`, `@Repository` | 모든 도메인 컨트롤러/서비스 | 변경 없음 |
| `jakarta.persistence.*` | 모든 Entity | 변경 없음 (Jakarta EE 11도 호환) |
| `JpaRepository` 상속 | 모든 repository | 변경 없음 |
| Spring Security DSL (`http.csrf(...)`, `authorizeHttpRequests(...)`) | `SecurityConfig` | SS7 호환 |
| **CSRF** stateless JWT로 명시 disable | `SecurityConfig.filterChain()` | SS7 default 변경 영향 없음 (이미 명시 disable) |
| `@SpringBootTest` | 모든 `*ApplicationTests` | JUnit 6 호환 |
| `BCryptPasswordEncoder` | `PasswordEncoder` Bean | 변경 없음 |
| `jjwt 0.12.x` API | `JwtTokenProvider`, `JwtAuthFilter` | 변경 없음 |
| `@KafkaListener`, `KafkaTemplate` | 모든 producer/consumer | spring-kafka 4.x 호환 |
| `record` DTO + Jackson 직렬화 | 모든 API DTO | Jackson 3 호환 (record 직렬화는 안정 API) |
| ArchUnit 1.3.0 | `*ArchitectureTest` | JUnit 6 호환 |

### ⚠️ 변경됐지만 영향 없음

| 변경 사항 | 왜 영향 없는가 |
|---|---|
| **Jackson 3** — 직렬화 동작 일부 변경 | 우리 DTO는 모두 `record` (불변, 표준 필드 직렬화). 커스텀 `@JsonInclude` 1곳뿐(`ApiResponse`), Jackson 3에서도 동일 동작. |
| **JUnit 6** — `@ExtendWith` 새 패턴 | `@SpringBootTest`가 자동 등록, 별도 변경 불필요. |
| **Spring Security 7** — CSRF default 변경 | REST API에 명시적 `csrf(disable)` 적용 — default 변경 영향 0. |
| **Undertow 제거** | Spring Boot 기본은 Tomcat이라 우리 영향 없음. |
| **36개 deprecated API 제거** | 우리 코드에 사용된 deprecated API 없음. |

### 🚧 향후 진짜 비즈니스 로직 작성 시 주의 (잠재 이슈)

| 영역 | 주의 사항 |
|---|---|
| Jackson 커스텀 Serializer/Deserializer | 추가 시 Jackson 3 API 사용 (`com.fasterxml.jackson.core 3.x`) |
| Spring Security 커스텀 AuthenticationProvider | SS7에서 일부 인터페이스 시그니처 변경 — 추가 시 SS7 docs 확인 |
| `WebMvcConfigurer` 커스텀 | Spring Framework 7 변경 사항 확인 |
| 외부 라이브러리 (예: Liquibase, MapStruct) | SB4 호환 버전 사용 — BOM이 일부 관리 |

---

## 업그레이드 검증 절차

각 브랜치에서:

```bash
# 1. 컴파일
./gradlew compileJava

# 2. 테스트 (ArchUnit 포함)
./gradlew test

# 3. 부트 실행
./gradlew bootRun

# 4. 엔드포인트 호출
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"pass1234"}'
```

learning 브랜치는 `cd learning-java`.

---

## 변경된 파일 (브랜치당 1개)

- `build.gradle.kts` (root)  — platform/knowledge/engagement 12 브랜치
- `learning-java/build.gradle.kts` — learning 4 브랜치

변경 내용:
```diff
- id("org.springframework.boot") version "3.3.5"
+ id("org.springframework.boot") version "4.0.6"
```

`io.spring.dependency-management 1.1.6`은 유지 (SB4와 호환).

---

## 만약 빌드 실패하면 — 트러블슈팅

| 에러 | 원인 가능성 | 해결 |
|---|---|---|
| `Unsupported class file major version` | Java 21 미설치 | JDK 21 설치 또는 toolchain 자동 다운로드 |
| `Could not resolve all dependencies for ...` | Maven Central 일시 장애 | 잠시 후 재시도 |
| `Class file has wrong version` | 캐시된 Java 17/19 bytecode | `./gradlew clean build` |
| Spring Security 관련 컴파일 에러 | SS7 API 변경 (커스텀 코드 있을 때) | [SS7 마이그레이션 가이드](https://docs.spring.io/spring-security/reference/migration/index.html) 참조 |

---

## 참고 자료

- [Spring Boot 4.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Release-Notes)
- [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [Gradle 9 Compatibility Matrix](https://docs.gradle.org/current/userguide/compatibility.html)
