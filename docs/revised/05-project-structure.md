# Project Structure

## Repository Layout (Monorepo)

Recommended high-level layout:

- `pom.xml` (parent)
- `docker-compose.yml`
- `docs/api-contracts/`
- `docs/architecture/` (legacy)
- `docs/revised/` (canonical markdown set)
- `quickstock-core-service/`
- `payments-service/`

## Core Service Package Layering

Suggested package responsibilities:

- `config/` security, OpenAPI, scheduling, beans
- `controller/` REST controllers
- `dto/` request/response models
- `domain/` entities, enums, domain exceptions
- `repository/` Spring Data repositories
- `service/` business logic and transactions
- `integration/payments/` outbound payments client
- `web/error/` global exception handling

## Payments Service Package Layering

Suggested package responsibilities:

- `config/` service security and OpenAPI
- `controller/` payment endpoints
- `dto/` payloads
- `domain/` entities and enums
- `repository/` persistence
- `service/` idempotency and payment orchestration
- `integration/core/` callback or API integration to core
- `web/error/` consistent error responses

## Flyway Conventions

- Store migrations in `src/main/resources/db/migration/`.
- Use `V<version>__<description>.sql` naming.
- Keep existing migration files immutable.
- Apply only forward migrations.

## Local Developer Workflow

- Start infrastructure with Docker Compose.
- Run `quickstock-core-service` and `payments-service`.
- Verify migration logs for both services.
- Validate service health endpoints and key API flows.

