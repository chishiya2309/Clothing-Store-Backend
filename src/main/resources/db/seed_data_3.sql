-- SEED DATA Part 3: Orders, Payments, Reviews, Wishlists, Blog, Cart
-- Run after seed_data_2.sql

-- ========== ORDERS (15 đơn hàng) ==========
INSERT INTO orders (user_id, order_code, shipping_name, shipping_phone, shipping_province, shipping_district, shipping_ward, shipping_address, subtotal, shipping_fee, discount_amount, total_amount, voucher_id, status, note) VALUES
((SELECT id FROM users WHERE email='nguyenvana@gmail.com'), 'DH20260501001', 'Nguyễn Văn A', '0901234567', 'TP. Hồ Chí Minh', 'Quận 1',         'Phường Bến Nghé',   '123 Nguyễn Huệ',        648000,  0,     50000,  598000, (SELECT id FROM vouchers WHERE code='SUMMER50K'), 'completed',   NULL),
((SELECT id FROM users WHERE email='tranthib@gmail.com'),   'DH20260502001', 'Trần Thị B',   '0912345678', 'Hà Nội',           'Quận Hoàn Kiếm', 'Phường Hàng Bạc',   '78 Hàng Đào',           299000,  30000, 29900,  299100, (SELECT id FROM vouchers WHERE code='WELCOME10'), 'completed',   'Giao giờ hành chính'),
((SELECT id FROM users WHERE email='nguyenvana@gmail.com'), 'DH20260505001', 'Nguyễn Văn A', '0901234567', 'TP. Hồ Chí Minh', 'Quận 7',         'Phường Tân Phong',  '456 Nguyễn Thị Thập',  960000,  0,     0,      960000, NULL,                                            'completed',   NULL),
((SELECT id FROM users WHERE email='phamthid@gmail.com'),   'DH20260507001', 'Phạm Thị D',   '0934567890', 'TP. Hồ Chí Minh', 'Quận 3',         'Phường 6',          '12 Võ Văn Tần',         1049000, 0,     0,      1049000,NULL,                                            'completed',   NULL),
((SELECT id FROM users WHERE email='hoangvane@gmail.com'),  'DH20260510001', 'Hoàng Văn E',  '0945678901', 'Hà Nội',           'Quận Cầu Giấy',  'Phường Dịch Vọng',  '34 Xuân Thủy',          338000,  30000, 0,      368000, NULL,                                            'completed',   NULL),
((SELECT id FROM users WHERE email='vuvang@gmail.com'),     'DH20260512001', 'Vũ Văn G',     '0967890123', 'Hải Phòng',        'Quận Ngô Quyền', 'Phường Máy Chai',   '78 Lạch Tray',          898000,  0,     50000,  848000, (SELECT id FROM vouchers WHERE code='SUMMER50K'), 'completed',   NULL),
((SELECT id FROM users WHERE email='tranthib@gmail.com'),   'DH20260515001', 'Trần Thị B',   '0912345678', 'Hà Nội',           'Quận Hoàn Kiếm', 'Phường Hàng Bạc',   '78 Hàng Đào',           1150000, 0,     200000, 950000, (SELECT id FROM vouchers WHERE code='VIP20'),     'completed',   NULL),
((SELECT id FROM users WHERE email='buivani@gmail.com'),    'DH20260518001', 'Bùi Văn I',    '0989012345', 'TP. Hồ Chí Minh', 'Quận Gò Vấp',    'Phường 10',         '12 Quang Trung',        420000,  30000, 0,      450000, NULL,                                            'shipping',    NULL),
((SELECT id FROM users WHERE email='levanc@gmail.com'),     'DH20260520001', 'Lê Văn C',     '0923456789', 'Đà Nẵng',          'Quận Hải Châu',  'Phường Thanh Bình', '90 Trần Phú',           558000,  0,     0,      558000, NULL,                                            'processing',  'Gói quà tặng'),
((SELECT id FROM users WHERE email='ngothif@gmail.com'),    'DH20260522001', 'Ngô Thị F',    '0956789012', 'TP. Hồ Chí Minh', 'Quận Bình Thạnh','Phường 25',         '56 Điện Biên Phủ',      199000,  30000, 0,      229000, NULL,                                            'pending',     NULL),
((SELECT id FROM users WHERE email='nguyenvana@gmail.com'), 'DH20260523001', 'Nguyễn Văn A', '0901234567', 'TP. Hồ Chí Minh', 'Quận 1',         'Phường Bến Nghé',   '123 Nguyễn Huệ',        780000,  0,     0,      780000, NULL,                                            'pending',     NULL),
((SELECT id FROM users WHERE email='dangthih@gmail.com'),   'DH20260510002', 'Đặng Thị H',   '0978901234', 'Cần Thơ',          'Quận Ninh Kiều', 'Phường An Hòa',     '90 Đường 3/2',          350000,  30000, 0,      380000, NULL,                                            'cancelled',   'Đổi ý'),
((SELECT id FROM users WHERE email='phamthid@gmail.com'),   'DH20260525001', 'Phạm Thị D',   '0934567890', 'TP. Hồ Chí Minh', 'Quận 3',         'Phường 6',          '12 Võ Văn Tần',         598000,  0,     50000,  548000, (SELECT id FROM vouchers WHERE code='SUMMER50K'), 'completed',   NULL),
((SELECT id FROM users WHERE email='vuvang@gmail.com'),     'DH20260527001', 'Vũ Văn G',     '0967890123', 'Hải Phòng',        'Quận Ngô Quyền', 'Phường Máy Chai',   '78 Lạch Tray',          169000,  30000, 0,      199000, NULL,                                            'completed',   NULL),
((SELECT id FROM users WHERE email='lythik@gmail.com'),     'DH20260528001', 'Lý Thị K',     '0990123456', 'TP. Hồ Chí Minh', 'Quận Tân Bình',  'Phường 15',         '100 Cộng Hòa',          499000,  30000, 49900,  479100, (SELECT id FROM vouchers WHERE code='WELCOME10'), 'completed',   NULL);

