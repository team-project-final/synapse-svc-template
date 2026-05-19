# Synapse Service Template — W1~W4 진행 과정 종합 정리

> **작성일**: 2026-05-19
> **대상**: 4개 서비스 × 4주차 = 16개 브랜치

---

## 서비스 개요

| 서비스 | 브랜치 prefix | 도메인 | 언어 | 포트 |
|--------|--------------|--------|------|------|
| **platform** | `skeleton/platform/` | auth, audit, billing, notification (4개) | Java (Spring Boot) | 8080 |
| **knowledge** | `skeleton/knowledge/` | note, graph, chunking (3개) | Java (Spring Boot) | 8081 |
| **engagement** | `skeleton/engagement/` | community, gamification (2개) | Java (Spring Boot) | 8082 |
| **learning** | `skeleton/learning/` | card, srs (Java) + recommendation (Python) | Java + Python (이중 언어) | 8083, 8084 |

---

## 공통 진화 흐름

```
W1  도메인 분리 골격        ─ 패키지 경계 확립
 ↓
W2  횡단 관심사 (global/)   ─ 인증·예외·응답 통일
 ↓
W3  Kafka 이벤트 통신       ─ 도메인 간 직접 호출 제거
 ↓
W4  라이트 헥사고날 + 아키텍처 테스트 ─ 의존 방향 코드로 강제
```

---

## W1: 도메인 분리 골격

### 공통 패턴
- master에서 분기 → 서비스별 도메인을 **독립 패키지**로 split
- 각 도메인 내부는 동일한 5계층: `controller/` → `service/` → `repository/` → `entity/` → `dto/`
- 횡단 관심사(인증, 예외처리)는 **의도적으로 미포함** — W2에서 추가

### 서비스별 특이점

| 서비스 | 도메인 구성 | 특이점 |
|--------|-----------|--------|
| platform | auth, audit, billing, notification | 가장 많은 4개 도메인. 전형적 인프라 서비스 |
| knowledge | note, graph, chunking | **chunking은 controller 없음** (HTTP 입구 X) — W3 Kafka가 진짜 입구 |
| engagement | community, gamification | gamification은 **조회 전용** API — 포인트/뱃지 부여는 W3 이벤트로 트리거 예정 |
| learning | card, srs (Java) + recommendation (Python) | **이중 언어 모노레포**: `learning-java/` + `learning-ai/` + `docker-compose.yml` |

### 규모
- 평균 ~30 파일, ~680 lines (master 대비)

---

## W2: `global/` 횡단 관심사

### 공통 패턴
- W1 위에 `global/` 패키지 추가
- 4개 서비스 모두 **동일한 구조**의 횡단 관심사 도입

### 공통 추가 항목

```
global/
├── config/      SecurityConfig, RedisConfig
├── exception/   ErrorCode(enum), BusinessException, GlobalExceptionHandler(@RestControllerAdvice)
├── response/    ApiResponse<T>  — {success, data, error, timestamp}
├── security/    JwtTokenProvider, JwtAuthFilter
└── util/        (빈 placeholder)
```

- 환경별 프로파일 분리: `application-local.yml`, `application-dev.yml`, `application-prod.yml`
- 의존성 추가: `spring-security`, `jjwt 0.12.x`, `spring-data-redis`

### 서비스별 특이점

| 서비스 | 특이점 |
|--------|--------|
| platform | ErrorCode prefix: `A___(auth)`, `AU___(audit)`, `B___(billing)`, `N___(notification)` |
| knowledge | chunking은 HTTP 입구 없으므로 SecurityConfig 대상 외 |
| engagement | gamification/leaderboard는 `permitAll` — 점수 변경 API 자체가 없음 |
| learning | Java `global/` = Python `core/` 1:1 대응. 응답 JSON 포맷 양쪽 완전 동일 |

### 규모
- 평균 +15 파일, +580 lines (W1 대비 증분)

---

## W3: Kafka 이벤트 기반 통신

### 공통 패턴
- 각 도메인에 `kafka/producer/` 또는 `kafka/consumer/` 서브패키지 추가
- `global/config/KafkaConfig.java` — KafkaTemplate, ConsumerFactory Bean
- `global/kafka/event/` — 도메인 간 공유 이벤트 DTO (synapse-shared 분리 전 임시 위치)
- 토픽 네이밍: `synapse.{service}.{domain}.{event-name}.v{N}`
- **도메인 간 import 완전 차단** — 이벤트로만 소통

### 서비스별 이벤트 흐름

**platform** (도메인 간 직접 호출 0건):
```
auth → UserRegistered → notification, audit, billing (fan-out)
billing → PaymentCompleted → audit
billing → BillingChargeRequested
notification ← NotificationRequested
```

**knowledge** (파이프라인형):
```
NoteController → NoteEventPublisher → [note.created.v1]
  → chunking/Consumer → ChunkingService → [chunk-ready.v1]
    → graph/Consumer → Node/Edge 자동 생성
```
- chunking이 드디어 Kafka Consumer를 입구로 **동작 가능**해짐

**engagement** (fan-in 비대칭 — OUT 1 / IN 4):
```
community → [comment-created.v1] (유일한 발행)
gamification ← [user-registered.v1]      (platform)
             ← [comment-created.v1]      (자체)
             ← [card-reviewed.v1]        (learning)
             ← [note.created.v1]         (knowledge)
```
- 4개 외부 이벤트 → gamification으로 fan-in 통합 수신

