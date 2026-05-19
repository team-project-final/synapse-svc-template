# synapse-platform-svc — W1 skeleton

> **출발선**: 도메인 4-way split + 계층 3종 (`controller / service / repository`) + `entity / dto`.
> 횡단 관심사(`global/`)는 W2에서 도입.

## 패키지 구조 (W1)

```
src/main/java/com/synapse/platform/
├── PlatformApplication.java
├── auth/            ← 도메인 1
│   ├── controller/  ── AuthController
│   ├── service/     ── AuthService
│   ├── repository/  ── UserRepository
│   ├── entity/      ── User
│   └── dto/
│       ├── request/  ── LoginRequest
│       └── response/ ── TokenResponse
├── audit/           ← 도메인 2 (감사 로그)
├── billing/         ← 도메인 3 (과금)
└── notification/    ← 도메인 4 (알림)
```

각 도메인은 동일한 5-계층 패턴. **도메인 간 직접 import 금지** — 이 단계는 컴파일러가 막지 못하지만 컨벤션으로 약속, W4에서 ArchUnit으로 강제.

## 실행

```bash
./gradlew bootRun
```

엔드포인트 (스텁):
- `POST /api/v1/auth/login`
- `GET  /api/v1/audit/logs`
- `POST /api/v1/billing/charge`
- `POST /api/v1/notifications`

## 다음 주차

- `skeleton/platform/w2` — `global/` (config·exception·response·security·util)
- `skeleton/platform/w3` — 도메인별 `kafka/` 추가
- `skeleton/platform/w4` — `api/application/domain/infrastructure` + ArchUnit