-- ========== ORDER ITEMS ==========
INSERT INTO order_items (order_id, product_variant_id, product_name, variant_info, quantity, unit_price, subtotal) VALUES
((SELECT id FROM orders WHERE order_code='DH20260501001'), (SELECT id FROM product_variants WHERE sku='POLO-CLS-M-WHT'),  'Áo Polo Nam Classic Pique',  'M / Trắng',          1, 299000, 299000),
((SELECT id FROM orders WHERE order_code='DH20260501001'), (SELECT id FROM product_variants WHERE sku='THUN-BSC-M-WHT'),  'Áo Thun Nam Basic Cổ Tròn',  'M / Trắng',          1, 169000, 169000),
((SELECT id FROM orders WHERE order_code='DH20260501001'), (SELECT id FROM product_variants WHERE sku='THUN-BSC-M-BLK'),  'Áo Thun Nam Basic Cổ Tròn',  'M / Đen',            1, 169000, 169000),
((SELECT id FROM orders WHERE order_code='DH20260502001'), (SELECT id FROM product_variants WHERE sku='POLO-CLS-S-WHT'),  'Áo Polo Nam Classic Pique',  'S / Trắng',          1, 299000, 299000),
((SELECT id FROM orders WHERE order_code='DH20260505001'), (SELECT id FROM product_variants WHERE sku='JEAN-SLM-30-BLU'), 'Quần Jeans Nam Slim Fit',    '30 / Xanh Medium',   2, 480000, 960000),
((SELECT id FROM orders WHERE order_code='DH20260507001'), (SELECT id FROM product_variants WHERE sku='SOMI-OXF-M-WHT'),  'Áo Sơ Mi Nam Oxford Trắng', 'M / Trắng',          1, 399000, 399000),
((SELECT id FROM orders WHERE order_code='DH20260507001'), (SELECT id FROM product_variants WHERE sku='SILK-VNK-M-CRM'),  'Áo Sơ Mi Nữ Lụa Cổ V',      'M / Kem',            1, 550000, 550000),
((SELECT id FROM orders WHERE order_code='DH20260510001'), (SELECT id FROM product_variants WHERE sku='THUN-BSC-M-WHT'),  'Áo Thun Nam Basic Cổ Tròn',  'M / Trắng',          2, 169000, 338000),
((SELECT id FROM orders WHERE order_code='DH20260512001'), (SELECT id FROM product_variants WHERE sku='SOMI-OXF-L-WHT'),  'Áo Sơ Mi Nam Oxford Trắng', 'L / Trắng',          1, 399000, 399000),
((SELECT id FROM orders WHERE order_code='DH20260512001'), (SELECT id FROM product_variants WHERE sku='DAM-HOA-M-FLR'),   'Đầm Liền Hoa Nhí Vintage',  'M / Hoa Nhí Xanh',   1, 499000, 499000),
((SELECT id FROM orders WHERE order_code='DH20260515001'), (SELECT id FROM product_variants WHERE sku='SILK-VNK-M-CRM'),  'Áo Sơ Mi Nữ Lụa Cổ V',      'M / Kem',            1, 550000, 550000),
((SELECT id FROM orders WHERE order_code='DH20260515001'), (SELECT id FROM product_variants WHERE sku='BABY-TEE-S-PNK'),  'Áo Thun Nữ Baby Tee',        'S / Hồng',           2, 149000, 298000),
((SELECT id FROM orders WHERE order_code='DH20260515001'), (SELECT id FROM product_variants WHERE sku='DAM-HOA-L-FLR'),   'Đầm Liền Hoa Nhí Vintage',  'L / Hoa Nhí Xanh',   1, 499000, 499000),
((SELECT id FROM orders WHERE order_code='DH20260518001'), (SELECT id FROM product_variants WHERE sku='POLO-CLX-M-GRY'),  'Áo Polo Nam Coolmax Sport',  'M / Xám',            1, 420000, 420000),
((SELECT id FROM orders WHERE order_code='DH20260520001'), (SELECT id FROM product_variants WHERE sku='JEAN-SLM-31-BLU'), 'Quần Jeans Nam Slim Fit',    '31 / Xanh Medium',   1, 480000, 480000),
((SELECT id FROM orders WHERE order_code='DH20260522001'), (SELECT id FROM product_variants WHERE sku='THUN-BSC-M-WHT'),  'Áo Thun Nam Basic Cổ Tròn',  'M / Trắng',          1, 169000, 169000),
((SELECT id FROM orders WHERE order_code='DH20260523001'), (SELECT id FROM product_variants WHERE sku='POLO-CLS-M-NVY'),  'Áo Polo Nam Classic Pique',  'M / Xanh Navy',      1, 299000, 299000),
((SELECT id FROM orders WHERE order_code='DH20260523001'), (SELECT id FROM product_variants WHERE sku='JEAN-SLM-30-BLU'), 'Quần Jeans Nam Slim Fit',    '30 / Xanh Medium',   1, 480000, 480000),
((SELECT id FROM orders WHERE order_code='DH20260525001'), (SELECT id FROM product_variants WHERE sku='POLO-CLS-S-WHT'),  'Áo Polo Nam Classic Pique',  'S / Trắng',          2, 299000, 598000),
((SELECT id FROM orders WHERE order_code='DH20260527001'), (SELECT id FROM product_variants WHERE sku='THUN-BSC-M-WHT'),  'Áo Thun Nam Basic Cổ Tròn',  'M / Trắng',          1, 169000, 169000),
((SELECT id FROM orders WHERE order_code='DH20260528001'), (SELECT id FROM product_variants WHERE sku='DAM-HOA-M-FLR'),   'Đầm Liền Hoa Nhí Vintage',  'M / Hoa Nhí Xanh',   1, 499000, 499000);

