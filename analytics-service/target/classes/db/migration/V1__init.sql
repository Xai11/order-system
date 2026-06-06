CREATE TABLE IF NOT EXISTS order_stats (
    id          BIGSERIAL PRIMARY KEY,
    status      VARCHAR(50)  NOT NULL,
    count       BIGINT       NOT NULL,
    last_updated TIMESTAMP   NOT NULL
);

CREATE TABLE IF NOT EXISTS payment_failures (
    id           BIGSERIAL PRIMARY KEY,
    reason       VARCHAR(100) NOT NULL,
    count        BIGINT       NOT NULL,
    window_start TIMESTAMP    NOT NULL,
    window_end   TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS latency_metrics (
    id          BIGSERIAL PRIMARY KEY,
    metric_name VARCHAR(100)     NOT NULL,
    avg_ms      DOUBLE PRECISION NOT NULL,
    p95_ms      DOUBLE PRECISION NOT NULL,
    recorded_at TIMESTAMP        NOT NULL
);
