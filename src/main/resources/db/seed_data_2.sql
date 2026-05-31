-- SEED DATA Part 2: Variants, Images, Collections, Vouchers
-- Run after seed_data_1.sql

-- ========== PRODUCT VARIANTS ==========
-- Product 1: Áo Polo Classic (id=1 in products, but actual id depends on existing data)
-- Using subquery-safe approach with known product order

-- SP1: Áo Polo Classic Pique (product_id from seed = row after admin)
INSERT INTO product_variants (product_id, sku, size, color, stock_quantity, additional_price, is_active) VALUES
(1, 'POLO-CLS-S-WHT', 'S', 'Trắng', 25, 0, TRUE),
(1, 'POLO-CLS-M-WHT', 'M', 'Trắng', 40, 0, TRUE),
(1, 'POLO-CLS-L-WHT', 'L', 'Trắng', 35, 0, TRUE),
(1, 'POLO-CLS-XL-WHT', 'XL', 'Trắng', 20, 0, TRUE),
(1, 'POLO-CLS-M-NVY', 'M', 'Xanh Navy', 30, 0, TRUE),
(1, 'POLO-CLS-L-NVY', 'L', 'Xanh Navy', 30, 0, TRUE),
(1, 'POLO-CLS-M-BLK', 'M', 'Đen', 35, 0, TRUE),
(1, 'POLO-CLS-L-BLK', 'L', 'Đen', 25, 0, TRUE),
-- SP2: Polo Coolmax
(2, 'POLO-CLX-M-GRY', 'M', 'Xám', 20, 0, TRUE),
(2, 'POLO-CLX-L-GRY', 'L', 'Xám', 25, 0, TRUE),
(2, 'POLO-CLX-M-BLU', 'M', 'Xanh Dương', 20, 0, TRUE),
(2, 'POLO-CLX-L-BLU', 'L', 'Xanh Dương', 15, 0, TRUE),
-- SP3: Thun Basic
(3, 'THUN-BSC-S-WHT', 'S', 'Trắng', 50, 0, TRUE),
(3, 'THUN-BSC-M-WHT', 'M', 'Trắng', 60, 0, TRUE),
(3, 'THUN-BSC-L-WHT', 'L', 'Trắng', 45, 0, TRUE),
(3, 'THUN-BSC-M-BLK', 'M', 'Đen', 55, 0, TRUE),
(3, 'THUN-BSC-L-BLK', 'L', 'Đen', 40, 0, TRUE),
(3, 'THUN-BSC-M-GRY', 'M', 'Xám', 35, 0, TRUE),
-- SP4: Thun Oversize
(4, 'THUN-OVS-M-BLK', 'M', 'Đen', 30, 0, TRUE),
(4, 'THUN-OVS-L-BLK', 'L', 'Đen', 35, 0, TRUE),
(4, 'THUN-OVS-XL-BLK', 'XL', 'Đen', 20, 0, TRUE),
(4, 'THUN-OVS-L-WHT', 'L', 'Trắng', 25, 0, TRUE),
-- SP5: Sơ Mi Oxford
(5, 'SOMI-OXF-M-WHT', 'M', 'Trắng', 20, 0, TRUE),
(5, 'SOMI-OXF-L-WHT', 'L', 'Trắng', 25, 0, TRUE),
(5, 'SOMI-OXF-XL-WHT', 'XL', 'Trắng', 15, 0, TRUE),
(5, 'SOMI-OXF-M-BLU', 'M', 'Xanh Nhạt', 20, 0, TRUE),
(5, 'SOMI-OXF-L-BLU', 'L', 'Xanh Nhạt', 20, 0, TRUE),
-- SP7: Jeans Slim
(7, 'JEAN-SLM-29-BLU', '29', 'Xanh Medium', 15, 0, TRUE),
(7, 'JEAN-SLM-30-BLU', '30', 'Xanh Medium', 25, 0, TRUE),
(7, 'JEAN-SLM-31-BLU', '31', 'Xanh Medium', 30, 0, TRUE),
(7, 'JEAN-SLM-32-BLU', '32', 'Xanh Medium', 25, 0, TRUE),
(7, 'JEAN-SLM-33-BLU', '33', 'Xanh Medium', 15, 0, TRUE),
-- SP11: Baby Tee Nữ
(11, 'BABY-TEE-S-PNK', 'S', 'Hồng', 40, 0, TRUE),
(11, 'BABY-TEE-M-PNK', 'M', 'Hồng', 50, 0, TRUE),
(11, 'BABY-TEE-S-WHT', 'S', 'Trắng', 45, 0, TRUE),
(11, 'BABY-TEE-M-WHT', 'M', 'Trắng', 55, 0, TRUE),
(11, 'BABY-TEE-S-BLK', 'S', 'Đen', 35, 0, TRUE),
(11, 'BABY-TEE-M-BLK', 'M', 'Đen', 40, 0, TRUE),
-- SP13: Sơ Mi Lụa Nữ
(13, 'SILK-VNK-S-CRM', 'S', 'Kem', 15, 0, TRUE),
(13, 'SILK-VNK-M-CRM', 'M', 'Kem', 20, 0, TRUE),
(13, 'SILK-VNK-S-BLK', 'S', 'Đen', 15, 0, TRUE),
(13, 'SILK-VNK-M-BLK', 'M', 'Đen', 18, 0, TRUE),
-- SP15: Đầm Hoa Nhí
(15, 'DAM-HOA-S-FLR', 'S', 'Hoa Nhí Xanh', 12, 0, TRUE),
(15, 'DAM-HOA-M-FLR', 'M', 'Hoa Nhí Xanh', 18, 0, TRUE),
(15, 'DAM-HOA-L-FLR', 'L', 'Hoa Nhí Xanh', 10, 0, TRUE),
(15, 'DAM-HOA-M-RED', 'M', 'Hoa Nhí Đỏ', 15, 0, TRUE);

