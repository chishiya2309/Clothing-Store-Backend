# CHƯƠNG 5: ÁP DỤNG MẪU THIẾT KẾ {#chương-5-áp-dụng-mẫu-thiết-kế .P1}

Nhằm đảm bảo tính linh hoạt, dễ bảo trì và tối ưu hóa hệ thống bán quần áo trực tuyến, nhóm phát triển đã áp dụng linh hoạt nhiều Mẫu thiết kế phần mềm (Design Patterns). Hệ thống bám sát vào cấu trúc các package như `pattern`, `event`, `listener`, `scheduler`, `policy`. Dưới đây là phân tích chi tiết của 20 mẫu thiết kế theo bố cục chuẩn.

## 5.1. Nhóm mẫu thiết kế Khởi tạo (Creational Patterns) {#nhóm-mẫu-thiết-kế-khởi-tạo .P2}

### 5.1.1. Mẫu Độc bản (Singleton Pattern)

- **Công dụng chung:** Đảm bảo một lớp chỉ có duy nhất một thể hiện (instance) và cung cấp điểm truy cập toàn cục đến nó.
- **Áp dụng để giải quyết vấn đề:** Tránh lãng phí tài nguyên khi khởi tạo lại nhiều lần các lớp không mang trạng thái (stateless) như Service, Repository.
- **Hiện thực hóa:** Thể hiện qua Spring IOC Container (các annotation `@Service`, `@Repository`, `@RestController`). Các lớp như `AuthServiceImpl`, `VoucherServiceImpl` chỉ được khởi tạo một lần duy nhất.
- **Lợi ích:** Tiết kiệm tối đa tài nguyên bộ nhớ, dễ quản lý luồng dữ liệu và chia sẻ trạng thái tập trung.

### 5.1.2. Mẫu Xây dựng (Builder Pattern)

- **Công dụng chung:** Tách rời việc xây dựng đối tượng phức tạp khỏi biểu diễn của nó để cùng một tiến trình có thể tạo ra các biểu diễn khác nhau.
- **Áp dụng để giải quyết vấn đề:** Khắc phục tình trạng hàm khởi tạo có quá nhiều tham số (Telescoping Constructor), tránh nhầm lẫn vị trí khi truyền dữ liệu.
- **Hiện thực hóa:** Thể hiện qua annotation `@Builder` của Lombok trên các lớp Entity (`Order`, `Product`) và các DTO (`OrderResponse`, `ProductDto`).
- **Lợi ích:** Việc khởi tạo cực kỳ an toàn, cấu trúc rõ ràng theo từng bước chuỗi (method chaining), mã nguồn dễ đọc hơn.

### 5.1.3. Mẫu Phương thức Nông trại (Factory Method Pattern)

- **Công dụng chung:** Cung cấp một giao diện để tạo đối tượng trong lớp cha, nhưng cho phép các lớp con thay đổi kiểu đối tượng sẽ được tạo.
- **Áp dụng để giải quyết vấn đề:** Cần một nơi tập trung để quyết định sinh định danh tự động (SKU, Slug) mà không ràng buộc cứng vào logic chính.
- **Hiện thực hóa:** Thể hiện ở lớp `ProductIdentityFactory` trong gói `pattern/factory/product`.
- **Lợi ích:** Tách biệt logic khởi tạo, tuân thủ nguyên lý Open/Closed, dễ dàng mở rộng định dạng mới trong tương lai.

## 5.2. Nhóm mẫu thiết kế Cấu trúc (Structural Patterns) {#nhóm-mẫu-thiết-kế-cấu-trúc .P2}

### 5.2.1. Mẫu Bộ thích ứng (Adapter Pattern)

- **Công dụng chung:** Bao bọc đối tượng bằng một giao diện tương thích, cho phép các interface không tương thích làm việc cùng nhau.
- **Áp dụng để giải quyết vấn đề:** Chuẩn hóa nhiều loại thanh toán khác nhau (tiền mặt, ví điện tử, thẻ ngân hàng) vốn có cấu trúc API khác biệt.
- **Hiện thực hóa:** Thể hiện ở lớp `PaymentGatewayAdapter`, `VnPayAdapter`, `MomoAdapter` (gói `pattern/adapter/payment`), giúp chuyển đổi hệ thống về chung một phương thức `pay()`.
- **Lợi ích:** Giúp hệ thống không phụ thuộc chặt vào mã của bên thứ ba, dễ dàng đổi sang nhà cung cấp thanh toán mới mà không sửa logic cốt lõi.

