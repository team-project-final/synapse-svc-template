# synapse-platform-svc — W4 skeleton (최종)

> **추가**: 각 도메인을 `api/application/domain/infrastructure` 라이트 헥사고날로 재구성.
> **ArchUnit**으로 도메인 격리·계층 방향·룰 격리를 자동 강제.

## 패키지 구조 (W4)

각 도메인이 동일한 4-계층 패턴:

```
src/main/java/com/synapse/platform/auth/    (← 4개 도메인 모두 동일)
├── api/                                     ← W3의 controller/
│   ├── AuthController.java
│   └── dto/
│       ├── request/LoginRequest.java
│       └── response/TokenResponse.java
├── application/                             ← W3의 service/
│   ├── AuthService.java                      (UseCase)
│   └── port/                                 (outbound 인터페이스)
│       ├── UserPort.java
│       └── EventPort.java
├── domain/                                   ← W3의 entity/
│   ├── User.java
│   └── policy/                               ← NEW (도메인 룰)
│       └── PasswordPolicy.java
└── infrastructure/                           ← W3의 repository/ + kafka/
    ├── persistence/
    │   ├── UserJpaRepository.java            (Spring Data, package-private)
    │   └── UserPersistenceAdapter.java       (implements UserPort)
    └── messaging/
        └── UserEventKafkaAdapter.java        (implements EventPort)
```

4개 도메인 전체 구조:

```
com.synapse.platform/
├── PlatformApplication.java
├── auth/        api/ application/{port/} domain/{policy/} infrastructure/{persistence/, messaging/}
├── audit/       api/ application/{port/} domain/{policy/} infrastructure/{persistence/, messaging/}
├── billing/     api/ application/{port/} domain/{policy/} infrastructure/{persistence/, messaging/}
├── notification/api/ application/{port/} domain/{policy/} infrastructure/{persistence/, messaging/}
└── global/      config/ exception/ response/ security/ util/ kafka/event/
```

## ArchUnit 강제 규칙

`src/test/java/com/synapse/platform/arch/PlatformArchitectureTest.java`가 다음을 검증:

| # | 규칙 | 위반 시 fail |
|---|---|---|
| 1 | 도메인 슬라이스 간 직접 의존 금지 (`global/` 제외) | `auth → billing` import |
| 2 | `domain/` → 다른 계층 의존 금지 | `domain.User`가 `application.AuthService` import |
| 3 | `application/` → `api/`·`infrastructure/` 의존 금지 (`port/` 예외) | service가 controller dto 직접 사용 |
| 4 | `api/` → `infrastructure/` 의존 금지 | controller가 JpaRepository 직접 사용 |
| 5 | `domain.policy/` → 외부 의존성 0 | policy 안에 Spring 어노테이션 |
| 6 | `JpaRepository`는 `infrastructure.persistence`에만 | 다른 곳에 repository |
| 7 | `@KafkaListener`는 `infrastructure.messaging`에만 | service에 직접 리스너 |

## W3 → W4 변화 요약

| 항목 | W3 | W4 |
|---|---|---|
| 도메인 패키지 | `controller/service/repository/entity/dto/kafka/` | `api/application/domain/infrastructure/` |
| 영속성 | `repository/` (JpaRepository 직접 사용) | `infrastructure/persistence/{Repo, Adapter}` + `application/port/` |
| 메시징 | `kafka/{producer,consumer}/` | `infrastructure/messaging/` + `application/port/` |
| 룰 격리 | service에 비즈니스 룰 혼재 | `domain/policy/`에 분리 |
| 강제 메커니즘 | 컨벤션 (문서) | ArchUnit 테스트로 자동 강제 |

## 실행 + 검증

```bash
./gradlew bootRun                # 앱 실행
./gradlew test                   # 전체 테스트 + ArchUnit 검증
./gradlew test --tests "*ArchitectureTest"   # 아키텍처 룰만 검증
```

## 다음 단계 (이 템플릿 외부)

1. **synapse-shared 멀티모듈 publish** — `global/kafka/event/`의 stub을 `com.synapse.shared.event.*`로 교체
2. **다른 *-svc에 W4 적용** — knowledge/engagement/learning이 동일 구조로 마이그레이션
3. **CI 통합** — `./gradlew test`를 PR gate로 (ArchUnit fail = merge block)
4. **syn 레포 BACKEND_STRUCTURE.md** — 이 W4 트리를 공식 컨벤션 문서로
