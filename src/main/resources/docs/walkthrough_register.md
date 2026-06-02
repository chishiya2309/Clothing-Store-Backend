# Hoàn tất Triển khai Đăng ký tài khoản (QĐ16)

Tôi đã hoàn thành việc triển khai chức năng đăng ký tài khoản qua Email có xác thực (OTP Token via Redis + Email via Brevo) theo quy định QĐ16 của hệ thống Clothing Store.

## Các thay đổi chính

- **Bổ sung thư viện**: Cập nhật `pom.xml` để sử dụng JWT (`jjwt` API) và thêm các test dependencies cần thiết (`spring-boot-starter-test`, `spring-security-test`).
- **Cấu hình hệ thống (`application-dev.yml`)**: Thêm cấu hình JWT tokens, thông số Brevo API, và `app.verification-token-ttl`.
- **Spring Security & Redis (`config/`)**: 
  - Khởi tạo `SecurityConfig.java` để tắt CSRF và cho phép các endpoints `/api/auth/**` public.
  - Setup `JwtAuthenticationFilter` và `JwtTokenProvider` cho logic phân giải token JWT.
  - Setup `RedisConfig.java` với `StringRedisSerializer` để xử lý session / tokens.
  - Setup `AsyncConfig.java` cho việc gửi email bất đồng bộ.
- **Repositories (`repository/`)**: 
  - `UserRepository` (với `existsByEmail` check trùng lặp).
  - `MembershipTierRepository` (để lấy rank mặc định).
- **Security & User Details (`security/`)**:
  - `CustomUserDetailsService`: Liên kết Spring Security với entity `User` (kiểm tra trạng thái isActive và role).
- **Service Layer (`service/`)**:
  - `AuthServiceImpl`: Logic kiểm tra trùng email, mã hóa mật khẩu (`BCryptPasswordEncoder`), lưu account với trạng thái `emailVerified = false`, tạo UUID token đẩy lên Redis (TTL 15p). Cung cấp hàm `verifyEmail()` và `resendVerificationEmail()`.
  - `BrevoEmailServiceImpl`: Sử dụng `RestClient` để kết nối thẳng tới API của Brevo v3 (đỡ phải cài đặt SDK cũ).
- **API Endpoints (`controller/AuthController.java`)**: 
  - `POST /api/auth/register`
  - `GET /api/auth/verify-email?token=...`
  - `POST /api/auth/resend-verification`
- **Unit Testing (`src/test/...`)**: 
  - Đã thiết lập JUnit 5 + Mockito cho `AuthServiceImplTest` (bao phủ mọi logic service).
  - Thiết lập Standalone MockMvc cho `AuthControllerTest` để kiểm tra HTTP layer mapping.

> [!TIP]
> **Về việc gửi Email Async**: Brevo REST API call đã được wrap bằng `@Async` để đảm bảo API Đăng ký tài khoản phản hồi lập tức (201 Created) mà không bị treo chờ network request gửi thư hoàn tất.

> [!WARNING]
> **Biến môi trường cần thiết**: Hãy đảm bảo bạn đã cung cấp `JWT_SECRET` (chuỗi Base64 dài) và `BREVO_API_KEY` vào file `.env` hoặc trực tiếp trong runtime configuration của IDE. 

## Kiểm thử

Tất cả các Unit tests đã chạy thành công qua Maven (`BUILD SUCCESS`), chứng minh logic:
1. Đăng ký thành công tạo Redis token.
2. Từ chối đăng ký với email trùng.
3. Xác thực email chuyển `emailVerified = true` và xóa Redis token.
4. Yêu cầu gửi lại email xác thực hoạt động đúng.

Tiếp theo, bạn có thể kiểm thử Manual qua **Swagger UI** (`http://localhost:8080/swagger-ui.html`) sau khi khởi động app và cấu hình đầy đủ biến môi trường.

Nếu mọi thứ đã chuẩn xác, chúng ta có thể sang phase tiếp theo (đăng ký bằng Google OAuth) hoặc tiến hành viết API cho Login. Bạn muốn làm gì tiếp theo?
