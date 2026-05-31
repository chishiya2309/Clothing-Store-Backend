-- SEED DATA Part 3: Orders, Payments, Reviews, Wishlists, Blog, Cart
-- Run after seed_data_2.sql

-- ========== ORDERS (15 đơn hàng) ==========
INSERT INTO orders (user_id, order_code, shipping_name, shipping_phone, shipping_province, shipping_district, shipping_ward, shipping_address, subtotal, shipping_fee, discount_amount, total_amount, voucher_id, status, note) VALUES
(2, 'DH20260501001', 'Nguyễn Văn A', '0901234567', 'TP. Hồ Chí Minh', 'Quận 1', 'Phường Bến Nghé', '123 Nguyễn Huệ', 648000, 0, 50000, 598000, 2, 'completed', NULL),
(3, 'DH20260502001', 'Trần Thị B', '0912345678', 'Hà Nội', 'Quận Hoàn Kiếm', 'Phường Hàng Bạc', '78 Hàng Đào', 299000, 30000, 29900, 299100, 1, 'completed', 'Giao giờ hành chính'),
(2, 'DH20260505001', 'Nguyễn Văn A', '0901234567', 'TP. Hồ Chí Minh', 'Quận 7', 'Phường Tân Phong', '456 Nguyễn Thị Thập', 960000, 0, 0, 960000, NULL, 'completed', NULL),
(5, 'DH20260507001', 'Phạm Thị D', '0934567890', 'TP. Hồ Chí Minh', 'Quận 3', 'Phường 6', '12 Võ Văn Tần', 1049000, 0, 0, 1049000, NULL, 'completed', NULL),
(6, 'DH20260510001', 'Hoàng Văn E', '0945678901', 'Hà Nội', 'Quận Cầu Giấy', 'Phường Dịch Vọng', '34 Xuân Thủy', 338000, 30000, 0, 368000, NULL, 'completed', NULL),
(8, 'DH20260512001', 'Vũ Văn G', '0967890123', 'Hải Phòng', 'Quận Ngô Quyền', 'Phường Máy Chai', '78 Lạch Tray', 898000, 0, 50000, 848000, 2, 'completed', NULL),
(3, 'DH20260515001', 'Trần Thị B', '0912345678', 'Hà Nội', 'Quận Hoàn Kiếm', 'Phường Hàng Bạc', '78 Hàng Đào', 1150000, 0, 200000, 950000, 4, 'completed', NULL),
(10, 'DH20260518001', 'Bùi Văn I', '0989012345', 'TP. Hồ Chí Minh', 'Quận Gò Vấp', 'Phường 10', '12 Quang Trung', 420000, 30000, 0, 450000, NULL, 'shipping', NULL),
(4, 'DH20260520001', 'Lê Văn C', '0923456789', 'Đà Nẵng', 'Quận Hải Châu', 'Phường Thanh Bình', '90 Trần Phú', 558000, 0, 0, 558000, NULL, 'processing', 'Gói quà tặng'),
(7, 'DH20260522001', 'Ngô Thị F', '0956789012', 'TP. Hồ Chí Minh', 'Quận Bình Thạnh', 'Phường 25', '56 Điện Biên Phủ', 199000, 30000, 0, 229000, NULL, 'pending', NULL),
(2, 'DH20260523001', 'Nguyễn Văn A', '0901234567', 'TP. Hồ Chí Minh', 'Quận 1', 'Phường Bến Nghé', '123 Nguyễn Huệ', 780000, 0, 0, 780000, NULL, 'pending', NULL),
(9, 'DH20260510002', 'Đặng Thị H', '0978901234', 'Cần Thơ', 'Quận Ninh Kiều', 'Phường An Hòa', '90 Đường 3/2', 350000, 30000, 0, 380000, NULL, 'cancelled', 'Đổi ý'),
(5, 'DH20260525001', 'Phạm Thị D', '0934567890', 'TP. Hồ Chí Minh', 'Quận 3', 'Phường 6', '12 Võ Văn Tần', 598000, 0, 50000, 548000, 2, 'completed', NULL),
(8, 'DH20260527001', 'Vũ Văn G', '0967890123', 'Hải Phòng', 'Quận Ngô Quyền', 'Phường Máy Chai', '78 Lạch Tray', 169000, 30000, 0, 199000, NULL, 'completed', NULL),
(11, 'DH20260528001', 'Lý Thị K', '0990123456', 'TP. Hồ Chí Minh', 'Quận Tân Bình', 'Phường 15', '100 Cộng Hòa', 499000, 30000, 49900, 479100, 1, 'completed', NULL);

