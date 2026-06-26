## 4.2. ERD

### Bảng: `activity_logs`

**Ý nghĩa**: Log hoạt động (yêu cầu hệ thống #9): đăng nhập, thay đổi dữ liệu, xử lý đơn hàng.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của activity_logs |
| 2 | `user_id` | bigint | Khóa ngoại (FK -> users(id)) | Mã tham chiếu đến users(id) |
| 3 | `action` | character | NOT NULL | Thông tin action |
| 4 | `entity_type` | character | None | Thông tin entity_type |
| 5 | `entity_id` | bigint | None | Thông tin entity_id |
| 6 | `old_data` | jsonb | None | Thông tin old_data |
| 7 | `new_data` | jsonb | None | Thông tin new_data |
| 8 | `ip_address` | inet | None | Thông tin ip_address |
| 9 | `user_agent` | text | None | Thông tin user_agent |
| 10 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |

### Bảng: `addresses`

**Ý nghĩa**: Địa chỉ giao hàng. Mỗi KH có nhiều địa chỉ, 1 địa chỉ mặc định.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của addresses |
| 2 | `user_id` | bigint | NOT NULL, Khóa ngoại (FK -> users(id)) | Mã tham chiếu đến users(id) |
| 3 | `recipient_name` | character | NOT NULL | Thông tin recipient_name |
| 4 | `phone` | character | NOT NULL | Thông tin phone |
| 5 | `province` | character | NOT NULL | Thông tin province |
| 6 | `district` | character | NOT NULL | Thông tin district |
| 7 | `ward` | character | NOT NULL | Thông tin ward |
| 8 | `street_address` | character | NOT NULL | Thông tin street_address |
| 9 | `is_default` | boolean | NOT NULL, DEFAULT | Thông tin is_default |
| 10 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |
| 11 | `updated_at` | timestamp | NOT NULL, DEFAULT | Thông tin updated_at |

### Bảng: `banners`

**Ý nghĩa**: Banner/slider trang chủ. Admin quản lý thứ tự, thời gian hiển thị.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của banners |
| 2 | `title` | character | NOT NULL | Thông tin title |
| 3 | `image_url` | text | NOT NULL | Thông tin image_url |
| 4 | `link_url` | text | None | Thông tin link_url |
| 5 | `display_order` | integer | NOT NULL, DEFAULT | Thông tin display_order |
| 6 | `is_active` | boolean | NOT NULL, DEFAULT | Thông tin is_active |
| 7 | `start_date` | timestamp | None | Thông tin start_date |
| 8 | `end_date` | timestamp | None | Thông tin end_date |
| 9 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |
| 10 | `updated_at` | timestamp | NOT NULL, DEFAULT | Thông tin updated_at |

### Bảng: `blog_categories`

**Ý nghĩa**: Danh mục blog: Xu hướng thời trang, Hướng dẫn phối đồ, Tin tức.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của blog_categories |
| 2 | `name` | character | NOT NULL | Thông tin name |
| 3 | `slug` | character | NOT NULL | Thông tin slug |
| 4 | `description` | text | None | Thông tin description |
| 5 | `display_order` | integer | NOT NULL, DEFAULT | Thông tin display_order |
| 6 | `is_active` | boolean | NOT NULL, DEFAULT | Thông tin is_active |
| 7 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |
| 8 | `updated_at` | timestamp | NOT NULL, DEFAULT | Thông tin updated_at |

### Bảng: `blog_post_tags`

**Ý nghĩa**: Lưu thông tin về blog_post_tags.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của blog_post_tags |
| 2 | `blog_post_id` | bigint | NOT NULL, Khóa ngoại (FK -> blog_posts(id)) | Mã tham chiếu đến blog_posts(id) |
| 3 | `blog_tag_id` | bigint | NOT NULL, Khóa ngoại (FK -> blog_tags(id)) | Mã tham chiếu đến blog_tags(id) |

### Bảng: `blog_posts`

**Ý nghĩa**: Bài viết blog/tin tức thời trang.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của blog_posts |
| 2 | `title` | character | NOT NULL | Thông tin title |
| 3 | `slug` | character | NOT NULL | Thông tin slug |
| 4 | `content` | text | NOT NULL | Thông tin content |
| 5 | `excerpt` | text | None | Thông tin excerpt |
| 6 | `thumbnail_url` | text | None | Thông tin thumbnail_url |
| 7 | `category_id` | bigint | Khóa ngoại (FK -> blog_categories(id)) | Mã tham chiếu đến blog_categories(id) |
| 8 | `author_id` | bigint | NOT NULL, Khóa ngoại (FK -> users(id)) | Mã tham chiếu đến users(id) |
| 9 | `is_published` | boolean | NOT NULL, DEFAULT | Thông tin is_published |
| 10 | `published_at` | timestamp | None | Thông tin published_at |
| 11 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |
| 12 | `updated_at` | timestamp | NOT NULL, DEFAULT | Thông tin updated_at |

### Bảng: `blog_tags`

**Ý nghĩa**: Tags cho blog: áo polo, mùa hè, streetwear, v.v.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của blog_tags |
| 2 | `name` | character | NOT NULL | Thông tin name |
| 3 | `slug` | character | NOT NULL | Thông tin slug |
| 4 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |

### Bảng: `cart_items`

**Ý nghĩa**: Giỏ hàng KH đã đăng nhập. Khách vãng lai dùng Redis (session-based).

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của cart_items |
| 2 | `user_id` | bigint | NOT NULL, Khóa ngoại (FK -> users(id)) | Mã tham chiếu đến users(id) |
| 3 | `product_variant_id` | bigint | NOT NULL, Khóa ngoại (FK -> product_variants(id)) | Mã tham chiếu đến product_variants(id) |
| 4 | `quantity` | integer | NOT NULL, DEFAULT | Thông tin quantity |
| 5 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |
| 6 | `updated_at` | timestamp | NOT NULL, DEFAULT | Thông tin updated_at |
| 7 | `CONSTRAINT` | cart_items_quantity_check | None | Thông tin CONSTRAINT |

### Bảng: `categories`

**Ý nghĩa**: Danh mục sản phẩm đa cấp (QĐ5: tối đa 3 cấp). VD: Nam > Áo > Áo Polo.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của categories |
| 2 | `name` | character | NOT NULL | Thông tin name |
| 3 | `slug` | character | NOT NULL | Thông tin slug |
| 4 | `parent_id` | bigint | Khóa ngoại (FK -> categories(id)) | Mã tham chiếu đến categories(id) |
| 5 | `description` | text | None | Thông tin description |
| 6 | `display_order` | integer | NOT NULL, DEFAULT | Thông tin display_order |
| 7 | `is_active` | boolean | NOT NULL, DEFAULT | Thông tin is_active |
| 8 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |
| 9 | `updated_at` | timestamp | NOT NULL, DEFAULT | Thông tin updated_at |

### Bảng: `checkout_session_items`

**Ý nghĩa**: Snapshot san pham tai thoi diem checkout.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của checkout_session_items |
| 2 | `checkout_session_id` | bigint | NOT NULL, Khóa ngoại (FK -> checkout_sessions(id)) | Mã tham chiếu đến checkout_sessions(id) |
| 3 | `product_variant_id` | bigint | NOT NULL, Khóa ngoại (FK -> product_variants(id)) | Mã tham chiếu đến product_variants(id) |
| 4 | `product_name` | character | NOT NULL | Thông tin product_name |
| 5 | `variant_info` | character | NOT NULL | Thông tin variant_info |
| 6 | `quantity` | integer | NOT NULL | Thông tin quantity |
| 7 | `unit_price` | numeric(12,2) | NOT NULL | Thông tin unit_price |
| 8 | `subtotal` | numeric(12,2) | NOT NULL | Thông tin subtotal |
| 9 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |
| 10 | `CONSTRAINT` | checkout_session_items_quantity_check | None | Thông tin CONSTRAINT |
| 11 | `CONSTRAINT` | checkout_session_items_subtotal_check | None | Thông tin CONSTRAINT |
| 12 | `CONSTRAINT` | checkout_session_items_unit_price_check | None | Thông tin CONSTRAINT |

### Bảng: `checkout_sessions`

**Ý nghĩa**: Checkout session luu snapshot gio hang va dia chi truoc khi tao don hang.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của checkout_sessions |
| 2 | `checkout_code` | character | NOT NULL | Thông tin checkout_code |
| 3 | `user_id` | bigint | NOT NULL, Khóa ngoại (FK -> users(id)) | Mã tham chiếu đến users(id) |
| 4 | `shipping_name` | character | NOT NULL | Thông tin shipping_name |
| 5 | `shipping_phone` | character | NOT NULL | Thông tin shipping_phone |
| 6 | `shipping_province` | character | NOT NULL | Thông tin shipping_province |
| 7 | `shipping_district` | character | NOT NULL | Thông tin shipping_district |
| 8 | `shipping_ward` | character | NOT NULL | Thông tin shipping_ward |
| 9 | `shipping_address` | character | NOT NULL | Thông tin shipping_address |
| 10 | `subtotal` | numeric(12,2) | NOT NULL | Thông tin subtotal |
| 11 | `shipping_fee` | numeric(12,2) | NOT NULL, DEFAULT | Thông tin shipping_fee |
| 12 | `discount_amount` | numeric(12,2) | NOT NULL, DEFAULT | Thông tin discount_amount |
| 13 | `total_amount` | numeric(12,2) | NOT NULL | Thông tin total_amount |
| 14 | `voucher_id` | bigint | Khóa ngoại (FK -> vouchers(id)) | Mã tham chiếu đến vouchers(id) |
| 15 | `payment_method` | public.payment_method | NOT NULL | Thông tin payment_method |
| 16 | `status` | public.checkout_session_status | NOT NULL, DEFAULT | Thông tin status |
| 17 | `expires_at` | timestamp | NOT NULL | Thông tin expires_at |
| 18 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |
| 19 | `updated_at` | timestamp | NOT NULL, DEFAULT | Thông tin updated_at |
| 20 | `CONSTRAINT` | checkout_sessions_discount_amount_check | None | Thông tin CONSTRAINT |
| 21 | `CONSTRAINT` | checkout_sessions_shipping_fee_check | None | Thông tin CONSTRAINT |
| 22 | `CONSTRAINT` | checkout_sessions_subtotal_check | None | Thông tin CONSTRAINT |
| 23 | `CONSTRAINT` | checkout_sessions_total_amount_check | None | Thông tin CONSTRAINT |

### Bảng: `collection_products`

**Ý nghĩa**: Lưu thông tin về collection_products.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của collection_products |
| 2 | `collection_id` | bigint | NOT NULL, Khóa ngoại (FK -> collections(id)) | Mã tham chiếu đến collections(id) |
| 3 | `product_id` | bigint | NOT NULL, Khóa ngoại (FK -> products(id)) | Mã tham chiếu đến products(id) |
| 4 | `display_order` | integer | NOT NULL, DEFAULT | Thông tin display_order |

### Bảng: `collections`

**Ý nghĩa**: Bộ sưu tập sản phẩm theo mùa/chủ đề.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của collections |
| 2 | `name` | character | NOT NULL | Thông tin name |
| 3 | `slug` | character | NOT NULL | Thông tin slug |
| 4 | `description` | text | None | Thông tin description |
| 5 | `banner_url` | text | None | Thông tin banner_url |
| 6 | `start_date` | timestamp | None | Thông tin start_date |
| 7 | `end_date` | timestamp | None | Thông tin end_date |
| 8 | `is_active` | boolean | NOT NULL, DEFAULT | Thông tin is_active |
| 9 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |
| 10 | `updated_at` | timestamp | NOT NULL, DEFAULT | Thông tin updated_at |

### Bảng: `inventory_reservations`

**Ý nghĩa**: Giu ton kho tam thoi cho checkout, chua tru stock_quantity khi reserve.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của inventory_reservations |
| 2 | `checkout_session_id` | bigint | NOT NULL, Khóa ngoại (FK -> checkout_sessions(id)) | Mã tham chiếu đến checkout_sessions(id) |
| 3 | `product_variant_id` | bigint | NOT NULL, Khóa ngoại (FK -> product_variants(id)) | Mã tham chiếu đến product_variants(id) |
| 4 | `quantity` | integer | NOT NULL | Thông tin quantity |
| 5 | `status` | public.reservation_status | NOT NULL, DEFAULT | Thông tin status |
| 6 | `expires_at` | timestamp | NOT NULL | Thông tin expires_at |
| 7 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |
| 8 | `updated_at` | timestamp | NOT NULL, DEFAULT | Thông tin updated_at |
| 9 | `CONSTRAINT` | inventory_reservations_quantity_check | None | Thông tin CONSTRAINT |

### Bảng: `membership_tiers`

**Ý nghĩa**: Hạng thành viên: Đồng, Bạc, Vàng, Kim cương. Admin có thể sửa qua CMS.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của membership_tiers |
| 2 | `name` | character | NOT NULL | Thông tin name |
| 3 | `slug` | character | NOT NULL | Thông tin slug |
| 4 | `min_points` | integer | NOT NULL, DEFAULT | Thông tin min_points |
| 5 | `discount_percent` | numeric(5,2) | NOT NULL, DEFAULT | Thông tin discount_percent |
| 6 | `description` | text | None | Thông tin description |
| 7 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |
| 8 | `updated_at` | timestamp | NOT NULL, DEFAULT | Thông tin updated_at |
| 9 | `CONSTRAINT` | membership_tiers_discount_percent_check | None | Thông tin CONSTRAINT |

### Bảng: `order_items`

**Ý nghĩa**: Chi tiết đơn hàng. Snapshot giá/tên SP tại thời điểm mua (QĐ3).

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của order_items |
| 2 | `order_id` | bigint | NOT NULL, Khóa ngoại (FK -> orders(id)) | Mã tham chiếu đến orders(id) |
| 3 | `product_variant_id` | bigint | NOT NULL, Khóa ngoại (FK -> product_variants(id)) | Mã tham chiếu đến product_variants(id) |
| 4 | `product_name` | character | NOT NULL | Thông tin product_name |
| 5 | `variant_info` | character | NOT NULL | Thông tin variant_info |
| 6 | `quantity` | integer | NOT NULL | Thông tin quantity |
| 7 | `unit_price` | numeric(12,2) | NOT NULL | Thông tin unit_price |
| 8 | `subtotal` | numeric(12,2) | NOT NULL | Thông tin subtotal |
| 9 | `CONSTRAINT` | order_items_quantity_check | None | Thông tin CONSTRAINT |
| 10 | `CONSTRAINT` | order_items_subtotal_check | None | Thông tin CONSTRAINT |
| 11 | `CONSTRAINT` | order_items_unit_price_check | None | Thông tin CONSTRAINT |

### Bảng: `order_status_histories`

**Ý nghĩa**: Timeline trang thai don hang phuc vu audit va Staff order detail.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của order_status_histories |
| 2 | `order_id` | bigint | NOT NULL, Khóa ngoại (FK -> orders(id)) | Mã tham chiếu đến orders(id) |
| 3 | `from_status` | public.order_status | None | Thông tin from_status |
| 4 | `to_status` | public.order_status | NOT NULL | Thông tin to_status |
| 5 | `changed_by` | bigint | Khóa ngoại (FK -> users(id)) | Mã tham chiếu đến users(id) |
| 6 | `changed_by_role` | public.user_role | None | Thông tin changed_by_role |
| 7 | `reason` | text | None | Thông tin reason |
| 8 | `metadata` | jsonb | None | Thông tin metadata |
| 9 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |

### Bảng: `orders`

**Ý nghĩa**: Đơn hàng (QĐ8). Ship: đơn < 500K → 30K, đơn >= 500K → miễn phí (app logic).

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của orders |
| 2 | `user_id` | bigint | NOT NULL, Khóa ngoại (FK -> users(id)) | Mã tham chiếu đến users(id) |
| 3 | `order_code` | character | NOT NULL | Thông tin order_code |
| 4 | `shipping_name` | character | NOT NULL | Thông tin shipping_name |
| 5 | `shipping_phone` | character | NOT NULL | Thông tin shipping_phone |
| 6 | `shipping_province` | character | NOT NULL | Thông tin shipping_province |
| 7 | `shipping_district` | character | NOT NULL | Thông tin shipping_district |
| 8 | `shipping_ward` | character | NOT NULL | Thông tin shipping_ward |
| 9 | `shipping_address` | character | NOT NULL | Thông tin shipping_address |
| 10 | `subtotal` | numeric(12,2) | NOT NULL | Thông tin subtotal |
| 11 | `shipping_fee` | numeric(12,2) | NOT NULL, DEFAULT | Thông tin shipping_fee |
| 12 | `discount_amount` | numeric(12,2) | NOT NULL, DEFAULT | Thông tin discount_amount |
| 13 | `total_amount` | numeric(12,2) | NOT NULL | Thông tin total_amount |
| 14 | `voucher_id` | bigint | Khóa ngoại (FK -> vouchers(id)) | Mã tham chiếu đến vouchers(id) |
| 15 | `status` | public.order_status | NOT NULL, DEFAULT | Thông tin status |
| 16 | `note` | text | None | Thông tin note |
| 17 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |
| 18 | `updated_at` | timestamp | NOT NULL, DEFAULT | Thông tin updated_at |
| 19 | `CONSTRAINT` | orders_discount_amount_check | None | Thông tin CONSTRAINT |
| 20 | `CONSTRAINT` | orders_shipping_fee_check | None | Thông tin CONSTRAINT |
| 21 | `CONSTRAINT` | orders_subtotal_check | None | Thông tin CONSTRAINT |
| 22 | `CONSTRAINT` | orders_total_amount_check | None | Thông tin CONSTRAINT |

### Bảng: `payment_attempts`

**Ý nghĩa**: Lan thu thanh toan online gan voi checkout session truoc khi co order.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của payment_attempts |
| 2 | `payment_reference` | character | NOT NULL | Thông tin payment_reference |
| 3 | `checkout_session_id` | bigint | NOT NULL, Khóa ngoại (FK -> checkout_sessions(id)) | Mã tham chiếu đến checkout_sessions(id) |
| 4 | `method` | public.payment_method | NOT NULL | Thông tin method |
| 5 | `amount` | numeric(12,2) | NOT NULL | Thông tin amount |
| 6 | `status` | public.payment_attempt_status | NOT NULL, DEFAULT | Thông tin status |
| 7 | `payment_url` | text | None | Thông tin payment_url |
| 8 | `gateway_transaction_id` | character | None | Thông tin gateway_transaction_id |
| 9 | `gateway_payload` | jsonb | None | Thông tin gateway_payload |
| 10 | `failure_reason` | text | None | Thông tin failure_reason |
| 11 | `requires_refund_reason` | text | None | Thông tin requires_refund_reason |
| 12 | `expires_at` | timestamp | NOT NULL | Thông tin expires_at |
| 13 | `completed_at` | timestamp | None | Thông tin completed_at |
| 14 | `failed_at` | timestamp | None | Thông tin failed_at |
| 15 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |
| 16 | `updated_at` | timestamp | NOT NULL, DEFAULT | Thông tin updated_at |
| 17 | `CONSTRAINT` | payment_attempts_amount_check | None | Thông tin CONSTRAINT |

### Bảng: `payments`

**Ý nghĩa**: Thanh toán. Không lưu thông tin thẻ — delegate cho VNPay/MoMo.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của payments |
| 2 | `order_id` | bigint | NOT NULL, Khóa ngoại (FK -> orders(id)) | Mã tham chiếu đến orders(id) |
| 3 | `method` | public.payment_method | NOT NULL | Thông tin method |
| 4 | `amount` | numeric(12,2) | NOT NULL | Thông tin amount |
| 5 | `status` | public.payment_status | NOT NULL, DEFAULT | Thông tin status |
| 6 | `transaction_id` | character | None | Thông tin transaction_id |
| 7 | `payment_data` | jsonb | None | Thông tin payment_data |
| 8 | `paid_at` | timestamp | None | Thông tin paid_at |
| 9 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |
| 10 | `CONSTRAINT` | payments_amount_check | None | Thông tin CONSTRAINT |

### Bảng: `product_images`

**Ý nghĩa**: Ảnh sản phẩm lưu trên S3. QĐ1: tối thiểu 1 ảnh main.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của product_images |
| 2 | `product_id` | bigint | NOT NULL, Khóa ngoại (FK -> products(id)) | Mã tham chiếu đến products(id) |
| 3 | `image_url` | text | NOT NULL | Thông tin image_url |
| 4 | `image_type` | public.image_type | NOT NULL, DEFAULT | Thông tin image_type |
| 5 | `display_order` | integer | NOT NULL, DEFAULT | Thông tin display_order |
| 6 | `alt_text` | character | None | Thông tin alt_text |
| 7 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |

### Bảng: `product_variants`

**Ý nghĩa**: Biến thể sản phẩm (QĐ2: SKU duy nhất, tồn kho >= 0). QĐ6: auto giảm khi đặt hàng.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của product_variants |
| 2 | `product_id` | bigint | NOT NULL, Khóa ngoại (FK -> products(id)) | Mã tham chiếu đến products(id) |
| 3 | `sku` | character | NOT NULL | Thông tin sku |
| 4 | `size` | character | NOT NULL | Thông tin size |
| 5 | `color` | character | NOT NULL | Thông tin color |
| 6 | `stock_quantity` | integer | NOT NULL, DEFAULT | Thông tin stock_quantity |
| 7 | `additional_price` | numeric(12,2) | NOT NULL, DEFAULT | Thông tin additional_price |
| 8 | `is_active` | boolean | NOT NULL, DEFAULT | Thông tin is_active |
| 9 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |
| 10 | `updated_at` | timestamp | NOT NULL, DEFAULT | Thông tin updated_at |
| 11 | `CONSTRAINT` | product_variants_stock_quantity_check | None | Thông tin CONSTRAINT |

### Bảng: `products`

**Ý nghĩa**: Sản phẩm. QĐ1: tên không trùng cùng danh mục. QĐ4: xóa mềm nếu đã có đơn hàng.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của products |
| 2 | `name` | character | NOT NULL | Thông tin name |
| 3 | `slug` | character | NOT NULL | Thông tin slug |
| 4 | `description` | text | None | Thông tin description |
| 5 | `material` | character | None | Thông tin material |
| 6 | `care_instructions` | text | None | Thông tin care_instructions |
| 7 | `category_id` | bigint | NOT NULL, Khóa ngoại (FK -> categories(id)) | Mã tham chiếu đến categories(id) |
| 8 | `base_price` | numeric(12,2) | NOT NULL | Thông tin base_price |
| 9 | `sale_price` | numeric(12,2) | None | Thông tin sale_price |
| 10 | `is_active` | boolean | NOT NULL, DEFAULT | Thông tin is_active |
| 11 | `is_featured` | boolean | NOT NULL, DEFAULT | Thông tin is_featured |
| 12 | `total_sold` | integer | NOT NULL, DEFAULT | Thông tin total_sold |
| 13 | `average_rating` | numeric(3,2) | DEFAULT | Thông tin average_rating |
| 14 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |
| 15 | `updated_at` | timestamp | NOT NULL, DEFAULT | Thông tin updated_at |
| 16 | `deleted_at` | timestamp | None | Thông tin deleted_at |
| 17 | `CONSTRAINT` | products_average_rating_check | None | Thông tin CONSTRAINT |
| 18 | `CONSTRAINT` | products_base_price_check | None | Thông tin CONSTRAINT |
| 19 | `CONSTRAINT` | products_sale_price_check | None | Thông tin CONSTRAINT |
| 20 | `CONSTRAINT` | products_total_sold_check | None | Thông tin CONSTRAINT |

### Bảng: `review_images`

**Ý nghĩa**: Ảnh đánh giá (QĐ13: tối đa 5 ảnh — enforce ở app logic).

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của review_images |
| 2 | `review_id` | bigint | NOT NULL, Khóa ngoại (FK -> reviews(id)) | Mã tham chiếu đến reviews(id) |
| 3 | `image_url` | text | NOT NULL | Thông tin image_url |
| 4 | `display_order` | integer | NOT NULL, DEFAULT | Thông tin display_order |
| 5 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |

### Bảng: `reviews`

**Ý nghĩa**: Đánh giá sản phẩm (QĐ9, QĐ13). Chỉ KH đã mua + đơn hoàn thành mới được đánh giá.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của reviews |
| 2 | `user_id` | bigint | NOT NULL, Khóa ngoại (FK -> users(id)) | Mã tham chiếu đến users(id) |
| 3 | `product_id` | bigint | NOT NULL, Khóa ngoại (FK -> products(id)) | Mã tham chiếu đến products(id) |
| 4 | `order_id` | bigint | NOT NULL, Khóa ngoại (FK -> orders(id)) | Mã tham chiếu đến orders(id) |
| 5 | `rating` | smallint | NOT NULL | Thông tin rating |
| 6 | `content` | text | NOT NULL | Thông tin content |
| 7 | `is_approved` | boolean | NOT NULL, DEFAULT | Thông tin is_approved |
| 8 | `admin_reply` | text | None | Thông tin admin_reply |
| 9 | `replied_at` | timestamp | None | Thông tin replied_at |
| 10 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |
| 11 | `updated_at` | timestamp | NOT NULL, DEFAULT | Thông tin updated_at |
| 12 | `delete_reason` | text | None | Thông tin delete_reason |
| 13 | `is_active` | boolean | NOT NULL, DEFAULT | Thông tin is_active |
| 14 | `CONSTRAINT` | reviews_content_check | None | Thông tin CONSTRAINT |
| 15 | `CONSTRAINT` | reviews_rating_check | None | Thông tin CONSTRAINT |

### Bảng: `users`

**Ý nghĩa**: Người dùng hệ thống: admin và khách hàng. QĐ14: không được sửa email. QĐ16: email unique.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của users |
| 2 | `email` | character | NOT NULL | Thông tin email |
| 3 | `password_hash` | character | None | Thông tin password_hash |
| 4 | `full_name` | character | NOT NULL | Thông tin full_name |
| 5 | `phone` | character | None | Thông tin phone |
| 6 | `gender` | public.gender_type | None | Thông tin gender |
| 7 | `date_of_birth` | date | None | Thông tin date_of_birth |
| 8 | `avatar_url` | text | None | Thông tin avatar_url |
| 9 | `role` | public.user_role | NOT NULL, DEFAULT | Thông tin role |
| 10 | `loyalty_points` | integer | NOT NULL, DEFAULT | Thông tin loyalty_points |
| 11 | `membership_tier_id` | bigint | Khóa ngoại (FK -> membership_tiers(id)) | Mã tham chiếu đến membership_tiers(id) |
| 12 | `auth_provider` | character | DEFAULT | Thông tin auth_provider |
| 13 | `email_verified` | boolean | NOT NULL, DEFAULT | Thông tin email_verified |
| 14 | `is_active` | boolean | NOT NULL, DEFAULT | Thông tin is_active |
| 15 | `last_login_at` | timestamp | None | Thông tin last_login_at |
| 16 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |
| 17 | `updated_at` | timestamp | NOT NULL, DEFAULT | Thông tin updated_at |
| 18 | `deleted_at` | timestamp | None | Thông tin deleted_at |
| 19 | `CONSTRAINT` | users_loyalty_points_check | None | Thông tin CONSTRAINT |

### Bảng: `voucher_reservations`

**Ý nghĩa**: Giu luot su dung voucher tam thoi cho checkout, chua tang times_used khi reserve.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của voucher_reservations |
| 2 | `checkout_session_id` | bigint | NOT NULL, Khóa ngoại (FK -> checkout_sessions(id)) | Mã tham chiếu đến checkout_sessions(id) |
| 3 | `voucher_id` | bigint | NOT NULL, Khóa ngoại (FK -> vouchers(id)) | Mã tham chiếu đến vouchers(id) |
| 4 | `discount_amount` | numeric(12,2) | NOT NULL, DEFAULT | Thông tin discount_amount |
| 5 | `status` | public.reservation_status | NOT NULL, DEFAULT | Thông tin status |
| 6 | `expires_at` | timestamp | NOT NULL | Thông tin expires_at |
| 7 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |
| 8 | `updated_at` | timestamp | NOT NULL, DEFAULT | Thông tin updated_at |
| 9 | `CONSTRAINT` | voucher_reservations_discount_amount_check | None | Thông tin CONSTRAINT |

### Bảng: `vouchers`

**Ý nghĩa**: Mã giảm giá (QĐ7). QĐ11: mỗi đơn chỉ 1 voucher, kiểm tra điều kiện.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của vouchers |
| 2 | `code` | character | NOT NULL | Thông tin code |
| 3 | `discount_type` | public.discount_type | NOT NULL | Thông tin discount_type |
| 4 | `discount_value` | numeric(12,2) | NOT NULL | Thông tin discount_value |
| 5 | `max_discount_amount` | numeric(12,2) | None | Thông tin max_discount_amount |
| 6 | `min_order_amount` | numeric(12,2) | NOT NULL, DEFAULT | Thông tin min_order_amount |
| 7 | `start_date` | timestamp | NOT NULL | Thông tin start_date |
| 8 | `end_date` | timestamp | NOT NULL | Thông tin end_date |
| 9 | `usage_limit` | integer | NOT NULL, DEFAULT | Thông tin usage_limit |
| 10 | `times_used` | integer | NOT NULL, DEFAULT | Thông tin times_used |
| 11 | `is_active` | boolean | NOT NULL, DEFAULT | Thông tin is_active |
| 12 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |
| 13 | `updated_at` | timestamp | NOT NULL, DEFAULT | Thông tin updated_at |
| 14 | `CONSTRAINT` | vouchers_check | None | Thông tin CONSTRAINT |
| 15 | `CONSTRAINT` | vouchers_discount_value_check | None | Thông tin CONSTRAINT |
| 16 | `CONSTRAINT` | vouchers_times_used_check | None | Thông tin CONSTRAINT |

### Bảng: `wishlists`

**Ý nghĩa**: Danh sách sản phẩm yêu thích.

| STT | Tên Field | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|---|
| 1 | `id` | bigint | NOT NULL, Khóa chính (PK) | Mã định danh của wishlists |
| 2 | `user_id` | bigint | NOT NULL, Khóa ngoại (FK -> users(id)) | Mã tham chiếu đến users(id) |
| 3 | `product_id` | bigint | NOT NULL, Khóa ngoại (FK -> products(id)) | Mã tham chiếu đến products(id) |
| 4 | `created_at` | timestamp | NOT NULL, DEFAULT | Thông tin created_at |

