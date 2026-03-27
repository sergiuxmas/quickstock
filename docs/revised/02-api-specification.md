# API Specification

## Contract Governance

- OpenAPI contracts under `docs/api-contracts/` are the source of truth.
- Implementations in `quickstock-core-service` and `payments-service` must conform to contract schemas, status codes, and security requirements.
- Additive changes are allowed when backward compatible; breaking changes require explicit versioning and migration notes.

## Core Service API (MVP)

### Authentication

- `POST /auth/register` (optional by rollout scope)
- `POST /auth/login`

### Products

- `GET /products` (pagination + filtering)
- `GET /products/{id}`

Supported list filters:

- `active`
- `sku`
- `name`
- `currency`
- `minPrice`
- `maxPrice`

### Admin Catalog and Inventory

- `POST /admin/products`
- `PATCH /admin/products/{id}`
- `POST /admin/inventory/{productId}/adjust`

### Orders

- `POST /orders`
- `POST /orders/{orderId}/confirm`
- `POST /orders/{orderId}/cancel`
- `GET /orders/{orderId}`
- `GET /orders`

### Core Payments-Facing Endpoints

- `POST /orders/{orderId}/payments`
- `GET /orders/{orderId}/payments/{paymentId}`
- `POST /internal/payments/finalized` (internal callback receiver)

## Payments Service API (MVP)

- `POST /payments`
- `GET /payments/{paymentId}`
- `GET /payments?orderId={id}`
- `POST /payments/{paymentId}/finalize`
- `POST /webhooks/provider` (optional for simulation)
- `GET /health`

## Error Model

All APIs should return a consistent error shape:

```json
{
  "timestamp": "2026-03-09T10:05:00Z",
  "status": 409,
  "error": "Conflict",
  "code": "IDEMPOTENCY_KEY_REUSE_MISMATCH",
  "message": "Idempotency-Key was already used with different payload.",
  "correlationId": "<uuid>"
}
```

Standard status usage:

- `400` invalid request/validation failure
- `401` unauthorized
- `403` forbidden
- `404` resource not found
- `409` idempotency or invalid state conflict
- `422` business-rule violation
- `500` or `503` internal/downstream failure

