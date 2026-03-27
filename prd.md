# QuickStock Product Requirements Document (PRD)

## 1. Product Overview

QuickStock is a backend commerce platform for small-to-medium online shops. It focuses on reliable stock management and payment orchestration through two Spring Boot services:

- `quickstock-core-service`: catalog, inventory, orders, reservations, and auth
- `payments-service`: payment intents, status transitions, and callback/event delivery

Current architecture uses synchronous callbacks between services, with an option to evolve toward event-driven integration.

## 2. Problem Statement

Online shops often fail in two critical areas:

- Inventory becomes inconsistent under concurrency and retries (overselling risk)
- Payment state and order state diverge (double charge, stuck order, or unreleased stock)

QuickStock solves this by enforcing reservation-based order flow, idempotent payment interactions, and explicit state transitions with auditable persistence.

## 3. Goals and Non-Goals

### Goals

- Prevent overselling with reservation and expiry rules
- Keep order and payment states consistent under retries/failures
- Provide secure admin/customer access with JWT + role-based authorization
- Support local and Docker environments with Flyway-managed schemas
- Provide clear implementation guidance for incremental feature delivery
- Enforce contract-first API delivery using versioned OpenAPI specs so Core and future UI clients remain compatible across releases

### Non-Goals (Current MVP)

- Frontend/UI delivery
- Real external payment gateway integration (simulation-first)
- Analytics/reporting product features
- Multi-region/global deployment design

## 4. Target Users and Roles

- Admin:
  - Manages product catalog and inventory
  - Monitors order and payment outcomes
- Customer:
  - Browses products
  - Creates/confirms orders and initiates payments
- System Integrator/Developer:
  - Evolves service contracts and migration scripts
  - Maintains operational quality and tests

## 5. Scope

### In Scope

- Product catalog and filtering (`GET /products` with pagination/filtering)
- Inventory availability and reservation accounting
- Order lifecycle with reservation expiry (15 minutes default)
- Payments lifecycle with idempotency semantics
- Core <-> Payments integration via authenticated callbacks
- Database-per-service with Flyway migrations
- Seeded users/products for local development bootstrap
- Versioned OpenAPI contracts for both services and callback payloads under `docs/api-contracts/`
- Backward-compatible API evolution rules for external consumers (including future UI clients)
- Contract validation in CI for API/callback changes
- Canonical architecture and implementation source-of-truth documentation is maintained in `docs/revised/`; implementation decisions MUST follow this set when conflicts exist.

Note: The PRD defines mandatory MVP endpoints and behaviors. Additional endpoints referenced in architecture documents (e.g., detailed product, admin, and payment simulation endpoints) must either be explicitly included in feature specs or marked out-of-scope per release plan to avoid contract drift.

### Out of Scope (unless explicitly specified in feature docs)

- Full event-driven migration as default integration pattern
- External provider settlement/reconciliation
- Frontend experience and UX requirements

## 6. Functional Requirements

### FR-1 Authentication and Authorization

- Core service exposes `POST /auth/login` to issue JWT access tokens
- Authentication uses database users (`users` table), not in-memory users
- Roles must be normalized and enforced (`ADMIN`, `CUSTOMER`)
- Protected endpoints must reject missing/invalid JWT

### FR-2 Product Listing

- Core service exposes `GET /products`
- Supports pagination and filters: `active`, `sku`, `name`, `currency`, `minPrice`, `maxPrice`
- Filtering must be case-insensitive where relevant (`sku`, `name`, `currency`)
- Blank string filters are ignored
- Response envelope includes page metadata and item list

### FR-3 Inventory Integrity

- Inventory stores `available_qty`, `reserved_qty`, and versioning for safe updates
- Stock can only be reserved on order confirmation
- Failed/expired reservations must release stock

### FR-4 Order and Reservation Lifecycle

- Order state transitions must be explicit and validated
- Reservation expiry default is 15 minutes unless amended by specification
- Expiry processing must be safe for retries and concurrent executions

### FR-5 Payment Processing and Idempotency

- Payments are keyed with `idempotency_key`
- Same key + same request returns same logical result
- Same key + different request payload returns conflict
- Terminal payment states are propagated to core reliably

### FR-6 Data Management and Migrations

- Every schema change must be applied through Flyway migration scripts
- Core and Payments migrations are independent and service-owned
- Existing migrations are immutable; follow-up changes require new versions
- Baseline dev data includes users and initial products/inventory

### FR-7 API Contract Governance and Compatibility

- Every externally consumed REST endpoint and inter-service callback must be defined in versioned OpenAPI documents under `docs/api-contracts/`
- Implementations in `quickstock-core-service` and `payments-service` must conform to their OpenAPI contracts (request/response schema, status codes, auth requirements)
- Additive, backward-compatible changes are allowed in minor versions; breaking changes require explicit version bump, migration notes, and consumer impact documentation
- API/callback contract updates must include compatibility tests for current Core consumers and a future UI client integration baseline
- Error responses must follow a consistent schema across services (error code, message, trace/correlation context where applicable)
- Every API listed in `docs/revised/02-api-specification.md` must map to a versioned OpenAPI contract in `docs/api-contracts/`

## 7. Data and Domain Constraints

- Product currency is 3-char code (e.g., `USD`)
- Monetary values use decimal precision (`NUMERIC(12,2)`)
- Quantities are non-negative and line item quantity must be > 0
- Email is unique in `users`
- `idempotency_key` is unique in `payments`