-- ========== PAYMENTS ==========
INSERT INTO payments (order_id, method, amount, status, transaction_id, paid_at) VALUES
((SELECT id FROM orders WHERE order_code='DH20260501001'), 'vnpay', 598000,  'completed', 'VNP20260501001234',  '2026-05-01 10:30:00+07'),
((SELECT id FROM orders WHERE order_code='DH20260502001'), 'cod',   299100,  'completed', NULL,                 '2026-05-04 14:00:00+07'),
((SELECT id FROM orders WHERE order_code='DH20260505001'), 'momo',  960000,  'completed', 'MOMO20260505005678', '2026-05-05 09:15:00+07'),
((SELECT id FROM orders WHERE order_code='DH20260507001'), 'vnpay', 1049000, 'completed', 'VNP20260507009012',  '2026-05-07 16:45:00+07'),
((SELECT id FROM orders WHERE order_code='DH20260510001'), 'cod',   368000,  'completed', NULL,                 '2026-05-12 11:00:00+07'),
((SELECT id FROM orders WHERE order_code='DH20260512001'), 'vnpay', 848000,  'completed', 'VNP20260512003456',  '2026-05-12 20:30:00+07'),
((SELECT id FROM orders WHERE order_code='DH20260515001'), 'momo',  950000,  'completed', 'MOMO20260515007890', '2026-05-15 13:00:00+07'),
((SELECT id FROM orders WHERE order_code='DH20260518001'), 'vnpay', 450000,  'completed', 'VNP20260518001111',  '2026-05-18 08:00:00+07'),
((SELECT id FROM orders WHERE order_code='DH20260520001'), 'vnpay', 558000,  'pending',   NULL,                 NULL),
((SELECT id FROM orders WHERE order_code='DH20260522001'), 'cod',   229000,  'pending',   NULL,                 NULL),
((SELECT id FROM orders WHERE order_code='DH20260523001'), 'momo',  780000,  'pending',   NULL,                 NULL),
((SELECT id FROM orders WHERE order_code='DH20260510002'), 'vnpay', 380000,  'failed',    NULL,                 NULL),
((SELECT id FROM orders WHERE order_code='DH20260525001'), 'vnpay', 548000,  'completed', 'VNP20260525002222',  '2026-05-25 15:30:00+07'),
((SELECT id FROM orders WHERE order_code='DH20260527001'), 'cod',   199000,  'completed', NULL,                 '2026-05-29 10:00:00+07'),
((SELECT id FROM orders WHERE order_code='DH20260528001'), 'vnpay', 479100,  'completed', 'VNP20260528003333',  '2026-05-28 17:00:00+07');

