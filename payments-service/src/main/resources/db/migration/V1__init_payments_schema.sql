CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS payments
(
    id              UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    order_id        UUID           NOT NULL,
    amount          NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
    currency        CHAR(3)        NOT NULL,
    status          TEXT           NOT NULL,
    idempotency_key TEXT           NOT NULL UNIQUE,
    provider_ref    TEXT           NULL,
    failure_reason  TEXT           NULL,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_payments_order_created ON payments (order_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_payments_status_created ON payments (status, created_at DESC);
