# Phân Tích & Đề Xuất Áp Dụng Design Pattern Cho Codebase Clothing Store Backend

Dựa vào việc kiểm tra kiến trúc codebase hiện tại của dự án, dự án đang là đồ án môn **Mẫu Thiết Kế Phần Mềm (Design Pattern)** nên việc tích hợp và giải thích các pattern là rất quan trọng.

Dưới đây là các Design Pattern đã được áp dụng, cũng như các đề xuất có thể áp dụng ngay để nâng cấp hệ thống:

---

## 1. Các Design Pattern CÓ THỂ ÁP DỤNG (Đề xuất)

### 1.1. Strategy Pattern (Mẫu Chiến lược)

- **Bối cảnh:** Hiện tại hệ thống có Enum `PaymentMethod` gồm `cod`, `vnpay`, `momo`. Việc xử lý thanh toán cho từng phương thức này sẽ khác biệt hoàn toàn (gọi các API khác nhau, mã hóa checksum khác nhau). Nếu dùng `if/else` hoặc `switch/case` trong `OrderService` hay `PaymentService`, code sẽ rất cồng kềnh và vi phạm nguyên lý OCP (Open-Closed Principle).
- **Cách áp dụng:**
  - Tạo Interface `PaymentStrategy` có hàm `String processPayment(Order order)`.
  - Tạo các class triển khai: `VNPayStrategy`, `MomoStrategy`, `CodStrategy`.
  - Tạo `PaymentContext` để thực thi strategy dựa vào Enum tương ứng.

### 1.2. Factory Method Pattern (Mẫu Phương thức Nông trại)

- **Bối cảnh:** Đi kèm với Strategy Pattern, chúng ta cần một cơ sở để khởi tạo các Strategy object phù hợp.
- **Cách áp dụng:**
  - Tạo một `PaymentStrategyFactory`. Factory này sẽ nhận tham số là `PaymentMethod` (hoặc String) và trả về đúng đối tượng `PaymentStrategy` tương ứng mà không cần phải gọi toán tử `new` rải rác khắp nơi.

### 1.3. State Pattern (Mẫu Trạng thái)

- **Bối cảnh:** Đối tượng `Order` trong hệ thống có Enum `OrderStatus` gồm: `pending`, `processing`, `shipping`, `completed`, `cancelled`. Quá trình chuyển đổi giữa các trạng thái có các luật lệ khắt khe (VD: Không thể chuyển từ `completed` sang `cancelled`, từ `cancelled` không thể quay lại `processing`).
- **Cách áp dụng:**
  - Thay vì dùng biến trạng thái và nhiều câu lệnh `if/else` để kiểm tra logic trước khi cập nhật, ta xây dựng interface `OrderState`.
  - Các trạng thái cụ thể (`PendingState`, `ProcessingState`...) sẽ cài đặt interface này và tự xử lý logic cho phép hoặc từ chối chuyển sang trạng thái kế tiếp.

### 1.4. Observer Pattern (Mẫu Quan sát) - [ĐÃ TRIỂN KHAI]

- **Bối cảnh:** Trong class `AuthServiceImpl`, trước đây sau khi lưu User mới vào DB (`register`) hoặc khi yêu cầu đặt lại mật khẩu (`forgotPassword`), hệ thống gọi trực tiếp `emailService.send...`. Điều này tạo ra sự kết dính chặt chẽ (Tight Coupling). Nếu sau này khi register thành công, ta muốn thêm chức năng gửi SMS, thưởng điểm tích lũy, v.v. thì file `AuthServiceImpl` sẽ phải phình to.
- **Cách áp dụng (Đã thực hiện):**
  - Sử dụng Spring Event (`ApplicationEventPublisher`).
  - Đã tạo các sự kiện `UserRegisteredEvent` và `PasswordResetRequestedEvent`.
  - `AuthService` chỉ cần publish các event này.
  - Đã tạo `EmailNotificationListener` với `@EventListener` và `@Async` để lắng nghe các sự kiện này và xử lý việc gửi email hoàn toàn độc lập, không block luồng xử lý chính.

---

## 2. Các Design Pattern ĐÃ ĐƯỢC ÁP DỤNG (Có thể viết vào Báo cáo)

### 2.1. Builder Pattern

- **Ở đâu:** Sử dụng cực kỳ phổ biến trong toàn bộ các file Entities (`User`, `Product`, `ActivityLog`...) và các DTOs (thông qua annotation `@Builder` của Lombok).
- **Lợi ích:** Tránh được Constructor Telescoping (hàm khởi tạo quá dài), giúp việc tạo object mới (đặc biệt khi mapping DTO -> Entity hoặc response) trở nên tường minh, dễ đọc và an toàn.

### 2.2. Singleton Pattern

- **Ở đâu:** Các layer của Spring Framework (`@Service`, `@RestController`, `@Repository`).
- **Lợi ích:** Spring IOC Container đảm bảo mỗi Service như `AuthServiceImpl`, `BrevoEmailServiceImpl` chỉ có đúng **một phiên bản (instance)** duy nhất trong bộ nhớ và được chia sẻ (Dependency Injection) đến các Controller cần dùng, giúp tiết kiệm bộ nhớ và dễ quản lý luồng dữ liệu.

### 2.3. Facade Pattern

- **Ở đâu:** Chính `AuthServiceImpl` là một Facade điển hình.
- **Lợi ích:** Nó giấu đi sự phức tạp của việc làm việc với `UserRepository`, `JwtTokenProvider`, `RedisTemplate`, `EmailService`, `PasswordEncoder`. Client (Controller) chỉ cần gọi 1 hàm đơn giản là `login` hoặc `register`, mọi logic phối hợp đằng sau đã được Facade lo liệu.

### 2.4. Adapter Pattern

- **Ở đâu:** Lớp `BrevoEmailServiceImpl` implements interface `EmailService`.
- **Lợi ích:** Nó đóng vai trò như một Adapter, chuyển đổi các yêu cầu gửi email từ hệ thống chuẩn (`sendPasswordResetEmail`) thành định dạng HTTP API JSON (`RestClient`) đặc thù mà bên thứ 3 (hệ thống Brevo SMTP) yêu cầu. Nếu sau này dự án đổi sang SendGrid hay AWS SES, chỉ cần viết một Adapter mới mà không cần sửa Core Logic.

---

**Kết luận:** Nếu báo cáo cần trình bày đa dạng mẫu thiết kế, tôi khuyến nghị bạn nên **triển khai sớm Strategy Pattern cho Thanh toán** và **Observer Pattern cho Gửi Email** để làm nổi bật kỹ thuật lập trình nâng cao trong đồ án.

4 Design Patterns áp dụng
Pattern	Áp dụng cho	Lợi ích
Strategy	Chính sách hủy đơn	Mở rộng dễ — thêm admin cancel không sửa code cũ
Observer	Sự kiện sau khi hủy đơn	Tách hoàn kho + gửi email riêng biệt
Builder	DTO Response (Lombok @Builder)	Đã có sẵn, tiếp tục áp dụng
Repository	Spring Data JPA	Đã có sẵn, bổ sung custom query