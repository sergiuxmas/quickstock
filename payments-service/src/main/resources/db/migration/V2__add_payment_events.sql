CREATE TABLE IF NOT EXISTS payment_events
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    payment_id UUID NOT NULL REFERENCES payments
(
    id
) ON DELETE CASCADE,
    event_type TEXT NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now
(
)
    );

CREATE INDEX IF NOT EXISTS idx_payment_events_payment_created ON payment_events(payment_id, created_at DESC);
