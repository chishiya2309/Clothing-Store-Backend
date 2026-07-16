-- SEED DATA Part 4: Additional catalog and order coverage for demo
-- Run after seed_data_3.sql
-- Totals after seed_data_4.sql:
--   - 11 users (1 admin + 10 customers)
--   - 50 products
--   - 20 orders

-- ========== PRODUCTS (+30 sản phẩm để đủ 50 sản phẩm) ==========
WITH product_seed (
    name,
    slug,
    description,
    material,
    care_instructions,
    category_slug,
    base_price,
    sale_price,
    is_featured,
    total_sold,
    average_rating
) AS (
    VALUES
    ('Áo Polo Nam Heritage Stripe', 'ao-polo-nam-heritage-stripe', 'Áo polo nam phối sọc cổ tinh tế, phom regular fit dễ mặc đi làm lẫn cuối tuần. Bề mặt vải đứng form nhưng vẫn thoáng khí.', 'Cotton Pique 95%, Spandex 5%', 'Giặt máy 30°C, không sấy nhiệt cao', 'ao-polo-nam', 429000, 359000, TRUE, 132, 4.60),
    ('Áo Polo Nam Dry Tech 24H', 'ao-polo-nam-dry-tech-24h', 'Áo polo công nghệ sợi khô nhanh, phù hợp người thường xuyên di chuyển hoặc chơi thể thao nhẹ. Chất vải mịn, mặc cả ngày vẫn dễ chịu.', 'Polyester 88%, Spandex 12%', 'Giặt nhẹ, không dùng nước xả đậm đặc', 'ao-polo-nam', 459000, 399000, FALSE, 98, 4.40),
    ('Áo Polo Nam Minimal Monogram', 'ao-polo-nam-minimal-monogram', 'Áo polo phong cách tối giản với logo monogram thêu nhỏ ở ngực trái. Tông màu trung tính giúp phối với jeans hoặc kaki đều đẹp.', 'Cotton Mercerized 100%', 'Giặt túi lưới, ủi mặt trái', 'ao-polo-nam', 449000, NULL, TRUE, 74, 4.30),
    ('Áo Thun Nam Supima Premium', 'ao-thun-nam-supima-premium', 'Áo thun nam dùng sợi Supima mềm mượt, bề mặt mịn và ít bai dão sau nhiều lần giặt. Form gọn gàng, mặc một mình hoặc layer đều hợp.', 'Supima Cotton 100%', 'Giặt máy 30°C, phơi ngang', 'ao-thun-nam', 299000, 269000, TRUE, 188, 4.70),
    ('Áo Thun Nam Raglan Retro', 'ao-thun-nam-raglan-retro', 'Áo thun raglan phối tay màu tương phản theo phong cách retro. Phần cổ bo chắc tay, hợp vibe casual và streetwear nhẹ.', 'Cotton 100%', 'Giặt nhẹ, lộn trái khi phơi', 'ao-thun-nam', 279000, 239000, FALSE, 121, 4.40),
    ('Áo Thun Nam Airy Mesh', 'ao-thun-nam-airy-mesh', 'Áo thun nam dệt mắt lưới nhỏ giúp thoáng khí hơn trong thời tiết nóng. Bề mặt có độ nhám nhẹ tạo cảm giác thể thao hiện đại.', 'Polyester 72%, Cotton 28%', 'Không sấy nóng, tránh ngâm lâu', 'ao-thun-nam', 319000, 289000, FALSE, 86, 4.20),
    ('Áo Thun Nam Graphic Sài Gòn', 'ao-thun-nam-graphic-sai-gon', 'Áo thun in graphic lấy cảm hứng từ nhịp sống Sài Gòn về đêm. Chất vải dày vừa phải, lên form oversize nhưng vẫn gọn gàng.', 'Cotton 240GSM', 'Giặt máy chế độ nhẹ, lộn trái', 'ao-thun-nam', 299000, NULL, TRUE, 164, 4.50),
    ('Áo Sơ Mi Nam Poplin Stretch', 'ao-so-mi-nam-poplin-stretch', 'Sơ mi nam chất poplin co giãn nhẹ, mặc ôm gọn nhưng không gò bó. Phù hợp môi trường công sở cần vẻ ngoài chỉn chu.', 'Cotton 97%, Spandex 3%', 'Ủi nhiệt trung bình, treo ngay sau khi giặt', 'ao-so-mi-nam', 499000, 449000, TRUE, 92, 4.60),
    ('Áo Sơ Mi Nam Cuban Resort', 'ao-so-mi-nam-cuban-resort', 'Sơ mi cổ Cuban thoáng mát, mang hơi hướng nghỉ dưỡng hiện đại. Phom suông vừa phải, dễ phối với short hoặc quần linen.', 'Rayon 55%, Linen 45%', 'Giặt tay hoặc giặt nhẹ, phơi ngang', 'ao-so-mi-nam', 539000, 489000, FALSE, 67, 4.30),
    ('Áo Sơ Mi Nam Kẻ Sọc Smart', 'ao-so-mi-nam-ke-soc-smart', 'Sơ mi kẻ sọc mảnh tạo hiệu ứng người mặc trông cao và gọn hơn. Kiểu dáng thanh lịch, hợp cả đi làm lẫn gặp gỡ đối tác.', 'Cotton 100%', 'Giặt máy 30°C, ủi hơi nước', 'ao-so-mi-nam', 519000, NULL, FALSE, 59, 4.20),
    ('Quần Jeans Nam Straight Wash', 'quan-jeans-nam-straight-wash', 'Quần jeans nam dáng straight hiện đại, wash xanh đậm dễ phối nhiều kiểu áo. Đường may chắc chắn, ống quần đứng form đẹp.', 'Denim Cotton 99%, Elastane 1%', 'Giặt riêng lần đầu, lộn trái khi giặt', 'quan-jeans-nam', 649000, 579000, TRUE, 113, 4.50),
    ('Quần Jeans Nam Loose Vintage', 'quan-jeans-nam-loose-vintage', 'Quần jeans loose fit mang cảm hứng vintage, phù hợp phong cách thoải mái và trẻ trung. Wash nhạt tạo chiều sâu bề mặt vải.', 'Denim Cotton 100%', 'Không tẩy, phơi nơi râm mát', 'quan-jeans-nam', 669000, 619000, FALSE, 71, 4.40),
    ('Quần Kaki Nam Pleated', 'quan-kaki-nam-pleated', 'Quần kaki nam có ly trước giúp tổng thể trông lịch sự hơn mà vẫn thoải mái. Chất vải đứng dáng, hợp đi làm và đi chơi cuối tuần.', 'Cotton Twill 98%, Spandex 2%', 'Ủi mặt trái, giặt nhẹ', 'quan-kaki-nam', 479000, 429000, FALSE, 88, 4.30),
    ('Quần Kaki Nam Cargo Tapered', 'quan-kaki-nam-cargo-tapered', 'Quần cargo tapered gọn gàng hơn kiểu cargo truyền thống, túi hộp vừa đủ điểm nhấn. Hợp phối sneaker và tee basic.', 'Cotton 97%, Spandex 3%', 'Giặt nhẹ, không vắt quá mạnh', 'quan-kaki-nam', 499000, 459000, TRUE, 95, 4.40),
    ('Quần Short Nam Linen', 'quan-short-nam-linen', 'Quần short linen nam nhẹ và thoáng, cạp vừa, chiều dài trên gối dễ mặc mùa hè. Màu sắc trung tính để phối đồ nhanh.', 'Linen 55%, Cotton 45%', 'Giặt tay hoặc máy nhẹ, phơi ngang', 'quan-kaki-nam', 329000, 289000, FALSE, 143, 4.50),
    ('Áo Thun Nữ Ribbed Fitted', 'ao-thun-nu-ribbed-fitted', 'Áo thun nữ dệt gân ôm nhẹ cơ thể, cổ tròn nhỏ gọn tạo cảm giác nữ tính. Dễ phối với chân váy, jeans hoặc quần ống rộng.', 'Cotton Rib 95%, Spandex 5%', 'Giặt nhẹ, phơi ngang để giữ form', 'ao-thun-nu', 239000, 199000, TRUE, 176, 4.70),
    ('Áo Thun Nữ Graphic Weekend', 'ao-thun-nu-graphic-weekend', 'Áo thun in slogan mềm mại, lên dáng trẻ trung cho những outfit đi cà phê hoặc dạo phố. Form hơi rộng nhưng không bị thùng thình.', 'Cotton 100%', 'Lộn trái khi giặt, không sấy nóng', 'ao-thun-nu', 219000, 179000, TRUE, 210, 4.80),
    ('Áo Thun Nữ Modal U-Neck', 'ao-thun-nu-modal-u-neck', 'Áo thun cổ U chất modal mượt, mặc mát tay và rủ đẹp. Phù hợp làm item nền cho nhiều set đồ tối giản.', 'Modal 92%, Spandex 8%', 'Giặt máy túi lưới, phơi nơi thoáng mát', 'ao-thun-nu', 259000, 229000, FALSE, 118, 4.60),
    ('Áo Sơ Mi Nữ Satin Office', 'ao-so-mi-nu-satin-office', 'Sơ mi satin nữ bề mặt bóng nhẹ, mang lại cảm giác sang nhưng vẫn dễ mặc hằng ngày. Cổ áo nhỏ và tay bo giúp tổng thể thanh lịch.', 'Satin Polyester 100%', 'Giặt tay, ủi nhiệt thấp', 'ao-so-mi-nu', 529000, 469000, TRUE, 84, 4.60),
    ('Áo Sơ Mi Nữ Cropped Poplin', 'ao-so-mi-nu-cropped-poplin', 'Sơ mi crop poplin hiện đại, phần thân ngắn vừa phải nên vẫn lịch sự. Hợp phối với quần cạp cao hoặc chân váy bút chì.', 'Cotton Poplin 100%', 'Giặt nhẹ, treo móc sau khi giặt', 'ao-so-mi-nu', 449000, 399000, FALSE, 69, 4.40),
    ('Quần Jeans Nữ Straight High Rise', 'quan-jeans-nu-straight-high-rise', 'Quần jeans nữ lưng cao dáng straight tôn chân và dễ phối với tee hoặc sơ mi. Chất denim có độ cứng vừa đủ để đứng form đẹp.', 'Cotton 99%, Elastane 1%', 'Lộn trái khi giặt, tránh sấy nhiệt cao', 'quan-jeans-nu', 599000, 539000, TRUE, 91, 4.50),
    ('Quần Jeans Nữ Baggy Light Blue', 'quan-jeans-nu-baggy-light-blue', 'Quần jeans baggy màu xanh nhạt trẻ trung, thích hợp phong cách năng động. Form rộng vừa phải giúp di chuyển thoải mái cả ngày.', 'Denim Cotton 100%', 'Giặt riêng lần đầu, phơi nơi râm', 'quan-jeans-nu', 649000, 569000, TRUE, 104, 4.60),
    ('Đầm Liền Midi Xếp Ly', 'dam-lien-midi-xep-ly', 'Đầm midi xếp ly nhẹ ở thân váy, tạo độ bay và thanh thoát khi di chuyển. Thiết kế tối giản dễ mặc đi làm hoặc tiệc nhẹ.', 'Polyester Crepe 100%', 'Giặt tay, không vắt xoắn', 'dam-lien', 799000, 699000, TRUE, 76, 4.70),
    ('Đầm Liền Linen Smocked', 'dam-lien-linen-smocked', 'Đầm linen nhún ngực mềm mại, thoáng mát cho những ngày nắng. Dáng váy nữ tính, phù hợp đi chơi, du lịch hoặc chụp hình.', 'Linen 70%, Cotton 30%', 'Giặt nhẹ, phơi ngang, ủi hơi nước', 'dam-lien', 729000, 649000, FALSE, 58, 4.50),
    ('Mũ Lưỡi Trai Cotton Logo', 'mu-luoi-trai-cotton-logo', 'Mũ lưỡi trai cotton form basic với logo thêu nhỏ trước trán. Phần khóa sau dễ điều chỉnh, phù hợp cả nam và nữ.', 'Cotton Canvas 100%', 'Giặt tay, không vò mạnh phần vành', 'mu-non', 199000, 169000, FALSE, 132, 4.40),
    ('Mũ Bucket Chống Nắng', 'mu-bucket-chong-nang', 'Mũ bucket vành vừa, che nắng tốt nhưng vẫn gọn mặt. Chất liệu nhẹ, dễ gấp mang theo trong túi.', 'Nylon 70%, Cotton 30%', 'Giặt tay, phơi khô tự nhiên', 'mu-non', 189000, NULL, TRUE, 147, 4.50),
    ('Nón Len Rib Knit Beanie', 'non-len-rib-knit-beanie', 'Beanie dệt gân ôm đầu vừa phải, giữ ấm nhẹ cho thời tiết se lạnh hoặc outfit layering. Màu cơ bản dễ phối cùng áo khoác.', 'Acrylic 100%', 'Giặt tay, phơi ngang', 'mu-non', 179000, 149000, FALSE, 63, 4.30),
    ('Túi Tote Canvas Daily', 'tui-tote-canvas-daily', 'Túi tote canvas khổ vừa, đựng được laptop mỏng và đồ dùng hằng ngày. Quai chắc tay, hợp đi học, đi làm hoặc dạo phố.', 'Canvas Cotton 100%', 'Lau sạch bằng khăn ẩm, giặt tay khi cần', 'tui-balo', 279000, 249000, TRUE, 155, 4.60),
    ('Balo Laptop Urban 15 Inch', 'balo-laptop-urban-15-inch', 'Balo urban nhiều ngăn, có ngăn riêng cho laptop 15 inch và quai đeo êm vai. Thiết kế tối giản phù hợp môi trường học tập lẫn công sở.', 'Polyester chống thấm 100%', 'Lau sạch bằng khăn ẩm, không giặt máy', 'tui-balo', 649000, 589000, TRUE, 73, 4.50),
    ('Túi Đeo Chéo Mini Utility', 'tui-deo-cheo-mini-utility', 'Túi đeo chéo mini gọn nhẹ với nhiều ngăn nhỏ tiện dụng. Phù hợp đi chơi, đi cà phê hoặc mang theo phụ kiện cá nhân hằng ngày.', 'Nylon Oxford 100%', 'Lau bề mặt bằng khăn ẩm', 'tui-balo', 329000, 289000, FALSE, 94, 4.40)
)
INSERT INTO products (
    name,
    slug,
    description,
    material,
    care_instructions,
    category_id,
    base_price,
    sale_price,
    is_active,
    is_featured,
    total_sold,
    average_rating
)
SELECT
    name,
    slug,
    description,
    material,
    care_instructions,
    (SELECT id FROM categories WHERE slug = category_slug),
    base_price,
    sale_price,
    TRUE,
    is_featured,
    total_sold,
    average_rating