### 5.2.2. Mẫu Mặt tiền (Facade Pattern)

- **Công dụng chung:** Cung cấp một giao diện thống nhất, cấp cao cho một tập hợp các giao diện trong hệ thống con để che giấu sự phức tạp.
- **Áp dụng để giải quyết vấn đề:** Quy trình đặt hàng quá phức tạp (kiểm tra giỏ hàng, tồn kho, giữ voucher, tạo đơn, thanh toán), làm Controller bị phình to và rườm rà.
- **Hiện thực hóa:** Thể hiện ở `CheckoutServiceFacade` trong gói `pattern/facade/checkout`, gom tất cả các bước khởi tạo đơn hàng đó lại thành một luồng đơn giản.
- **Lợi ích:** Cung cấp một giao diện gọn gàng cho client, giảm sự phụ thuộc chéo giữa Controller và nhiều hệ thống con bên dưới.

### 5.2.3. Mẫu Tổ hợp (Composite Pattern)

- **Công dụng chung:** Tổ chức các đối tượng theo cấu trúc cây để biểu diễn hệ thống phân cấp toàn phần hay bán phần.
- **Áp dụng để giải quyết vấn đề:** Quản lý danh mục sản phẩm (Category) lồng nhau thành nhiều cấp (Cha - Con - Cháu).
- **Hiện thực hóa:** Thể hiện ở thực thể `Category` (bản thân Category chứa một danh sách các `Category` con bên trong nó).
- **Lợi ích:** Cho phép hệ thống tương tác và xử lý đồng nhất đối với danh mục gốc và các danh mục con.

### 5.2.4. Mẫu Ủy nhiệm (Proxy Pattern)

- **Công dụng chung:** Cung cấp một đối tượng đóng thế (surrogate) để kiểm soát quyền truy cập tới đối tượng gốc.
- **Áp dụng để giải quyết vấn đề:** Cần kiểm tra quyền truy cập (bảo mật) nghiêm ngặt trước khi cho phép gọi vào một hàm nghiệp vụ cụ thể.
- **Hiện thực hóa:** Thể hiện qua cơ chế AOP Proxy của Spring Security (`@PreAuthorize("hasRole('ADMIN')")`). Proxy sẽ chặn request API lại để kiểm tra.
- **Lợi ích:** Tách biệt hoàn toàn logic phân quyền ra khỏi nghiệp vụ lõi, tái sử dụng nhanh chóng thông qua annotation.

## 5.3. Nhóm mẫu thiết kế Hành vi (Behavioral Patterns) {#nhóm-mẫu-thiết-kế-hành-vi .P2}

### 5.3.1. Mẫu Trạng thái (State Pattern)

- **Công dụng chung:** Cho phép đối tượng thay đổi hành vi của nó khi trạng thái nội bộ thay đổi.
- **Áp dụng để giải quyết vấn đề:** Trạng thái của Voucher (Active, Inactive, Expired, Upcoming, Exhausted). Mỗi trạng thái có cách xử lý riêng khi áp dụng hoặc vô hiệu hóa.
- **Hiện thực hóa:** Thể hiện ở các lớp `VoucherState`, `ActiveVoucherState`, `ExpiredVoucherState` trong gói `pattern/state/voucher`.
- **Lợi ích:** Kiểm soát chặt chẽ hành vi hợp lệ của Voucher tại từng thời điểm, xóa bỏ triệt để các khối `if-else` lồng nhau.

### 5.3.2. Mẫu Chiến lược (Strategy Pattern)

- **Công dụng chung:** Định nghĩa một tập hợp thuật toán, đóng gói từng thuật toán và làm cho chúng có thể thay thế lẫn nhau.
- **Áp dụng để giải quyết vấn đề:** Có nhiều cách tính giảm giá cho Voucher (giảm phần trăm, giảm tiền cố định).
- **Hiện thực hóa:** Thể hiện qua `VoucherDiscountStrategy` và các lớp triển khai như `PercentageDiscountStrategy`, `FixedAmountDiscountStrategy` trong gói `pattern/strategy/voucher`.
- **Lợi ích:** Đóng gói các thuật toán tính toán độc lập, xóa bỏ `if-else` dài dòng, dễ dàng thêm mới các loại ưu đãi sau này.

### 5.3.3. Mẫu Quan sát (Observer Pattern)

