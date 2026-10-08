-- ============================================================
-- PHASE 9: ORDER VOUCHER SNAPSHOTS
-- PostgreSQL 16
-- ============================================================

BEGIN;

ALTER TABLE vouchers
    DROP CONSTRAINT IF EXISTS vouchers_discount_value_check;

ALTER TABLE vouchers
    DROP CONSTRAINT IF EXISTS chk_vouchers_discount_value;

ALTER TABLE vouchers
    ADD CONSTRAINT chk_vouchers_discount_value CHECK (
        (discount_type = 'cheapest_item_free'::discount_type AND discount_value >= 0)
        OR (discount_type <> 'cheapest_item_free'::discount_type AND discount_value > 0)
    );

CREATE TABLE IF NOT EXISTS order_vouchers (
    id                  BIGSERIAL       PRIMARY KEY,
    order_id            BIGINT          NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    voucher_id          BIGINT          REFERENCES vouchers(id) ON DELETE SET NULL,
    voucher_code        VARCHAR(50)     NOT NULL,
    discount_type       discount_type   NOT NULL,
    voucher_slot        voucher_slot    NOT NULL,
    discount_amount     NUMERIC(12,2)   NOT NULL DEFAULT 0 CHECK (discount_amount >= 0),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_order_vouchers_order_slot
    ON order_vouchers(order_id, voucher_slot);

COMMENT ON TABLE order_vouchers IS 'Snapshot cac voucher da ap dung vao don hang, giu dung lich su khi voucher thay doi sau nay.';

COMMIT;
