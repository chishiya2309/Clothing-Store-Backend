-- ============================================================
-- PHASE 6: VOUCHER DISCOUNT TYPES AND CHECKOUT VOUCHER SLOTS
-- PostgreSQL 16
-- ============================================================

BEGIN;

ALTER TYPE discount_type ADD VALUE IF NOT EXISTS 'shipping_fixed_amount';
ALTER TYPE discount_type ADD VALUE IF NOT EXISTS 'cheapest_item_free';

DO $$
BEGIN
    CREATE TYPE voucher_slot AS ENUM ('product', 'shipping');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

ALTER TABLE voucher_reservations
    ADD COLUMN IF NOT EXISTS voucher_slot voucher_slot;

UPDATE voucher_reservations
SET voucher_slot = 'product'
WHERE voucher_slot IS NULL;

ALTER TABLE voucher_reservations
    ALTER COLUMN voucher_slot SET DEFAULT 'product',
    ALTER COLUMN voucher_slot SET NOT NULL;

ALTER TABLE voucher_reservations
    DROP CONSTRAINT IF EXISTS voucher_reservations_checkout_session_id_key;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uq_voucher_reservations_checkout_slot'
    ) THEN
        ALTER TABLE voucher_reservations
            ADD CONSTRAINT uq_voucher_reservations_checkout_slot
            UNIQUE (checkout_session_id, voucher_slot);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_voucher_reservations_checkout_slot
    ON voucher_reservations(checkout_session_id, voucher_slot);

COMMENT ON TABLE voucher_reservations IS 'Giu luot su dung voucher tam thoi cho checkout theo slot product/shipping, chua tang times_used khi reserve.';

COMMIT;