FROM product_seed;

-- ========== PRODUCT VARIANTS ==========
WITH variant_seed (product_slug, sku, size, color, stock_quantity) AS (
    VALUES
    ('ao-polo-nam-heritage-stripe', 'POLO-HST-M-GRN', 'M', 'Xanh Rêu', 30),
    ('ao-polo-nam-heritage-stripe', 'POLO-HST-L-GRN', 'L', 'Xanh Rêu', 26),
    ('ao-polo-nam-heritage-stripe', 'POLO-HST-M-NVY', 'M', 'Xanh Navy', 24),
    ('ao-polo-nam-dry-tech-24h', 'POLO-DRY-M-BLK', 'M', 'Đen', 28),
    ('ao-polo-nam-dry-tech-24h', 'POLO-DRY-L-BLK', 'L', 'Đen', 22),
    ('ao-polo-nam-dry-tech-24h', 'POLO-DRY-M-GRY', 'M', 'Xám', 25),
    ('ao-polo-nam-minimal-monogram', 'POLO-MONO-M-CRM', 'M', 'Kem', 20),
    ('ao-polo-nam-minimal-monogram', 'POLO-MONO-L-CRM', 'L', 'Kem', 18),
    ('ao-polo-nam-minimal-monogram', 'POLO-MONO-XL-BLK', 'XL', 'Đen', 16),
    ('ao-thun-nam-supima-premium', 'TEE-SUPIMA-M-WHT', 'M', 'Trắng', 48),
    ('ao-thun-nam-supima-premium', 'TEE-SUPIMA-L-OLV', 'L', 'Olive', 40),
    ('ao-thun-nam-supima-premium', 'TEE-SUPIMA-XL-NVY', 'XL', 'Xanh Navy', 32),
    ('ao-thun-nam-raglan-retro', 'TEE-RAG-S-WHT', 'S', 'Trắng/Đen', 34),
    ('ao-thun-nam-raglan-retro', 'TEE-RAG-M-WHT', 'M', 'Trắng/Đen', 36),
    ('ao-thun-nam-raglan-retro', 'TEE-RAG-L-NVY', 'L', 'Kem/Xanh Navy', 28),
    ('ao-thun-nam-airy-mesh', 'TEE-MESH-M-GRY', 'M', 'Xám', 24),
    ('ao-thun-nam-airy-mesh', 'TEE-MESH-L-GRY', 'L', 'Xám', 22),
    ('ao-thun-nam-airy-mesh', 'TEE-MESH-XL-BLK', 'XL', 'Đen', 20),
    ('ao-thun-nam-graphic-sai-gon', 'TEE-SGN-M-BLK', 'M', 'Đen', 30),
    ('ao-thun-nam-graphic-sai-gon', 'TEE-SGN-L-BLK', 'L', 'Đen', 26),
    ('ao-thun-nam-graphic-sai-gon', 'TEE-SGN-XL-CRM', 'XL', 'Kem', 18),
    ('ao-so-mi-nam-poplin-stretch', 'SHIRT-POP-M-WHT', 'M', 'Trắng', 24),
    ('ao-so-mi-nam-poplin-stretch', 'SHIRT-POP-L-WHT', 'L', 'Trắng', 22),
    ('ao-so-mi-nam-poplin-stretch', 'SHIRT-POP-M-BLU', 'M', 'Xanh Nhạt', 20),
    ('ao-so-mi-nam-cuban-resort', 'SHIRT-CUB-M-BEG', 'M', 'Be', 18),
    ('ao-so-mi-nam-cuban-resort', 'SHIRT-CUB-L-BEG', 'L', 'Be', 16),
    ('ao-so-mi-nam-cuban-resort', 'SHIRT-CUB-XL-GRN', 'XL', 'Xanh Sage', 14),
    ('ao-so-mi-nam-ke-soc-smart', 'SHIRT-STR-M-BLU', 'M', 'Xanh Sọc', 18),
    ('ao-so-mi-nam-ke-soc-smart', 'SHIRT-STR-L-BLU', 'L', 'Xanh Sọc', 16),
    ('ao-so-mi-nam-ke-soc-smart', 'SHIRT-STR-XL-GRY', 'XL', 'Xám Sọc', 12),
    ('quan-jeans-nam-straight-wash', 'DENIM-STR-30-DBL', '30', 'Xanh Đậm', 20),
    ('quan-jeans-nam-straight-wash', 'DENIM-STR-31-DBL', '31', 'Xanh Đậm', 24),
    ('quan-jeans-nam-straight-wash', 'DENIM-STR-32-LBL', '32', 'Xanh Wash Nhạt', 18),
    ('quan-jeans-nam-loose-vintage', 'DENIM-LOOSE-30-LBU', '30', 'Xanh Nhạt', 18),
    ('quan-jeans-nam-loose-vintage', 'DENIM-LOOSE-31-LBU', '31', 'Xanh Nhạt', 20),
    ('quan-jeans-nam-loose-vintage', 'DENIM-LOOSE-32-BLK', '32', 'Đen Wash', 14),
    ('quan-kaki-nam-pleated', 'KAKI-PLT-30-BEI', '30', 'Be', 24),
    ('quan-kaki-nam-pleated', 'KAKI-PLT-31-BEI', '31', 'Be', 28),
    ('quan-kaki-nam-pleated', 'KAKI-PLT-32-NAV', '32', 'Xanh Navy', 16),
    ('quan-kaki-nam-cargo-tapered', 'KAKI-CRG-29-OLV', '29', 'Olive', 22),
    ('quan-kaki-nam-cargo-tapered', 'KAKI-CRG-30-OLV', '30', 'Olive', 20),
    ('quan-kaki-nam-cargo-tapered', 'KAKI-CRG-31-BLK', '31', 'Đen', 18),
    ('quan-short-nam-linen', 'SHORT-LIN-M-BEI', 'M', 'Be', 26),
    ('quan-short-nam-linen', 'SHORT-LIN-L-BEI', 'L', 'Be', 22),
    ('quan-short-nam-linen', 'SHORT-LIN-XL-NVY', 'XL', 'Xanh Navy', 18),
    ('ao-thun-nu-ribbed-fitted', 'TEE-RIB-S-WHT', 'S', 'Trắng', 36),
    ('ao-thun-nu-ribbed-fitted', 'TEE-RIB-M-WHT', 'M', 'Trắng', 34),
    ('ao-thun-nu-ribbed-fitted', 'TEE-RIB-M-BLK', 'M', 'Đen', 30),
    ('ao-thun-nu-graphic-weekend', 'TEE-WKD-S-CRM', 'S', 'Kem', 42),
    ('ao-thun-nu-graphic-weekend', 'TEE-WKD-M-CRM', 'M', 'Kem', 40),
    ('ao-thun-nu-graphic-weekend', 'TEE-WKD-M-PNK', 'M', 'Hồng Phấn', 30),
    ('ao-thun-nu-modal-u-neck', 'TEE-MODAL-S-BLK', 'S', 'Đen', 28),
    ('ao-thun-nu-modal-u-neck', 'TEE-MODAL-M-BLK', 'M', 'Đen', 26),
    ('ao-thun-nu-modal-u-neck', 'TEE-MODAL-L-MOC', 'L', 'Nâu Mocha', 18),
    ('ao-so-mi-nu-satin-office', 'SHIRT-SATIN-S-IVORY', 'S', 'Ivory', 18),
    ('ao-so-mi-nu-satin-office', 'SHIRT-SATIN-M-IVORY', 'M', 'Ivory', 20),
    ('ao-so-mi-nu-satin-office', 'SHIRT-SATIN-M-BLK', 'M', 'Đen', 16),
    ('ao-so-mi-nu-cropped-poplin', 'SHIRT-CROP-S-BLU', 'S', 'Xanh Nhạt', 16),
    ('ao-so-mi-nu-cropped-poplin', 'SHIRT-CROP-M-BLU', 'M', 'Xanh Nhạt', 18),
    ('ao-so-mi-nu-cropped-poplin', 'SHIRT-CROP-L-WHT', 'L', 'Trắng', 14),
    ('quan-jeans-nu-straight-high-rise', 'JEAN-F-STR-26-IND', '26', 'Indigo', 20),
    ('quan-jeans-nu-straight-high-rise', 'JEAN-F-STR-27-IND', '27', 'Indigo', 22),
    ('quan-jeans-nu-straight-high-rise', 'JEAN-F-STR-28-BLU', '28', 'Xanh Wash', 16),
    ('quan-jeans-nu-baggy-light-blue', 'JEAN-F-BAG-26-LBU', '26', 'Xanh Nhạt', 24),
    ('quan-jeans-nu-baggy-light-blue', 'JEAN-F-BAG-27-LBU', '27', 'Xanh Nhạt', 26),
    ('quan-jeans-nu-baggy-light-blue', 'JEAN-F-BAG-28-GRY', '28', 'Xám Tro', 14),
    ('dam-lien-midi-xep-ly', 'DRESS-PLEAT-S-BLK', 'S', 'Đen', 14),
    ('dam-lien-midi-xep-ly', 'DRESS-PLEAT-M-BLK', 'M', 'Đen', 16),
    ('dam-lien-midi-xep-ly', 'DRESS-PLEAT-L-MAR', 'L', 'Đỏ Mận', 10),
    ('dam-lien-linen-smocked', 'DRESS-LINEN-S-BEI', 'S', 'Be', 14),
    ('dam-lien-linen-smocked', 'DRESS-LINEN-M-BEI', 'M', 'Be', 16),
    ('dam-lien-linen-smocked', 'DRESS-LINEN-M-GRN', 'M', 'Xanh Rêu', 12),
    ('mu-luoi-trai-cotton-logo', 'HAT-CAP-BLK-FREE', 'FREE', 'Đen', 45),
    ('mu-luoi-trai-cotton-logo', 'HAT-CAP-WHT-FREE', 'FREE', 'Trắng', 38),
    ('mu-luoi-trai-cotton-logo', 'HAT-CAP-NAV-FREE', 'FREE', 'Xanh Navy', 34),
    ('mu-bucket-chong-nang', 'HAT-BUCKET-BEI', 'FREE', 'Be', 40),
    ('mu-bucket-chong-nang', 'HAT-BUCKET-BLK', 'FREE', 'Đen', 36),
    ('mu-bucket-chong-nang', 'HAT-BUCKET-OLV', 'FREE', 'Olive', 28),
    ('non-len-rib-knit-beanie', 'HAT-BEANIE-CRM', 'FREE', 'Kem', 24),
    ('non-len-rib-knit-beanie', 'HAT-BEANIE-GRY', 'FREE', 'Xám', 22),
    ('non-len-rib-knit-beanie', 'HAT-BEANIE-BLK', 'FREE', 'Đen', 20),
    ('tui-tote-canvas-daily', 'TOTE-CANVAS-IVORY', 'FREE', 'Ivory', 44),
    ('tui-tote-canvas-daily', 'TOTE-CANVAS-BLK', 'FREE', 'Đen', 38),
    ('tui-tote-canvas-daily', 'TOTE-CANVAS-OLV', 'FREE', 'Olive', 26),
    ('balo-laptop-urban-15-inch', 'BACKPACK-URBAN-BLK', 'FREE', 'Đen', 20),
    ('balo-laptop-urban-15-inch', 'BACKPACK-URBAN-GRY', 'FREE', 'Xám', 18),
    ('balo-laptop-urban-15-inch', 'BACKPACK-URBAN-NAV', 'FREE', 'Xanh Navy', 16),
    ('tui-deo-cheo-mini-utility', 'SLING-MINI-BLK', 'FREE', 'Đen', 28),
    ('tui-deo-cheo-mini-utility', 'SLING-MINI-SAND', 'FREE', 'Cát', 24),
    ('tui-deo-cheo-mini-utility', 'SLING-MINI-GRN', 'FREE', 'Xanh Rêu', 18)
)
INSERT INTO product_variants (
    product_id,
    sku,
    size,
    color,
    stock_quantity,
    additional_price,
    is_active
)
SELECT
    p.id,
    v.sku,
    v.size,
    v.color,
    v.stock_quantity,
    0,
    TRUE