**learning** (Java ↔ Python 양방향):
```
Java(SrsService) → [card-reviewed.v1], [recommendation-request.v1]
  → Python(consumer) → AI 추천 처리
    → [recommendation-ready.v1]
      → Java(RecommendationReadyConsumer) → 캐시
```
- Java와 Python은 서로를 모름 — Kafka 토픽만 공유

### 규모
- 평균 +15 파일, +580 lines (W2 대비 증분)

---

## W4: 라이트 헥사고날 + 아키텍처 테스트 자동 강제

### 공통 패턴 — 패키지 구조 전환

| W1~W3 (기존) | W4 (헥사고날) | 역할 |
|-------------|-------------|------|
| `controller/` + `dto/` | `api/` | HTTP 입구 (Inbound Adapter) |
| `service/` | `application/` + `port/` | 유스케이스 + 포트 인터페이스 |
| `entity/` + 도메인 룰 | `domain/` + `policy/` | 순수 비즈니스 규칙 |
| `repository/` + `kafka/` | `infrastructure/persistence/` + `messaging/` | 외부 연동 (Outbound Adapter) |

### 공통 ArchUnit 룰 (7개 기본)

1. **도메인 슬라이스 격리** — 도메인 A가 도메인 B 패키지 직접 import 금지
2. **domain → 타계층 의존 금지** — domain은 api, application, infrastructure를 모름
3. **application → infrastructure 의존 금지** — port 인터페이스만 의존
4. **api → infrastructure 직접 참조 금지**
5. **domain.policy 외부 의존 0** — `java..*` + `domain.*` 외 금지
6. **JpaRepository는 infrastructure.persistence에만** 위치
7. **@KafkaListener는 infrastructure.messaging에만** 위치

### 서비스별 특이점

| 서비스 | 특이점 |
|--------|--------|
| platform | 7룰 기본. `PasswordPolicy` 등 도메인 정책 분리 |
| knowledge | **8번째 룰 추가**: chunking은 `api/` 자체가 없어 controller-less 자동 통과 |
| engagement | `PointPolicy` (활동별 점수), `BadgeAchievementPolicy` (누적→뱃지 단계) — **정책이 곧 비즈니스** |
| learning | Java=ArchUnit 7룰 + Python=**import-linter 4 contract** (layers, domain-pure, api-no-infra, app-no-infra) |

### 핵심 설계 원칙
- **라이트 헥사고날**: 풀 헥사고날 대비 실용적 타협 — JPA 어노테이션을 domain에 허용, inbound port(UseCase 인터페이스) 생략
- **policy 클래스**: 외부 의존 0인 순수 Java/Python. 단위 테스트 100ms 이내. 비즈니스 규칙 변경 시 이 파일만 수정
- **port 인터페이스**: application 계층이 infrastructure를 직접 모름 → DB/메시징 교체 시 application 코드 무변경

### 규모
- 평균 +54 파일, +750 lines (W3 대비 증분) — 구조 재편으로 파일 수가 가장 많이 증가

---

## 주차별 누적 규모 비교

| 주차 | platform | knowledge | engagement | learning |
|------|----------|-----------|------------|----------|
| W1 | 31f / 1,072L | 28f / 591L | 26f / 524L | 34f / 549L |
| W2 | 43f / 1,476L | 40f / 877L | 38f / 743L | 51f / 938L |
| W3 | 55f / 1,974L | 49f / 1,192L | 49f / 1,080L | 63f / 1,254L |
| W4 | 70f / 2,196L | 63f / 1,478L | 60f / 1,383L | 75f / 1,578L |

> f = files changed, L = lines inserted (vs master)

---

## learning 서비스 고유 특성

learning은 **유일한 이중 언어(Java + Python) 모노레포**로, 모든 주차에서 동일 개념을 양쪽 언어에 대칭 적용:

| 주차 | Java | Python |
|------|------|--------|
| W1 | Spring Boot + JPA | FastAPI + Pydantic |
| W2 | `global/` (Filter + @RestControllerAdvice) | `core/` (Depends + exception handler) |
| W3 | `spring-kafka` (KafkaTemplate) | `aiokafka` (비동기 consumer/producer) |
| W4 | ArchUnit (7룰) | import-linter (4 contract) |
| Port 정의 | `interface` | `Protocol` (PEP 544) |

---

## 핵심 takeaway

1. **W1~W4는 누적 진화** — 각 주차 브랜치가 이전 주차를 포함 (w2 = w1 + 횡단관심사)
2. **4개 서비스 동일 패턴** — 주차별 추가 항목이 서비스와 무관하게 일관적
3. **매 주차 하나의 문제 해결**:
   - W1: "코드를 어디에 놓을 것인가" → 도메인 패키지 분리
   - W2: "공통 관심사 중복" → global/ 중앙화
   - W3: "도메인 간 강결합" → Kafka 이벤트 전환
   - W4: "아키텍처 규칙 위반 방지" → 헥사고날 + ArchUnit 자동 강제
4. **서비스별 개성은 유지** — knowledge의 controller-less, engagement의 fan-in, learning의 이중 언어
