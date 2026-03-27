# QuickStock — Core + Payments (Java 21, Spring Boot, Maven, PostgreSQL, Flyway)

QuickStock is a backend commerce platform split into two Spring Boot services:

- `quickstock-core-service`: products, inventory, orders, reservations, auth
- `payments-service`: payment intents, status transitions, callback/event delivery

The current integration model is callback-based; event-driven evolution is possible via future specs.

## Documentation Precedence

When documentation conflicts, use this precedence order:

1. `docs/revised/` is the authoritative implementation reference.
2. `docs/api-contracts/` is the source of truth for API contracts.
3. `docs/architecture/` is draft/legacy context and is non-authoritative.

## What Spec-Kit Adds Here

Spec-Kit integration in this repository is for **engineering guidance and validation**, not full autonomous code generation.

### Advantages

- Keeps implementation aligned with a single source of truth (`prd.md`)
- Breaks large changes into controlled steps (`spec -> plan -> tasks`)
- Improves design quality with explicit trade-offs before coding
- Reduces regressions by validating implementation against documented scope
- Makes cross-service changes (`core` + `payments`) easier to track and review

### How/When to Use Spec-Kit

Use Spec-Kit when you:

- introduce a new feature or API contract
- modify order/payment state behavior or inventory rules
- change cross-service interaction (callbacks/events/auth)
- plan non-trivial refactors and want explicit acceptance criteria

Do not use it as a one-shot “generate full production code” tool.

### Typical Workflow

```bash
# 1) Keep PRD current (main product scope)
# edit: prd.md

# 2) Create/update feature specification
speckit.specify

# 3) Generate technical plan
speckit.plan

# 4) Generate dependency-ordered tasks
speckit.tasks

# 5) Run consistency analysis
speckit.analyze
```

Note: some Speckit flows enforce feature branch naming conventions.

## Tech Stack

- Java 21
- Maven (multi-module)
- Spring Boot
- PostgreSQL 16 (Docker)
- Flyway migrations

## Repository Structure

```text
quickstock/
|- docker-compose.yml
|- prd.md
|- quickstock-core-service/
|  |- pom.xml
|  `- src/main/resources/db/migration/
`- payments-service/
   |- pom.xml
   `- src/main/resources/db/migration/
```

## Prerequisites

- Java 21
- Docker + Docker Compose
- Maven

Verify:

```bash
java -version
docker --version
docker compose version
mvn -version
```

## Run Modes and Hostnames

Use DB hostnames based on where the app runs:

- App runs locally -> use `localhost:5433` (core DB), `localhost:5434` (payments DB)
- App runs in Docker Compose -> use `core-db:5432` and `payments-db:5432`

Default API ports:

- Core API: `http://localhost:8081`
- Payments API: `http://localhost:8082`

## Start Infrastructure

From repo root:

```bash
docker compose up -d
docker ps
```

Stop:

```bash
docker compose down
```

Stop and remove persisted DB volumes:

```bash
docker compose down -v
```

## Flyway Migrations

Migration folders:

- Core: `quickstock-core-service/src/main/resources/db/migration/`
- Payments: `payments-service/src/main/resources/db/migration/`

Rules:

- Flyway runs on startup when enabled
- Do not edit applied migrations; create a new versioned script instead
- Keep each migration service-owned (no cross-service schema coupling)

## Build, Test, Run

From repository root:

```bash
mvn clean install
```

Run core service:

```bash
cd quickstock-core-service
mvn spring-boot:run
```

Run payments service (new terminal):

```bash
cd payments-service
mvn spring-boot:run
```

Run tests for one module:

```bash
cd quickstock-core-service
mvn test
```

## High-Level Business Flow

1. Customer creates and confirms an order in Core
2. Core reserves inventory and sets reservation expiry
3. Core starts payment in Payments using idempotency semantics
4. Payments resolves payment state and notifies Core
5. Core finalizes order and adjusts reservations accordingly

## Troubleshooting (Most Common)

- Flyway does not run: verify profile, datasource URL, and `spring.flyway.enabled=true`
- Schema validation error: DB schema differs from entities; add a new migration to align
- DB connection refused: check compose health and hostname/port mapping for current run mode
- JWT signing key/JWK errors: verify JWT secret/key configuration and token encoder setup

## Source References

- Product requirements: `prd.md`
- Project constitution: `.specify/memory/constitution.md`
- Authoritative implementation docs: `docs/revised/`
- API contract source of truth: `docs/api-contracts/`
- Draft/legacy (non-authoritative) architecture docs: `docs/architecture/`
- Core migrations: `quickstock-core-service/src/main/resources/db/migration/`
- Payments migrations: `payments-service/src/main/resources/db/migration/`