- **Công dụng chung:** Định nghĩa mối phụ thuộc 1-Nhiều, khi một đối tượng đổi trạng thái, tất cả đối tượng phụ thuộc được tự động thông báo.
- **Áp dụng để giải quyết vấn đề:** Cần thực thi nhiều tác vụ (gửi mail, trừ tồn kho) sau khi Đơn hàng thay đổi trạng thái mà không làm chậm quy trình chính.
- **Hiện thực hóa:** Thể hiện qua lớp `OrderStatusChangedEvent` (gói `event`) và các lớp lắng nghe như `EmailNotificationListener` (gói `listener`).
- **Lợi ích:** Giảm độ kết dính (Loose Coupling), xử lý bất đồng bộ các tác vụ hậu kỳ giúp phản hồi người dùng nhanh hơn.

### 5.3.4. Mẫu Phương thức Khuôn mẫu (Template Method Pattern)

- **Công dụng chung:** Định nghĩa bộ khung của thuật toán trong lớp cha, giao việc cài đặt chi tiết cho các lớp con.
- **Áp dụng để giải quyết vấn đề:** Quy trình xuất báo cáo CSV có các bước chung giống hệt nhau (Header -> Dữ liệu -> Xuất) nhưng chi tiết mỗi loại báo cáo lại khác biệt.
- **Hiện thực hóa:** Thể hiện ở `CsvReportExporterTemplate` và các lớp con như `RevenueCsvExporter`, `BestsellerCsvExporter` trong gói `pattern/template/report`.
- **Lợi ích:** Tái sử dụng bộ khung chuẩn, giảm mã lặp (code duplication), thống nhất luồng thuật toán cốt lõi.

### 5.3.5. Mẫu Lệnh (Command Pattern)

- **Công dụng chung:** Đóng gói một yêu cầu thành một đối tượng độc lập, cho phép trì hoãn thực thi, đưa vào hàng đợi hoặc lưu log.
- **Áp dụng để giải quyết vấn đề:** Cần đóng gói hành động tác động đến hệ thống để dễ dàng điều phối và thực thi sau.
- **Hiện thực hóa:** Thể hiện ở các lớp `CatalogCommand` và `CatalogCommandExecutor` trong gói `pattern/command/catalog`.
- **Lợi ích:** Tách biệt đối tượng ra lệnh khỏi đối tượng thực thi, dễ dàng quản lý luồng gọi hàm phức tạp.

### 5.3.6. Mẫu Chuỗi Trách nhiệm (Chain of Responsibility)

- **Công dụng chung:** Cho phép chuyển yêu cầu dọc theo một chuỗi các đối tượng xử lý nối tiếp nhau đến khi có một đối tượng xử lý nó.
- **Áp dụng để giải quyết vấn đề:** Yêu cầu quét vi phạm nội dung đánh giá (từ thô tục, link spam) phải đi qua nhiều bộ lọc liên tiếp.
- **Hiện thực hóa:** Thể hiện ở `SpamScanner`, `ProfanityScanner` triển khai từ `ReviewContentScanner` trong gói `pattern/chain/review`.
- **Lợi ích:** Nếu bộ lọc đầu phát hiện vi phạm, chuỗi lập tức dừng lại, tối ưu hiệu suất và cấu trúc kiểm duyệt rất linh hoạt.

### 5.3.7. Mẫu Khách viếng thăm (Visitor Pattern)

- **Công dụng chung:** Định nghĩa một hoạt động mới mà không làm thay đổi các lớp của các đối tượng mà nó thao tác.
- **Áp dụng để giải quyết vấn đề:** Cần duyệt cây danh mục (Category) để tính độ sâu, đếm sản phẩm, phát hiện vòng lặp cha-con mà không sửa code lớp Category.
- **Hiện thực hóa:** Thể hiện ở `CategoryVisitor`, `CategoryDepthVisitor`, `CategoryCycleDetectionVisitor` trong gói `pattern/visitor/category`.
- **Lợi ích:** Đảm bảo nguyên lý Trách nhiệm duy nhất, tách rời thuật toán tính toán phức tạp ra khỏi thực thể dữ liệu.

## 5.4. Các mẫu thiết kế Kiến trúc & Dữ liệu (Architecture & Data Patterns) {#các-mẫu-thiết-kế-kiến-trúc--dữ-liệu .P2}

### 5.4.1. Mẫu Đặt trước (Reservation Pattern)