-- ========== ORDER ITEMS ==========
INSERT INTO order_items (order_id, product_variant_id, product_name, variant_info, quantity, unit_price, subtotal) VALUES
(1, 2, 'Áo Polo Nam Classic Pique', 'M / Trắng', 1, 299000, 299000),
(1, 14, 'Áo Thun Nam Basic Cổ Tròn', 'M / Trắng', 1, 169000, 169000),
(1, 16, 'Áo Thun Nam Basic Cổ Tròn', 'M / Đen', 1, 169000, 169000),
(2, 1, 'Áo Polo Nam Classic Pique', 'S / Trắng', 1, 299000, 299000),
(3, 29, 'Quần Jeans Nam Slim Fit', '30 / Xanh Medium', 2, 480000, 960000),
(4, 23, 'Áo Sơ Mi Nam Oxford Trắng', 'M / Trắng', 1, 399000, 399000),
(4, 39, 'Áo Sơ Mi Nữ Lụa Cổ V', 'M / Kem', 1, 550000, 550000),
(5, 14, 'Áo Thun Nam Basic Cổ Tròn', 'M / Trắng', 2, 169000, 338000),
(6, 24, 'Áo Sơ Mi Nam Oxford Trắng', 'L / Trắng', 1, 399000, 399000),
(6, 42, 'Đầm Liền Hoa Nhí Vintage', 'M / Hoa Nhí Xanh', 1, 499000, 499000),
(7, 39, 'Áo Sơ Mi Nữ Lụa Cổ V', 'M / Kem', 1, 550000, 550000),
(7, 33, 'Áo Thun Nữ Baby Tee', 'S / Hồng', 2, 149000, 298000),
(7, 43, 'Đầm Liền Hoa Nhí Vintage', 'L / Hoa Nhí Xanh', 1, 499000, 499000),
(8, 9, 'Áo Polo Nam Coolmax Sport', 'M / Xám', 1, 420000, 420000),
(9, 30, 'Quần Jeans Nam Slim Fit', '31 / Xanh Medium', 1, 480000, 480000),
(10, 14, 'Áo Thun Nam Basic Cổ Tròn', 'M / Trắng', 1, 169000, 169000),
(11, 5, 'Áo Polo Nam Classic Pique', 'M / Xanh Navy', 1, 299000, 299000),
(11, 29, 'Quần Jeans Nam Slim Fit', '30 / Xanh Medium', 1, 480000, 480000),
(13, 1, 'Áo Polo Nam Classic Pique', 'S / Trắng', 2, 299000, 598000),
(14, 14, 'Áo Thun Nam Basic Cổ Tròn', 'M / Trắng', 1, 169000, 169000),
(15, 42, 'Đầm Liền Hoa Nhí Vintage', 'M / Hoa Nhí Xanh', 1, 499000, 499000);

-- ========== PAYMENTS ==========
INSERT INTO payments (order_id, method, amount, status, transaction_id, paid_at) VALUES
(1, 'vnpay', 598000, 'completed', 'VNP20260501001234', '2026-05-01 10:30:00+07'),
(2, 'cod', 299100, 'completed', NULL, '2026-05-04 14:00:00+07'),
(3, 'momo', 960000, 'completed', 'MOMO20260505005678', '2026-05-05 09:15:00+07'),
(4, 'vnpay', 1049000, 'completed', 'VNP20260507009012', '2026-05-07 16:45:00+07'),
(5, 'cod', 368000, 'completed', NULL, '2026-05-12 11:00:00+07'),
(6, 'vnpay', 848000, 'completed', 'VNP20260512003456', '2026-05-12 20:30:00+07'),
(7, 'momo', 950000, 'completed', 'MOMO20260515007890', '2026-05-15 13:00:00+07'),
(8, 'vnpay', 450000, 'completed', 'VNP20260518001111', '2026-05-18 08:00:00+07'),
(9, 'vnpay', 558000, 'pending', NULL, NULL),
(10, 'cod', 229000, 'pending', NULL, NULL),
(11, 'momo', 780000, 'pending', NULL, NULL),
(12, 'vnpay', 380000, 'failed', NULL, NULL),
(13, 'vnpay', 548000, 'completed', 'VNP20260525002222', '2026-05-25 15:30:00+07'),
(14, 'cod', 199000, 'completed', NULL, '2026-05-29 10:00:00+07'),
(15, 'vnpay', 479100, 'completed', 'VNP20260528003333', '2026-05-28 17:00:00+07');