-- ========== REVIEWS ==========
INSERT INTO reviews (user_id, product_id, order_id, rating, content, is_approved, admin_reply, replied_at) VALUES
((SELECT id FROM users WHERE email='nguyenvana@gmail.com'), (SELECT id FROM products WHERE slug='ao-polo-nam-classic-pique'), (SELECT id FROM orders WHERE order_code='DH20260501001'), 5, 'Áo polo rất đẹp, chất vải mềm mại và thoáng mát. Form áo chuẩn, mặc rất thoải mái. Sẽ mua thêm màu khác.', TRUE, 'Cảm ơn bạn đã ủng hộ! Chúc bạn mua sắm vui vẻ ạ.', '2026-05-03 09:00:00+07'),
((SELECT id FROM users WHERE email='nguyenvana@gmail.com'), (SELECT id FROM products WHERE slug='ao-thun-nam-basic-co-tron'),  (SELECT id FROM orders WHERE order_code='DH20260501001'), 5, 'Áo thun basic nhưng chất lượng cao, cotton dày dặn không bị xù sau vài lần giặt. Giá hợp lý.', TRUE, NULL, NULL),
((SELECT id FROM users WHERE email='tranthib@gmail.com'),   (SELECT id FROM products WHERE slug='ao-polo-nam-classic-pique'), (SELECT id FROM orders WHERE order_code='DH20260502001'), 4, 'Áo đẹp nhưng hơi rộng so với size chart. Nên đặt nhỏ hơn 1 size. Chất vải OK.', TRUE, 'Cảm ơn feedback! Mình sẽ cập nhật size chart chi tiết hơn ạ.', '2026-05-06 10:00:00+07'),
((SELECT id FROM users WHERE email='nguyenvana@gmail.com'), (SELECT id FROM products WHERE slug='quan-jeans-nam-slim-fit'),   (SELECT id FROM orders WHERE order_code='DH20260505001'), 5, 'Quần jeans co giãn tốt, mặc rất thoải mái. Wash đẹp, đường may chắc chắn.', TRUE, NULL, NULL),
((SELECT id FROM users WHERE email='phamthid@gmail.com'),   (SELECT id FROM products WHERE slug='ao-so-mi-nam-oxford-trang'), (SELECT id FROM orders WHERE order_code='DH20260507001'), 4, 'Sơ mi Oxford chất lượng tốt, tuy nhiên cần ủi kỹ trước khi mặc. Form chuẩn công sở.', TRUE, NULL, NULL),
((SELECT id FROM users WHERE email='phamthid@gmail.com'),   (SELECT id FROM products WHERE slug='ao-so-mi-nu-lua-co-v'),      (SELECT id FROM orders WHERE order_code='DH20260507001'), 5, 'Áo lụa rất mềm mịn, mặc lên sang trọng. Đóng gói cẩn thận. Rất hài lòng!', TRUE, 'Cảm ơn chị! Bên mình còn nhiều mẫu lụa mới nhé.', '2026-05-10 14:00:00+07'),
((SELECT id FROM users WHERE email='hoangvane@gmail.com'),  (SELECT id FROM products WHERE slug='ao-thun-nam-basic-co-tron'),  (SELECT id FROM orders WHERE order_code='DH20260510001'), 4, 'Chất cotton mát, nhưng sau 3 lần giặt hơi co lại một chút. Nên mua size lớn hơn.', TRUE, NULL, NULL),
((SELECT id FROM users WHERE email='vuvang@gmail.com'),     (SELECT id FROM products WHERE slug='ao-so-mi-nam-oxford-trang'), (SELECT id FROM orders WHERE order_code='DH20260512001'), 5, 'Oxford trắng chuẩn men, mặc đi làm rất ổn. Sẽ mua thêm màu xanh.', TRUE, NULL, NULL),
((SELECT id FROM users WHERE email='vuvang@gmail.com'),     (SELECT id FROM products WHERE slug='dam-lien-hoa-nhi-vintage'),  (SELECT id FROM orders WHERE order_code='DH20260512001'), 4, 'Đầm hoa nhí rất xinh, đúng như hình. Chỉ hơi mỏng nên cần mặc lót bên trong.', TRUE, NULL, NULL),
((SELECT id FROM users WHERE email='tranthib@gmail.com'),   (SELECT id FROM products WHERE slug='ao-so-mi-nu-lua-co-v'),      (SELECT id FROM orders WHERE order_code='DH20260515001'), 5, 'Mua lần 2 rồi, chất lụa mặc lên rất sang. Ship nhanh, đóng gói đẹp.', TRUE, NULL, NULL),
((SELECT id FROM users WHERE email='tranthib@gmail.com'),   (SELECT id FROM products WHERE slug='ao-thun-nu-baby-tee'),       (SELECT id FROM orders WHERE order_code='DH20260515001'), 5, 'Baby tee hot trend! Mặc ôm vừa vặn, chất cotton mềm. Mua 2 cái luôn.', TRUE, NULL, NULL),
((SELECT id FROM users WHERE email='phamthid@gmail.com'),   (SELECT id FROM products WHERE slug='ao-polo-nam-classic-pique'), (SELECT id FROM orders WHERE order_code='DH20260525001'), 4, 'Polo classic đẹp, nhưng phần cổ hơi cứng mới mua. Giặt vài lần sẽ mềm hơn.', FALSE, NULL, NULL),
((SELECT id FROM users WHERE email='vuvang@gmail.com'),     (SELECT id FROM products WHERE slug='ao-thun-nam-basic-co-tron'),  (SELECT id FROM orders WHERE order_code='DH20260527001'), 5, 'Áo thun basic tốt nhất từng mua. Cotton dày mà vẫn thoáng. Sẽ quay lại mua.', TRUE, NULL, NULL);