FROM variant_seed v
JOIN products p ON p.slug = v.product_slug;

-- ========== PRODUCT IMAGES ==========
WITH image_seed (product_slug, image_url, image_type, display_order, alt_text) AS (
    VALUES
    ('ao-polo-nam-heritage-stripe', 'https://placehold.co/800x1000/355070/FFFFFF?text=Polo+Heritage', 'main', 0, 'Áo Polo Nam Heritage Stripe'),
    ('ao-polo-nam-dry-tech-24h', 'https://placehold.co/800x1000/1F2937/FFFFFF?text=Polo+Dry+Tech', 'main', 0, 'Áo Polo Nam Dry Tech 24H'),
    ('ao-polo-nam-minimal-monogram', 'https://placehold.co/800x1000/E7E5E4/111827?text=Polo+Monogram', 'main', 0, 'Áo Polo Nam Minimal Monogram'),
    ('ao-thun-nam-supima-premium', 'https://placehold.co/800x1000/F9FAFB/111827?text=Tee+Supima', 'main', 0, 'Áo Thun Nam Supima Premium'),
    ('ao-thun-nam-raglan-retro', 'https://placehold.co/800x1000/F3F4F6/111827?text=Tee+Raglan', 'main', 0, 'Áo Thun Nam Raglan Retro'),
    ('ao-thun-nam-airy-mesh', 'https://placehold.co/800x1000/9CA3AF/FFFFFF?text=Tee+Mesh', 'main', 0, 'Áo Thun Nam Airy Mesh'),
    ('ao-thun-nam-graphic-sai-gon', 'https://placehold.co/800x1000/111827/FFFFFF?text=Tee+Sai+Gon', 'main', 0, 'Áo Thun Nam Graphic Sài Gòn'),
    ('ao-so-mi-nam-poplin-stretch', 'https://placehold.co/800x1000/DBEAFE/1E3A8A?text=Shirt+Poplin', 'main', 0, 'Áo Sơ Mi Nam Poplin Stretch'),
    ('ao-so-mi-nam-cuban-resort', 'https://placehold.co/800x1000/D6D3D1/1C1917?text=Shirt+Cuban', 'main', 0, 'Áo Sơ Mi Nam Cuban Resort'),
    ('ao-so-mi-nam-ke-soc-smart', 'https://placehold.co/800x1000/E0F2FE/0F172A?text=Shirt+Stripe', 'main', 0, 'Áo Sơ Mi Nam Kẻ Sọc Smart'),
    ('quan-jeans-nam-straight-wash', 'https://placehold.co/800x1000/1D4ED8/FFFFFF?text=Jeans+Straight', 'main', 0, 'Quần Jeans Nam Straight Wash'),
    ('quan-jeans-nam-loose-vintage', 'https://placehold.co/800x1000/60A5FA/FFFFFF?text=Jeans+Loose', 'main', 0, 'Quần Jeans Nam Loose Vintage'),
    ('quan-kaki-nam-pleated', 'https://placehold.co/800x1000/D6C3A3/1F2937?text=Kaki+Pleated', 'main', 0, 'Quần Kaki Nam Pleated'),
    ('quan-kaki-nam-cargo-tapered', 'https://placehold.co/800x1000/556B2F/FFFFFF?text=Kaki+Cargo', 'main', 0, 'Quần Kaki Nam Cargo Tapered'),
    ('quan-short-nam-linen', 'https://placehold.co/800x1000/EAD7C0/1F2937?text=Short+Linen', 'main', 0, 'Quần Short Nam Linen'),
    ('ao-thun-nu-ribbed-fitted', 'https://placehold.co/800x1000/FFF7ED/9A3412?text=Ribbed+Tee', 'main', 0, 'Áo Thun Nữ Ribbed Fitted'),
    ('ao-thun-nu-graphic-weekend', 'https://placehold.co/800x1000/FFE4E6/9F1239?text=Weekend+Tee', 'main', 0, 'Áo Thun Nữ Graphic Weekend'),
    ('ao-thun-nu-modal-u-neck', 'https://placehold.co/800x1000/1F2937/FFFFFF?text=Modal+Tee', 'main', 0, 'Áo Thun Nữ Modal U-Neck'),
    ('ao-so-mi-nu-satin-office', 'https://placehold.co/800x1000/FAF5FF/6B21A8?text=Satin+Shirt', 'main', 0, 'Áo Sơ Mi Nữ Satin Office'),
    ('ao-so-mi-nu-cropped-poplin', 'https://placehold.co/800x1000/DBEAFE/1D4ED8?text=Crop+Poplin', 'main', 0, 'Áo Sơ Mi Nữ Cropped Poplin'),
    ('quan-jeans-nu-straight-high-rise', 'https://placehold.co/800x1000/2563EB/FFFFFF?text=Women+Straight', 'main', 0, 'Quần Jeans Nữ Straight High Rise'),
    ('quan-jeans-nu-baggy-light-blue', 'https://placehold.co/800x1000/93C5FD/0F172A?text=Women+Baggy', 'main', 0, 'Quần Jeans Nữ Baggy Light Blue'),
    ('dam-lien-midi-xep-ly', 'https://placehold.co/800x1000/111827/FFFFFF?text=Midi+Dress', 'main', 0, 'Đầm Liền Midi Xếp Ly'),
    ('dam-lien-linen-smocked', 'https://placehold.co/800x1000/E7D8C9/1C1917?text=Linen+Dress', 'main', 0, 'Đầm Liền Linen Smocked'),
    ('mu-luoi-trai-cotton-logo', 'https://placehold.co/800x1000/111827/FFFFFF?text=Logo+Cap', 'main', 0, 'Mũ Lưỡi Trai Cotton Logo'),
    ('mu-bucket-chong-nang', 'https://placehold.co/800x1000/D6C3A3/1F2937?text=Bucket+Hat', 'main', 0, 'Mũ Bucket Chống Nắng'),
    ('non-len-rib-knit-beanie', 'https://placehold.co/800x1000/9CA3AF/111827?text=Beanie', 'main', 0, 'Nón Len Rib Knit Beanie'),
    ('tui-tote-canvas-daily', 'https://placehold.co/800x1000/F5F5F4/1C1917?text=Tote+Canvas', 'main', 0, 'Túi Tote Canvas Daily'),
    ('balo-laptop-urban-15-inch', 'https://placehold.co/800x1000/374151/FFFFFF?text=Urban+Backpack', 'main', 0, 'Balo Laptop Urban 15 Inch'),
    ('tui-deo-cheo-mini-utility', 'https://placehold.co/800x1000/4B5563/FFFFFF?text=Mini+Sling', 'main', 0, 'Túi Đeo Chéo Mini Utility')
)
INSERT INTO product_images (
    product_id,
    image_url,
    image_type,
    display_order,
    alt_text
)
SELECT
    p.id,
    i.image_url,
    i.image_type,
    i.display_order,
    i.alt_text