-- ========== PRODUCT IMAGES ==========
INSERT INTO product_images (product_id, image_url, image_type, display_order, alt_text) VALUES
(1, 'https://placehold.co/800x1000/navy/white?text=Polo+Classic', 'main', 0, 'Áo Polo Classic Pique'),
(1, 'https://placehold.co/800x1000/white/navy?text=Polo+White', 'gallery', 1, 'Áo Polo Classic Trắng'),
(1, 'https://placehold.co/800x1000/111/white?text=Polo+Black', 'gallery', 2, 'Áo Polo Classic Đen'),
(2, 'https://placehold.co/800x1000/888/white?text=Polo+Coolmax', 'main', 0, 'Áo Polo Coolmax Sport'),
(3, 'https://placehold.co/800x1000/fff/111?text=Thun+Basic', 'main', 0, 'Áo Thun Basic Cổ Tròn'),
(3, 'https://placehold.co/800x1000/111/fff?text=Thun+Black', 'gallery', 1, 'Áo Thun Basic Đen'),
(4, 'https://placehold.co/800x1000/222/fff?text=Oversize', 'main', 0, 'Áo Thun Oversize'),
(5, 'https://placehold.co/800x1000/f5f5f5/333?text=Oxford', 'main', 0, 'Sơ Mi Oxford Trắng'),
(5, 'https://placehold.co/800x1000/b3d4fc/333?text=Oxford+Blue', 'gallery', 1, 'Sơ Mi Oxford Xanh Nhạt'),
(7, 'https://placehold.co/800x1000/4a6fa5/fff?text=Jeans+Slim', 'main', 0, 'Quần Jeans Slim Fit'),
(11, 'https://placehold.co/800x1000/ffb6c1/333?text=Baby+Tee', 'main', 0, 'Baby Tee Hồng'),
(11, 'https://placehold.co/800x1000/fff/333?text=Baby+Tee+W', 'gallery', 1, 'Baby Tee Trắng'),
(13, 'https://placehold.co/800x1000/faebd7/333?text=Silk+V-neck', 'main', 0, 'Sơ Mi Lụa Cổ V'),
(15, 'https://placehold.co/800x1000/e8f5e9/333?text=Floral+Dress', 'main', 0, 'Đầm Hoa Nhí Vintage'),
(15, 'https://placehold.co/800x1000/ffebee/333?text=Floral+Red', 'gallery', 1, 'Đầm Hoa Nhí Đỏ'),
(20, 'https://placehold.co/800x1000/6d8fad/fff?text=Denim+Jacket', 'main', 0, 'Áo Khoác Denim Nữ');