-- ========== REVIEW IMAGES ==========
-- Dùng subquery kết hợp user + product để định danh review chính xác
INSERT INTO review_images (review_id, image_url, display_order) VALUES
((SELECT r.id FROM reviews r JOIN users u ON r.user_id=u.id JOIN products p ON r.product_id=p.id WHERE u.email='nguyenvana@gmail.com' AND p.slug='ao-polo-nam-classic-pique'), 'https://placehold.co/400x400/navy/white?text=Review+1a', 0),
((SELECT r.id FROM reviews r JOIN users u ON r.user_id=u.id JOIN products p ON r.product_id=p.id WHERE u.email='nguyenvana@gmail.com' AND p.slug='ao-polo-nam-classic-pique'), 'https://placehold.co/400x400/navy/white?text=Review+1b', 1),
((SELECT r.id FROM reviews r JOIN users u ON r.user_id=u.id JOIN products p ON r.product_id=p.id WHERE u.email='nguyenvana@gmail.com' AND p.slug='quan-jeans-nam-slim-fit'),   'https://placehold.co/400x400/4a6fa5/fff?text=Review+Jeans', 0),
((SELECT r.id FROM reviews r JOIN users u ON r.user_id=u.id JOIN products p ON r.product_id=p.id WHERE u.email='phamthid@gmail.com'   AND p.slug='ao-so-mi-nu-lua-co-v'),      'https://placehold.co/400x400/faebd7/333?text=Review+Silk', 0),
((SELECT r.id FROM reviews r JOIN users u ON r.user_id=u.id JOIN products p ON r.product_id=p.id WHERE u.email='vuvang@gmail.com'     AND p.slug='dam-lien-hoa-nhi-vintage'),  'https://placehold.co/400x400/e8f5e9/333?text=Review+Dress', 0);

