# QuickStock — Core + Payments (Java 17, Spring Boot, Maven, Postgres, Flyway)

This repository contains **two Spring Boot applications**:

- **quickstock-core-service** — Products, Inventory, Orders, Reservations/Expiration
- **payments-service** — Payments API (idempotent payment attempts + provider simulation)  
  Communicates with Core via REST callbacks (or can be upgraded to events later).

---

## Tech Stack

- Java **17**
- Maven
- Spring Boot (latest compatible with Java 17)
- PostgreSQL (Docker)
- Flyway (DB migrations)

---

## Repository Structure

```
quickstock/
├─ docker-compose.yml
├─ quickstock-core-service/
│  ├─ pom.xml
│  └─ src/main/resources/db/migration/...
└─ payments-service/
   ├─ pom.xml
   └─ src/main/resources/db/migration/...
```

---

## Prerequisites

- **Java 17**
- Docker + Docker Compose
- Git

Optional:
- IntelliJ IDEA / VS Code

Verify:
```bash
java -version
docker --version
docker compose version
```

---

## 1) Start PostgreSQL (persistent)

From repo root:

```bash
docker compose up -d
docker ps
```

Default ports (example):
- Core DB: `localhost:5433`
- Payments DB: `localhost:5434`

> Data is persisted using Docker named volumes.

---

## 2) Configure application.yml

Each service has its own config file:

### Core
`quickstock-core-service/src/main/resources/application.yml`

Example (adjust as needed):
```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/quickstock_core
    username: quickstock
    password: quickstock_pw
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true

payments:
  base-url: http://localhost:8082
```

### Payments
`payments-service/src/main/resources/application.yml`

```yaml
server:
  port: 8082

spring:
  datasource:
    url: jdbc:postgresql://localhost:5434/quickstock_payments
    username: quickstock
    password: quickstock_pw
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true

core:
  base-url: http://localhost:8081
```

---

## 3) Flyway migrations

Migrations live here:

- Core: `quickstock-core-service/src/main/resources/db/migration/`
- Payments: `payments-service/src/main/resources/db/migration/`

Naming convention:
- `V1__init_core_schema.sql`
- `V2__add_outbox.sql`
- `V3__seed_data.sql` (optional)

Flyway runs automatically on startup.

---

## 4) Build & Run

### Build all modules (from repo root)
```bash
mvn clean test
```

### Run Core service
```bash
cd quickstock-core-service
../mvn spring-boot:run
```

### Run Payments service (new terminal)
```bash
cd payments-service
../mvn spring-boot:run
```

Services:
- Core API: `http://localhost:8081`
- Payments API: `http://localhost:8082`

---

## 5) High-Level Flow (Core ↔ Payments)

1. Customer creates order in Core
2. Customer confirms order → Core reserves inventory (`RESERVED`) and sets `expiresAt`
3. Customer starts payment → Core calls Payments service with `Idempotency-Key`
4. Payments responds `PENDING`, later resolves to `SUCCESS` or `FAILED`
5. Payments notifies Core (callback)
6. Core finalizes order:
   - `SUCCESS` → `PAID` and reserved stock is finalized
   - `FAILED` → reservation is released (order stays `RESERVED` or becomes `CANCELLED` per rules)
7. Background job expires unpaid reserved orders after 15 minutes → releases inventory

---

## 6) Common Commands

### Stop databases
```bash
docker compose down
```

### Stop + remove persisted data (CAREFUL)
```bash
docker compose down -v
```

### Run tests only for one service
```bash
cd quickstock-core-service
../mvn test
```

---

## 7) Git ignore

This repo should include `.gitignore` for:
- `target/`
- `.idea/`
- `.vscode/`
- `.env`
- local `application-local.yml`

---

## Next steps (recommended)

- Add OpenAPI/Swagger for both APIs
- Add Testcontainers integration tests (Postgres)
- Add auth between services (API key/JWT)
- Add outbox pattern + async messaging (optional)

---

## Troubleshooting

- **Flyway checksum error**: you edited an existing migration. Create a new migration instead.
- **Port already in use**: change `server.port` in `application.yml`.
- **DB connection refused**: ensure `docker compose up -d` is running and ports match.
