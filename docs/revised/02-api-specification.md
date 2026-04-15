# API Specification

## Contract Governance

- Service-owned OpenAPI contracts under `quickstock-core-service/src/main/openapi/` and `payments-service/src/main/openapi/` are the source of truth.
- Implementations in `quickstock-core-service` and `payments-service` must conform to contract schemas, status codes, and security requirements.
- Additive changes are allowed when backward compatible; breaking changes require explicit versioning and migration notes.

## Core Service API (MVP)

The current MVP contract baseline is defined in `quickstock-core-service/src/main/openapi/openapi.yaml`.

### Authentication

- `POST /auth/login`

Out of current MVP scope unless explicitly added in a new feature spec:

- `POST /auth/register`

### Products

- `GET /products` (pagination + filtering)

Supported list filters:

- `active`
- `sku`
- `name`
- `currency`
- `minPrice`
- `maxPrice`

Out of current MVP contract scope unless explicitly added in a new feature spec and OpenAPI update:

- `GET /products/{id}`
- `POST /admin/products`
- `PATCH /admin/products/{id}`
- `POST /admin/inventory/{productId}/adjust`
- `POST /orders`
- `POST /orders/{orderId}/confirm`
- `POST /orders/{orderId}/cancel`
- `GET /orders/{orderId}`
- `GET /orders`
- `POST /orders/{orderId}/payments`
- `GET /orders/{orderId}/payments/{paymentId}`
- `POST /internal/payments/finalized` (internal callback receiver)

## Payments Service API (MVP)

The current MVP contract baseline is defined in `payments-service/src/main/openapi/openapi.yaml`.

- `POST /payments`
- `GET /payments/{paymentId}`
- `POST /payments/{paymentId}/finalize`
- `GET /health`

Out of current MVP contract scope unless explicitly added in a new feature spec and OpenAPI update:

- `GET /payments?orderId={id}`
- `POST /webhooks/provider` (optional for simulation)

## Error Model

All APIs must return a consistent error shape:

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

