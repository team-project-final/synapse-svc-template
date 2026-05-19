# synapse-knowledge-svc — W4 skeleton (최종)

> **추가**: 라이트 헥사고날 재구성 + ArchUnit. knowledge 특수: **controller-less 도메인 예외 룰**.

---

## 📂 W4 구조 — 3 도메인 (chunking은 api/ 없음)

```
com.synapse.knowledge/
├── KnowledgeApplication.java
│
├── note/                     api/{Controller, dto/}, application/{Service, port/}, domain/{Note, policy/}, infrastructure/{persistence/, messaging/}
├── graph/                    api/, application/{port/{NodePort,EdgePort,EventPort?}}, domain/{Node, Edge, policy/GraphLinkPolicy}, infrastructure/{persistence/, messaging/}
├── chunking/                 (api/ 없음!) application/{Service, port/}, domain/{ChunkJob, Chunk, policy/ChunkingPolicy}, infrastructure/{persistence/, messaging/}
└── global/                   config/, exception/, response/, security/, util/, kafka/event/
```

### chunking의 W4 구조 — controller-less 도메인의 정석

```
chunking/
├── application/
│   ├── ChunkingService.java
│   └── port/{ChunkJobPort, ChunkPort, EventPort}
├── domain/
│   ├── ChunkJob.java, Chunk.java
│   └── policy/ChunkingPolicy.java     (청크 크기·overlap)
└── infrastructure/
    ├── persistence/
    │   ├── ChunkJobJpaRepository.java (package-private)
    │   ├── ChunkJpaRepository.java    (package-private)
    │   └── ChunkingPersistenceAdapter.java   (ChunkJobPort + ChunkPort 구현)
    └── messaging/
        ├── NoteCreatedKafkaConsumer.java     ← 진짜 입구
        └── ChunkEventKafkaAdapter.java        (EventPort 구현)
```

`api/` 패키지가 **없습니다**. ArchUnit이 이걸 위반으로 잡으면 안 됨 → 룰 8번에서 예외 처리.

---

## 🛡 ArchUnit 8개 룰

`src/test/java/com/synapse/knowledge/arch/KnowledgeArchitectureTest.java`:

| # | 룰 | platform 동일? |
|---|---|---|
| 1 | 도메인 슬라이스 격리 | 동일 |
| 2 | `domain/`은 다른 계층 import 금지 | 동일 |
| 3 | `application/`은 api·infrastructure import 금지 (port 예외) | 동일 |
| 4 | `api/`는 infrastructure import 금지 | 동일 |
| 5 | `domain.policy/`는 외부 의존성 0 | 동일 |
| 6 | JpaRepository는 infrastructure.persistence에만 | 동일 |
| 7 | @KafkaListener는 infrastructure.messaging에만 | 동일 |
| **8** | **note·graph는 Controller 보유 필수 (chunking 예외)** | **knowledge 전용** |

### 룰 8 — controller-less 도메인 예외

```java
@Test
void non_pipeline_domains_should_have_controller() {
    classes()
        .that().resideInAnyPackage(BASE + ".note.api..", BASE + ".graph.api..")
        .and().haveSimpleNameEndingWith("Controller")
        .should().beAnnotatedWith(RestController.class)
        .check(CLASSES);
}
```

note·graph에 `Controller`라는 이름의 클래스가 있다면 반드시 `@RestController`여야 함.
chunking은 패키지 자체가 룰의 대상이 아니라 자동 통과.

> 💡 다른 방식: 룰을 "controller가 있어야 한다"가 아니라 **"controller가 있다면 @RestController여야 한다"**로 약하게 작성. 새 controller-less 도메인이 추가되어도 룰 수정 불필요.

---

## 🔄 W3 → W4 변화 요약

| 항목 | W3 | W4 |
|---|---|---|
| 도메인 패키지 | `controller/service/repository/entity/dto/kafka/` | `api/application/domain/infrastructure/` |
| chunking 구조 | controller 없음, service 직접 호출 | controller 없음 + 명시적 port/adapter |
| GraphService 의존 | NodeRepository, EdgeRepository 구체 | NodePort + EdgePort 인터페이스 |
| Persistence Adapter | 도메인당 1개 | graph는 Node+Edge를 한 adapter가 양쪽 port 구현 |
| 강제 메커니즘 | 컨벤션 (문서) | **ArchUnit 8룰 (knowledge 특수 1개 포함)** |

자세한 통증 → 해법 분석은 [platform/w3의 W4 항목 섹션](https://github.com/team-project-final/synapse-svc-template/blob/skeleton/platform/w3/README.md) 참고.

---

## ▶️ 실행 + 검증

```bash
./gradlew bootRun                    # H2 + 로컬 Kafka (선택)
./gradlew test                       # 전체 + ArchUnit
./gradlew test --tests "*ArchitectureTest"   # 룰만
```

위반 시뮬레이션:
- chunking에 RestController 추가 → 룰 8은 안 잡지만, 그 외 룰들은 작동.
- note의 service에서 graph 클래스 import → 룰 1이 잡음.
- domain/policy/에 @Component → 룰 5가 잡음.

---

## 🎓 knowledge가 W4에서 배우는 것

1. **모든 도메인이 같은 모양일 필요 없음** — chunking처럼 controller-less 도메인은 정당.
2. **ArchUnit 룰은 도메인 특수성을 반영해야 함** — 일률 적용은 오히려 위반 강제.
3. **두 entity가 한 도메인이면 한 adapter가 여러 port 구현 OK** — graph의 GraphPersistenceAdapter가 NodePort + EdgePort.

---

## 다음 단계 (template 외부)

W4가 마지막 — synapse-knowledge-svc 실제 레포로 옮기고:
1. synapse-shared 의존성 활성화 → `global/kafka/event/` 삭제
2. Vector DB (예: pgvector) 통합 → chunking이 임베딩까지
3. graph DB (Neo4j) 선택 시 → infrastructure/persistence/만 교체 (application은 무변)
