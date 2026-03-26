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

## 12. Traceability to Repository Artifacts

- Architecture + scope references: `README.md`, `docs/architecture/`
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
- What is the exact API contract/versioning policy for external consumers of `core-service`?
- What minimum coverage threshold should be enforced in Maven for CI gates?
- Should seeded user credentials remain dev-only profile specific?

