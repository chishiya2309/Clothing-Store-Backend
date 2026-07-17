# 👕 Clothing Store Backend

RESTful API server cho ứng dụng thương mại điện tử thời trang, xây dựng bằng **Spring Boot 4** với kiến trúc phân lớp (Controller → Service → Repository) và áp dụng nhiều **Design Pattern** thực tế.

> **Đồ án môn học** — Nhóm sinh viên tại ĐH Công nghệ Kỹ thuật TP.HCM (HCMUTE)

---

## 📑 Mục Lục

- [Tech Stack](#-tech-stack)
- [Kiến Trúc Tổng Quan](#-kiến-trúc-tổng-quan)
- [Tính Năng Chính](#-tính-năng-chính)
- [Design Patterns](#-design-patterns)
- [Quick Start](#-quick-start)
- [Biến Môi Trường](#-biến-môi-trường)
- [Khởi Tạo Database](#-khởi-tạo-database)
- [Tài Khoản Demo](#-tài-khoản-demo)
- [API Documentation](#-api-documentation)
- [Rate Limiting](#-rate-limiting)
- [Testing](#-testing)
- [Docker](#-docker)
- [CI/CD & Deployment](#-cicd--deployment)
- [Cấu Trúc Dự Án](#-cấu-trúc-dự-án)

---

## 🛠 Tech Stack

| Layer         | Công nghệ                                       |
| ------------- | ----------------------------------------------- |
| **Framework** | Spring Boot 4.0.6 (Java 17)                     |
| **Database**  | PostgreSQL + Spring Data JPA (Hibernate)        |
| **Caching**   | Redis (Session, Rate Limiting)                  |
| **Security**  | Spring Security + JWT (jjwt 0.12.6)             |
| **OAuth**     | Google Sign-In (google-api-client)              |
| **Email**     | Brevo (Sendinblue) Transactional API            |
| **Storage**   | AWS S3 (Banner / Image upload)                  |
| **Payment**   | VNPay + MoMo (sandbox & production)             |
| **ML**        | Smile 3.1 (K-Means Clustering — gợi ý sản phẩm) |
| **API Docs**  | SpringDoc OpenAPI 3 (Swagger UI)                |
| **Testing**   | JUnit 5 + Testcontainers (PostgreSQL)           |
| **CI/CD**     | GitHub Actions → Render                         |
| **Container** | Docker (multi-stage build)                      |

---

## 🏗 Kiến Trúc Tổng Quan

```
┌──────────────────────────────────────────────────────┐
│                    Client (Frontend)                 │
└──────────────────┬───────────────────────────────────┘
                   │ HTTP/REST
┌──────────────────▼───────────────────────────────────┐
│  RateLimitingFilter → JwtAuthenticationFilter        │
│  ┌─────────────────────────────────────────────────┐ │
│  │              Controller Layer                   │ │
│  │  Guest / Customer / Staff / Admin Controllers   │ │
│  └──────────────────┬──────────────────────────────┘ │
│  ┌──────────────────▼──────────────────────────────┐ │
│  │              Service Layer                      │ │
│  │  Business Logic + Design Patterns               │ │
│  │  (Strategy, Command, Chain, Observer, ...)      │ │
│  └──────────────────┬──────────────────────────────┘ │
│  ┌──────────────────▼──────────────────────────────┐ │
│  │            Repository Layer (JPA)               │ │
│  └──────────────────┬──────────────────────────────┘ │
│                     │                                │
│  ┌─────────┐  ┌─────▼────┐  ┌──────────┐           │
│  │  Redis  │  │PostgreSQL│  │  AWS S3   │           │
│  └─────────┘  └──────────┘  └──────────┘           │
└──────────────────────────────────────────────────────┘
```

---

## ✨ Tính Năng Chính

### 🛒 Khách hàng (Customer)

- Đăng ký / Đăng nhập (Email + Google OAuth)
- Xác thực email, quên mật khẩu, đổi mật khẩu
- Duyệt sản phẩm, bộ sưu tập, danh mục
- Tìm kiếm với trending search
- Giỏ hàng (Guest cart + Authenticated cart)
- Wishlist (Danh sách yêu thích)
- Checkout với nhiều phương thức thanh toán (COD, VNPay, MoMo)
- Quản lý đơn hàng & lịch sử trạng thái
- Đánh giá sản phẩm (có upload ảnh)
- Sổ địa chỉ giao hàng
- Hệ thống voucher / mã giảm giá
- **Gợi ý sản phẩm** bằng K-Means Clustering (Smile ML)

### 👨‍💼 Nhân viên (Staff)

- Quản lý sản phẩm (CRUD, variant, ảnh)
- Quản lý danh mục & bộ sưu tập
- Quản lý đơn hàng (cập nhật trạng thái)
- Quản lý voucher & Flash Sale
- Kiểm duyệt đánh giá
- Báo cáo tồn kho

### 🔑 Quản trị (Admin)

- Quản lý người dùng & phân quyền
- Dashboard & Báo cáo doanh thu
- Quản lý banner quảng cáo
- A/B Testing (Experiment)
- Thống kê bán hàng

### ⚡ Flash Sale

- Chiến dịch giảm giá theo thời gian
- Reservation (giữ chỗ sản phẩm)
- Tự động hết hạn

### 🔐 Bảo mật

- JWT Access Token + Refresh Token
- Rate Limiting theo IP (Redis-backed)
- CORS configuration
- Role-based Access Control (CUSTOMER / STAFF / ADMIN)

### 📧 Email

- Email xác thực tài khoản
- Email đặt lại mật khẩu
- Tích hợp Brevo Transactional API

### 🔍 SEO

- Sitemap XML tự động
- Robots.txt

---

## 🧩 Design Patterns

Dự án áp dụng **13 Design Pattern** trong package `pattern/`:

| Pattern                     | Mục đích                                                    |
| --------------------------- | ----------------------------------------------------------- |
| **Strategy**                | Chọn phương thức thanh toán (COD / VNPay / MoMo)            |
| **Command**                 | Xử lý hành động đơn hàng                                    |
| **Chain of Responsibility** | Pipeline xử lý checkout                                     |
| **Observer**                | Event-driven (đơn hàng tạo, trạng thái thay đổi, gửi email) |
| **Template Method**         | Quy trình xử lý chung với bước tùy biến                     |
| **Factory**                 | Tạo đối tượng linh hoạt                                     |
| **Facade**                  | Đơn giản hóa các nghiệp vụ phức tạp                         |
| **Adapter**                 | Tích hợp hệ thống bên ngoài                                 |
| **State**                   | Quản lý trạng thái đơn hàng / bộ sưu tập                    |
| **Specification**           | Query điều kiện linh hoạt                                   |
| **Visitor**                 | Duyệt cấu trúc dữ liệu                                      |
| **Policy**                  | Kiểm tra chuyển trạng thái đơn hàng                         |
| **Reservation**             | Giữ chỗ tồn kho & voucher                                   |

---

## 🚀 Quick Start

### Yêu cầu

- **Java 17+** (Eclipse Temurin khuyến nghị)
- **PostgreSQL 15+**
- **Redis 7+**
- **Maven 3.9+** (hoặc dùng `mvnw` wrapper đi kèm)

### Các bước

```bash
# 1. Clone repo
git clone https://github.com/chishiya2309/Clothing-Store-Backend.git
cd Clothing-Store-Backend

# 2. Tạo file .env từ template
cp .env.example .env
# Sửa giá trị trong .env cho phù hợp

# 3. Khởi tạo database (xem phần "Khởi Tạo Database" bên dưới)

# 4. Chạy ứng dụng
./mvnw spring-boot:run
```

Backend mặc định chạy tại **http://localhost:8080**.

Swagger UI: **http://localhost:8080/swagger-ui.html**

---

## 🔧 Biến Môi Trường

Tạo file `.env` tại thư mục gốc. Tham khảo `.env.example`:

| Biến               | Bắt buộc | Mô tả                                                                       |
| ------------------ | :------: | --------------------------------------------------------------------------- |
| `DB_URL`           |    ✅    | JDBC URL PostgreSQL (vd: `jdbc:postgresql://localhost:5432/clothing_store`) |
| `DB_USERNAME`      |    ✅    | Username database                                                           |
| `DB_PASSWORD`      |    ✅    | Password database                                                           |
| `REDIS_HOST`       |    ✅    | Host Redis (vd: `localhost`)                                                |
| `REDIS_PORT`       |    ✅    | Port Redis (mặc định: `6379`)                                               |
| `REDIS_PASSWORD`   |          | Password Redis (bỏ trống nếu không có)                                      |
| `JWT_SECRET`       |    ✅    | Secret key cho JWT (≥ 256-bit)                                              |
| `BREVO_API_KEY`    |    ✅    | API key Brevo (Sendinblue)                                                  |
| `GOOGLE_CLIENT_ID` |    ✅    | Google OAuth Client ID                                                      |
| `AWS_ACCESS_KEY`   |          | AWS Access Key (cho S3 upload)                                              |
| `AWS_SECRET_KEY`   |          | AWS Secret Key                                                              |
| `AWS_REGION`       |          | AWS Region (mặc định: `ap-southeast-1`)                                     |
| `AWS_S3_BUCKET`    |          | Tên S3 bucket                                                               |
| `VNPAY_ENABLED`    |          | Bật VNPay (`true`/`false`, mặc định: `false`)                               |
| `MOMO_ENABLED`     |          | Bật MoMo (`true`/`false`, mặc định: `false`)                                |
| `BACKEND_URL`      |          | URL backend (mặc định: `http://localhost:8080`)                             |
| `FRONTEND_URL`     |          | URL frontend (mặc định: `http://localhost:5173`)                            |

> 💡 Xem hướng dẫn tích hợp thanh toán tại `docs/VNPAY_SANDBOX_GUIDE.md` và `docs/MOMO_SANDBOX_GUIDE.md`.

---

## 🗄 Khởi Tạo Database

Chạy các script SQL theo **đúng thứ tự** trên PostgreSQL:

```
src/main/resources/db/
├── 1. database_schema.sql                          # Schema chính
├── 2. phase1_checkout_schema_patch.sql              # Checkout & Payment
├── 3. phase2_momo_payment_method_patch.sql          # MoMo payment method
├── 4. phase3_order_status_history_schema_patch.sql   # Lịch sử trạng thái đơn
├── 5. phase4_review_moderation_schema_patch.sql      # Kiểm duyệt đánh giá
├── 6. phase5_flash_sale_schema_patch.sql             # Flash Sale
├── 7. phase5_ab_testing_analytics_schema_patch.sql   # A/B Testing & Analytics
├── 8. seed_data_1.sql                               # Users + Products cơ bản
├── 9. seed_data_2.sql                               # Thêm sản phẩm + variant
├── 10. seed_data_3.sql                              # Đơn hàng mẫu
└── 11. seed_data_4.sql                              # Dữ liệu mở rộng
```

Sau khi chạy đủ seed data:

- **1 admin** + **10 khách hàng** seed sẵn
- **50 sản phẩm** có ảnh, mô tả tiếng Việt, variant (size/color)
- **20 đơn hàng** mẫu với nhiều trạng thái

> ⚠️ Ứng dụng dùng `ddl-auto: validate` — Hibernate chỉ kiểm tra, **không tự tạo bảng**. Bạn phải chạy script SQL trước khi khởi động app.

---

## 👤 Tài Khoản Demo

### Admin

| Email              | Password    |
| ------------------ | ----------- |
| `admin@nhom10.com` | `Admin@123` |

### Khách hàng (cùng password `Admin@123`)

| Email                  |
| ---------------------- |
| `nguyenvana@gmail.com` |
| `tranthib@gmail.com`   |
| `phamthid@gmail.com`   |
| `lythik@gmail.com`     |

---

## 📖 API Documentation

Khi ứng dụng đang chạy ở profile **dev**, truy cập Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

### Tổng quan API Endpoints

| Nhóm          | Prefix                        | Mô tả                                               |
| ------------- | ----------------------------- | --------------------------------------------------- |
| **Auth**      | `/api/auth/*`                 | Đăng ký, đăng nhập, refresh token, Google OAuth     |
| **Guest**     | `/api/guest/*`                | Sản phẩm, danh mục, bộ sưu tập, Flash Sale (public) |
| **Products**  | `/api/products/*`             | Tìm kiếm, chi tiết sản phẩm                         |
| **Cart**      | `/api/cart/*`                 | Giỏ hàng (authenticated)                            |
| **Checkout**  | `/api/checkout/*`             | Tạo phiên thanh toán                                |
| **Orders**    | `/api/orders/*`               | Đơn hàng khách hàng                                 |
| **Reviews**   | `/api/reviews/*`              | Đánh giá sản phẩm                                   |
| **Wishlist**  | `/api/wishlist/*`             | Danh sách yêu thích                                 |
| **Address**   | `/api/addresses/*`            | Sổ địa chỉ                                          |
| **Voucher**   | `/api/vouchers/*`             | Mã giảm giá                                         |
| **Profile**   | `/api/profile/*`              | Thông tin cá nhân                                   |
| **Recommend** | `/api/recommendations/*`      | Gợi ý sản phẩm (ML)                                 |
| **Staff**     | `/api/staff/*`                | Quản lý (nhân viên)                                 |
| **Admin**     | `/api/admin/*`                | Quản trị hệ thống                                   |
| **Payment**   | `/api/payments/*`             | Callback VNPay / MoMo                               |
| **SEO**       | `/sitemap.xml`, `/robots.txt` | SEO endpoints                                       |

---

## 🛡 Rate Limiting

Hệ thống áp dụng rate limiting theo IP (Redis-backed), trả về `429 Too Many Requests` với các header chuẩn:

| Header                  | Mô tả                           |
| ----------------------- | ------------------------------- |
| `X-RateLimit-Limit`     | Số request tối đa trong window  |
| `X-RateLimit-Remaining` | Số request còn lại              |
| `X-RateLimit-Reset`     | Thời điểm reset (epoch seconds) |
| `Retry-After`           | Số giây chờ (chỉ khi bị chặn)   |

### Cấu hình mặc định

| Endpoint                                          | Limit | Window  |
| ------------------------------------------------- | :---: | ------- |
| `POST /api/auth/login`                            |   5   | 60 giây |
| `POST /api/auth/register`, `forgot-password`, ... |   5   | 10 phút |
| `POST /api/auth/google`                           |  10   | 60 giây |
| `POST /api/auth/refresh`                          |  30   | 60 giây |
| `GET /api/products/**`, `/api/guest/**`           |  180  | 60 giây |

> 💡 **Frontend tip:** Dùng `Retry-After` hoặc `X-RateLimit-Reset` để hiển thị countdown khi gặp `429`.

---

## 🧪 Testing

```bash
# Chạy unit tests
./mvnw test

# Chạy integration tests (cần Docker cho Testcontainers)
./mvnw verify -Pintegration

# Build + test đầy đủ
./mvnw clean verify
```

Integration tests sử dụng **Testcontainers** để tự động tạo PostgreSQL container.

---

## 🐳 Docker

### Build & Run

```bash
# Build image
docker build -t clothing-store-backend .

# Run với docker-compose
docker compose up -d
```

Docker sử dụng **multi-stage build** (Eclipse Temurin 17):

- **Stage 1:** Build JAR với Maven
- **Stage 2:** Runtime JRE-only image (~128MB RAM)

> Đảm bảo file `.env` đã được cấu hình trước khi chạy `docker compose up`.

---

## 🚢 CI/CD & Deployment

### GitHub Actions

Pipeline tự động khi push/PR vào `main`:

1. **🧪 Build & Test** — Checkout → JDK 17 → `mvnw clean verify`
2. **🚀 Deploy to Render** — Trigger deploy hook (chỉ khi push vào `main`)

### Render

Cấu hình deployment trong `render.yaml`:

- **Runtime:** Docker
- **Health Check:** `/actuator/health`
- **Profile:** `prod`

### Spring Profiles

| Profile | Mục đích                                        |
| ------- | ----------------------------------------------- |
| `dev`   | Local development (Swagger UI bật, SQL logging) |
| `test`  | Testing (Testcontainers)                        |
| `prod`  | Production (Swagger tắt, tối ưu hiệu năng)      |

---

## 📂 Cấu Trúc Dự Án

```
Clothing-Store-Backend/
├── .github/workflows/          # CI/CD pipeline
├── docs/                       # Hướng dẫn tích hợp (VNPay, MoMo)
├── src/
│   ├── main/
│   │   ├── java/.../backend/
│   │   │   ├── config/         # Cấu hình (CORS, Redis, S3, Security, OpenAPI, Payment)
│   │   │   ├── controller/     # REST Controllers
│   │   │   │   ├── admin/      #   └── Admin endpoints
│   │   │   │   └── staff/      #   └── Staff endpoints
│   │   │   ├── dto/            # Data Transfer Objects
│   │   │   ├── entity/         # JPA Entities (34 entities)
│   │   │   ├── enums/          # Enum types (OrderStatus, PaymentMethod, ...)
│   │   │   ├── event/          # Domain Events + Listeners
│   │   │   ├── exception/      # Custom exceptions & Global handler
│   │   │   ├── listener/       # Event listeners
│   │   │   ├── pattern/        # 🧩 Design Patterns (13 patterns)
│   │   │   │   ├── adapter/
│   │   │   │   ├── chain/
│   │   │   │   ├── command/
│   │   │   │   ├── facade/
│   │   │   │   ├── factory/
│   │   │   │   ├── observer/
│   │   │   │   ├── policy/
│   │   │   │   ├── reservation/
│   │   │   │   ├── specification/
│   │   │   │   ├── state/
│   │   │   │   ├── strategy/
│   │   │   │   ├── template/
│   │   │   │   └── visitor/
│   │   │   ├── policy/         # Business policies
│   │   │   ├── repository/     # Spring Data JPA repositories
│   │   │   ├── scheduler/      # Scheduled tasks (checkout expiration)
│   │   │   ├── security/       # JWT, Rate Limiting, Authentication
│   │   │   ├── service/        # Business logic (42+ services)
│   │   │   └── util/           # Utility classes
│   │   └── resources/
│   │       ├── application*.yml # Spring profiles config
│   │       └── db/             # SQL schema + seed data
│   └── test/                   # Unit & Integration tests
├── Dockerfile                  # Multi-stage Docker build
├── docker-compose.yml          # Docker Compose config
├── render.yaml                 # Render deployment config
└── pom.xml                     # Maven dependencies
```

---

## 📄 License

Đồ án học tập — Nhóm sinh viên tại HCMUTE.