-- ========== WISHLISTS ==========
INSERT INTO wishlists (user_id, product_id) VALUES
((SELECT id FROM users WHERE email='nguyenvana@gmail.com'), (SELECT id FROM products WHERE slug='ao-so-mi-nam-oxford-trang')),
((SELECT id FROM users WHERE email='nguyenvana@gmail.com'), (SELECT id FROM products WHERE slug='ao-so-mi-nu-lua-co-v')),
((SELECT id FROM users WHERE email='nguyenvana@gmail.com'), (SELECT id FROM products WHERE slug='dam-lien-hoa-nhi-vintage')),
((SELECT id FROM users WHERE email='tranthib@gmail.com'),   (SELECT id FROM products WHERE slug='quan-jeans-nam-slim-fit')),
((SELECT id FROM users WHERE email='tranthib@gmail.com'),   (SELECT id FROM products WHERE slug='ao-khoac-nu-denim')),
((SELECT id FROM users WHERE email='phamthid@gmail.com'),   (SELECT id FROM products WHERE slug='ao-thun-nu-baby-tee')),
((SELECT id FROM users WHERE email='phamthid@gmail.com'),   (SELECT id FROM products WHERE slug='dam-lien-hoa-nhi-vintage')),
((SELECT id FROM users WHERE email='ngothif@gmail.com'),    (SELECT id FROM products WHERE slug='ao-polo-nam-classic-pique')),
((SELECT id FROM users WHERE email='ngothif@gmail.com'),    (SELECT id FROM products WHERE slug='ao-thun-nam-basic-co-tron')),
((SELECT id FROM users WHERE email='ngothif@gmail.com'),    (SELECT id FROM products WHERE slug='quan-jeans-nam-slim-fit')),
((SELECT id FROM users WHERE email='vuvang@gmail.com'),     (SELECT id FROM products WHERE slug='ao-thun-nu-baby-tee')),
((SELECT id FROM users WHERE email='vuvang@gmail.com'),     (SELECT id FROM products WHERE slug='ao-so-mi-nu-lua-co-v')),
((SELECT id FROM users WHERE email='dangthih@gmail.com'),   (SELECT id FROM products WHERE slug='ao-so-mi-nam-oxford-trang')),
((SELECT id FROM users WHERE email='dangthih@gmail.com'),   (SELECT id FROM products WHERE slug='ao-so-mi-nam-linen-casual')),
((SELECT id FROM users WHERE email='lythik@gmail.com'),     (SELECT id FROM products WHERE slug='ao-thun-nu-baby-tee')),
((SELECT id FROM users WHERE email='lythik@gmail.com'),     (SELECT id FROM products WHERE slug='dam-lien-hoa-nhi-vintage')),
((SELECT id FROM users WHERE email='lythik@gmail.com'),     (SELECT id FROM products WHERE slug='ao-khoac-nu-denim'));

-- ========== CART ITEMS ==========
INSERT INTO cart_items (user_id, product_variant_id, quantity) VALUES
((SELECT id FROM users WHERE email='ngothif@gmail.com'),  (SELECT id FROM product_variants WHERE sku='BABY-TEE-S-PNK'), 1),
((SELECT id FROM users WHERE email='ngothif@gmail.com'),  (SELECT id FROM product_variants WHERE sku='DAM-HOA-M-FLR'),  1),
((SELECT id FROM users WHERE email='dangthih@gmail.com'), (SELECT id FROM product_variants WHERE sku='POLO-CLS-L-WHT'), 2),
((SELECT id FROM users WHERE email='dangthih@gmail.com'), (SELECT id FROM product_variants WHERE sku='SOMI-OXF-L-WHT'), 1),
((SELECT id FROM users WHERE email='lythik@gmail.com'),   (SELECT id FROM product_variants WHERE sku='BABY-TEE-S-WHT'), 1),
((SELECT id FROM users WHERE email='lythik@gmail.com'),   (SELECT id FROM product_variants WHERE sku='SILK-VNK-S-CRM'), 1);

-- ========== BLOG POSTS ==========
INSERT INTO blog_posts (title, slug, content, excerpt, thumbnail_url, category_id, author_id, is_published, published_at) VALUES

('5 Cách Phối Áo Polo Nam Đẹp Mắt Nhất 2026', '5-cach-phoi-ao-polo-nam-2026',
 'Áo polo là item không thể thiếu trong tủ đồ của các chàng trai. Dưới đây là 5 cách phối đồ với áo polo giúp bạn trông phong cách và lịch lãm hơn...',
 'Khám phá 5 cách phối áo polo nam trendy nhất năm 2026',
 'https://placehold.co/800x400/00bcd4/fff?text=Polo+Style',
 (SELECT id FROM blog_categories WHERE slug = 'phong-cach-xu-huong'),
 (SELECT id FROM users WHERE role = 'admin' LIMIT 1),
 TRUE, '2026-05-10 08:00:00+07'),

