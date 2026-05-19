# synapse-knowledge-svc — W1 skeleton

> **한 줄 정의**: "3개 도메인(note·graph·chunking)을 분리한 가장 단순한 Spring Boot 서비스. chunking은 controller 없는 **pipeline 도메인**."

---

## 🎯 이 단계의 목표

- [x] 3 도메인 split (note, graph, chunking) 이해
- [x] **controller-less 도메인** 패턴 이해 — 모든 도메인이 HTTP 입구를 가지는 건 아니다
- [x] 도메인별 책임 차이 (CRUD / 관계형 / 비동기 파이프라인)

---

## 🧭 큰 그림: 왜 도메인을 먼저 나누나요?

### ❌ 안티패턴 — 계층(layer)만으로 나누면?

```
src/main/java/com/synapse/knowledge/
├── controller/           NoteController, GraphController, ChunkingController (if existed)
├── service/              NoteService, GraphService, ChunkingService
├── repository/           NoteRepository, NodeRepository, EdgeRepository, ChunkJobRepository, ...
└── entity/               Note, Node, Edge, ChunkJob, Chunk
```

작은 프로젝트엔 괜찮지만, 도메인 3개에 entity 6개로 늘면:
1. **`repository/` 폴더 비대화** — 6개가 한 폴더에 섞임. note·graph·chunking 어느 도메인 것인지 한눈에 안 옴.
2. **도메인 간 우발적 의존 유혹** — `GraphService`가 `NoteRepository`를 그냥 가져다 쓰는 게 자연스러워 보임 (실은 도메인 격리 위반).
3. **chunking 분리 어려움** — 나중에 chunking을 별도 서비스로 추출하려면 폴더가 사방에 흩어져 있어 추적 비용 큼.

### ✅ 도메인 우선 분리 (이 프로젝트 채택)

```
src/main/java/com/synapse/knowledge/
├── note/        ← 도메인 (note 관련 모든 것이 여기)
│   ├── controller/ service/ repository/ entity/ dto/
├── graph/       ← 도메인
│   ├── controller/ service/ repository/ entity/ dto/
└── chunking/    ← 도메인 (controller 없는 pipeline)
    ├── service/ repository/ entity/
```

- **응집도(Cohesion) 높음** — note 작업 시 `note/` 폴더 하나만 보면 됨.
- **격리 시각화** — `GraphService`가 `import com.synapse.knowledge.note.service.NoteService;` 하면 누가 봐도 어색.
- **추출 용이** — `chunking/`을 통째로 별도 서비스로 옮기기 쉬움.

---

## 🧱 5계층의 정석 (note·graph 도메인)

