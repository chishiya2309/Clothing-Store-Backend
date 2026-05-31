-- SEED DATA Part 1: Users, Categories, Products
-- Run after database_schema.sql

-- ========== USERS (10 KH + 1 admin đã có) ==========
INSERT INTO users (email, password_hash, full_name, phone, gender, date_of_birth, avatar_url, role, loyalty_points, membership_tier_id, email_verified, is_active) VALUES
('nguyenvana@gmail.com', '$2a$12$LJ3m4ys3Lk0TSwHjfJvOaOGPGP7RQHJ0YqpDdbxFi.Ky2VxkFpCu', 'Nguyễn Văn A', '0901234567', 'male', '1998-03-15', NULL, 'customer', 1200, 2, TRUE, TRUE),
('tranthib@gmail.com', '$2a$12$LJ3m4ys3Lk0TSwHjfJvOaOGPGP7RQHJ0YqpDdbxFi.Ky2VxkFpCu', 'Trần Thị B', '0912345678', 'female', '2000-07-22', NULL, 'customer', 5500, 4, TRUE, TRUE),
('levanc@gmail.com', '$2a$12$LJ3m4ys3Lk0TSwHjfJvOaOGPGP7RQHJ0YqpDdbxFi.Ky2VxkFpCu', 'Lê Văn C', '0923456789', 'male', '1995-11-08', NULL, 'customer', 300, 1, TRUE, TRUE),
('phamthid@gmail.com', '$2a$12$LJ3m4ys3Lk0TSwHjfJvOaOGPGP7RQHJ0YqpDdbxFi.Ky2VxkFpCu', 'Phạm Thị D', '0934567890', 'female', '2001-01-30', NULL, 'customer', 2100, 3, TRUE, TRUE),
('hoangvane@gmail.com', '$2a$12$LJ3m4ys3Lk0TSwHjfJvOaOGPGP7RQHJ0YqpDdbxFi.Ky2VxkFpCu', 'Hoàng Văn E', '0945678901', 'male', '1997-09-12', NULL, 'customer', 800, 2, TRUE, TRUE),
('ngothif@gmail.com', '$2a$12$LJ3m4ys3Lk0TSwHjfJvOaOGPGP7RQHJ0YqpDdbxFi.Ky2VxkFpCu', 'Ngô Thị F', '0956789012', 'female', '1999-05-25', NULL, 'customer', 0, 1, TRUE, TRUE),
('vuvang@gmail.com', '$2a$12$LJ3m4ys3Lk0TSwHjfJvOaOGPGP7RQHJ0YqpDdbxFi.Ky2VxkFpCu', 'Vũ Văn G', '0967890123', 'male', '1996-12-03', NULL, 'customer', 3500, 3, TRUE, TRUE),
('dangthih@gmail.com', '$2a$12$LJ3m4ys3Lk0TSwHjfJvOaOGPGP7RQHJ0YqpDdbxFi.Ky2VxkFpCu', 'Đặng Thị H', '0978901234', 'female', '2002-08-18', NULL, 'customer', 150, 1, TRUE, TRUE),
('buivani@gmail.com', '$2a$12$LJ3m4ys3Lk0TSwHjfJvOaOGPGP7RQHJ0YqpDdbxFi.Ky2VxkFpCu', 'Bùi Văn I', '0989012345', 'male', '1994-04-07', NULL, 'customer', 6200, 4, TRUE, TRUE),
('lythik@gmail.com', '$2a$12$LJ3m4ys3Lk0TSwHjfJvOaOGPGP7RQHJ0YqpDdbxFi.Ky2VxkFpCu', 'Lý Thị K', '0990123456', 'female', '2003-02-14', NULL, 'customer', 50, 1, TRUE, TRUE);

