# BÁO CÁO ÁP DỤNG DESIGN PATTERN TRONG DỰ ÁN CLOTHING STORE (BACKEND)

Dự án phần mềm Clothing Store được phát triển trên nền tảng Spring Boot. Nhằm đảm bảo tính mở rộng, khả năng bảo trì và cấu trúc mã nguồn tối ưu, đội ngũ phát triển đã tích hợp nhiều Mẫu thiết kế phần mềm (Design Patterns). Dưới đây là phân tích chi tiết về **07 mẫu thiết kế** đã và đang được triển khai trong hệ thống, trình bày theo văn phong khoa học.

---

## 1. Mẫu Quan Sát (Observer Pattern)

- **Mẫu thiết kế áp dụng:** Observer Pattern (Behavioral Pattern).
- **Áp dụng cho chức năng:**
  1. Quản lý xử lý sự kiện khi Đơn hàng (Order) thay đổi trạng thái: Kích hoạt đồng thời các tác vụ gửi email xác nhận (`EmailNotificationObserver`), giảm/hoàn số lượng tồn kho (`InventoryObserver`), và cộng điểm tích lũy cho khách hàng (`LoyaltyPointObserver`).
  2. Quản lý vòng đời thay đổi trạng thái của người dùng (Ví dụ: Khóa / Mở khóa tài khoản) kéo theo các nghiệp vụ Audit, Revoke Token.
- **Lý do lựa chọn (Lợi ích đạt được):**
  - **Giảm độ kết dính (Loose Coupling):** Các tác vụ xử lý hậu kỳ (như gửi email hay cập nhật tồn kho) được tách biệt hoàn toàn khỏi `OrderService` và `UserService`. Việc thêm hoặc bớt một chức năng (ví dụ: thêm gửi SMS) sẽ không làm thay đổi lõi nghiệp vụ của hệ thống (tuân thủ nguyên lý Open/Closed).
  - **Tối ưu hiệu năng:** Cho phép các tác vụ nặng như Gửi Email được đánh dấu `@Async` để xử lý bất đồng bộ, không gây block luồng xử lý chính của người dùng.
- **Hạn chế gặp phải:**
  - Gây khó khăn trong quá trình theo dõi lỗi (tracing & debugging) vì luồng thực thi không còn mang tính tuần tự từ trên xuống dưới mà bị phân mảnh vào các Listeners chạy ngầm. Thứ tự thực thi của các Observer không được đảm bảo tuyệt đối.

---

## 2. Mẫu Chiến Lược (Strategy Pattern)

- **Mẫu thiết kế áp dụng:** Strategy Pattern (Behavioral Pattern).
- **Áp dụng cho chức năng:**
  1. Xử lý thanh toán đa phương thức (`PaymentStrategy` bao gồm `CodPaymentStrategy`, `VnPayPaymentStrategy`, `MoMoPaymentStrategy`).
  2. Quản lý chính sách định giá hạng thành viên (Membership Tier Upgrade) cho khách hàng (`StandardUpgradeStrategy`, `PromoUpgradeStrategy`).
- **Lý do lựa chọn (Lợi ích đạt được):**
  - **Đóng gói thuật toán linh hoạt:** Hệ thống yêu cầu nhiều phương pháp thanh toán với cơ chế gọi API và mã hóa checksum khác nhau hoàn toàn. Pattern này đóng gói từng thuật toán vào các class triển khai riêng biệt, giao quyền quyết định chiến lược cho `PaymentContext` tại thời điểm runtime (thực thi).
  - **Xóa bỏ mã rác (Spaghetti Code):** Tránh được việc sử dụng cấu trúc `if-else` hoặc `switch-case` lồng nhau phức tạp trong service.
  - **Dễ dàng bảo trì:** Khi tích hợp thêm phương thức thanh toán mới (như ZaloPay, Visa), nhà phát triển chỉ cần định nghĩa thêm một Strategy mới mà không ảnh hưởng đến logic thanh toán hiện hành.
- **Hạn chế gặp phải:**
  - Làm tăng số lượng tệp (classes) trong kiến trúc thư mục.
  - Cần phải bổ sung thêm Factory hoặc Context để Client có khả năng quyết định lúc nào nên sử dụng Strategy nào.

---

## 3. Mẫu Phương Thức Nông Trại (Factory Method Pattern)

- **Mẫu thiết kế áp dụng:** Factory Method Pattern (Creational Pattern).
- **Áp dụng cho chức năng:** Khởi tạo bộ tính toán giảm giá cho Voucher khuyến mãi (`DiscountFactory` để khởi tạo `PercentageDiscount` hoặc `FixedAmountDiscount` tùy vào cấu hình voucher).
- **Lý do lựa chọn (Lợi ích đạt được):**
  - **Tách biệt logic khởi tạo:** Việc quyết định tạo đối tượng tính toán nào (giảm theo % hay giảm số tiền cố định) được đẩy cho Factory đảm nhiệm dựa trên thuộc tính `discount_type`. Lớp service chính không cần sử dụng từ khóa `new` trực tiếp.
  - **Khả năng mở rộng tốt:** Đặt nền móng sẵn sàng cho tương lai khi hệ thống yêu cầu các loại khuyến mãi phức tạp hơn (như Miễn phí vận chuyển, Mua X tặng Y).
