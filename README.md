# synapse-learning-svc — W3 skeleton

> **추가**: **Java ↔ Python = Kafka only**. HTTP 직접 호출 폐기. 양쪽 모두 producer + consumer를 가짐.

---

## 🎯 W3의 핵심: 비동기 양방향 통신

```
[Java: 복습 기록]
SrsService.review(req)
  ├──→ ReviewRecord 저장
  ├──→ Kafka publish: synapse.learning.card.card-reviewed.v1
  │        (다른 서비스 — engagement, knowledge 등이 구독)
  └──→ Kafka publish: synapse.learning.srs.recommendation-request.v1
                                ↓
                  [Python: AI 추천]
                  consumer.py listens
                    → recommend_cards(...)
                    → Kafka publish: synapse.learning.ai.recommendation-ready.v1
                                ↓
                  [Java: 결과 수신]
                  RecommendationReadyConsumer.onRecommendationReady()
                    → 캐시 저장 → 다음 카드 조회 시 활용
```

**핵심**: Java는 Python을 모름. Python은 Java를 모름. 둘 다 Kafka만 안다.

---

## 📂 W3 추가 구조

```
synapse-learning-svc/
├── learning-java/
│   └── src/main/java/com/synapse/learning/
│       ├── card/ srs/                           ← W1~W2 그대로
│       ├── srs/
│       │   ├── kafka/                            ← NEW
│       │   │   ├── producer/SrsEventPublisher    CardReviewed + RecommendationRequest 발행
│       │   │   └── consumer/RecommendationReadyConsumer   Python 응답 수신
│       │   └── service/SrsService.java           Kafka publish 흐름 추가
│       └── global/
│           ├── config/KafkaConfig.java            NEW
│           └── kafka/event/                       NEW (스텁)
│               ├── CardReviewed.java
│               ├── SrsRecommendationRequest.java
│               └── SrsRecommendationReady.java
│
└── learning-ai/
    └── app/
        ├── api/ services/ models/ core/          ← W2 그대로
        ├── kafka/                                 ← NEW
        │   ├── events.py                          Pydantic 모델 (Java record와 1:1)
        │   ├── consumer.py                        aiokafka consumer (background task)
        │   └── producer.py                        aiokafka producer
        ├── ai/                                    ← NEW (W4에서 모듈 분리 시작)
        │   └── recommendation_model.py
        └── main.py                                lifespan으로 Kafka 컨슈머 부팅
```

---

## 📛 토픽 일람 (learning 관점)

| 토픽 | 방향 | 발행자 → 수신자 |
|---|---|---|
| `synapse.learning.card.card-reviewed.v1` | Java OUT | learning-java → engagement, knowledge 등 |
| `synapse.learning.srs.recommendation-request.v1` | Java OUT | learning-java → learning-ai |
| `synapse.learning.ai.recommendation-ready.v1` | Java IN | learning-ai → learning-java |

> 💡 **언어가 다르지만 토픽 컨벤션은 동일** (`synapse.{service}.{domain}.{event}.v{N}`). Avro 스키마로 통일하면 Java/Python 양쪽이 같은 .avsc에서 클래스 생성.

---

## 🪞 Java ↔ Python 동일 컨셉 매핑 (W3 갱신)

| 컨셉 | Java (W3) | Python (W3) |
|---|---|---|
| 이벤트 클래스 | `record CardReviewed(...)` | `class CardReviewed(BaseModel)` |
| Producer | `KafkaTemplate.send()` | `AIOKafkaProducer.send_and_wait()` |
| Consumer | `@KafkaListener(topics=...)` | `AIOKafkaConsumer + async for` |
| 직렬화 | `JsonSerializer` | `json.dumps` |
| acks/retries | `application.yml` `spring.kafka.producer.*` | producer `acks="all"` |
| Trust packages | `JsonDeserializer.TRUSTED_PACKAGES` | 모델 검증으로 자동 |

→ **응답 JSON이 동일하게 직렬화돼야** 양쪽이 서로의 이벤트를 읽을 수 있음. Java의 `record` field name (camelCase)을 Python의 Pydantic field name과 정확히 맞춰야 함 (`requestId`, `userId` 등).

---

## ▶️ 실행 (양쪽 + Kafka)

```bash
# 1. Kafka 띄우기 (별도 docker-compose 필요)
docker compose up -d kafka

# 2. Java
cd learning-java && ./gradlew bootRun &

# 3. Python
cd learning-ai && uvicorn app.main:app --port 8084 &

# 4. 복습 요청 → Java가 두 토픽 발행 → Python이 추천 → Java가 결과 수신
curl -X POST http://localhost:8083/api/v1/srs/review \
  -H "Content-Type: application/json" \
  -d '{"cardId":1,"userId":42,"quality":4}'

# 로그 확인:
# Java: "Publishing CardReviewed: ..."
# Java: "Publishing SrsRecommendationRequest: ..."
# Python: "SrsRecommendationRequest: requestId=..."
# Python: "Published SrsRecommendationReady: ..."
# Java: "RecommendationReady from Python: requestId=..."
```

---

## 🔁 W2 → W3 변화 요약

| 항목 | W2 | W3 |
|---|---|---|
| Java ↔ Python 통신 | 미정의 (HTTP 직접 호출 가능) | **Kafka 이벤트 only** |
| Python 백그라운드 작업 | 없음 (HTTP 요청-응답만) | `lifespan`에서 컨슈머 시작 |
| 의존성 | (양쪽 W2 그대로) | Java: + spring-kafka / Python: + aiokafka |
| Java service | DB 호출 후 응답 | Event 발행 추가 |

---

## 🚀 W4에서 추가되는 것들 — learning 특수

W4의 평소 항목(`api/application/{port}/domain/{policy}/infrastructure/{persistence,messaging}` + ArchUnit)은 **Java 쪽**에 적용. **Python 쪽은 같은 패턴을 다른 도구로**:

| Java | Python | 역할 |
|---|---|---|
| ArchUnit (compile-time → test) | **import-linter** (CI 시점 lint) | 의존 방향 강제 |
| `api/application/domain/infrastructure/` | `api/ application/ domain/ infrastructure/` 같은 폴더명 | 동일 의도 |
| Spring Data JPA package-private | repository 모듈 + `__all__` 명시 | 의도된 export 강제 |

W4 README에서 자세히. 특히 **Python에서 import-linter를 쓰는 이유** — Python은 컴파일러가 약해서 "도메인 격리"가 컨벤션만으로 사문화되기 쉬움. 자동 검증이 더 절실.

자세한 통증→해법은 [platform/w3 README의 W4 항목](https://github.com/team-project-final/synapse-svc-template/blob/skeleton/platform/w3/README.md).

---

## 🔭 다음 (마지막)

- `skeleton/learning/w4` — Java=ArchUnit + Python=import-linter, 양쪽 라이트 헥사고날