## 8. Non-Functional Requirements

### NFR-1 Reliability

- Service startup must fail fast if schema validation fails
- Flyway migrations must run automatically at startup in supported profiles

### NFR-2 Security

- JWT signing configuration must be valid and deterministic in each environment
- No hardcoded production secrets
- Service-to-service communication must be authenticated

### NFR-3 Operability

- Structured logs with enough context for tracing order/payment flows
- Health endpoints available for runtime checks
- Docker Compose dev environment supported (`core-db`, `payments-db`)

### NFR-4 Quality

- Unit tests required for business logic changes
- Integration tests required for persistence and API behavior changes
- Contract updates required for API/callback behavior changes

### NFR-5 API Contract Quality and Consumer Compatibility

- CI must fail if OpenAPI validation or contract tests fail for changed APIs/callbacks
- Pagination and filtering response envelopes must remain stable for consumer parsing
- Deprecations must be documented with replacement guidance and removal timeline
- Core APIs must be documented for two consumer types: inter-service callers and future UI clients
- CI should include OpenAPI lint/diff checks, backward compatibility checks, and Flyway migration validation on clean databases

### NFR-6 Documentation Governance

- `docs/revised/` is the canonical and authoritative engineering documentation set (source of truth).
- `docs/architecture/` contains legacy draft/historical materials only and MUST NOT be used as source of truth for implementation, API, workflow, schema, or service-boundary decisions.
- If content differs between `docs/revised/` and `docs/architecture/`, `docs/revised/` prevails; any needed updates must be made in `docs/revised/` via PR.
- PRs that change API, workflows, schema, or service boundaries must include documentation updates in `docs/revised/`.

## 9. Success Metrics

- Zero oversell incidents in tested reservation scenarios
- 100% idempotency consistency for retried payment requests in test suites
- All migrations applied automatically on clean environment startup
- Critical flows covered by automated tests (auth, product listing, reservation/payment transitions)

## 10. Risks and Mitigations

- Risk: schema drift between entity expectations and DB types
  - Mitigation: strict Flyway + `ddl-auto=validate`, migration review checklist
- Risk: JWT misconfiguration causing auth failures
  - Mitigation: startup validation tests and integration test for login token issuance
- Risk: callback delivery mismatch between services
  - Mitigation: contract-first changes and compatibility tests

## 11. Spec-Kit Integration Scope (Important)

Spec-Kit usage in this repository is **not** intended as full automatic code generation.
It must **not** be interpreted as a mechanism that autonomously builds complete features end-to-end without human implementation decisions and review.

Spec-Kit is used to:

- optimize and structure application context before coding
- guide implementation step-by-step (`spec -> plan -> tasks`)
- propose implementation options with explicit trade-offs
- validate that delivered code aligns with requirements, contracts, and constraints

Out of scope for Spec-Kit in this repository:

- fully autonomous feature implementation without developer oversight
- one-shot generation of production-ready code as the primary delivery model

This PRD is the source artifact for generating and refining downstream specs.
All downstream specs and implementation plans must treat `docs/revised/` as authoritative and must not treat `docs/architecture/` documents as normative inputs.

## 12. Traceability to Repository Artifacts

- Architecture + scope references: `README.md`, `docs/revised/README.md`
- Revised architecture set:
  - `docs/revised/01-overview.md`
  - `docs/revised/02-api-specification.md`
  - `docs/revised/03-database-design.md`
  - `docs/revised/04-workflows-and-integration.md`
  - `docs/revised/05-project-structure.md`
  - `docs/revised/06-testing-and-quality.md`
  - `docs/revised/07-ci-cd-and-release.md`
- Documentation precedence rule: `docs/revised/` is authoritative for architecture and implementation guidance; `docs/api-contracts/` is authoritative for API contract artifacts; `docs/architecture/` is historical draft reference only.
- API contracts (source of truth): `docs/api-contracts/`
- Draft/legacy architecture sources (reference only; non-authoritative):
  - `docs/architecture/QuickStock_Implementation_Spec.docx` (REST surface and workflow expectations)
  - `docs/architecture/QuickStock_Java_Project_Structure.docx` (OpenAPI placement and package-level API concerns)
  - `docs/architecture/QuickStock_Implementation_Spec_v4_with_Payments_Service.docx` (payments service integration requirements)
  - `docs/architecture/QuickStock_Database_Technical_Document.docx` (schema/Flyway reference)
- Core schema and seeds:
  - `quickstock-core-service/src/main/resources/db/migration/V1__init_core_schema.sql`
  - `quickstock-core-service/src/main/resources/db/migration/V2__add_outbox.sql`
  - `quickstock-core-service/src/main/resources/db/migration/V3__seed_main_users.sql`
  - `quickstock-core-service/src/main/resources/db/migration/V4__seed_products_and_inventory.sql`
- Payments schema:
  - `payments-service/src/main/resources/db/migration/V1__init_payments_schema.sql`
  - `payments-service/src/main/resources/db/migration/V2__add_payment_events.sql`
- Runtime/dev topology: `docker-compose.yml`
- Governance constraints: `.specify/memory/constitution.md`

## 13. Open Questions for Next Specs Iteration

- Should payment callbacks remain the default integration long-term, or should event-driven migration become a planned milestone?
- What minimum coverage threshold should be enforced in Maven for CI gates?
- Should seeded user credentials remain dev-only profile specific?
- What deprecation period (e.g., 1 or 2 release cycles) should be enforced before removing deprecated API fields/endpoints?

