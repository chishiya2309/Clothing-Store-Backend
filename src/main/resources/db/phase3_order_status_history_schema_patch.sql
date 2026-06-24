-- ============================================================
-- PHASE 3 PATCH: Order status history schema
-- Safe to run on an existing PostgreSQL database.
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS order_status_histories (
    id              BIGSERIAL       PRIMARY KEY,
    order_id        BIGINT          NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    from_status     order_status,
    to_status       order_status    NOT NULL,
    changed_by      BIGINT          REFERENCES users(id) ON DELETE SET NULL,
    changed_by_role user_role,
    reason          TEXT,
    metadata        JSONB,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE order_status_histories IS 'Timeline trang thai don hang phuc vu audit va Staff order detail.';

CREATE INDEX IF NOT EXISTS idx_order_status_history_order_time_id
    ON order_status_histories(order_id, created_at, id);

COMMIT;