-- ========== REVIEWS ==========
INSERT INTO reviews (user_id, product_id, order_id, rating, content, is_approved, admin_reply, replied_at) VALUES
(2, 1, 1, 5, 'Áo polo rất đẹp, chất vải mềm mại và thoáng mát. Form áo chuẩn, mặc rất thoải mái. Sẽ mua thêm màu khác.', TRUE, 'Cảm ơn bạn đã ủng hộ! Chúc bạn mua sắm vui vẻ ạ.', '2026-05-03 09:00:00+07'),
(2, 3, 1, 5, 'Áo thun basic nhưng chất lượng cao, cotton dày dặn không bị xù sau vài lần giặt. Giá hợp lý.', TRUE, NULL, NULL),
(3, 1, 2, 4, 'Áo đẹp nhưng hơi rộng so với size chart. Nên đặt nhỏ hơn 1 size. Chất vải OK.', TRUE, 'Cảm ơn feedback! Mình sẽ cập nhật size chart chi tiết hơn ạ.', '2026-05-06 10:00:00+07'),
(2, 7, 3, 5, 'Quần jeans co giãn tốt, mặc rất thoải mái. Wash đẹp, đường may chắc chắn.', TRUE, NULL, NULL),
(5, 5, 4, 4, 'Sơ mi Oxford chất lượng tốt, tuy nhiên cần ủi kỹ trước khi mặc. Form chuẩn công sở.', TRUE, NULL, NULL),
(5, 13, 4, 5, 'Áo lụa rất mềm mịn, mặc lên sang trọng. Đóng gói cẩn thận. Rất hài lòng!', TRUE, 'Cảm ơn chị! Bên mình còn nhiều mẫu lụa mới nhé.', '2026-05-10 14:00:00+07'),
(6, 3, 5, 4, 'Chất cotton mát, nhưng sau 3 lần giặt hơi co lại một chút. Nên mua size lớn hơn.', TRUE, NULL, NULL),
(8, 5, 6, 5, 'Oxford trắng chuẩn men, mặc đi làm rất ổn. Sẽ mua thêm màu xanh.', TRUE, NULL, NULL),
(8, 15, 6, 4, 'Đầm hoa nhí rất xinh, đúng như hình. Chỉ hơi mỏng nên cần mặc lót bên trong.', TRUE, NULL, NULL),
(3, 13, 7, 5, 'Mua lần 2 rồi, chất lụa mặc lên rất sang. Ship nhanh, đóng gói đẹp.', TRUE, NULL, NULL),
(3, 11, 7, 5, 'Baby tee hot trend! Mặc ôm vừa vặn, chất cotton mềm. Mua 2 cái luôn.', TRUE, NULL, NULL),
(5, 1, 13, 4, 'Polo classic đẹp, nhưng phần cổ hơi cứng mới mua. Giặt vài lần sẽ mềm hơn.', FALSE, NULL, NULL),
(8, 3, 14, 5, 'Áo thun basic tốt nhất từng mua. Cotton dày mà vẫn thoáng. Sẽ quay lại mua.', TRUE, NULL, NULL);

-- ========== REVIEW IMAGES ==========
INSERT INTO review_images (review_id, image_url, display_order) VALUES
(1, 'https://placehold.co/400x400/navy/white?text=Review+1a', 0),
(1, 'https://placehold.co/400x400/navy/white?text=Review+1b', 1),
(4, 'https://placehold.co/400x400/4a6fa5/fff?text=Review+Jeans', 0),
(6, 'https://placehold.co/400x400/faebd7/333?text=Review+Silk', 0),
(9, 'https://placehold.co/400x400/e8f5e9/333?text=Review+Dress', 0);

-- ========== WISHLISTS ==========
INSERT INTO wishlists (user_id, product_id) VALUES
(2, 5), (2, 13), (2, 15),
(3, 7), (3, 20),
(5, 11), (5, 15),
(7, 1), (7, 3), (7, 7),
(8, 11), (8, 13),
(9, 5), (9, 6),
(11, 11), (11, 15), (11, 20);

-- ========== CART ITEMS ==========
INSERT INTO cart_items (user_id, product_variant_id, quantity) VALUES
(7, 33, 1), (7, 42, 1),
(9, 7, 2), (9, 24, 1),
(11, 35, 1), (11, 39, 1);

