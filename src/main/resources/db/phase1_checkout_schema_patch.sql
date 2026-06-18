-- ============================================================
-- PHASE 1 PATCH: Checkout reservation schema
-- Safe to run on an existing PostgreSQL database.
-- This patch only adds missing enum types, tables, and indexes.
-- It does not modify existing tables, seed data, or application config.
-- ============================================================

BEGIN;

-- ============================================================
-- ENUM TYPES
-- ============================================================

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'checkout_session_status') THEN
        CREATE TYPE checkout_session_status AS ENUM ('creating', 'reserved', 'completed', 'failed', 'expired', 'released');
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'reservation_status') THEN
        CREATE TYPE reservation_status AS ENUM ('active', 'consumed', 'released', 'expired');
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'payment_attempt_status') THEN
        CREATE TYPE payment_attempt_status AS ENUM ('pending', 'completed', 'failed', 'expired', 'requires_refund', 'refund_requested', 'refunded');
    END IF;
END
$$;

-- ============================================================
-- CHECKOUT SESSIONS
-- ============================================================

CREATE TABLE IF NOT EXISTS checkout_sessions (
    id                  BIGSERIAL       PRIMARY KEY,
    checkout_code       VARCHAR(30)     NOT NULL UNIQUE,
    user_id             BIGINT          NOT NULL REFERENCES users(id) ON DELETE RESTRICT,

    shipping_name       VARCHAR(100)    NOT NULL,
    shipping_phone      VARCHAR(15)     NOT NULL,
    shipping_province   VARCHAR(100)    NOT NULL,
    shipping_district   VARCHAR(100)    NOT NULL,
    shipping_ward       VARCHAR(100)    NOT NULL,
    shipping_address    VARCHAR(255)    NOT NULL,

    subtotal            NUMERIC(12,2)   NOT NULL CHECK (subtotal >= 0),
    shipping_fee        NUMERIC(12,2)   NOT NULL DEFAULT 0 CHECK (shipping_fee >= 0),
    discount_amount     NUMERIC(12,2)   NOT NULL DEFAULT 0 CHECK (discount_amount >= 0),
    total_amount        NUMERIC(12,2)   NOT NULL CHECK (total_amount >= 0),

    voucher_id          BIGINT          REFERENCES vouchers(id) ON DELETE SET NULL,
    payment_method      payment_method  NOT NULL,
    status              checkout_session_status NOT NULL DEFAULT 'creating',
    expires_at          TIMESTAMPTZ     NOT NULL,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE checkout_sessions IS 'Checkout session luu snapshot gio hang va dia chi truoc khi tao don hang.';

-- ============================================================
-- CHECKOUT SESSION ITEMS
-- ============================================================

CREATE TABLE IF NOT EXISTS checkout_session_items (
    id                  BIGSERIAL       PRIMARY KEY,
    checkout_session_id BIGINT          NOT NULL REFERENCES checkout_sessions(id) ON DELETE CASCADE,
    product_variant_id  BIGINT          NOT NULL REFERENCES product_variants(id) ON DELETE RESTRICT,
    product_name        VARCHAR(255)    NOT NULL,
    variant_info        VARCHAR(100)    NOT NULL,
    quantity            INTEGER         NOT NULL CHECK (quantity > 0),
    unit_price          NUMERIC(12,2)   NOT NULL CHECK (unit_price >= 0),
    subtotal            NUMERIC(12,2)   NOT NULL CHECK (subtotal >= 0),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE checkout_session_items IS 'Snapshot san pham tai thoi diem checkout.';

-- ============================================================
-- INVENTORY RESERVATIONS
-- ============================================================

CREATE TABLE IF NOT EXISTS inventory_reservations (
    id                  BIGSERIAL       PRIMARY KEY,
    checkout_session_id BIGINT          NOT NULL REFERENCES checkout_sessions(id) ON DELETE CASCADE,
    product_variant_id  BIGINT          NOT NULL REFERENCES product_variants(id) ON DELETE RESTRICT,
    quantity            INTEGER         NOT NULL CHECK (quantity > 0),
    status              reservation_status NOT NULL DEFAULT 'active',
    expires_at          TIMESTAMPTZ     NOT NULL,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    UNIQUE (checkout_session_id, product_variant_id)
);

COMMENT ON TABLE inventory_reservations IS 'Giu ton kho tam thoi cho checkout, chua tru stock_quantity khi reserve.';

-- ============================================================
-- VOUCHER RESERVATIONS
-- ============================================================

CREATE TABLE IF NOT EXISTS voucher_reservations (
    id                  BIGSERIAL       PRIMARY KEY,
    checkout_session_id BIGINT          NOT NULL REFERENCES checkout_sessions(id) ON DELETE CASCADE,
    voucher_id          BIGINT          NOT NULL REFERENCES vouchers(id) ON DELETE RESTRICT,
    discount_amount     NUMERIC(12,2)   NOT NULL DEFAULT 0 CHECK (discount_amount >= 0),
    status              reservation_status NOT NULL DEFAULT 'active',
    expires_at          TIMESTAMPTZ     NOT NULL,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    UNIQUE (checkout_session_id)
);

COMMENT ON TABLE voucher_reservations IS 'Giu luot su dung voucher tam thoi cho checkout, chua tang times_used khi reserve.';

-- ============================================================
-- PAYMENT ATTEMPTS
-- ============================================================

CREATE TABLE IF NOT EXISTS payment_attempts (
    id                      BIGSERIAL       PRIMARY KEY,
    payment_reference       VARCHAR(50)     NOT NULL UNIQUE,
    checkout_session_id     BIGINT          NOT NULL REFERENCES checkout_sessions(id) ON DELETE CASCADE,
    method                  payment_method  NOT NULL,
    amount                  NUMERIC(12,2)   NOT NULL CHECK (amount >= 0),
    status                  payment_attempt_status NOT NULL DEFAULT 'pending',
    payment_url             TEXT,
    gateway_transaction_id  VARCHAR(255),
    gateway_payload         JSONB,
    failure_reason          TEXT,
    requires_refund_reason  TEXT,
    expires_at              TIMESTAMPTZ     NOT NULL,
    completed_at            TIMESTAMPTZ,
    failed_at               TIMESTAMPTZ,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE payment_attempts IS 'Lan thu thanh toan online gan voi checkout session truoc khi co order.';

-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_checkout_sessions_user ON checkout_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_checkout_sessions_status ON checkout_sessions(status);
CREATE INDEX IF NOT EXISTS idx_checkout_sessions_expires ON checkout_sessions(expires_at);
CREATE INDEX IF NOT EXISTS idx_checkout_sessions_code ON checkout_sessions(checkout_code);

CREATE INDEX IF NOT EXISTS idx_checkout_session_items_checkout ON checkout_session_items(checkout_session_id);
CREATE INDEX IF NOT EXISTS idx_checkout_session_items_variant ON checkout_session_items(product_variant_id);

CREATE INDEX IF NOT EXISTS idx_inventory_reservations_variant_status_expires ON inventory_reservations(product_variant_id, status, expires_at);
CREATE INDEX IF NOT EXISTS idx_inventory_reservations_checkout ON inventory_reservations(checkout_session_id);
CREATE INDEX IF NOT EXISTS idx_inventory_reservations_status_expires ON inventory_reservations(status, expires_at);

CREATE INDEX IF NOT EXISTS idx_voucher_reservations_voucher_status_expires ON voucher_reservations(voucher_id, status, expires_at);
CREATE INDEX IF NOT EXISTS idx_voucher_reservations_checkout ON voucher_reservations(checkout_session_id);
CREATE INDEX IF NOT EXISTS idx_voucher_reservations_status_expires ON voucher_reservations(status, expires_at);

CREATE INDEX IF NOT EXISTS idx_payment_attempts_checkout ON payment_attempts(checkout_session_id);
CREATE INDEX IF NOT EXISTS idx_payment_attempts_status_expires ON payment_attempts(status, expires_at);
CREATE INDEX IF NOT EXISTS idx_payment_attempts_gateway_transaction ON payment_attempts(gateway_transaction_id) WHERE gateway_transaction_id IS NOT NULL;

COMMIT;