FROM image_seed i
JOIN products p ON p.slug = i.product_slug;

-- ========== COLLECTIONS ==========
INSERT INTO collection_products (collection_id, product_id, display_order) VALUES
((SELECT id FROM collections WHERE slug = 'new-arrivals'), (SELECT id FROM products WHERE slug = 'ao-polo-nam-heritage-stripe'), 5),
((SELECT id FROM collections WHERE slug = 'new-arrivals'), (SELECT id FROM products WHERE slug = 'ao-so-mi-nu-satin-office'), 6),
((SELECT id FROM collections WHERE slug = 'new-arrivals'), (SELECT id FROM products WHERE slug = 'quan-jeans-nu-baggy-light-blue'), 7),
((SELECT id FROM collections WHERE slug = 'new-arrivals'), (SELECT id FROM products WHERE slug = 'tui-tote-canvas-daily'), 8),
((SELECT id FROM collections WHERE slug = 'best-sellers'), (SELECT id FROM products WHERE slug = 'ao-thun-nam-supima-premium'), 6),
((SELECT id FROM collections WHERE slug = 'best-sellers'), (SELECT id FROM products WHERE slug = 'ao-thun-nu-graphic-weekend'), 7),
((SELECT id FROM collections WHERE slug = 'best-sellers'), (SELECT id FROM products WHERE slug = 'mu-bucket-chong-nang'), 8),
((SELECT id FROM collections WHERE slug = 'bst-he-2026'), (SELECT id FROM products WHERE slug = 'quan-short-nam-linen'), 7),
((SELECT id FROM collections WHERE slug = 'bst-he-2026'), (SELECT id FROM products WHERE slug = 'dam-lien-linen-smocked'), 8);

