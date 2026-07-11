CREATE TABLE processed_events (
    id              BIGSERIAL PRIMARY KEY,
    event_id        VARCHAR(64) NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    source_service  VARCHAR(100) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    payload         TEXT NOT NULL,
    error_message   TEXT,
    retry_count     INTEGER NOT NULL DEFAULT 0,
    received_at     TIMESTAMP NOT NULL DEFAULT now(),
    processed_at    TIMESTAMP
);

CREATE UNIQUE INDEX idx_processed_events_event_id ON processed_events (event_id);
CREATE INDEX idx_processed_events_status ON processed_events (status);
CREATE INDEX idx_processed_events_received_at ON processed_events (received_at);