- **Công dụng chung:** Khóa (Lock) tạm thời một nguồn tài nguyên có giới hạn để đảm bảo nó thuộc về người dùng đang thực hiện giao dịch.
- **Áp dụng để giải quyết vấn đề:** Tránh tình trạng bán vượt mức (overselling). Cần giữ số lượng sản phẩm và giữ chỗ voucher trong khoảng thời gian khách checkout.
- **Hiện thực hóa:** Thể hiện ở gói `pattern/reservation` kết hợp `CheckoutExpirationScheduler` (gói `scheduler`). Xác nhận khi thanh toán xong, giải phóng khi hết hạn hoặc thất bại.
- **Lợi ích:** Bảo đảm tính nhất quán dữ liệu tồn kho trong môi trường có lưu lượng người mua hàng song song rất lớn.

### 5.4.2. Mẫu Kho lưu trữ (Repository Pattern)

- **Công dụng chung:** Đóng vai trò lớp trung gian (Collection-like) giữa tầng xử lý nghiệp vụ và truy xuất cơ sở dữ liệu.
- **Áp dụng để giải quyết vấn đề:** Tránh để tầng nghiệp vụ gọi các câu lệnh SQL/HQL phức tạp, gây khó bảo trì.
- **Hiện thực hóa:** Thể hiện qua các interface kế thừa từ `JpaRepository` (`UserRepository`, `OrderRepository`).
- **Lợi ích:** Trừu tượng hóa hoàn toàn việc kết nối Database. Tận dụng khả năng tự động sinh câu truy vấn an toàn của Spring Data.

### 5.4.3. Mẫu Đơn vị công việc (Unit of Work Pattern)

- **Công dụng chung:** Theo dõi mọi thay đổi trong một tác vụ và cập nhật đồng loạt xuống cơ sở dữ liệu như một giao dịch duy nhất.
- **Áp dụng để giải quyết vấn đề:** Nếu quy trình tạo đơn hàng dài bị lỗi ở bước cuối cùng, dữ liệu tồn kho hoặc thanh toán sẽ bị sai lệch.
- **Hiện thực hóa:** Thể hiện qua chú thích `@Transactional` được gắn trên các hàm của Service.
- **Lợi ích:** Đảm bảo tính nguyên tử (ACID). Nếu một thao tác lỗi, toàn bộ lệnh DB trước đó tự động bị Rollback (hủy bỏ).

### 5.4.4. Mẫu Đặc tả (Specification Pattern)

- **Công dụng chung:** Kết hợp nhiều tiêu chí để kiểm tra sự thỏa mãn của đối tượng, hoặc tạo ra bộ lọc dữ liệu đa năng.
- **Áp dụng để giải quyết vấn đề:** Khách hàng tìm kiếm sản phẩm theo đa tiêu chí (khoảng giá, tên, danh mục). SQL thuần sẽ sinh ra vô số khối `if-else`.
- **Hiện thực hóa:** Thể hiện ở các lớp triển khai giao diện `Specification` của Spring Data JPA trong gói `pattern/specification`.
- **Lợi ích:** Cho phép nối ghép các điều kiện lọc động qua phép logic AND/OR tại thời gian thực một cách uyển chuyển.

### 5.4.5. Mẫu Đối tượng chuyển đổi dữ liệu (DTO Pattern)

- **Công dụng chung:** Đóng gói và vận chuyển dữ liệu giữa các tiến trình hoặc các tầng (Layer) của ứng dụng.
- **Áp dụng để giải quyết vấn đề:** Tránh trả trực tiếp lớp thực thể (Entity) qua API để không rò rỉ dữ liệu nhạy cảm (mật khẩu, khóa bảo mật).
- **Hiện thực hóa:** Thể hiện ở toàn bộ các lớp có hậu tố `Request` và `Response` (`UserResponse`, `OrderDto`) dùng để giao tiếp qua REST API.
- **Lợi ích:** Tăng cường bảo mật, tạo định dạng JSON thống nhất cho Frontend sử dụng độc lập với thiết kế Database.

### 5.4.6. Mẫu Chính sách (Policy Pattern)

- **Công dụng chung:** Tách rời tập hợp các nguyên tắc, luật lệ kinh doanh ra khỏi đối tượng để dễ dàng tái cấu hình.
- **Áp dụng để giải quyết vấn đề:** Quy trình thay đổi trạng thái đơn hàng có nhiều điều khoản khắt khe (VD: đơn đã hủy không được phép giao hàng).
- **Hiện thực hóa:** Thể hiện ở lớp `OrderStatusTransitionPolicy` trong gói `policy`.
- **Lợi ích:** Gom mọi quy tắc kiểm tra ràng buộc tập trung tại một nơi, ranh giới nghiệp vụ rõ ràng và sạch sẽ hơn.