-- ========== ADDRESSES ==========
INSERT INTO addresses (user_id, recipient_name, phone, province, district, ward, street_address, is_default) VALUES
(2, 'Nguyễn Văn A', '0901234567', 'TP. Hồ Chí Minh', 'Quận 1', 'Phường Bến Nghé', '123 Nguyễn Huệ', TRUE),
(2, 'Nguyễn Văn A', '0901234567', 'TP. Hồ Chí Minh', 'Quận 7', 'Phường Tân Phong', '456 Nguyễn Thị Thập', FALSE),
(3, 'Trần Thị B', '0912345678', 'Hà Nội', 'Quận Hoàn Kiếm', 'Phường Hàng Bạc', '78 Hàng Đào', TRUE),
(4, 'Lê Văn C', '0923456789', 'Đà Nẵng', 'Quận Hải Châu', 'Phường Thanh Bình', '90 Trần Phú', TRUE),
(5, 'Phạm Thị D', '0934567890', 'TP. Hồ Chí Minh', 'Quận 3', 'Phường 6', '12 Võ Văn Tần', TRUE),
(6, 'Hoàng Văn E', '0945678901', 'Hà Nội', 'Quận Cầu Giấy', 'Phường Dịch Vọng', '34 Xuân Thủy', TRUE),
(7, 'Ngô Thị F', '0956789012', 'TP. Hồ Chí Minh', 'Quận Bình Thạnh', 'Phường 25', '56 Điện Biên Phủ', TRUE),
(8, 'Vũ Văn G', '0967890123', 'Hải Phòng', 'Quận Ngô Quyền', 'Phường Máy Chai', '78 Lạch Tray', TRUE),
(9, 'Đặng Thị H', '0978901234', 'Cần Thơ', 'Quận Ninh Kiều', 'Phường An Hòa', '90 Đường 3/2', TRUE),
(10, 'Bùi Văn I', '0989012345', 'TP. Hồ Chí Minh', 'Quận Gò Vấp', 'Phường 10', '12 Quang Trung', TRUE);

-- ========== CATEGORIES (3 cấp) ==========
-- Level 1
INSERT INTO categories (name, slug, parent_id, description, display_order, is_active) VALUES
('Nam', 'nam', NULL, 'Thời trang nam', 1, TRUE),
('Nữ', 'nu', NULL, 'Thời trang nữ', 2, TRUE),
('Phụ kiện', 'phu-kien', NULL, 'Phụ kiện thời trang', 3, TRUE);

-- Level 2
INSERT INTO categories (name, slug, parent_id, description, display_order, is_active) VALUES
('Áo Nam', 'ao-nam', 1, 'Các loại áo nam', 1, TRUE),
('Quần Nam', 'quan-nam', 1, 'Các loại quần nam', 2, TRUE),
('Áo Nữ', 'ao-nu', 2, 'Các loại áo nữ', 1, TRUE),
('Quần Nữ', 'quan-nu', 2, 'Các loại quần nữ', 2, TRUE),
('Váy & Đầm', 'vay-dam', 2, 'Váy và đầm nữ', 3, TRUE),
('Mũ & Nón', 'mu-non', 3, 'Mũ nón thời trang', 1, TRUE),
('Túi & Balo', 'tui-balo', 3, 'Túi xách và balo', 2, TRUE);

-- Level 3
INSERT INTO categories (name, slug, parent_id, description, display_order, is_active) VALUES
('Áo Polo Nam', 'ao-polo-nam', 4, 'Áo polo nam', 1, TRUE),
('Áo Thun Nam', 'ao-thun-nam', 4, 'Áo thun nam cổ tròn', 2, TRUE),
('Áo Sơ Mi Nam', 'ao-so-mi-nam', 4, 'Áo sơ mi nam', 3, TRUE),
('Quần Jeans Nam', 'quan-jeans-nam', 5, 'Quần jeans nam', 1, TRUE),
('Quần Kaki Nam', 'quan-kaki-nam', 5, 'Quần kaki nam', 2, TRUE),
('Áo Thun Nữ', 'ao-thun-nu', 6, 'Áo thun nữ', 1, TRUE),
('Áo Sơ Mi Nữ', 'ao-so-mi-nu', 6, 'Áo sơ mi nữ', 2, TRUE),
('Quần Jeans Nữ', 'quan-jeans-nu', 7, 'Quần jeans nữ', 1, TRUE),
('Đầm Liền', 'dam-lien', 8, 'Đầm liền thân', 1, TRUE);

-- ========== PRODUCTS (20 SP) ==========
INSERT INTO products (name, slug, description, material, care_instructions, category_id, base_price, sale_price, is_active, is_featured, total_sold, average_rating) VALUES

-- Áo Polo Nam (category: ao-polo-nam)
('Áo Polo Nam Classic Pique', 'ao-polo-nam-classic-pique',
 'Áo polo nam chất liệu Cotton Pique cao cấp, form regular fit thoải mái. Thiết kế cổ bẻ thanh lịch, phù hợp đi làm và dạo phố.',
 'Cotton Pique 100%', 'Giặt máy ở 30°C, không tẩy, ủi nhẹ',
 (SELECT id FROM categories WHERE slug = 'ao-polo-nam'), 350000, 299000, TRUE, TRUE, 156, 4.50),

