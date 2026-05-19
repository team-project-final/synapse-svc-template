# synapse-learning-svc — W1 skeleton

> **한 줄 정의**: "Java(Spring Boot) + Python(FastAPI) **이중 언어 모노레포**. 한 레포에 두 개의 독립 서비스가 폴더로 분리."

이 서비스가 특별한 이유: **Java만으로 부족한 도메인이 있기 때문**. Card/SRS는 Spring Boot로 충분하지만, AI 추천은 PyTorch/scikit-learn 생태계가 필요. 한 도메인 묶음 안에서 언어를 섞습니다.

---

## 🎯 이 단계의 목표

- [x] **모노레포 안에 언어 분리** 구조 이해
- [x] learning-java/와 learning-ai/가 어떻게 독립적으로 동작하는지
- [x] W3에서 두 언어 사이의 통신이 왜 Kafka여야 하는지 미리 감 잡기

---

## 📂 패키지 구조 (W1)

```
synapse-learning-svc/
├── docker-compose.yml                   ← 로컬에서 양쪽 동시 실행
├── README.md
│
├── learning-java/                        ← Spring Boot (card · srs)
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── Dockerfile (port 8083)
│   └── src/main/java/com/synapse/learning/
│       ├── LearningApplication.java
│       ├── card/                          카드 CRUD
│       │   ├── controller/CardController
│       │   ├── service/CardService
│       │   ├── repository/CardRepository
│       │   ├── entity/Card                { ownerId, frontText, backText }
│       │   └── dto/
│       └── srs/                           Spaced Repetition System
│           ├── controller/SrsController     POST /api/v1/srs/review
│           ├── service/SrsService           SM-2 알고리즘 단순화
│           ├── repository/ReviewRecordRepository
│           ├── entity/ReviewRecord          { cardId, userId, quality, nextReviewAt }
│           └── dto/
│
└── learning-ai/                          ← FastAPI (ai)
    ├── pyproject.toml
    ├── requirements.txt
    ├── Dockerfile (port 8084)
    └── app/
        ├── main.py                         FastAPI 인스턴스 + health endpoint
        ├── api/v1/recommendation.py        POST /api/v1/recommendations
        ├── services/recommendation_service.py
        ├── models/recommendation.py         Pydantic 스키마
        └── repositories/                    DB/벡터DB 접근 (W3 이후)
```

---

## 🧭 왜 한 레포에 두 언어?

### 대안과 비교

| 옵션 | 장점 | 단점 |
|---|---|---|
| **별도 레포 (synapse-learning-svc-java + synapse-learning-svc-ai)** | 언어별 독립 CI/CD | "어떤 레포가 학습 도메인이지?" 혼란, 동시 변경 어려움 |
| **한 언어로 통합 (Python만 or Java만)** | 단일 스택 | Java의 SRS 도메인 vs Python의 추론 모델 — 한쪽이 비효율 |
| **한 레포 + 폴더 분리 (W1 선택)** | 도메인 응집 + 언어별 최적 | 빌드 환경 두 개 관리 |

**도메인이 같으면 같이 산다.** 카드·복습·추천은 모두 "학습"이라는 한 비즈니스 컨텍스트. 한 팀이 책임지는 게 자연스러움.

### Java ↔ Python 통신은?

**W1에서는 미정의**. Java에서 Python을 호출하고 싶으면 그냥 HTTP REST를 직접 부를 수도 있지만:
- 동기 호출 → 양쪽이 강결합
- Python이 죽으면 Java도 응답 못 함
- 양방향 인증 처리 골치

→ **W3에서 Kafka 이벤트로 통일 예정.** 자세한 건 W3 README.

---

## ▶️ 실행하기

### 양쪽 동시 (docker-compose)

```bash
docker compose up --build
```

- Java: http://localhost:8083
- Python: http://localhost:8084

### 각각 따로

**Java**:
```bash
cd learning-java
./gradlew bootRun
```

**Python**:
```bash
cd learning-ai
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8084
```

### 엔드포인트 호출

```bash
# Java — 카드 생성
curl -X POST http://localhost:8083/api/v1/cards \
  -H "Content-Type: application/json" \
  -d '{"ownerId":1,"frontText":"Hello","backText":"안녕"}'

# Java — 복습
curl -X POST http://localhost:8083/api/v1/srs/review \
  -H "Content-Type: application/json" \
  -d '{"cardId":1,"userId":1,"quality":4}'

# Python — 추천
curl -X POST http://localhost:8084/api/v1/recommendations \
  -H "Content-Type: application/json" \
  -d '{"user_id":1,"context":"vocabulary","top_k":3}'

# Python — 헬스
curl http://localhost:8084/actuator/health
```

---

## 🚫 W1에서 의도적으로 안 하는 것들

| 안 한 것 | 어디서? |
|---|---|
| Java/Python 공통 응답 포맷 | **W2** — Java=ApiResponse, Python=core.response (같은 모양) |
| 통일 예외 처리 | **W2** — Java=GlobalExceptionHandler, Python=core.exceptions |
| Java↔Python 이벤트 통신 | **W3** — 양쪽 Kafka 컨슈머/프로듀서 |
| 도메인 격리 자동 강제 | **W4** — Java=ArchUnit, Python=import-linter |

---

## 🔭 다음 주차

| 브랜치 | 추가되는 것 |
|---|---|
| `skeleton/learning/w2` | 양쪽에 `global/`(Java) / `core/`(Python) — 응답·예외·설정 표준화 |
| `skeleton/learning/w3` | Java↔Python = **Kafka 이벤트 only**. HTTP 직접 호출 금지. |
| `skeleton/learning/w4` | Java=ArchUnit + Python=import-linter (양쪽 모두 의존 방향 강제) |

---

## 🚀 W2에서 추가되는 것들 — learning 특수

W2의 통증 → 해법 패턴은 platform과 동일. 단, learning은 **양쪽 언어 모두**에 동일 컨셉을 적용:

| 컨셉 | Java 위치 | Python 위치 |
|---|---|---|
| 통일 응답 봉투 | `global/response/ApiResponse<T>` | `app/core/response.py` (`ApiResponse[T]`) |
| 예외 처리 일원화 | `global/exception/GlobalExceptionHandler` (`@RestControllerAdvice`) | `app/core/exceptions.py` (FastAPI `ExceptionHandler`) |
| 설정 관리 | `application-{local,dev,prod}.yml` | `app/core/config.py` (pydantic-settings) |
| 의존성 주입 | Spring `@Autowired` | FastAPI `Depends()` |
| 인증 | `global/security/JwtAuthFilter` | `app/core/security.py` (FastAPI dependency) |

**같은 통증을 양쪽 언어에 각각 해결**. 신입은 한쪽 언어 작업하면서 다른 쪽을 자연스럽게 이해할 수 있는 구조.

자세한 통증 → 해법은 [platform/w1 README의 W2 추가 항목](https://github.com/team-project-final/synapse-svc-template/blob/skeleton/platform/w1/README.md) 참고.