-- ========== BLOG POSTS ==========
INSERT INTO blog_posts (title, slug, content, excerpt, thumbnail_url, category_id, author_id, is_published, published_at) VALUES
('5 Cách Phối Áo Polo Nam Đẹp Mắt Nhất 2026', '5-cach-phoi-ao-polo-nam-2026', 'Áo polo là item không thể thiếu trong tủ đồ của các chàng trai. Dưới đây là 5 cách phối đồ với áo polo giúp bạn trông phong cách và lịch lãm hơn...', 'Khám phá 5 cách phối áo polo nam trendy nhất năm 2026', 'https://placehold.co/800x400/00bcd4/fff?text=Polo+Style', 2, 1, TRUE, '2026-05-10 08:00:00+07'),
('Xu Hướng Thời Trang Hè 2026: Những Gì Đang Hot?', 'xu-huong-thoi-trang-he-2026', 'Mùa hè 2026 đến với nhiều xu hướng mới thú vị. Từ baby tee, oversized silhouettes đến sustainable fashion...', 'Cập nhật xu hướng thời trang mùa hè mới nhất', 'https://placehold.co/800x400/ff5722/fff?text=Summer+Trends', 1, 1, TRUE, '2026-05-15 10:00:00+07'),
('Hướng Dẫn Chọn Size Quần Jeans Chuẩn Không Cần Chỉnh', 'huong-dan-chon-size-quan-jeans', 'Chọn sai size quần jeans là nỗi đau chung của nhiều người. Bài viết này sẽ hướng dẫn bạn cách đo và chọn size jeans phù hợp nhất...', 'Mẹo chọn size jeans chuẩn cho cả nam và nữ', 'https://placehold.co/800x400/3f51b5/fff?text=Jeans+Guide', 2, 1, TRUE, '2026-05-20 09:00:00+07'),
('Giảm Giá Lên Đến 30% - Flash Sale Tháng 6', 'flash-sale-thang-6-2026', 'Chào đón mùa hè, shop giảm giá lên đến 30% tất cả sản phẩm. Nhanh tay săn deal ngay!', 'Flash sale tháng 6 giảm đến 30%', 'https://placehold.co/800x400/f44336/fff?text=Flash+Sale', 3, 1, TRUE, '2026-05-28 08:00:00+07'),
('Chất Liệu Vải Nào Phù Hợp Cho Mùa Hè Việt Nam?', 'chat-lieu-vai-mua-he-viet-nam', 'Với khí hậu nóng ẩm Việt Nam, việc chọn chất liệu vải phù hợp rất quan trọng...', 'Tìm hiểu các loại vải thoáng mát cho mùa hè', 'https://placehold.co/800x400/4caf50/fff?text=Fabric+Guide', 2, 1, TRUE, '2026-05-25 10:00:00+07');

-- ========== BLOG TAGS ==========
INSERT INTO blog_tags (name, slug) VALUES
('áo polo', 'ao-polo'), ('mùa hè', 'mua-he'), ('phối đồ', 'phoi-do'),
('xu hướng', 'xu-huong'), ('quần jeans', 'quan-jeans'), ('khuyến mãi', 'khuyen-mai'),
('chất liệu', 'chat-lieu'), ('streetwear', 'streetwear'), ('công sở', 'cong-so');

INSERT INTO blog_post_tags (blog_post_id, blog_tag_id) VALUES
(1, 1), (1, 3), (1, 9),
(2, 2), (2, 4), (2, 8),
(3, 5), (3, 3),
(4, 6), (4, 2),
(5, 7), (5, 2);

-- ========== ACTIVITY LOGS ==========
INSERT INTO activity_logs (user_id, action, entity_type, entity_id, ip_address, user_agent, created_at) VALUES
(1, 'login', 'user', 1, '192.168.1.1', 'Mozilla/5.0 Chrome/126', '2026-05-01 08:00:00+07'),
(1, 'create_product', 'product', 1, '192.168.1.1', 'Mozilla/5.0 Chrome/126', '2026-05-01 09:00:00+07'),
(2, 'login', 'user', 2, '103.1.2.3', 'Mozilla/5.0 Chrome/126', '2026-05-01 10:00:00+07'),
(2, 'create_order', 'order', 1, '103.1.2.3', 'Mozilla/5.0 Chrome/126', '2026-05-01 10:15:00+07'),
(1, 'update_order', 'order', 1, '192.168.1.1', 'Mozilla/5.0 Chrome/126', '2026-05-01 11:00:00+07'),
(3, 'login', 'user', 3, '113.5.6.7', 'Mozilla/5.0 Safari/17', '2026-05-02 14:00:00+07'),
(3, 'create_order', 'order', 2, '113.5.6.7', 'Mozilla/5.0 Safari/17', '2026-05-02 14:30:00+07'),
(1, 'approve_review', 'review', 1, '192.168.1.1', 'Mozilla/5.0 Chrome/126', '2026-05-03 09:00:00+07'),
(5, 'login', 'user', 5, '42.10.11.12', 'Mozilla/5.0 Chrome/126', '2026-05-07 16:00:00+07'),
(1, 'create_voucher', 'voucher', 5, '192.168.1.1', 'Mozilla/5.0 Chrome/126', '2026-05-28 08:00:00+07');