-- ========== COLLECTIONS ==========
INSERT INTO collections (name, slug, description, banner_url, start_date, end_date, is_active) VALUES
('BST Hè 2026', 'bst-he-2026', 'Bộ sưu tập mùa hè 2026 - Tươi mát & Năng động', 'https://placehold.co/1920x600/00bcd4/fff?text=BST+He+2026', '2026-05-01 00:00:00+07', '2026-08-31 23:59:59+07', TRUE),
('Best Sellers', 'best-sellers', 'Sản phẩm bán chạy nhất', 'https://placehold.co/1920x600/ff5722/fff?text=Best+Sellers', NULL, NULL, TRUE),
('New Arrivals', 'new-arrivals', 'Hàng mới về', 'https://placehold.co/1920x600/4caf50/fff?text=New+Arrivals', NULL, NULL, TRUE);

INSERT INTO collection_products (collection_id, product_id, display_order) VALUES
(1, 3, 1), (1, 4, 2), (1, 11, 3), (1, 12, 4), (1, 15, 5), (1, 19, 6),
(2, 11, 1), (2, 3, 2), (2, 7, 3), (2, 4, 4), (2, 1, 5),
(3, 17, 1), (3, 18, 2), (3, 20, 3), (3, 6, 4);

-- ========== VOUCHERS ==========
INSERT INTO vouchers (code, discount_type, discount_value, max_discount_amount, min_order_amount, start_date, end_date, usage_limit, times_used, is_active) VALUES
('WELCOME10', 'percentage', 10, 100000, 200000, '2026-01-01 00:00:00+07', '2026-12-31 23:59:59+07', 1000, 45, TRUE),
('SUMMER50K', 'fixed_amount', 50000, NULL, 300000, '2026-05-01 00:00:00+07', '2026-08-31 23:59:59+07', 500, 120, TRUE),
('FREESHIP', 'fixed_amount', 30000, NULL, 0, '2026-01-01 00:00:00+07', '2026-12-31 23:59:59+07', 2000, 350, TRUE),
('VIP20', 'percentage', 20, 200000, 500000, '2026-01-01 00:00:00+07', '2026-12-31 23:59:59+07', 100, 12, TRUE),
('FLASH30', 'percentage', 30, 150000, 400000, '2026-06-01 00:00:00+07', '2026-06-30 23:59:59+07', 200, 0, TRUE);

-- ========== BANNERS ==========
INSERT INTO banners (title, image_url, link_url, display_order, is_active, start_date, end_date) VALUES
('BST Hè 2026 - Giảm đến 30%', 'https://placehold.co/1920x600/00bcd4/fff?text=Summer+Sale+2026', '/collections/bst-he-2026', 1, TRUE, '2026-05-01 00:00:00+07', '2026-08-31 23:59:59+07'),
('Flash Sale Tháng 6', 'https://placehold.co/1920x600/f44336/fff?text=Flash+Sale+June', '/products?sale=true', 2, TRUE, '2026-06-01 00:00:00+07', '2026-06-30 23:59:59+07'),
('Miễn phí ship đơn từ 500K', 'https://placehold.co/1920x600/4caf50/fff?text=Free+Shipping+500K', '/products', 3, TRUE, NULL, NULL),
('Đồng phục công sở', 'https://placehold.co/1920x600/3f51b5/fff?text=Office+Collection', '/categories/ao-so-mi-nam', 4, TRUE, NULL, NULL);
