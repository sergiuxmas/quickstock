# Workflows and Integration

This document captures target-state business workflows. Current MVP contract baseline remains the endpoints explicitly defined in `docs/api-contracts/*.openapi.yaml`.

## Core Workflow: Confirm Order and Reserve Stock (target-state)

1. Validate order is in `CREATED` state.
2. Validate requested quantities against inventory.
3. Reserve stock transactionally:
   - `available_qty -= qty`
   - `reserved_qty += qty`
4. Transition order to `RESERVED`.
5. Set `reserved_at` and `expires_at` (`reserved_at + 15 min`).

## Payment Success Workflow (target-state)

1. Validate order is `RESERVED` and not expired.
2. Enforce idempotency for payment finalization.
3. Transition payment to `SUCCESS`.
4. Transition order to `PAID`.
5. Finalize reserved inventory exactly once.

## Payment Failure, Cancel, or Expiry (target-state)

All must release reserved stock safely:

- Transition order to `CANCELLED` or `EXPIRED`.
- Revert inventory reservation:
  - `reserved_qty -= qty`
  - `available_qty += qty`

## Reservation Expiry Job (target-state)

- Runs on schedule (for example, every minute).
- Finds orders in `RESERVED` with `expires_at < now`.
- Expires each order transactionally and releases stock.
- Must be safe under concurrency and retries.

## Core <-> Payments Integration

### Synchronous Callback Pattern (target-state)

- Core creates payment via `POST /payments` on payments service.
- Payments eventually reaches terminal state.
- Payments calls back core endpoint with final status when callback endpoints are included in contract scope.

### Event-Driven Pattern (Evolution)

- Payments emits `PaymentFinalized` event.
- Core consumes event and processes idempotently using `paymentId` as dedupe key.

### Failure Handling Rules

- At-least-once delivery for callbacks/events.
- Core processing must be idempotent.
- Retry with backoff for notification delivery.
- If downstream is unavailable, retain a retryable state and avoid data corruption.

