# synapse-knowledge-svc — W3 skeleton

> **추가**: 도메인별 `kafka/{producer,consumer}/`. **chunking이 비로소 동작 가능 상태**가 됩니다.

---

## 🎯 chunking의 진짜 모습 — Kafka가 입구

W1/W2에서 chunking은 외부 invokable이 아니었습니다. W3에서 비로소:

```
[전체 파이프라인]

Client ──POST /api/v1/notes──→ NoteController
                                    ↓
                              NoteService.create()
                                    ↓
                              NoteRepository.save()
                                    ↓
                  NoteEventPublisher.publishNoteCreated()
                                    ↓
                  ┌─────────────────────────────────┐
                  │ Kafka                            │
                  │ topic: synapse.knowledge.note.   │
                  │        created.v1                │
                  └────────────┬────────────────────┘
                               ↓
        ┌──────────────────────┴──────────────────────┐
        ↓                                             ↓
  chunking/NoteCreatedConsumer              graph/NoteCreatedConsumer
        ↓ (groupId: chunking)                          ↓ (groupId: graph)
  ChunkingService.process()                  Node 자동 생성
        ↓
  ChunkEventPublisher.publishChunkReady()
        ↓
  Kafka: synapse.knowledge.chunking.chunk-ready.v1
        ↓
  graph/ChunkReadyConsumer
        ↓ (groupId: graph)
  유사 노드 찾기 → edge 자동 생성
```

**핵심 관찰**:
- `note.NoteService`는 chunking이나 graph를 **모른다**. 그저 NoteCreated만 발행.
- chunking은 controller가 아니라 **Kafka가 입구**. controller-less 도메인의 본질.
- graph는 **두 종류 이벤트 구독**: NoteCreated(직접) + ChunkReady(chunking 통과 후).

---

## 📂 W3에서 추가된 구조

```
src/main/java/com/synapse/knowledge/
├── KnowledgeApplication.java
│
├── note/
│   ├── controller/ service/ repository/ entity/ dto/
│   └── kafka/                                       ← NEW
│       └── producer/NoteEventPublisher                NoteCreated 발행
│
├── graph/
│   ├── controller/ service/ repository/ entity/ dto/
│   └── kafka/                                       ← NEW
│       └── consumer/
│           ├── NoteCreatedConsumer                  note → graph 노드 자동 생성
│           └── ChunkReadyConsumer                   chunking → graph 링크 갱신
│
├── chunking/                                        ← controller 여전히 없음
│   ├── service/ repository/ entity/
│   └── kafka/                                       ← NEW (드디어 입구 생김)
│       ├── consumer/NoteCreatedConsumer              ChunkingService.process() 트리거
│       └── producer/ChunkEventPublisher              ChunkReady 발행
│
└── global/
    ├── config/{KafkaConfig (NEW), SecurityConfig, RedisConfig}
    └── kafka/event/                                 ← NEW (임시)
        ├── NoteCreated.java
        └── ChunkReady.java
```

---

## 📛 토픽 일람

| 토픽 | Publisher | Consumer | 의미 |
|---|---|---|---|
| `synapse.knowledge.note.created.v1` | note | chunking, graph | 노트가 생성됨 |
| `synapse.knowledge.chunking.chunk-ready.v1` | chunking | graph | 청크 처리 완료 |

향후 추가 예정:
- `synapse.knowledge.note.updated.v1` (제목/본문 변경 시 chunking 재실행)
- `synapse.knowledge.note.deleted.v1` (graph 노드 삭제 트리거)

---

## 🔁 W2 → W3 변화 요약

| 항목 | W2 | W3 |
|---|---|---|
| chunking 동작 | 외부에서 invokable 아님 | Kafka 컨슈머가 트리거 |
| 도메인 간 통신 | 컨벤션 (직접 호출 가능했음) | Kafka 이벤트 only |
| 의존성 | + security/jjwt/redis | + spring-kafka, spring-kafka-test |
| 설정 | 4 프로파일 | + `spring.kafka.*` |

자세한 통증 → 해법 분석은 [platform/w3 README](https://github.com/team-project-final/synapse-svc-template/blob/skeleton/platform/w3/README.md) 참고.

---

## 🚀 W4에서 추가되는 것들 — knowledge 특수

W3까지의 service가 너무 많은 일을 한다는 통증은 다른 서비스와 동일. 추가로 knowledge는:

### controller-less 도메인의 ArchUnit 룰 처리

플랫폼 ArchUnit 룰 중 일부는 chunking에서 **자동 통과**되거나 **수정 필요**:

- "controller는 RestController 어노테이션 필수" → chunking은 controller 자체가 없어 룰 무효 (자동 통과)
- "각 도메인 슬라이스에 api 패키지 존재" → chunking에는 api 패키지가 비어있음 → **예외 룰 추가 필요**

W4 ArchUnit에 다음과 같은 예외 처리 추가:

```java
@Test
void controller_less_domains_may_skip_api_package() {
    classes()
        .that().resideInAPackage("..chunking..")    // chunking은 api 없어도 OK
        .should()...;
}
```

### W4에 추가되는 항목들

| 항목 | knowledge 특수 |
|---|---|
| `note/api/, graph/api/` 재구성 | platform과 동일 패턴 |
| `chunking/api/` 비어있음 | controller-less 도메인의 예외 |
| `application/port/` | NotePort, NodePort, EdgePort, ChunkJobPort + 도메인별 EventPort |
| `domain/policy/` | NoteValidationPolicy, ChunkingPolicy (청크 크기·중첩 룰) |
| `infrastructure/persistence/`, `messaging/` | 4 도메인 (chunking 포함) 모두 |
| ArchUnit | 7룰 + chunking controller-less 예외 |

자세한 통증 → 해법 분석은 [platform/w3 README의 W4 항목 섹션](https://github.com/team-project-final/synapse-svc-template/blob/skeleton/platform/w3/README.md) 참고.

---

## 🔭 다음 주차

- `skeleton/knowledge/w4` — 라이트 헥사고날 + ArchUnit (controller-less 예외 룰 포함)
