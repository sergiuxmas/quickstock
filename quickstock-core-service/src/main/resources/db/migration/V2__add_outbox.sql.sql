CREATE TABLE IF NOT EXISTS outbox_events
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    aggregate_type TEXT NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type TEXT NOT NULL,
    payload JSONB NOT NULL,
    status TEXT NOT NULL DEFAULT 'NEW',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now
(
),
    sent_at TIMESTAMPTZ NULL
    );

CREATE INDEX IF NOT EXISTS idx_outbox_status_created ON outbox_events(status, created_at);
