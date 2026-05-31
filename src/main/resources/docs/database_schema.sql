-- ============================================================
-- DATABASE SCHEMA: Website Bán Quần Áo — Nhóm 10
-- PostgreSQL 16
-- Generated: 2026-05-31
-- ============================================================

-- Enable extensions
CREATE EXTENSION IF NOT EXISTS "pg_trgm";   -- Fuzzy search / autocomplete
CREATE EXTENSION IF NOT EXISTS "pgcrypto";  -- UUID generation, password hashing

-- ============================================================
-- ENUM TYPES
-- ============================================================

CREATE TYPE user_role AS ENUM ('admin', 'customer');
CREATE TYPE gender_type AS ENUM ('male', 'female', 'other');
CREATE TYPE order_status AS ENUM ('pending', 'processing', 'shipping', 'completed', 'cancelled');
CREATE TYPE payment_method AS ENUM ('cod', 'vnpay', 'momo');
CREATE TYPE payment_status AS ENUM ('pending', 'completed', 'failed', 'refunded');
CREATE TYPE discount_type AS ENUM ('percentage', 'fixed_amount');
CREATE TYPE image_type AS ENUM ('main', 'thumbnail', 'gallery');

-- ============================================================
-- 1. MEMBERSHIP TIERS (phải tạo trước users vì có FK)
-- ============================================================

