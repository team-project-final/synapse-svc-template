# synapse-learning-svc — W2 skeleton

> **추가**: Java 쪽은 `global/`, Python 쪽은 `core/`. **두 언어에 동일 컨셉을 다른 도구로** 구현.

---

## 📂 W2 구조

```
synapse-learning-svc/
├── learning-java/
│   └── src/main/java/com/synapse/learning/
│       ├── card/ srs/                          ← W1 그대로
│       └── global/                              ← NEW
│           ├── config/{SecurityConfig, RedisConfig}
│           ├── exception/{ErrorCode, BusinessException, GlobalExceptionHandler}
│           ├── response/ApiResponse<T>
│           ├── security/{JwtTokenProvider, JwtAuthFilter}
│           └── util/
│
└── learning-ai/
    └── app/
        ├── api/ services/ models/ repositories/  ← W1 그대로
        └── core/                                  ← NEW (Java의 global과 동일 역할)
            ├── config.py        pydantic-settings로 .env 자동 로드
            ├── response.py      ApiResponse[T] (Java와 같은 봉투)
            ├── exceptions.py    BusinessException + ErrorCode + FastAPI 핸들러
            └── dependencies.py  Depends() 의존성 주입
```

---

## 🪞 Java ↔ Python 동일 컨셉 매핑

이게 learning 서비스의 진짜 가치 — **신입이 한쪽 언어 작업하면서 다른 쪽을 자연스럽게 이해**.

| 컨셉 | Java (`global/`) | Python (`app/core/`) |
|---|---|---|
| 통일 응답 봉투 | `ApiResponse<T>` (record) | `ApiResponse[T]` (pydantic Generic) |
| 예외 정의 | `BusinessException + ErrorCode enum` | `BusinessException + ErrorCode Enum` |
| 예외 → 응답 변환 | `@RestControllerAdvice` (`GlobalExceptionHandler`) | `app.exception_handler` (`register_handlers`) |
| 인증 | `JwtAuthFilter (OncePerRequestFilter)` | `app/core/security.py` (W3에서 추가) |
| 설정 관리 | `application-{profile}.yml` | `pydantic-settings BaseSettings` |
| 의존성 주입 | `@Autowired` 생성자 | `Depends(get_settings)` |

응답 JSON은 양쪽이 동일:
```json
{"success": true, "data": {...}, "timestamp": "..."}
{"success": false, "error": {"code":"R001","message":"..."}, "timestamp":"..."}
```

→ **프론트엔드는 Java/Python API를 구별하지 않습니다.** 같은 봉투니까.

---

## ▶️ 실행

### Java (포트 8083)

```bash
cd learning-java
./gradlew bootRun
```

```bash
# 보호 엔드포인트 — JWT 없으면 401
curl http://localhost:8083/api/v1/cards
# → 401 (W3에서 토큰 발급 시나리오 추가 예정)

# 헬스
curl http://localhost:8083/actuator/health
```

### Python (포트 8084)

```bash
cd learning-ai
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8084
```

```bash
# 추천 — 통일 봉투로 응답
curl -X POST http://localhost:8084/api/v1/recommendations \
  -H "Content-Type: application/json" \
  -d '{"user_id":1,"top_k":3}'
# → {"success":true,"data":{"items":[...]},"timestamp":"..."}

# 헬스
curl http://localhost:8084/actuator/health
```

---

## 🔁 W1 → W2 변화 요약

| 항목 | W1 | W2 (Java) | W2 (Python) |
|---|---|---|---|
| 응답 포맷 | 도메인 DTO 직접 반환 | `ApiResponse<T>` | `ApiResponse[T]` |
| 예외 처리 | 없음 | `GlobalExceptionHandler` | `register_handlers(app)` |
| 인증 | 없음 | JWT + Filter Chain | (W3) JWT Dependency |
| 설정 | 단일 yml | 4 프로파일 yml | pydantic-settings + `.env` |

---

## 🚀 W3에서 추가되는 것들 — learning 특수

W3에서 **Java ↔ Python 직접 HTTP 호출 금지**. 통신은 모두 Kafka.

### 통증: HTTP 직접 호출의 문제 (양쪽 다 있음)

```
[W2 상태에서 만약 Java가 Python을 직접 호출하면]
SrsService → HTTP POST /api/v1/recommendations → Python
```

- **장애 전파**: Python down → Java down
- **응답 시간 합산**: 학습 화면 로딩이 양쪽 합
- **인증 두 번**: 양쪽이 따로 JWT 검증

### W3 해법: 양쪽 모두 Kafka

```
Java SrsService → CardReviewed 이벤트 발행 (Kafka)
                    ↓
              Python NoteCreatedConsumer (또는 별도 토픽)
                    ↓
              Python이 추천 결과를 ReviewRecommendation 이벤트로 응답
                    ↓
              Java가 별도 컨슈머로 수신, 캐시에 저장
```

양쪽 모두 **non-blocking**. 한쪽 죽어도 다른 쪽 정상.

### W3에 추가되는 항목들

| Java 쪽 | Python 쪽 |
|---|---|
| `card/kafka/producer/CardEventPublisher` | `app/kafka/producer.py` |
| `srs/kafka/producer/SrsEventPublisher` | `app/kafka/consumer.py` |
| `srs/kafka/consumer/RecommendationConsumer` | (Python이 SrsRecommendationRequest 토픽 구독) |
| `global/config/KafkaConfig` | (kafka-python 또는 aiokafka 의존성) |
| `global/kafka/event/*` stub | `app/kafka/events.py` (Pydantic 이벤트) |

자세한 통증→해법 분석은 [platform/w2 README의 W3 항목](https://github.com/team-project-final/synapse-svc-template/blob/skeleton/platform/w2/README.md) 참고.

---

## 🔭 다음

- `skeleton/learning/w3` — Java↔Python = Kafka. HTTP 직접 호출 금지.
- `skeleton/learning/w4` — Java=ArchUnit / Python=import-linter (양쪽 모두 의존 방향 강제)
