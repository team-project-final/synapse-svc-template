# synapse-learning-svc — W4 skeleton (최종)

> **추가**: 라이트 헥사고날 양쪽 모두. **Java=ArchUnit / Python=import-linter**로 의존 방향 자동 강제.

---

## 📂 W4 구조 — 양쪽 동일 형태, 다른 도구

```
synapse-learning-svc/
├── learning-java/
│   └── src/main/java/com/synapse/learning/
│       ├── card/        api/{Controller, dto/}, application/{Service, port/}, domain/{Card, policy/}, infrastructure/{persistence/, messaging/}
│       ├── srs/         api/, application/{Service, port/{ReviewRecordPort, EventPort}}, domain/{ReviewRecord, policy/SrsSchedulingPolicy}, infrastructure/{persistence/, messaging/}
│       └── global/      config/, exception/, response/, security/, util/, kafka/event/
│
└── learning-ai/
    ├── .importlinter                            ← Python의 ArchUnit 대응
    └── app/
        ├── api/v1/                              ← FastAPI 라우터 (Controller 역할)
        │   └── recommendation.py
        ├── application/                          ← Use case 함수 + port
        │   ├── recommendation_usecase.py         (Java의 @Service)
        │   ├── port.py                            (Protocol = 인터페이스)
        │   └── port_registry.py                   (Port → Adapter 바인딩, infra 의존 예외)
        ├── domain/                               ← 비즈니스 모델 + 룰
        │   ├── models.py                          Pydantic 모델 (도메인)
        │   └── policies.py                        RecommendationPolicy (외부 의존 0)
        ├── infrastructure/                       ← 외부 시스템 연결
        │   ├── ml/recommendation_adapter.py       RecommendationPort 구현
        │   └── messaging/                         Kafka consumer/producer (W3에서 이동)
        │       ├── events.py
        │       ├── consumer.py
        │       └── producer.py
        ├── core/                                 ← 횡단 (W2 그대로)
        │   ├── config.py response.py exceptions.py dependencies.py
        └── main.py
```

---

## 🛡 Java=ArchUnit / Python=import-linter

### Java (`LearningArchitectureTest.java`) — 7개 ArchUnit 룰

platform/W4와 동일한 7룰:
1. 도메인 슬라이스 격리 (card ↛ srs)
2. domain → 다른 계층 의존 금지
3. application → api/infrastructure 의존 금지 (port 예외)
4. api → infrastructure 의존 금지
5. domain.policy → 외부 의존성 0
6. JpaRepository = infrastructure.persistence만
7. @KafkaListener = infrastructure.messaging만

### Python (`.importlinter`) — 4개 contract

```ini
[importlinter:contract:layers]
name = Layered architecture
type = layers
layers =
    app.api
    app.application
    app.domain
containers = app

[importlinter:contract:domain-pure]
name = Domain has no outside dependencies
type = forbidden
source_modules = app.domain
forbidden_modules = app.api, app.application, app.infrastructure, app.core

[importlinter:contract:api-no-infra]
name = API must not depend on infrastructure
type = forbidden
source_modules = app.api
forbidden_modules = app.infrastructure

[importlinter:contract:app-no-infra]
name = Application must depend only on port interfaces
type = forbidden
source_modules = app.application.recommendation_usecase
forbidden_modules = app.infrastructure
```

실행:
```bash
cd learning-ai
pip install import-linter
lint-imports
```

### 왜 Python에 더 절실한가?

Java는 컴파일러가 강해서 "잘못된 import"가 어색하지만 실수로 들어가도 컴파일은 됨.
**Python은 컴파일러가 약해서** `from app.infrastructure import ...`를 application에 적어도 런타임까지 못 잡음. → 컨벤션이 가장 쉽게 사문화되는 언어 = import-linter가 더 필요.

---

## 🪞 Java ↔ Python 컨셉 매핑 (W4 최종)

| 컨셉 | Java | Python |
|---|---|---|
| 인터페이스(Port) | `interface UserPort {...}` | `class UserPort(Protocol)` (PEP 544) |
| 어댑터 | `@Component class UserPersistenceAdapter implements UserPort` | `class UserPersistenceAdapter(UserPort)` |
| Port → Adapter 바인딩 | Spring DI 자동 (`@Component`) | `port_registry.py`에 명시 |
| 도메인 정책 | `final class PasswordPolicy` (static methods) | `class RecommendationPolicy` (classmethods) |
| 의존 방향 검증 | ArchUnit | import-linter |
| 검증 시점 | `./gradlew test` | `lint-imports` (CI에서) |
| package-private 가시성 | `interface ...` (no public) | `__all__` + 컨벤션 (실제 강제는 불가) |

> 💡 **Python의 한계**: 진짜 package-private이 없음. `_` prefix 또는 `__all__`로 컨벤션. import-linter가 *대신* 호출 방향을 강제.

---

## 🔁 W3 → W4 변화 요약

| 항목 | W3 | W4 |
|---|---|---|
| Java 도메인 패키지 | controller/service/repository/entity/dto/kafka | api/application/{port}/domain/{policy}/infrastructure/{persistence,messaging} |
| Python 폴더 | api / services / models / kafka / ai / core | api / application / domain / infrastructure / core |
| 의존 강제 (Java) | 컨벤션 | ArchUnit 7룰 |
| 의존 강제 (Python) | 컨벤션 | import-linter 4 contract |
| Port/Adapter (양쪽) | 미정의 | 명시적 인터페이스 (Java interface / Python Protocol) |

자세한 통증→해법: [platform/w3의 W4 항목 섹션](https://github.com/team-project-final/synapse-svc-template/blob/skeleton/platform/w3/README.md).

---

## ▶️ 실행 + 검증

```bash
# 1. 양쪽 빌드
cd learning-java && ./gradlew build
cd ../learning-ai && pip install -e ".[dev]"

# 2. 의존 방향 검증
cd learning-java && ./gradlew test --tests "*ArchitectureTest"
cd ../learning-ai && lint-imports

# 3. 위반 시뮬레이션
# Python: app/application/recommendation_usecase.py 에 다음 줄 추가
#   from app.infrastructure.ml.recommendation_adapter import RecommendationMlAdapter
# → lint-imports 실행 시 fail:
#   "BROKEN" Application must depend only on port interfaces

# 4. 양쪽 동시 실행
cd .. && docker compose up --build
```

---

## 🎓 learning-svc가 W4에서 가장 잘 보여주는 것

**같은 아키텍처를 다른 언어/도구로 구현하는 모범 사례.**

- 인터페이스: Java `interface` ↔ Python `Protocol`
- DI: Spring `@Component` ↔ Python 명시적 binding
- 의존 강제: ArchUnit (테스트) ↔ import-linter (lint)
- 도메인 룰: Java static method ↔ Python classmethod

**팀이 두 언어를 동시에 다루는 도메인에서 가장 가치 있는 패턴**. Java만 알아도 Python 구조가 읽힘. 반대도 마찬가지.

---

## 다음 단계 (template 외부)

W4가 마지막. 실제 synapse-learning-svc 레포로 이동 후:

1. **synapse-shared 의존성 활성화** — Java/Python 양쪽이 같은 Avro 스키마에서 클래스 생성
2. **임베딩 모델 통합** — `infrastructure/ml/`에 실제 모델 (PyTorch/sentence-transformers)
3. **Vector DB 연동** — `infrastructure/persistence/`에 pgvector·Pinecone 등
4. **양쪽 CI gate** — ArchUnit + import-linter가 PR 머지 차단
