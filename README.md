# Clothing Store Backend

Spring Boot backend cho đồ án Clothing Store. Repo này hiện đã được bổ sung phần backend thuộc scope của Quân:

- Rate limiting cho các endpoint public/auth quan trọng.
- Seed data mở rộng để đủ dữ liệu demo và chấm quy trình.

## Quick Start

1. Tạo file `.env` hoặc export các biến môi trường tối thiểu:
   - `DB_URL`
   - `DB_USERNAME`
   - `DB_PASSWORD`
   - `REDIS_HOST`
   - `REDIS_PORT`
   - `JWT_SECRET`
2. Chạy ứng dụng:

```bash
./mvnw spring-boot:run
```

Backend mặc định chạy ở `http://localhost:8080`.

## Database Setup

Thứ tự khởi tạo dữ liệu đề xuất:

1. `src/main/resources/db/database_schema.sql`
2. `src/main/resources/db/phase1_checkout_schema_patch.sql`
3. `src/main/resources/db/phase2_momo_payment_method_patch.sql`
4. `src/main/resources/db/phase3_order_status_history_schema_patch.sql`
5. `src/main/resources/db/phase4_review_moderation_schema_patch.sql`
6. `src/main/resources/db/seed_data_1.sql`
7. `src/main/resources/db/seed_data_2.sql`
8. `src/main/resources/db/seed_data_3.sql`
9. `src/main/resources/db/seed_data_4.sql`

Sau khi chạy đủ 4 file seed:

- 1 admin + 10 khách hàng seed sẵn
- 50 sản phẩm có ảnh, mô tả tiếng Việt, variant
- 20 đơn hàng mẫu

## Demo Accounts

Tài khoản admin mặc định có trong `database_schema.sql`:

- Email: `admin@nhom10.com`
- Password: `Admin@123`

Các tài khoản khách hàng trong `seed_data_1.sql` đang dùng cùng bcrypt hash với tài khoản admin để tiện demo nhanh. Một vài email mẫu:

- `nguyenvana@gmail.com`
- `tranthib@gmail.com`
- `phamthid@gmail.com`
- `lythik@gmail.com`

## Rate Limiting

Hệ thống hiện áp dụng rate limiting theo IP và trả về `429 Too Many Requests` với các header:

- `X-RateLimit-Limit`
- `X-RateLimit-Remaining`
- `X-RateLimit-Reset`
- `Retry-After` (chỉ có khi bị chặn)

Rule mặc định:

- `POST /api/auth/login`: 5 request / 60 giây
- `POST /api/auth/register`, `/api/auth/resend-verification`, `/api/auth/forgot-password`, `/api/auth/reset-password`: 5 request / 10 phút
- `POST /api/auth/google`: 10 request / 60 giây
- `POST /api/auth/refresh`: 30 request / 60 giây
- `GET /api/products/**`, `/api/guest/**`, `/sitemap.xml`, `/robots.txt`: 180 request / 60 giây

## Gợi Ý Frontend

Frontend có thể dùng `Retry-After` hoặc `X-RateLimit-Reset` để hiển thị thông báo chờ khi gặp `429`, ví dụ ở login/search/autocomplete.

## Security Hardening Notes

Ngoài rate limiting, phần backend của Quân hiện đã bổ sung thêm vài lớp hardening nhỏ để giảm rủi ro demo bị bắt lỗi:

- Chỉ tin `X-Forwarded-For` và `X-Real-IP` khi request đi qua proxy nội bộ/loopback, tránh client ngoài tự spoof IP để né rate limit.
- CORS chuyển sang cấu hình origin allowlist và expose sẵn các header rate limit để frontend đọc được `Retry-After`, `X-RateLimit-*`.
- JWT token sai chữ ký được reject gọn trong provider thay vì làm log auth nhiễu.
- JWT filter bỏ qua xử lý nếu security context đã có authentication, đồng thời parse `Bearer` header theo kiểu trim + không phân biệt hoa thường để tránh lỗi vặt từ client.