-- ========== ORDERS (+5 đơn để đủ 20 đơn hàng) ==========
INSERT INTO orders (
    user_id,
    order_code,
    shipping_name,
    shipping_phone,
    shipping_province,
    shipping_district,
    shipping_ward,
    shipping_address,
    subtotal,
    shipping_fee,
    discount_amount,
    total_amount,
    voucher_id,
    status,
    note
) VALUES
((SELECT id FROM users WHERE email = 'buivani@gmail.com'), 'DH20260601001', 'Bùi Văn I', '0989012345', 'TP. Hồ Chí Minh', 'Quận Gò Vấp', 'Phường 10', '12 Quang Trung', 788000, 0, 50000, 738000, (SELECT id FROM vouchers WHERE code = 'SUMMER50K'), 'completed', 'Giao buổi tối sau 18h'),
((SELECT id FROM users WHERE email = 'lythik@gmail.com'), 'DH20260602001', 'Lý Thị K', '0990123456', 'TP. Hồ Chí Minh', 'Quận Tân Bình', 'Phường 15', '100 Cộng Hòa', 607000, 30000, 60700, 576300, (SELECT id FROM vouchers WHERE code = 'WELCOME10'), 'completed', NULL),
((SELECT id FROM users WHERE email = 'ngothif@gmail.com'), 'DH20260605001', 'Ngô Thị F', '0956789012', 'TP. Hồ Chí Minh', 'Quận Bình Thạnh', 'Phường 25', '56 Điện Biên Phủ', 758000, 30000, 0, 788000, NULL, 'shipping', 'Khách nhắn gọi trước khi giao'),
((SELECT id FROM users WHERE email = 'dangthih@gmail.com'), 'DH20260607001', 'Đặng Thị H', '0978901234', 'Cần Thơ', 'Quận Ninh Kiều', 'Phường An Hòa', '90 Đường 3/2', 1168000, 0, 200000, 968000, (SELECT id FROM vouchers WHERE code = 'VIP20'), 'completed', NULL),
((SELECT id FROM users WHERE email = 'levanc@gmail.com'), 'DH20260609001', 'Lê Văn C', '0923456789', 'Đà Nẵng', 'Quận Hải Châu', 'Phường Thanh Bình', '90 Trần Phú', 858000, 0, 0, 858000, NULL, 'processing', 'Giữ hộ tại quầy bảo vệ');