('Áo Polo Nam Coolmax Sport', 'ao-polo-nam-coolmax-sport',
 'Áo polo thể thao công nghệ Coolmax, thấm hút mồ hôi nhanh, thoáng mát suốt ngày dài.',
 'Polyester Coolmax 92%, Spandex 8%', 'Giặt máy ở 30°C, không sấy',
 (SELECT id FROM categories WHERE slug = 'ao-polo-nam'), 420000, NULL, TRUE, FALSE, 89, 4.30),

('Áo Polo Nam Bamboo Eco', 'ao-polo-nam-bamboo-eco',
 'Áo polo nam sợi tre tự nhiên, kháng khuẩn, thân thiện môi trường.',
 'Bamboo Fiber 60%, Cotton 40%', 'Giặt máy ở 30°C, phơi trong bóng râm',
 (SELECT id FROM categories WHERE slug = 'ao-polo-nam'), 390000, NULL, TRUE, FALSE, 73, 4.10),

-- Áo Thun Nam (category: ao-thun-nam)
('Áo Thun Nam Basic Cổ Tròn', 'ao-thun-nam-basic-co-tron',
 'Áo thun nam basic cotton tự nhiên, form slim fit trẻ trung. Dễ phối đồ, phù hợp mọi dịp.',
 'Cotton Compact 95%, Spandex 5%', 'Giặt máy ở 30°C, phơi trong bóng râm',
 (SELECT id FROM categories WHERE slug = 'ao-thun-nam'), 199000, 169000, TRUE, TRUE, 324, 4.70),

('Áo Thun Nam Oversize Streetwear', 'ao-thun-nam-oversize-streetwear',
 'Áo thun oversize phong cách streetwear, in hình độc đáo. Cotton dày dặn 250GSM.',
 'Cotton 250GSM', 'Giặt tay hoặc máy nhẹ, lộn trái khi giặt',
 (SELECT id FROM categories WHERE slug = 'ao-thun-nam'), 280000, NULL, TRUE, FALSE, 201, 4.40),

('Áo Thun Nam Café DriS', 'ao-thun-nam-cafe-dris',
 'Áo thun nam công nghệ sợi cà phê S.Café, khử mùi tự nhiên.',
 'S.Café Yarn 50%, Cotton 50%', 'Giặt máy ở 30°C',
 (SELECT id FROM categories WHERE slug = 'ao-thun-nam'), 320000, 279000, TRUE, FALSE, 145, 4.50),

-- Áo Sơ Mi Nam (category: ao-so-mi-nam)
('Áo Sơ Mi Nam Oxford Trắng', 'ao-so-mi-nam-oxford-trang',
 'Áo sơ mi nam chất Oxford dày dặn, cổ button-down lịch lãm. Phù hợp công sở và sự kiện.',
 'Cotton Oxford 100%', 'Giặt máy ở 40°C, ủi ở nhiệt độ trung bình',
 (SELECT id FROM categories WHERE slug = 'ao-so-mi-nam'), 450000, 399000, TRUE, TRUE, 178, 4.60),

('Áo Sơ Mi Nam Linen Casual', 'ao-so-mi-nam-linen-casual',
 'Áo sơ mi nam chất linen tự nhiên, thoáng mát cho mùa hè. Kiểu dáng regular fit thoải mái.',
 'Linen 70%, Cotton 30%', 'Giặt tay nhẹ nhàng, phơi ngang',
 (SELECT id FROM categories WHERE slug = 'ao-so-mi-nam'), 520000, NULL, TRUE, FALSE, 67, 4.20),

-- Quần Jeans Nam (category: quan-jeans-nam)
('Quần Jeans Nam Slim Fit', 'quan-jeans-nam-slim-fit',
 'Quần jeans nam slim fit co giãn nhẹ, wash medium blue. Phom dáng chuẩn Hàn Quốc.',
 'Cotton 98%, Elastane 2%', 'Giặt máy lộn trái, không tẩy, phơi trong bóng râm',
 (SELECT id FROM categories WHERE slug = 'quan-jeans-nam'), 550000, 480000, TRUE, TRUE, 245, 4.50),

('Quần Jeans Nam Regular Đen', 'quan-jeans-nam-regular-den',
 'Quần jeans nam regular fit màu đen cơ bản, vải denim dày 12oz.',
 'Denim 100% Cotton 12oz', 'Giặt riêng lần đầu, giặt máy ở 30°C',
 (SELECT id FROM categories WHERE slug = 'quan-jeans-nam'), 490000, NULL, TRUE, FALSE, 134, 4.30),