CREATE TABLE membership_tiers (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(50)     NOT NULL UNIQUE,
    slug            VARCHAR(50)     NOT NULL UNIQUE,
    min_points      INTEGER         NOT NULL DEFAULT 0,
    discount_percent NUMERIC(5,2)   NOT NULL DEFAULT 0 CHECK (discount_percent >= 0 AND discount_percent <= 100),
    description     TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE membership_tiers IS 'Hạng thành viên: Đồng, Bạc, Vàng, Kim cương. Admin có thể sửa qua CMS.';

-- ============================================================
-- 2. USERS
-- ============================================================

CREATE TABLE users (
    id                  BIGSERIAL       PRIMARY KEY,
    email               VARCHAR(255)    NOT NULL UNIQUE,
    password_hash       VARCHAR(255),                           -- NULL nếu đăng ký qua OAuth
    full_name           VARCHAR(100)    NOT NULL,
    phone               VARCHAR(15),
    gender              gender_type,
    date_of_birth       DATE,
    avatar_url          TEXT,
    role                user_role       NOT NULL DEFAULT 'customer',
    loyalty_points      INTEGER         NOT NULL DEFAULT 0 CHECK (loyalty_points >= 0),
    membership_tier_id  BIGINT          REFERENCES membership_tiers(id) ON DELETE SET NULL,
    auth_provider       VARCHAR(20)     DEFAULT 'email',        -- 'email' | 'google'
    email_verified      BOOLEAN         NOT NULL DEFAULT FALSE,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    last_login_at       TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ                              -- Soft delete
);

COMMENT ON TABLE users IS 'Người dùng hệ thống: admin và khách hàng. QĐ14: không được sửa email. QĐ16: email unique.';

-- ============================================================
-- 3. ADDRESSES
-- ============================================================

CREATE TABLE addresses (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recipient_name  VARCHAR(100)    NOT NULL,
    phone           VARCHAR(15)     NOT NULL,
    province        VARCHAR(100)    NOT NULL,
    district        VARCHAR(100)    NOT NULL,
    ward            VARCHAR(100)    NOT NULL,
    street_address  VARCHAR(255)    NOT NULL,
    is_default      BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE addresses IS 'Địa chỉ giao hàng. Mỗi KH có nhiều địa chỉ, 1 địa chỉ mặc định.';

-- ============================================================
-- 4. CATEGORIES (self-referencing, đa cấp tối đa 3 cấp)
-- ============================================================

CREATE TABLE categories (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    slug            VARCHAR(100)    NOT NULL UNIQUE,
    parent_id       BIGINT          REFERENCES categories(id) ON DELETE RESTRICT,
    description     TEXT,
    display_order   INTEGER         NOT NULL DEFAULT 0,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE categories IS 'Danh mục sản phẩm đa cấp (QĐ5: tối đa 3 cấp). VD: Nam > Áo > Áo Polo.';

-- ============================================================
-- 5. PRODUCTS
-- ============================================================

CREATE TABLE products (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(255)    NOT NULL,
    slug            VARCHAR(255)    NOT NULL UNIQUE,
    description     TEXT,
    material        VARCHAR(255),
    care_instructions TEXT,
    category_id     BIGINT          NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    base_price      NUMERIC(12,2)   NOT NULL CHECK (base_price >= 0),
    sale_price      NUMERIC(12,2)   CHECK (sale_price IS NULL OR sale_price >= 0),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    is_featured     BOOLEAN         NOT NULL DEFAULT FALSE,
    total_sold      INTEGER         NOT NULL DEFAULT 0 CHECK (total_sold >= 0),
    average_rating  NUMERIC(3,2)    DEFAULT 0 CHECK (average_rating >= 0 AND average_rating <= 5),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ                                  -- Soft delete (QĐ4)
);

COMMENT ON TABLE products IS 'Sản phẩm. QĐ1: tên không trùng cùng danh mục. QĐ4: xóa mềm nếu đã có đơn hàng.';

-- ============================================================
-- 6. PRODUCT VARIANTS (size + màu)
-- ============================================================

CREATE TABLE product_variants (
    id              BIGSERIAL       PRIMARY KEY,
    product_id      BIGINT          NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku             VARCHAR(50)     NOT NULL UNIQUE,
    size            VARCHAR(10)     NOT NULL,                    -- XS, S, M, L, XL, 2XL, 3XL, 4XL
    color           VARCHAR(50)     NOT NULL,
    stock_quantity  INTEGER         NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    additional_price NUMERIC(12,2)  NOT NULL DEFAULT 0,          -- Giá cộng thêm so với base_price
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    UNIQUE (product_id, size, color)                             -- QĐ2: mỗi combo size+màu unique per product
);

COMMENT ON TABLE product_variants IS 'Biến thể sản phẩm (QĐ2: SKU duy nhất, tồn kho >= 0). QĐ6: auto giảm khi đặt hàng.';

-- ============================================================
-- 7. PRODUCT IMAGES
-- ============================================================

CREATE TABLE product_images (
    id              BIGSERIAL       PRIMARY KEY,
    product_id      BIGINT          NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url       TEXT            NOT NULL,                    -- S3 URL
    image_type      image_type      NOT NULL DEFAULT 'gallery',
    display_order   INTEGER         NOT NULL DEFAULT 0,
    alt_text        VARCHAR(255),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE product_images IS 'Ảnh sản phẩm lưu trên S3. QĐ1: tối thiểu 1 ảnh main.';

-- ============================================================
-- 8. COLLECTIONS (Bộ sưu tập)
-- ============================================================

CREATE TABLE collections (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(255)    NOT NULL,
    slug            VARCHAR(255)    NOT NULL UNIQUE,
    description     TEXT,
    banner_url      TEXT,
    start_date      TIMESTAMPTZ,
    end_date        TIMESTAMPTZ,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE collections IS 'Bộ sưu tập sản phẩm theo mùa/chủ đề.';

-- ============================================================
-- 9. COLLECTION_PRODUCTS (Junction M:N)
-- ============================================================

CREATE TABLE collection_products (
    id              BIGSERIAL       PRIMARY KEY,
    collection_id   BIGINT          NOT NULL REFERENCES collections(id) ON DELETE CASCADE,
    product_id      BIGINT          NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    display_order   INTEGER         NOT NULL DEFAULT 0,

    UNIQUE (collection_id, product_id)
);

-- ============================================================
-- 10. VOUCHERS
-- ============================================================

CREATE TABLE vouchers (
    id                  BIGSERIAL       PRIMARY KEY,
    code                VARCHAR(50)     NOT NULL UNIQUE,
    discount_type       discount_type   NOT NULL,
    discount_value      NUMERIC(12,2)   NOT NULL CHECK (discount_value > 0),
    max_discount_amount NUMERIC(12,2),                           -- Giới hạn giảm tối đa (cho loại %)
    min_order_amount    NUMERIC(12,2)   NOT NULL DEFAULT 0,      -- QĐ11: điều kiện đơn tối thiểu
    start_date          TIMESTAMPTZ     NOT NULL,
    end_date            TIMESTAMPTZ     NOT NULL,
    usage_limit         INTEGER         NOT NULL DEFAULT 1,      -- QĐ7: giới hạn số lần sử dụng
    times_used          INTEGER         NOT NULL DEFAULT 0 CHECK (times_used >= 0),
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CHECK (end_date > start_date)
);

COMMENT ON TABLE vouchers IS 'Mã giảm giá (QĐ7). QĐ11: mỗi đơn chỉ 1 voucher, kiểm tra điều kiện.';

-- ============================================================
-- 11. ORDERS
-- ============================================================

CREATE TABLE orders (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             BIGINT          NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    order_code          VARCHAR(20)     NOT NULL UNIQUE,          -- VD: DH20260115001

    -- Snapshot địa chỉ (không FK vì address có thể thay đổi sau)
    shipping_name       VARCHAR(100)    NOT NULL,
    shipping_phone      VARCHAR(15)     NOT NULL,
    shipping_province   VARCHAR(100)    NOT NULL,
    shipping_district   VARCHAR(100)    NOT NULL,
    shipping_ward       VARCHAR(100)    NOT NULL,
    shipping_address    VARCHAR(255)    NOT NULL,

    -- Tính tiền
    subtotal            NUMERIC(12,2)   NOT NULL CHECK (subtotal >= 0),
    shipping_fee        NUMERIC(12,2)   NOT NULL DEFAULT 0 CHECK (shipping_fee >= 0),
    discount_amount     NUMERIC(12,2)   NOT NULL DEFAULT 0 CHECK (discount_amount >= 0),
    total_amount        NUMERIC(12,2)   NOT NULL CHECK (total_amount >= 0),

    voucher_id          BIGINT          REFERENCES vouchers(id) ON DELETE SET NULL,
    status              order_status    NOT NULL DEFAULT 'pending',
    note                TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE orders IS 'Đơn hàng (QĐ8). Ship: đơn < 500K → 30K, đơn >= 500K → miễn phí (app logic).';

-- ============================================================
-- 12. ORDER ITEMS
-- ============================================================

CREATE TABLE order_items (
    id                  BIGSERIAL       PRIMARY KEY,
    order_id            BIGINT          NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_variant_id  BIGINT          NOT NULL REFERENCES product_variants(id) ON DELETE RESTRICT,

    -- Snapshot thông tin SP tại thời điểm mua (QĐ3: giá mới không ảnh hưởng đơn cũ)
    product_name        VARCHAR(255)    NOT NULL,
    variant_info        VARCHAR(100)    NOT NULL,                 -- VD: "L / Trắng"
    quantity            INTEGER         NOT NULL CHECK (quantity > 0),
    unit_price          NUMERIC(12,2)   NOT NULL CHECK (unit_price >= 0),
    subtotal            NUMERIC(12,2)   NOT NULL CHECK (subtotal >= 0)
);

COMMENT ON TABLE order_items IS 'Chi tiết đơn hàng. Snapshot giá/tên SP tại thời điểm mua (QĐ3).';

-- ============================================================
-- 13. PAYMENTS
-- ============================================================

CREATE TABLE payments (
    id              BIGSERIAL       PRIMARY KEY,
    order_id        BIGINT          NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    method          payment_method  NOT NULL,
    amount          NUMERIC(12,2)   NOT NULL CHECK (amount >= 0),
    status          payment_status  NOT NULL DEFAULT 'pending',
    transaction_id  VARCHAR(255),                                -- ID từ VNPay/MoMo
    payment_data    JSONB,                                       -- Response data từ payment gateway
    paid_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE payments IS 'Thanh toán. Không lưu thông tin thẻ — delegate cho VNPay/MoMo.';

-- ============================================================
-- 14. REVIEWS
-- ============================================================

CREATE TABLE reviews (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id      BIGINT          NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    order_id        BIGINT          NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    rating          SMALLINT        NOT NULL CHECK (rating >= 1 AND rating <= 5),
    content         TEXT            NOT NULL CHECK (LENGTH(content) >= 10),  -- QĐ13: min 10 ký tự
    is_approved     BOOLEAN         NOT NULL DEFAULT FALSE,      -- QĐ9: admin duyệt
    admin_reply     TEXT,
    replied_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    UNIQUE (user_id, product_id, order_id)                       -- QĐ9: mỗi SP chỉ đánh giá 1 lần/đơn
);

COMMENT ON TABLE reviews IS 'Đánh giá sản phẩm (QĐ9, QĐ13). Chỉ KH đã mua + đơn hoàn thành mới được đánh giá.';

-- ============================================================
-- 15. REVIEW IMAGES
-- ============================================================

CREATE TABLE review_images (
    id              BIGSERIAL       PRIMARY KEY,
    review_id       BIGINT          NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    image_url       TEXT            NOT NULL,
    display_order   INTEGER         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE review_images IS 'Ảnh đánh giá (QĐ13: tối đa 5 ảnh — enforce ở app logic).';

-- ============================================================
-- 16. WISHLISTS
-- ============================================================

CREATE TABLE wishlists (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id      BIGINT          NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    UNIQUE (user_id, product_id)
);

COMMENT ON TABLE wishlists IS 'Danh sách sản phẩm yêu thích.';

-- ============================================================
-- 17. CART ITEMS (cho KH đã đăng nhập, guest dùng Redis)
-- ============================================================

CREATE TABLE cart_items (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_variant_id  BIGINT          NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    quantity            INTEGER         NOT NULL DEFAULT 1 CHECK (quantity > 0),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    UNIQUE (user_id, product_variant_id)
);

COMMENT ON TABLE cart_items IS 'Giỏ hàng KH đã đăng nhập. Khách vãng lai dùng Redis (session-based).';

-- ============================================================
-- 18. BANNERS
-- ============================================================

CREATE TABLE banners (
    id              BIGSERIAL       PRIMARY KEY,
    title           VARCHAR(255)    NOT NULL,
    image_url       TEXT            NOT NULL,
    link_url        TEXT,
    display_order   INTEGER         NOT NULL DEFAULT 0,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    start_date      TIMESTAMPTZ,
    end_date        TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE banners IS 'Banner/slider trang chủ. Admin quản lý thứ tự, thời gian hiển thị.';

-- ============================================================
-- 19. BLOG CATEGORIES
-- ============================================================

CREATE TABLE blog_categories (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL UNIQUE,
    slug            VARCHAR(100)    NOT NULL UNIQUE,
    description     TEXT,
    display_order   INTEGER         NOT NULL DEFAULT 0,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE blog_categories IS 'Danh mục blog: Xu hướng thời trang, Hướng dẫn phối đồ, Tin tức.';

-- ============================================================
-- 20. BLOG POSTS
-- ============================================================

CREATE TABLE blog_posts (
    id              BIGSERIAL       PRIMARY KEY,
    title           VARCHAR(255)    NOT NULL,
    slug            VARCHAR(255)    NOT NULL UNIQUE,
    content         TEXT            NOT NULL,
    excerpt         TEXT,
    thumbnail_url   TEXT,
    category_id     BIGINT          REFERENCES blog_categories(id) ON DELETE SET NULL,
    author_id       BIGINT          NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    is_published    BOOLEAN         NOT NULL DEFAULT FALSE,
    published_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE blog_posts IS 'Bài viết blog/tin tức thời trang.';

-- ============================================================
-- 21. BLOG TAGS
-- ============================================================

CREATE TABLE blog_tags (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(50)     NOT NULL UNIQUE,
    slug            VARCHAR(50)     NOT NULL UNIQUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE blog_tags IS 'Tags cho blog: áo polo, mùa hè, streetwear, v.v.';

-- ============================================================
-- 22. BLOG POST TAGS (Junction M:N)
-- ============================================================

CREATE TABLE blog_post_tags (
    id              BIGSERIAL       PRIMARY KEY,
    blog_post_id    BIGINT          NOT NULL REFERENCES blog_posts(id) ON DELETE CASCADE,
    blog_tag_id     BIGINT          NOT NULL REFERENCES blog_tags(id) ON DELETE CASCADE,

    UNIQUE (blog_post_id, blog_tag_id)
);

-- ============================================================
-- 23. ACTIVITY LOGS
-- ============================================================

CREATE TABLE activity_logs (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          REFERENCES users(id) ON DELETE SET NULL,
    action          VARCHAR(50)     NOT NULL,                    -- VD: 'login', 'create_product', 'update_order'
    entity_type     VARCHAR(50),                                 -- VD: 'product', 'order', 'user'
    entity_id       BIGINT,
    old_data        JSONB,
    new_data        JSONB,
    ip_address      INET,
    user_agent      TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE activity_logs IS 'Log hoạt động (yêu cầu hệ thống #9): đăng nhập, thay đổi dữ liệu, xử lý đơn hàng.';

-- ============================================================
-- INDEXES
-- ============================================================

-- Users
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_membership ON users(membership_tier_id);

-- Addresses
CREATE INDEX idx_addresses_user ON addresses(user_id);

-- Categories
CREATE INDEX idx_categories_parent ON categories(parent_id);
CREATE INDEX idx_categories_slug ON categories(slug);

-- Products: tìm kiếm fuzzy / autocomplete
CREATE INDEX idx_products_name_trgm ON products USING GIN (name gin_trgm_ops);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_active ON products(is_active) WHERE deleted_at IS NULL;
CREATE INDEX idx_products_featured ON products(is_featured) WHERE is_active = TRUE AND deleted_at IS NULL;
CREATE INDEX idx_products_slug ON products(slug);

-- Product Variants
CREATE INDEX idx_variants_product ON product_variants(product_id);
CREATE INDEX idx_variants_sku ON product_variants(sku);
CREATE INDEX idx_variants_stock_low ON product_variants(stock_quantity) WHERE stock_quantity < 10; -- QĐ6: cảnh báo

-- Product Images
CREATE INDEX idx_images_product ON product_images(product_id);

-- Orders
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_user_status ON orders(user_id, status);
CREATE INDEX idx_orders_created ON orders(created_at);
CREATE INDEX idx_orders_code ON orders(order_code);

-- Order Items
CREATE INDEX idx_order_items_order ON order_items(order_id);

-- Payments
CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_transaction ON payments(transaction_id) WHERE transaction_id IS NOT NULL;

-- Reviews
CREATE INDEX idx_reviews_product ON reviews(product_id, is_approved);
CREATE INDEX idx_reviews_user ON reviews(user_id);

-- Wishlists
CREATE INDEX idx_wishlists_user ON wishlists(user_id);

-- Cart Items
CREATE INDEX idx_cart_user ON cart_items(user_id);

-- Blog Posts
CREATE INDEX idx_blog_posts_category ON blog_posts(category_id);
CREATE INDEX idx_blog_posts_published ON blog_posts(is_published, published_at DESC);
CREATE INDEX idx_blog_posts_slug ON blog_posts(slug);

-- Activity Logs
CREATE INDEX idx_logs_user ON activity_logs(user_id);
CREATE INDEX idx_logs_entity ON activity_logs(entity_type, entity_id);
CREATE INDEX idx_logs_created ON activity_logs(created_at);
CREATE INDEX idx_logs_action ON activity_logs(action);

-- ============================================================
-- SEED DATA
-- ============================================================

-- Membership Tiers (admin có thể sửa qua CMS)
INSERT INTO membership_tiers (name, slug, min_points, discount_percent, description) VALUES
('Đồng',       'dong',       0,      0.00,   'Mặc định khi đăng ký'),
('Bạc',        'bac',        500,    3.00,   'Tương đương ~500.000 VNĐ chi tiêu'),
('Vàng',       'vang',       2000,   5.00,   'Tương đương ~2.000.000 VNĐ chi tiêu'),
('Kim cương',  'kim-cuong',  5000,   10.00,  'Tương đương ~5.000.000 VNĐ chi tiêu');

-- Admin user mặc định (password: Admin@123 — sử dụng bcrypt hash)
INSERT INTO users (email, password_hash, full_name, role, email_verified, is_active, membership_tier_id) VALUES
('admin@nhom10.com', '$2a$12$LJ3m4ys3Lk0TSwHjfJvOaOGPGP7RQHJ0YqpDdbxFi.Ky2VxkFpCu', 'Admin Nhóm 10', 'admin', TRUE, TRUE, 1);

-- Blog Categories mặc định
INSERT INTO blog_categories (name, slug, description, display_order) VALUES
('Xu hướng thời trang', 'xu-huong-thoi-trang', 'Cập nhật xu hướng thời trang mới nhất', 1),
('Hướng dẫn phối đồ',   'huong-dan-phoi-do',   'Mẹo và hướng dẫn phối đồ phong cách', 2),
('Tin tức',              'tin-tuc',              'Tin tức và sự kiện từ cửa hàng', 3);

-- ============================================================
-- FUNCTIONS: Auto-update updated_at
-- ============================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to all tables with updated_at
DO $$
DECLARE
    tbl TEXT;
BEGIN
    FOR tbl IN
        SELECT table_name
        FROM information_schema.columns
        WHERE column_name = 'updated_at'
          AND table_schema = 'public'
    LOOP
        EXECUTE format(
            'CREATE TRIGGER trigger_updated_at BEFORE UPDATE ON %I FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();',
            tbl
        );
    END LOOP;
END;
$$;
