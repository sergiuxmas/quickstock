# QuickStock Overview

## Product Summary

QuickStock is a backend commerce platform designed for small and medium online shops. It is split into two Spring Boot services:

- `quickstock-core-service`: authentication, products, inventory, and orders.
- `payments-service`: payment attempts, status transitions, and payment finalization notifications.

## Problem It Solves

- Prevents overselling under concurrent ordering.
- Keeps order and payment states consistent across retries and failures.
- Enforces explicit, auditable lifecycle transitions.

## Service Boundaries

### Core Service Responsibilities

- Product catalog and filtering.
- Inventory reservation and release.
- Order lifecycle transitions (`CREATED`, `RESERVED`, `PAID`, `CANCELLED`, `EXPIRED`).
- JWT authentication and role-based authorization.

### Payments Service Responsibilities

- Payment creation and finalization (`PENDING`, `SUCCESS`, `FAILED`).
- Idempotency enforcement via `Idempotency-Key`.
- Terminal-state notification back to core service.

### Boundary Rules

- Each service owns its own database.
- Services do not access each other's tables.
- Cross-service communication is API/callback based.

## MVP Outcomes

- Customers can browse products and initiate order/payment flow.
- Inventory reservation expires if payment is not completed within the configured window.
- Admin users can manage catalog and inventory.
- Payment retries remain safe and idempotent.

## Architecture Direction

Current implementation supports synchronous callback integration between core and payments. Event-driven integration remains an evolution path.

