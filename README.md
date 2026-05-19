# synapse-svc-template

Synapse 백엔드 서비스 골격 템플릿. 4개 *-svc 레포의 패키지 구조가 **W1 → W4**로 점진 확장되는 모습을 브랜치로 분리해 보여줍니다.

## 사용법

새 서비스를 시작할 때:

1. 우측 상단 **"Use this template"** 클릭
2. 출발 시점(주차)에 해당하는 브랜치 선택
3. 패키지명 `com.synapse.platform` → `com.synapse.{your-service}`로 일괄 치환

## 브랜치 인덱스

각 *-svc 레포의 패키지 구조 진화를 4단계로 분해. **누적 브랜치** — `wN`은 `w(N-1)`에서 분기.

### 진화 원칙

| 주차 | 추가되는 요소 | 한 줄 정의 |
|---|---|---|
| **W1** | 도메인 N-way split + `controller/service/repository/entity/dto` | 동작하는 최소 |
| **W2** | `global/` (config·exception·response·security·util) | 운영 가능한 골격 |
| **W3** | 도메인별 `kafka/` + `shared-events` 의존 | 도메인 간 이벤트 통신 |
| **W4** | `api / application / domain / infrastructure` 라이트 헥사고날 + ArchUnit | 강제되는 모듈리스 |

### platform-svc (auth · audit · billing · notification)

| 브랜치 | 내용 |
|---|---|
| `skeleton/platform/w1` | 4 도메인 골격 |
| `skeleton/platform/w2` | + `global/` |
| `skeleton/platform/w3` | + Kafka producer/consumer |
| `skeleton/platform/w4` | + 라이트 헥사고날 + ArchUnit |

### knowledge-svc (note · graph · chunking)

| 브랜치 | 내용 |
|---|---|
| `skeleton/knowledge/w1` | 3 도메인, chunking은 controller 無 |
| `skeleton/knowledge/w2` | + `global/` |
| `skeleton/knowledge/w3` | + chunking 파이프라인 (kafka in/out only) |
| `skeleton/knowledge/w4` | + ArchUnit "controller-less 도메인" 예외 룰 |

### engagement-svc (community · gamification)

| 브랜치 | 내용 |
|---|---|
| `skeleton/engagement/w1` | 2 도메인 골격 |
| `skeleton/engagement/w2` | + `global/` |
| `skeleton/engagement/w3` | + gamification fan-in (3 서비스 이벤트 구독) |
| `skeleton/engagement/w4` | + `domain/policy/` 룰 격리 |

### learning-svc (Java [card · srs] + Python [ai])

| 브랜치 | 내용 |
|---|---|
| `skeleton/learning/w1` | `learning-java/` + `learning-ai/` 폴더 분리 |
| `skeleton/learning/w2` | 양쪽에 `global/` · `core/` |
| `skeleton/learning/w3` | Java ↔ Python = Kafka only |
| `skeleton/learning/w4` | Java=ArchUnit, Python=import-linter |

## 규칙 요약

1. **도메인 간 직접 호출 금지** — 모두 Kafka 이벤트 (synapse-shared/shared-events) 경유
2. **`global/`만 횡단 의존 허용** — 도메인 코드는 다른 도메인 패키지를 import하지 않음
3. **계층 의존 방향** — `api → application → domain ← infrastructure` (W4 기준)
4. **`domain/`은 외부 의존 0** — JPA·Spring·Kafka 어노테이션 금지

자세한 컨벤션은 [team-project-final/syn](https://github.com/team-project-final/syn)의 `BACKEND_STRUCTURE.md` 참고.

## 관련 레포

- [synapse-shared](https://github.com/team-project-final/synapse-shared) — Avro 스키마 + 공통 라이브러리
- [synapse-platform-svc](https://github.com/team-project-final/synapse-platform-svc)
- [synapse-knowledge-svc](https://github.com/team-project-final/synapse-knowledge-svc)
- [synapse-engagement-svc](https://github.com/team-project-final/synapse-engagement-svc)
- [synapse-learning-svc](https://github.com/team-project-final/synapse-learning-svc)
- [synapse-gitops](https://github.com/team-project-final/synapse-gitops)