('Xu Hướng Thời Trang Hè 2026: Những Gì Đang Hot?', 'xu-huong-thoi-trang-he-2026',
 'Mùa hè 2026 đến với nhiều xu hướng mới thú vị. Từ baby tee, oversized silhouettes đến sustainable fashion...',
 'Cập nhật xu hướng thời trang mùa hè mới nhất',
 'https://placehold.co/800x400/ff5722/fff?text=Summer+Trends',
 (SELECT id FROM blog_categories WHERE slug = 'phong-cach-xu-huong'),
 (SELECT id FROM users WHERE role = 'admin' LIMIT 1),
 TRUE, '2026-05-15 10:00:00+07'),

('Hướng Dẫn Chọn Size Quần Jeans Chuẩn Không Cần Chỉnh', 'huong-dan-chon-size-quan-jeans',
 'Chọn sai size quần jeans là nỗi đau chung của nhiều người. Bài viết này sẽ hướng dẫn bạn cách đo và chọn size jeans phù hợp nhất...',
 'Mẹo chọn size jeans chuẩn cho cả nam và nữ',
 'https://placehold.co/800x400/3f51b5/fff?text=Jeans+Guide',
 (SELECT id FROM blog_categories WHERE slug = 'huong-dan-meo-hay'),
 (SELECT id FROM users WHERE role = 'admin' LIMIT 1),
 TRUE, '2026-05-20 09:00:00+07'),

('Giảm Giá Lên Đến 30% - Flash Sale Tháng 6', 'flash-sale-thang-6-2026',
 'Chào đón mùa hè, shop giảm giá lên đến 30% tất cả sản phẩm. Nhanh tay săn deal ngay!',
 'Flash sale tháng 6 giảm đến 30%',
 'https://placehold.co/800x400/f44336/fff?text=Flash+Sale',
 (SELECT id FROM blog_categories WHERE slug = 'khuyen-mai-su-kien'),
 (SELECT id FROM users WHERE role = 'admin' LIMIT 1),
 TRUE, '2026-05-28 08:00:00+07'),

('Chất Liệu Vải Nào Phù Hợp Cho Mùa Hè Việt Nam?', 'chat-lieu-vai-mua-he-viet-nam',
 'Với khí hậu nóng ẩm Việt Nam, việc chọn chất liệu vải phù hợp rất quan trọng...',
 'Tìm hiểu các loại vải thoáng mát cho mùa hè',
 'https://placehold.co/800x400/4caf50/fff?text=Fabric+Guide',
 (SELECT id FROM blog_categories WHERE slug = 'huong-dan-meo-hay'),
 (SELECT id FROM users WHERE role = 'admin' LIMIT 1),
 TRUE, '2026-05-25 10:00:00+07');

-- ========== BLOG TAGS ==========
INSERT INTO blog_tags (name, slug) VALUES
('áo polo', 'ao-polo'), ('mùa hè', 'mua-he'),   ('phối đồ', 'phoi-do'),
('xu hướng', 'xu-huong'),('quần jeans', 'quan-jeans'), ('khuyến mãi', 'khuyen-mai'),
('chất liệu', 'chat-lieu'),('streetwear', 'streetwear'),('công sở', 'cong-so');