-- ========== ORDER ITEMS ==========
INSERT INTO order_items (
    order_id,
    product_variant_id,
    product_name,
    variant_info,
    quantity,
    unit_price,
    subtotal
) VALUES
((SELECT id FROM orders WHERE order_code = 'DH20260601001'), (SELECT id FROM product_variants WHERE sku = 'POLO-HST-M-GRN'), 'Áo Polo Nam Heritage Stripe', 'M / Xanh Rêu', 1, 359000, 359000),
((SELECT id FROM orders WHERE order_code = 'DH20260601001'), (SELECT id FROM product_variants WHERE sku = 'KAKI-PLT-31-BEI'), 'Quần Kaki Nam Pleated', '31 / Be', 1, 429000, 429000),
((SELECT id FROM orders WHERE order_code = 'DH20260602001'), (SELECT id FROM product_variants WHERE sku = 'TEE-WKD-S-CRM'), 'Áo Thun Nữ Graphic Weekend', 'S / Kem', 2, 179000, 358000),
((SELECT id FROM orders WHERE order_code = 'DH20260602001'), (SELECT id FROM product_variants WHERE sku = 'TOTE-CANVAS-IVORY'), 'Túi Tote Canvas Daily', 'FREE / Ivory', 1, 249000, 249000),
((SELECT id FROM orders WHERE order_code = 'DH20260605001'), (SELECT id FROM product_variants WHERE sku = 'JEAN-F-BAG-27-LBU'), 'Quần Jeans Nữ Baggy Light Blue', '27 / Xanh Nhạt', 1, 569000, 569000),
((SELECT id FROM orders WHERE order_code = 'DH20260605001'), (SELECT id FROM product_variants WHERE sku = 'HAT-BUCKET-BEI'), 'Mũ Bucket Chống Nắng', 'FREE / Be', 1, 189000, 189000),
((SELECT id FROM orders WHERE order_code = 'DH20260607001'), (SELECT id FROM product_variants WHERE sku = 'SHIRT-SATIN-S-IVORY'), 'Áo Sơ Mi Nữ Satin Office', 'S / Ivory', 1, 469000, 469000),
((SELECT id FROM orders WHERE order_code = 'DH20260607001'), (SELECT id FROM product_variants WHERE sku = 'DRESS-PLEAT-M-BLK'), 'Đầm Liền Midi Xếp Ly', 'M / Đen', 1, 699000, 699000),
((SELECT id FROM orders WHERE order_code = 'DH20260609001'), (SELECT id FROM product_variants WHERE sku = 'BACKPACK-URBAN-BLK'), 'Balo Laptop Urban 15 Inch', 'FREE / Đen', 1, 589000, 589000),
((SELECT id FROM orders WHERE order_code = 'DH20260609001'), (SELECT id FROM product_variants WHERE sku = 'TEE-SUPIMA-L-OLV'), 'Áo Thun Nam Supima Premium', 'L / Olive', 1, 269000, 269000);

-- ========== PAYMENTS ==========
INSERT INTO payments (order_id, method, amount, status, transaction_id, paid_at) VALUES
((SELECT id FROM orders WHERE order_code = 'DH20260601001'), 'vnpay', 738000, 'completed', 'VNP20260601004567', '2026-06-01 20:15:00+07'),
((SELECT id FROM orders WHERE order_code = 'DH20260602001'), 'momo', 576300, 'completed', 'MOMO20260602001987', '2026-06-02 14:20:00+07'),
((SELECT id FROM orders WHERE order_code = 'DH20260605001'), 'vnpay', 788000, 'completed', 'VNP20260605007876', '2026-06-05 09:45:00+07'),
((SELECT id FROM orders WHERE order_code = 'DH20260607001'), 'momo', 968000, 'completed', 'MOMO20260607001234', '2026-06-07 18:05:00+07'),
((SELECT id FROM orders WHERE order_code = 'DH20260609001'), 'cod', 858000, 'pending', NULL, NULL);
