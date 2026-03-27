<!--
Sync Impact Report
- Version change: 1.0.0 -> 2.0.0
- Modified principles:
  - I. Specification-Driven Development (NON-NEGOTIABLE) -> I. Service-Bounded Ownership (NON-NEGOTIABLE)
  - II. Product-Based Architecture -> II. Specification and Contract-First Delivery
  - III. Security & Compliance First -> III. Order-Payment Integrity and Idempotency
  - IV. Platform Constraints & Controlling -> IV. Security and Access Control Baseline
  - V. Test Coverage & Quality Gates + VII. Observability & Operational Readiness -> V. Quality Gates and Operational Readiness
  - VIII. Feature Spec Organization (Contract-First) -> merged into Principle II
- Added sections:
  - Scope Boundaries
  - Delivery Workflow
- Removed sections:
  - Security & Compliance Requirements (financial controls folded into Principle III)
  - Implementation Sequence
  - Discovery-Dependent Scope
- Templates requiring updates:
  - ✅ updated: .specify/templates/plan-template.md
  - ✅ updated: .specify/templates/spec-template.md
  - ✅ updated: .specify/templates/tasks-template.md
  - ⚠ pending: .specify/templates/commands/*.md (directory not present in repository)
- Follow-up TODOs:
  - None
-->

# QuickStock Commerce Platform (QuickStock) Constitution

This constitution defines mandatory engineering and delivery rules for the QuickStock
monorepo, which contains two Spring Boot services: `quickstock-core-service` and
`payments-service`.

## Core Principles

### I. Service-Bounded Ownership (NON-NEGOTIABLE)

QuickStock MUST operate as two independently deployable services with strict boundaries:

- `quickstock-core-service` owns product catalog, inventory, orders, reservation, and expiry logic
- `payments-service` owns payment intents, provider simulation, and payment status transitions
- Each service MUST own its own database schema and Flyway migrations
- Cross-service direct database access is prohibited; interaction MUST occur through versioned
  API contracts and authenticated callbacks or documented events

**Rationale:** The architecture and implementation specs define service separation as the
primary control against tight coupling and inconsistent data ownership.

### II. Specification and Contract-First Delivery

All feature work MUST start from documented specifications under `specs/`:

- Feature folders: `specs/<feature-id>/`
- Every feature folder MUST include `spec.md`, `plan.md`, and `tasks.md`
- Any change to external API or callback behavior MUST include updates under
  `docs/api-contracts/` in the same change set
- `plan.md` MUST identify which service(s) are changed and how contract compatibility is preserved

**Rationale:** QuickStock relies on inter-service contracts (order -> payment request,
payment -> core callback). Contract drift is a direct regression risk.

### III. Order-Payment Integrity and Idempotency

Order and payment flows MUST preserve business invariants described in the architecture docs:

- Stock MUST be reserved only at order confirmation and released on cancellation,
  payment failure, or reservation expiry
- Reservation expiry MUST remain 15 minutes unless a documented amendment changes this value
- Payment creation and status update endpoints MUST enforce idempotency semantics
  (same idempotency key + same payload -> same result; mismatched payload -> conflict)
- Payment terminal states MUST notify core via authenticated callback or a documented event
  mechanism before release

**Rationale:** The MVP outcome depends on preventing overselling and duplicate financial state
transitions under retries and concurrent traffic.

### IV. Security and Access Control Baseline

Security controls are mandatory in both services:

- JWT-based authentication MUST protect non-public endpoints
- Role-based authorization MUST separate customer and admin capabilities in core APIs
- Service-to-service calls (core -> payments and payments -> core callback) MUST be authenticated
- Secrets and credentials MUST come from environment or secret stores and MUST NOT be hardcoded

**Rationale:** The documented scope includes admin inventory/order controls and payment callbacks,
which are high-impact attack surfaces.

### V. Quality Gates and Operational Readiness

No feature is complete without verifiable quality and operability evidence:

- Unit tests are mandatory for business logic changes
- Integration tests are mandatory for persistence, cross-service communication, or callback flows
- Contract tests are mandatory when API contracts change
- CI MUST fail on test failures and on coverage below the repository threshold configured in Maven
- Both services MUST expose health endpoints and structured logs with correlation-friendly context

**Rationale:** QuickStock is a stateful commerce workflow; regressions in inventory and payment
logic are expensive and difficult to remediate without strong automated checks.

## Scope Boundaries

Current constitutional scope is the backend MVP for small-shop commerce:

- In scope: product catalog, inventory management, order lifecycle, reservation expiry,
  simulated payments, and payment callbacks
- In scope: monorepo with `quickstock-core-service`, `payments-service`, root `pom.xml`,
  `docker-compose.yml`, and per-service Flyway migrations
- Out of scope by default: frontend/UI governance, reporting products, migration programs,
  and non-simulated external payment providers unless explicitly specified
- Evolution from callbacks to event-driven integration is allowed only through a documented
  spec amendment and backward compatibility plan

## Delivery Workflow

- Every change MUST map to a feature spec and include acceptance criteria and measurable outcomes
- Pull requests MUST include service impact (`core`, `payments`, or `both`) and evidence of
  required tests
- Any API/callback change MUST include contract diff documentation and consumer compatibility notes
- Changes affecting order states, inventory reservation, or payment status transitions MUST include
  regression tests for retry and duplicate-delivery scenarios

## Governance

- This constitution supersedes conflicting team habits or undocumented workflow preferences
- Amendments require: (1) proposal PR, (2) rationale, (3) migration/rollout impact,
  and (4) reviewer approval from both service owners when both services are affected
- Semantic versioning policy for this document:
    - MAJOR: removing/redefining a principle or governance rule in a backward-incompatible way
    - MINOR: adding a new principle/section or materially expanding obligations
    - PATCH: wording clarifications with no governance behavior change
- Compliance review is required in every feature `plan.md` and PR review;
  non-compliance MUST be tracked as explicit follow-up work before release

**Version**: 2.0.0 | **Ratified**: 2026-03-26 | **Last Amended**: 2026-03-26
