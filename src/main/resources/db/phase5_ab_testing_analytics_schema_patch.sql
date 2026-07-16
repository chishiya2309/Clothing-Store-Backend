-- ============================================================
-- PHASE 5: A/B TESTING + GA4-STYLE ANALYTICS EVENTS
-- ============================================================

CREATE TABLE IF NOT EXISTS experiments (
    id              BIGSERIAL       PRIMARY KEY,
    experiment_key  VARCHAR(120)    NOT NULL UNIQUE,
    name            VARCHAR(255)    NOT NULL,
    description     TEXT,
    status          VARCHAR(30)     NOT NULL DEFAULT 'draft',
    target_page     VARCHAR(255),
    starts_at       TIMESTAMPTZ,
    ends_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CHECK (status IN ('draft', 'running', 'paused', 'completed')),
    CHECK (ends_at IS NULL OR starts_at IS NULL OR ends_at > starts_at)
);

CREATE TABLE IF NOT EXISTS experiment_variants (
    id              BIGSERIAL       PRIMARY KEY,
    experiment_id   BIGINT          NOT NULL REFERENCES experiments(id) ON DELETE CASCADE,
    variant_key     VARCHAR(80)     NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    traffic_weight  INTEGER         NOT NULL DEFAULT 50 CHECK (traffic_weight >= 0 AND traffic_weight <= 100),
    is_control      BOOLEAN         NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    payload         TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    UNIQUE (experiment_id, variant_key)
);

CREATE TABLE IF NOT EXISTS analytics_events (
    id              BIGSERIAL       PRIMARY KEY,
    experiment_id   BIGINT          REFERENCES experiments(id) ON DELETE SET NULL,
    variant_id      BIGINT          REFERENCES experiment_variants(id) ON DELETE SET NULL,
    user_id         BIGINT          REFERENCES users(id) ON DELETE SET NULL,
    visitor_id      VARCHAR(120),
    session_id      VARCHAR(120),
    event_name      VARCHAR(120)    NOT NULL,
    page_path       VARCHAR(500),
    ga_client_id    VARCHAR(120),
    metadata        JSONB,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_experiments_key ON experiments(experiment_key);
CREATE INDEX IF NOT EXISTS idx_experiments_status_window ON experiments(status, starts_at, ends_at);
CREATE INDEX IF NOT EXISTS idx_experiment_variants_experiment ON experiment_variants(experiment_id);
CREATE INDEX IF NOT EXISTS idx_analytics_events_experiment_created ON analytics_events(experiment_id, created_at);
CREATE INDEX IF NOT EXISTS idx_analytics_events_variant_event ON analytics_events(variant_id, event_name);
CREATE INDEX IF NOT EXISTS idx_analytics_events_visitor ON analytics_events(visitor_id);

CREATE OR REPLACE TRIGGER trigger_experiments_updated_at
    BEFORE UPDATE ON experiments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE TRIGGER trigger_experiment_variants_updated_at
    BEFORE UPDATE ON experiment_variants
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