-- ========== BLOG POST TAGS ==========
INSERT INTO blog_post_tags (blog_post_id, blog_tag_id) VALUES
((SELECT id FROM blog_posts WHERE slug='5-cach-phoi-ao-polo-nam-2026'),      (SELECT id FROM blog_tags WHERE slug='ao-polo')),
((SELECT id FROM blog_posts WHERE slug='5-cach-phoi-ao-polo-nam-2026'),      (SELECT id FROM blog_tags WHERE slug='phoi-do')),
((SELECT id FROM blog_posts WHERE slug='5-cach-phoi-ao-polo-nam-2026'),      (SELECT id FROM blog_tags WHERE slug='cong-so')),
((SELECT id FROM blog_posts WHERE slug='xu-huong-thoi-trang-he-2026'),       (SELECT id FROM blog_tags WHERE slug='mua-he')),
((SELECT id FROM blog_posts WHERE slug='xu-huong-thoi-trang-he-2026'),       (SELECT id FROM blog_tags WHERE slug='xu-huong')),
((SELECT id FROM blog_posts WHERE slug='xu-huong-thoi-trang-he-2026'),       (SELECT id FROM blog_tags WHERE slug='streetwear')),
((SELECT id FROM blog_posts WHERE slug='huong-dan-chon-size-quan-jeans'),    (SELECT id FROM blog_tags WHERE slug='quan-jeans')),
((SELECT id FROM blog_posts WHERE slug='huong-dan-chon-size-quan-jeans'),    (SELECT id FROM blog_tags WHERE slug='phoi-do')),
((SELECT id FROM blog_posts WHERE slug='flash-sale-thang-6-2026'),           (SELECT id FROM blog_tags WHERE slug='khuyen-mai')),
((SELECT id FROM blog_posts WHERE slug='flash-sale-thang-6-2026'),           (SELECT id FROM blog_tags WHERE slug='mua-he')),
((SELECT id FROM blog_posts WHERE slug='chat-lieu-vai-mua-he-viet-nam'),     (SELECT id FROM blog_tags WHERE slug='chat-lieu')),
((SELECT id FROM blog_posts WHERE slug='chat-lieu-vai-mua-he-viet-nam'),     (SELECT id FROM blog_tags WHERE slug='mua-he'));

-- ========== ACTIVITY LOGS ==========
INSERT INTO activity_logs (user_id, action, entity_type, entity_id, ip_address, user_agent, created_at) VALUES
((SELECT id FROM users WHERE role='admin' LIMIT 1),         'login',          'user',    (SELECT id FROM users WHERE role='admin' LIMIT 1),          '192.168.1.1', 'Mozilla/5.0 Chrome/126', '2026-05-01 08:00:00+07'),
((SELECT id FROM users WHERE role='admin' LIMIT 1),         'create_product', 'product', (SELECT id FROM products WHERE slug='ao-polo-nam-classic-pique'), '192.168.1.1', 'Mozilla/5.0 Chrome/126', '2026-05-01 09:00:00+07'),
((SELECT id FROM users WHERE email='nguyenvana@gmail.com'), 'login',          'user',    (SELECT id FROM users WHERE email='nguyenvana@gmail.com'), '103.1.2.3',   'Mozilla/5.0 Chrome/126', '2026-05-01 10:00:00+07'),
((SELECT id FROM users WHERE email='nguyenvana@gmail.com'), 'create_order',   'order',   (SELECT id FROM orders WHERE order_code='DH20260501001'),   '103.1.2.3',   'Mozilla/5.0 Chrome/126', '2026-05-01 10:15:00+07'),
((SELECT id FROM users WHERE role='admin' LIMIT 1),         'update_order',   'order',   (SELECT id FROM orders WHERE order_code='DH20260501001'),   '192.168.1.1', 'Mozilla/5.0 Chrome/126', '2026-05-01 11:00:00+07'),
((SELECT id FROM users WHERE email='tranthib@gmail.com'),   'login',          'user',    (SELECT id FROM users WHERE email='tranthib@gmail.com'),    '113.5.6.7',   'Mozilla/5.0 Safari/17',  '2026-05-02 14:00:00+07'),
((SELECT id FROM users WHERE email='tranthib@gmail.com'),   'create_order',   'order',   (SELECT id FROM orders WHERE order_code='DH20260502001'),   '113.5.6.7',   'Mozilla/5.0 Safari/17',  '2026-05-02 14:30:00+07'),
((SELECT id FROM users WHERE role='admin' LIMIT 1),         'approve_review', 'review',  (SELECT r.id FROM reviews r JOIN users u ON r.user_id=u.id JOIN products p ON r.product_id=p.id WHERE u.email='nguyenvana@gmail.com' AND p.slug='ao-polo-nam-classic-pique'), '192.168.1.1', 'Mozilla/5.0 Chrome/126', '2026-05-03 09:00:00+07'),
((SELECT id FROM users WHERE email='phamthid@gmail.com'),   'login',          'user',    (SELECT id FROM users WHERE email='phamthid@gmail.com'),    '42.10.11.12', 'Mozilla/5.0 Chrome/126', '2026-05-07 16:00:00+07'),
((SELECT id FROM users WHERE role='admin' LIMIT 1),         'create_voucher', 'voucher', (SELECT id FROM vouchers WHERE code='FLASH30'),             '192.168.1.1', 'Mozilla/5.0 Chrome/126', '2026-05-28 08:00:00+07');