# synapse-knowledge-svc — W2 skeleton

> **추가**: `global/` 횡단 관심사 + JWT + 환경별 프로파일. note/graph 컨트롤러는 `ApiResponse<T>` 적용.

---

## 📂 W2에서 추가된 구조

```
src/main/java/com/synapse/knowledge/
├── KnowledgeApplication.java
├── note/ graph/ chunking/   ← W1 그대로 (note는 ApiResponse 적용 데모)
└── global/                  ← NEW
    ├── config/{SecurityConfig, RedisConfig}
    ├── exception/{ErrorCode, BusinessException, GlobalExceptionHandler}
    ├── response/ApiResponse<T>
    ├── security/{JwtTokenProvider, JwtAuthFilter}
    └── util/
```

`resources/`:
```
application.yml + application-{local,dev,prod}.yml
```

`ErrorCode` 코드 prefix:
- `C___` 공통, `N___` note, `G___` graph, `CH___` chunking

---

## ℹ️ knowledge-svc 특이점

### chunking과 SecurityConfig

chunking은 HTTP 입구가 없으므로 `SecurityConfig.requestMatchers`에 등장하지 않습니다. W3에서 Kafka 컨슈머가 추가되면, **메시지 수준의 신뢰**(토픽 권한, producer 신원)로 보호합니다 — JWT와 별개의 메커니즘.

### `permitAll` 차이

platform-svc는 `/api/v1/auth/**`를 permitAll로 노출 (로그인 자체는 인증 없이 가능해야 하므로). knowledge-svc는 로그인이 없어 그런 예외가 없고, `/actuator/health`만 permitAll.

---

## 🔄 W1 → W2 변화 요약

platform-svc/W2와 동일 패턴 — 자세한 내용은 [platform/w2 README](https://github.com/team-project-final/synapse-svc-template/blob/skeleton/platform/w2/README.md) 참고.

---

## 🚀 W3에서 추가되는 것들 — knowledge 특수

### chunking이 진짜 모습을 드러냅니다

W1/W2의 chunking은 외부 호출 불가능 — 그저 service+repository+entity가 있을 뿐. **W3에서 비로소 동작 가능 상태**가 됩니다:

```
[W3 흐름]
note POST /api/v1/notes → NoteCreated 이벤트 발행
                              ↓
                  chunking 컨슈머가 받음
                  → ChunkingService.process(noteId, body)
                  → ChunkReady 이벤트 발행
                              ↓
                  graph 컨슈머가 ChunkReady를 받음
                  → 그래프 노드 자동 생성/링크
```

**왜 W1/W2에서 안 되나?** 도메인 간 직접 호출 금지 컨벤션 때문. note가 chunking을 직접 호출하면 강결합. Kafka 이벤트 인프라가 갖춰진 W3 이후라야 안전.

### W3에 추가되는 항목들 (knowledge 관점)

| 항목 | 왜 필요? |
|---|---|
| `note/kafka/producer/NoteEventPublisher` | note 변경을 외부에 알림. graph·chunking이 자동 반응 |
| `graph/kafka/consumer/{NoteCreatedConsumer, ChunkReadyConsumer}` | note·chunking 이벤트를 받아 그래프 갱신 |
| `chunking/kafka/consumer/NoteCreatedConsumer` | controller 부재의 진짜 의미 — Kafka가 트리거 |
| `chunking/kafka/producer/ChunkEventPublisher` | 청크 완료를 graph에게 알림 |
| `global/config/KafkaConfig` | producer/consumer factory + @EnableKafka |
| `global/kafka/event/{NoteCreated, ChunkReady}` | 이벤트 클래스 임시 보관 (shared-events 이전 예정) |

> 자세한 통증 → 해법 패턴은 platform/w2 README의 W3 추가 항목 섹션 참고.

---

## 🔭 다음 주차 미리보기

- `skeleton/knowledge/w3` — Kafka 통신, chunking이 pipeline으로 진짜 동작
- `skeleton/knowledge/w4` — `api/application/domain/infrastructure` + ArchUnit (controller-less 예외 룰)
