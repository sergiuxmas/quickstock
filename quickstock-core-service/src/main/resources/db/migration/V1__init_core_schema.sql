CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- users (optional)
CREATE TABLE IF NOT EXISTS users
(
    id            UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    email         TEXT        NOT NULL UNIQUE,
    role          TEXT        NOT NULL,
    password_hash TEXT        NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS products
(
    id         UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    sku        TEXT           NOT NULL UNIQUE,
    name       TEXT           NOT NULL,
    price      NUMERIC(12, 2) NOT NULL,
    currency   CHAR(3)        NOT NULL,
    active     BOOLEAN        NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_products_active ON products (active);

CREATE TABLE IF NOT EXISTS inventory
(
    product_id    UUID PRIMARY KEY REFERENCES products (id),
    available_qty INT         NOT NULL CHECK (available_qty >= 0),
    reserved_qty  INT         NOT NULL CHECK (reserved_qty >= 0),
    version       BIGINT      NOT NULL DEFAULT 0,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS orders
(
    id           UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    customer_id  UUID           NOT NULL,
    status       TEXT           NOT NULL,
    currency     CHAR(3)        NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL CHECK (total_amount >= 0),
    reserved_at  TIMESTAMPTZ    NULL,
    expires_at   TIMESTAMPTZ    NULL,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_orders_customer_created ON orders (customer_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_orders_status_created ON orders (status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_orders_expires_at ON orders (expires_at);

CREATE TABLE IF NOT EXISTS order_items
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id      UUID           NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id    UUID           NOT NULL REFERENCES products (id),
    sku_snapshot  TEXT           NOT NULL,
    name_snapshot TEXT           NOT NULL,
    unit_price    NUMERIC(12, 2) NOT NULL,
    qty           INT            NOT NULL CHECK (qty > 0),
    line_total    NUMERIC(12, 2) NOT NULL CHECK (line_total >= 0)
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items (order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_items (product_id);