- **Hạn chế gặp phải:**
  - Cấu trúc có thể trở nên hơi cồng kềnh (overkill) nếu hệ thống chỉ có đúng 2 loại tính toán đơn giản, vì phải tạo ra thêm Interface và Class Factory thay vì chỉ dùng 1 hàm tính toán nội bộ.

---

## 4. Mẫu Phương Thức Khuôn Mẫu (Template Method Pattern)

- **Mẫu thiết kế áp dụng:** Template Method Pattern (Behavioral Pattern).
- **Áp dụng cho chức năng:** Quy trình trích xuất và kết xuất báo cáo thống kê dưới định dạng file CSV (Ví dụ: báo cáo doanh thu, sản phẩm bán chạy).
- **Lý do lựa chọn (Lợi ích đạt được):**
  - **Định hình cấu trúc bất biến:** Định nghĩa một "bộ khung" (`CsvReportExporterTemplate`) chuẩn hóa thuật toán xuất CSV gồm 4 bước cố định: _Ghi mã BOM (UTF-8) -> Ghi tiêu đề Header -> Duyệt ghi dữ liệu từng dòng (Row) -> Flush bộ đệm_.
  - **Giảm thiểu mã lặp (Code Duplication):** Các module xuất báo cáo cụ thể chỉ cần kế thừa lớp Template và cung cấp chi tiết cài đặt cho phương thức `writeHeader()` và `writeRow()`. Các bước I/O được tái sử dụng toàn bộ từ lớp cha.
- **Hạn chế gặp phải:**
  - Các lớp con bị giới hạn nghiêm ngặt bởi quy trình tuần tự mà lớp cha thiết lập.

---

## 5. Mẫu Xây Dựng (Builder Pattern)

- **Mẫu thiết kế áp dụng:** Builder Pattern (Creational Pattern).
- **Áp dụng cho chức năng:**
  1. Xây dựng cấu trúc đối tượng Đơn hàng (`Order`) thông qua `OrderBuilder` (Kết hợp User, danh sách CartItems, Address, Voucher, và tự động tính toán tổng phí).
  2. Khởi tạo các đối tượng thực thể (Entities) và Data Transfer Objects (DTOs) phổ biến trong dự án qua thư viện Lombok.
- **Lý do lựa chọn (Lợi ích đạt được):**
  - Giải quyết triệt để vấn đề Telescoping Constructor (hàm khởi tạo chứa quá nhiều tham số không cần thiết). Code khởi tạo đơn hàng trở nên cực kỳ rõ ràng, cấu trúc theo từng bước chuỗi (method chaining).
  - Cải thiện tính tường minh và an toàn khi ánh xạ (mapping) dữ liệu giữa các layer.
- **Hạn chế gặp phải:**
  - Tạo thêm các file/class Builder đi kèm, hoặc kéo dài thời gian biên dịch nếu sử dụng công cụ sinh mã tự động (Lombok).

---

## 6. Mẫu Đặc Tả (Specification Pattern)

- **Mẫu thiết kế áp dụng:** Specification Pattern (Structural / Domain-Driven Pattern).
- **Áp dụng cho chức năng:** Cơ chế tìm kiếm, lọc động và truy vấn dữ liệu theo nhiều tiêu chí của hệ thống (Ví dụ: Lọc người dùng theo vai trò, trạng thái hoặc từ khóa họ tên thông qua `UserSpecification`).
- **Lý do lựa chọn (Lợi ích đạt được):**
  - **Truy vấn linh hoạt (Dynamic Queries):** Khắc phục nhược điểm của Spring Data JPA truyền thống khi phải viết hàng chục method tĩnh với tên gọi dài dòng (như `findByRoleAndIsActiveAndEmailContaining`).
  - **Tính kết hợp cao:** Cho phép kết hợp các điều kiện lọc một cách động qua các phép toán (`AND`, `OR`).
- **Hạn chế gặp phải:**
  - Code Specification sử dụng Criteria API của JPA tương đối phức tạp và khó bảo trì hơn SQL thuần túy.

---

## 7. Mẫu Mặt Tiền (Facade Pattern)

- **Mẫu thiết kế áp dụng:** Facade Pattern (Structural Pattern).
- **Áp dụng cho chức năng:** Tầng dịch vụ xác thực và cấp phép người dùng (`AuthServiceImpl`).
- **Lý do lựa chọn (Lợi ích đạt được):**
  - Cung cấp một giao diện sử dụng cấp cao, đơn giản cho Controller (ví dụ như hàm `login()`, `register()`). Đằng sau nó che giấu sự tương tác phức tạp của `UserRepository`, `PasswordEncoder`, `JwtTokenProvider`, `RedisTemplate`, và `EmailService`.
  - **Giảm phụ thuộc module:** Controller chỉ cần tương tác duy nhất với Facade, không cần am hiểu chi tiết của các hệ thống con bên dưới.
- **Hạn chế gặp phải:**
  - Facade có rủi ro phình to trở thành "God Object" (đối tượng bao hàm mọi thứ) nếu ôm đồm quá nhiều phụ thuộc bên trong mà không phân rã tốt.