| 계층 | 역할 | 예시 |
|---|---|---|
| **controller/** | HTTP 요청 받음 → service에 위임. 비즈니스 로직 금지. | `NoteController.create()` → `noteService.create(request)` |
| **service/** | "무엇을 할지" 조립. 트랜잭션 경계. | `NoteService.create()`가 entity 생성 → repository 저장 |
| **repository/** | DB 접근만. JpaRepository 상속하면 CRUD 자동. | `NoteRepository extends JpaRepository<Note, Long>` |
| **entity/** | DB 테이블 = Java 클래스. `@Entity` 어노테이션. | `Note { id, title, body, ownerId }` |
| **dto/** | API 입출력 봉투. **entity 직접 노출 금지** (DB 변경이 즉시 API 변경 되는 결합 방지). | `CreateNoteRequest`, `NoteResponse` |

각 계층은 **아래 계층만** 알면 되고 **위 계층은 모름** (단방향 의존). 신입이 가장 자주 어기는 룰: 컨트롤러에 비즈니스 로직 박기, 엔티티를 응답 그대로 내보내기.

> chunking은 controller가 없어 4계층 (service/repository/entity/dto)만 사용. W3에서 Kafka가 controller 역할을 대신합니다.

---

## 🧭 3개 도메인의 성격이 다릅니다

| 도메인 | 성격 | HTTP API | 비고 |
|---|---|---|---|
| **note** | 일반 CRUD | `POST/GET /api/v1/notes` | platform/auth와 유사한 평범한 도메인 |
| **graph** | 관계형 — Node/Edge | `GET /api/v1/graph/nodes`, `POST /edges` | 두 개의 entity가 관계로 연결됨 |
| **chunking** | **파이프라인** — 입력 받아 처리 후 결과 저장 | **없음** | W3에서 Kafka in/out으로 동작 |

### chunking — controller가 없어도 도메인입니다

지금까지 (platform-svc 학습 흐름에서) 봐온 도메인은 모두 HTTP 컨트롤러를 가졌습니다. 하지만 도메인이 꼭 외부에 HTTP를 노출할 필요는 없습니다:

- **batch 도메인**: 야간 집계 같은 스케줄러 트리거
- **pipeline 도메인**: 다른 도메인의 데이터를 받아 가공
- **event-driven 도메인**: Kafka 이벤트만 수신해서 동작

chunking이 바로 이 케이스. W3에서 진짜 모습을 드러냅니다 — note가 `NoteCreated` 이벤트를 발행하면 chunking이 받아서 텍스트를 청크로 나눕니다.

**W1에서는** chunking이 외부에서 호출 불가능 — `ChunkingService.process()`는 다른 도메인에서 직접 호출하면 컨벤션 위반. **자기 자신의 단위 테스트로만 검증** 가능. 이게 약점이 아니라 의도된 상태입니다.

---

## 📂 패키지 구조 (W1)

```
synapse-knowledge-svc/
├── build.gradle.kts
├── settings.gradle.kts
└── src/main/java/com/synapse/knowledge/
    ├── KnowledgeApplication.java
    │
    ├── note/                            ← 일반 CRUD 도메인
    │   ├── controller/NoteController     POST/GET /api/v1/notes
    │   ├── service/NoteService
    │   ├── repository/NoteRepository
    │   ├── entity/Note                   { id, title, body, ownerId }
    │   └── dto/{request,response}/
    │
    ├── graph/                           ← 관계형 도메인
    │   ├── controller/GraphController    /api/v1/graph/{nodes,edges}
    │   ├── service/GraphService
    │   ├── repository/                   NodeRepository + EdgeRepository
    │   ├── entity/                       Node, Edge
    │   └── dto/{request,response}/
    │
    └── chunking/                        ← 파이프라인 도메인 (controller 없음)
        ├── service/ChunkingService       process(noteId, text)
        ├── repository/                   ChunkJobRepository + ChunkRepository
        └── entity/                       ChunkJob, Chunk
```

---

## ▶️ 실행하기

```bash
./gradlew bootRun
```

기본 포트: `8081` (platform-svc=8080과 충돌 방지).

```bash
# note 생성
curl -X POST http://localhost:8081/api/v1/notes \
  -H "Content-Type: application/json" \
  -d '{"title":"Hello","body":"World","ownerId":1}'

# note 조회
curl http://localhost:8081/api/v1/notes

# graph 노드 목록
curl http://localhost:8081/api/v1/graph/nodes

# graph 엣지 생성
curl -X POST http://localhost:8081/api/v1/graph/edges \
  -H "Content-Type: application/json" \
  -d '{"fromNodeId":1,"toNodeId":2,"relation":"RELATED_TO"}'

# chunking — 직접 호출 불가
# (서비스 메서드만 존재, W3에서 Kafka로 트리거)
```

---

## 🚫 W1에서 의도적으로 안 하는 것들

| 안 한 것 | 어디서? |
|---|---|
| 통일 응답/예외 처리/JWT | **W2** `global/` |
| note → chunking 트리거 (이벤트로) | **W3** kafka |
| ArchUnit "controller-less 도메인" 예외 룰 | **W4** `arch/` |

---

## 🔭 다음 주차 미리보기

| 브랜치 | 추가되는 것 |
|---|---|
| `skeleton/knowledge/w2` | `global/` (config·exception·response·security·util) |
| `skeleton/knowledge/w3` | 도메인별 `kafka/`, **chunking이 진짜 pipeline으로** (note → chunking Kafka 흐름) |
| `skeleton/knowledge/w4` | `api/application/domain/infrastructure` + ArchUnit (controller-less 도메인 예외 룰 포함) |

---

## 🚀 W2에서 추가되는 것들 — 각 항목이 **왜** 필요한가

> 이 섹션은 platform-svc W1 README와 동일한 의도. knowledge에서도 정확히 같은 통증이 있고 같은 해법이 적용됩니다.

W2에서 `global/`, JWT, 프로파일 분리가 추가되는 이유는 **platform-svc/W1 README의 "W2에서 추가되는 것들" 섹션**을 참고하세요. 같은 통증 패턴:

1. 응답 포맷이 도메인마다 달라 프론트 분기 지옥 → `ApiResponse<T>`
2. 3개 컨트롤러 × 평균 3 엔드포인트 = 9번 try-catch 복붙 → `@RestControllerAdvice`
3. note·graph가 누구나 호출 가능 → JWT
4. 단일 yml의 한계 + 시크릿 보안 → 프로파일 분리
5. 검증 boilerplate → Bean Validation

knowledge-svc 특수 사항:
- **chunking은 인증 무관** — 외부 HTTP 입구가 없으니 SecurityConfig 영향 없음. 다만 W3 Kafka 컨슈머의 인증/권한 검증은 별도 (메시지 헤더 또는 발행자 토픽 신뢰).

---

## 📚 용어 사전 (신입용)

| 용어 | 의미 |
|---|---|
| **Pipeline 도메인** | 외부 HTTP 입구 없이 다른 도메인/시스템의 데이터를 처리하는 도메인. (chunking) |
| **Node / Edge** | 그래프 데이터 모델의 정점과 간선. 지식 그래프에서 개념과 관계. |
| **Chunk** | 긴 텍스트를 검색·임베딩에 적합한 작은 단위로 분할한 조각. |
| **@Lob** | JPA에서 큰 텍스트/바이너리 컬럼을 표시 (CLOB/BLOB). |
| **@Transactional** | 메서드 시작-종료를 한 DB 트랜잭션으로 묶음. 예외 시 자동 롤백. |

> 더 일반적인 용어(DTO, Entity, JPA 등)는 [synapse-platform-svc W1 README](https://github.com/team-project-final/synapse-svc-template/blob/skeleton/platform/w1/README.md) 참고.