-- Quần Kaki Nam + Short (category: quan-kaki-nam)
('Quần Kaki Nam Slim Beige', 'quan-kaki-nam-slim-beige',
 'Quần kaki nam slim fit màu be, chất kaki co giãn thoải mái. Phù hợp công sở smart casual.',
 'Cotton 97%, Spandex 3%', 'Giặt máy ở 30°C, ủi mặt trái',
 (SELECT id FROM categories WHERE slug = 'quan-kaki-nam'), 420000, 369000, TRUE, FALSE, 98, 4.40),

('Quần Kaki Nam Jogger', 'quan-kaki-nam-jogger',
 'Quần kaki jogger nam bo gấu thể thao, chất liệu co giãn 4 chiều.',
 'Nylon 85%, Spandex 15%', 'Giặt máy ở 30°C, không sấy',
 (SELECT id FROM categories WHERE slug = 'quan-kaki-nam'), 380000, NULL, TRUE, FALSE, 156, 4.20),

('Quần Short Nam Chino', 'quan-short-nam-chino',
 'Quần short nam chino thanh lịch, dài ngang gối. Phù hợp mùa hè.',
 'Cotton Twill 98%, Spandex 2%', 'Giặt máy ở 30°C',
 (SELECT id FROM categories WHERE slug = 'quan-kaki-nam'), 299000, 249000, TRUE, FALSE, 189, 4.30),

-- Áo Thun Nữ + Khoác (category: ao-thun-nu)
('Áo Thun Nữ Baby Tee', 'ao-thun-nu-baby-tee',
 'Áo thun nữ baby tee ôm form, chất cotton mềm mại. Hot trend 2026.',
 'Cotton Combed 100%', 'Giặt máy ở 30°C, phơi trong bóng râm',
 (SELECT id FROM categories WHERE slug = 'ao-thun-nu'), 180000, 149000, TRUE, TRUE, 412, 4.80),

('Áo Thun Nữ Crop Top', 'ao-thun-nu-crop-top',
 'Áo crop top nữ năng động, phối với quần cạp cao cực chuẩn.',
 'Cotton Modal 60%, Polyester 40%', 'Giặt máy nhẹ, không vắt',
 (SELECT id FROM categories WHERE slug = 'ao-thun-nu'), 220000, NULL, TRUE, FALSE, 287, 4.50),

('Áo Khoác Nữ Denim', 'ao-khoac-nu-denim',
 'Áo khoác jeans nữ classic, wash medium vintage. Oversize nhẹ thời trang.',
 'Denim Cotton 100%', 'Giặt máy lộn trái, phơi trong bóng râm',
 (SELECT id FROM categories WHERE slug = 'ao-thun-nu'), 680000, 599000, TRUE, TRUE, 78, 4.60),

-- Áo Sơ Mi Nữ (category: ao-so-mi-nu)
('Áo Sơ Mi Nữ Lụa Cổ V', 'ao-so-mi-nu-lua-co-v',
 'Áo sơ mi nữ chất lụa cao cấp, cổ V thanh lịch. Phù hợp công sở và dạo phố.',
 'Lụa tơ tằm pha 70%, Polyester 30%', 'Giặt tay nhẹ, ủi ở nhiệt độ thấp',
 (SELECT id FROM categories WHERE slug = 'ao-so-mi-nu'), 650000, 550000, TRUE, TRUE, 89, 4.60),

-- Quần Jeans Nữ (category: quan-jeans-nu)
('Quần Jeans Nữ Skinny', 'quan-jeans-nu-skinny',
 'Quần jeans nữ skinny fit co giãn cực tốt, tôn dáng. Wash xanh đậm cổ điển.',
 'Cotton 92%, Polyester 6%, Elastane 2%', 'Giặt lộn trái ở 30°C',
 (SELECT id FROM categories WHERE slug = 'quan-jeans-nu'), 480000, 420000, TRUE, FALSE, 198, 4.40),

-- Đầm Liền (category: dam-lien)
('Đầm Liền Hoa Nhí Vintage', 'dam-lien-hoa-nhi-vintage',
 'Đầm liền hoa nhí phong cách vintage, chất voan nhẹ nhàng nữ tính. Dáng xòe ngang gối.',
 'Voan Chiffon 100%', 'Giặt tay, phơi trong bóng râm',
 (SELECT id FROM categories WHERE slug = 'dam-lien'), 580000, 499000, TRUE, TRUE, 167, 4.70),

('Đầm Liền Công Sở', 'dam-lien-cong-so',
 'Đầm liền dáng suông thanh lịch, chất liệu cao cấp phù hợp văn phòng.',
 'Polyester 65%, Viscose 35%', 'Giặt máy nhẹ, ủi ở nhiệt độ trung bình',
 (SELECT id FROM categories WHERE slug = 'dam-lien'), 720000, NULL, TRUE, FALSE, 56, 4.30);
