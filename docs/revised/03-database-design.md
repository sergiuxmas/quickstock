# Database Design

## Data Ownership and Boundaries

QuickStock uses database-per-service:

- `core-db` owned only by `quickstock-core-service`
- `payments-db` owned only by `payments-service`

Service boundary rule:

- No cross-service table access.
- Integration is done via API/callback/event payloads.

## Core Database Model

### `users` (auth)

- `id` UUID PK
- `email` TEXT UNIQUE NOT NULL
- `role` TEXT NOT NULL
- `password_hash` TEXT NOT NULL
- `created_at`, `updated_at` TIMESTAMPTZ

### `products`

- `id` UUID PK
- `sku` TEXT UNIQUE NOT NULL
- `name` TEXT NOT NULL
- `price` NUMERIC(12,2) NOT NULL
- `currency` CHAR(3) NOT NULL
- `active` BOOLEAN NOT NULL DEFAULT true
- `created_at`, `updated_at` TIMESTAMPTZ

### `inventory`

- `product_id` UUID PK FK -> `products(id)`
- `available_qty` INT CHECK >= 0
- `reserved_qty` INT CHECK >= 0
- `version` BIGINT NOT NULL DEFAULT 0
- `updated_at` TIMESTAMPTZ

### `orders`

- `id` UUID PK
- `customer_id` UUID NOT NULL
- `status` TEXT NOT NULL
- `currency` CHAR(3) NOT NULL
- `total_amount` NUMERIC(12,2) CHECK >= 0
- `reserved_at`, `expires_at` TIMESTAMPTZ
- `created_at`, `updated_at` TIMESTAMPTZ

### `order_items`

- `id` UUID PK
- `order_id` UUID FK -> `orders(id)`
- `product_id` UUID FK -> `products(id)`
- `sku_snapshot`, `name_snapshot` TEXT NOT NULL
- `unit_price` NUMERIC(12,2) NOT NULL
- `qty` INT CHECK > 0
- `line_total` NUMERIC(12,2) CHECK >= 0

### Optional: `outbox_events`

- Used for resilient asynchronous integration when event-driven flow is enabled.

## Payments Database Model

### `payments`

- `id` UUID PK
- `order_id` UUID NOT NULL
- `amount` NUMERIC(12,2) CHECK >= 0
- `currency` CHAR(3) NOT NULL
- `status` TEXT NOT NULL
- `idempotency_key` TEXT UNIQUE NOT NULL
- `provider_ref` TEXT NULL
- `failure_reason` TEXT NULL
- `created_at`, `updated_at` TIMESTAMPTZ

### Optional: `payment_events`

- `id` UUID PK
- `payment_id` UUID FK -> `payments(id)`
- `event_type` TEXT NOT NULL
- `payload` JSONB NOT NULL
- `created_at` TIMESTAMPTZ

## Migration Strategy (Flyway)

- Every schema change is a Flyway migration.
- Migration naming: `V<version>__<description>.sql`.
- Migrations are immutable after application.
- Follow-up corrections require a new migration version.

Expected locations:

- `quickstock-core-service/src/main/resources/db/migration/`
- `payments-service/src/main/resources/db/migration/`

## Local Docker Database Setup

- Core DB local port: `5433`
- Payments DB local port: `5434`
- Container hostnames from app containers: `core-db`, `payments-db`
- Data persistence via named volumes.

