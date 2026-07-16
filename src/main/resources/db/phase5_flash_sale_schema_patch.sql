-- ============================================================
-- PHASE 5: FLASH SALE CAMPAIGNS, QUOTAS, AND RESERVATIONS
-- PostgreSQL 16
-- ============================================================

BEGIN;

CREATE TABLE flash_sale_campaigns (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(150)    NOT NULL,
    description     TEXT,
    start_at        TIMESTAMPTZ     NOT NULL,
    end_at          TIMESTAMPTZ     NOT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_flash_sale_campaign_time CHECK (end_at > start_at)
);

CREATE TABLE flash_sale_items (
    id                  BIGSERIAL       PRIMARY KEY,
    campaign_id         BIGINT          NOT NULL REFERENCES flash_sale_campaigns(id) ON DELETE CASCADE,
    product_id          BIGINT          NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    flash_sale_price    NUMERIC(12,2)   NOT NULL,
    quota               INTEGER         NOT NULL,
    reserved_quantity   INTEGER         NOT NULL DEFAULT 0,
    sold_quantity       INTEGER         NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_flash_sale_item_campaign_product UNIQUE (campaign_id, product_id),
    CONSTRAINT chk_flash_sale_item_price CHECK (flash_sale_price >= 0),
    CONSTRAINT chk_flash_sale_item_quota CHECK (quota > 0),
    CONSTRAINT chk_flash_sale_item_reserved CHECK (reserved_quantity >= 0),
    CONSTRAINT chk_flash_sale_item_sold CHECK (sold_quantity >= 0),
    CONSTRAINT chk_flash_sale_item_capacity CHECK (reserved_quantity + sold_quantity <= quota)
);

ALTER TABLE checkout_session_items
    ADD COLUMN flash_sale_item_id BIGINT REFERENCES flash_sale_items(id) ON DELETE RESTRICT,
    ADD COLUMN price_source VARCHAR(20) NOT NULL DEFAULT 'REGULAR',
    ADD CONSTRAINT chk_checkout_item_price_source
        CHECK (price_source IN ('REGULAR', 'PRODUCT_SALE', 'FLASH_SALE'));

ALTER TABLE order_items
    ADD COLUMN flash_sale_item_id BIGINT REFERENCES flash_sale_items(id) ON DELETE RESTRICT,
    ADD COLUMN price_source VARCHAR(20) NOT NULL DEFAULT 'REGULAR',
    ADD CONSTRAINT chk_order_item_price_source
        CHECK (price_source IN ('REGULAR', 'PRODUCT_SALE', 'FLASH_SALE'));

CREATE TABLE flash_sale_reservations (
    id                  BIGSERIAL           PRIMARY KEY,
    checkout_session_id BIGINT              NOT NULL REFERENCES checkout_sessions(id) ON DELETE CASCADE,
    flash_sale_item_id  BIGINT              NOT NULL REFERENCES flash_sale_items(id) ON DELETE RESTRICT,
    quantity            INTEGER             NOT NULL CHECK (quantity > 0),
    status              reservation_status  NOT NULL DEFAULT 'active',
    expires_at          TIMESTAMPTZ          NOT NULL,
    created_at          TIMESTAMPTZ          NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ          NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_flash_sale_reservation_checkout_item
        UNIQUE (checkout_session_id, flash_sale_item_id)
);

CREATE INDEX idx_flash_sale_campaigns_active_time
    ON flash_sale_campaigns(is_active, start_at, end_at);
CREATE INDEX idx_flash_sale_items_campaign ON flash_sale_items(campaign_id);
CREATE INDEX idx_flash_sale_items_product ON flash_sale_items(product_id);
CREATE INDEX idx_flash_sale_reservations_checkout ON flash_sale_reservations(checkout_session_id);
CREATE INDEX idx_flash_sale_reservations_item_status_expires
    ON flash_sale_reservations(flash_sale_item_id, status, expires_at);
CREATE INDEX idx_flash_sale_reservations_status_expires
    ON flash_sale_reservations(status, expires_at);
CREATE INDEX idx_checkout_session_items_flash_sale
    ON checkout_session_items(flash_sale_item_id) WHERE flash_sale_item_id IS NOT NULL;
CREATE INDEX idx_order_items_flash_sale
    ON order_items(flash_sale_item_id) WHERE flash_sale_item_id IS NOT NULL;

COMMENT ON TABLE flash_sale_campaigns IS 'Flash sale campaigns with server-side start and end times.';
COMMENT ON TABLE flash_sale_items IS 'Products, prices, quotas, reservations, and sold counts for flash sale campaigns.';
COMMENT ON TABLE flash_sale_reservations IS 'Temporary flash sale quota reservations linked to checkout sessions.';

CREATE TRIGGER trigger_updated_at
    BEFORE UPDATE ON flash_sale_campaigns
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trigger_updated_at
    BEFORE UPDATE ON flash_sale_items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trigger_updated_at
    BEFORE UPDATE ON flash_sale_reservations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMIT;
