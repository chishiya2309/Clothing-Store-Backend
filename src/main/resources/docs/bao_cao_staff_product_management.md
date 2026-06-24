# Báo cáo chức năng quản lý sản phẩm của nhân viên (Staff Product Management)

## 1. Thông tin nhanh về nhánh

- **Nhánh hiện tại**: `feature/staff-product-management`
- **Nhánh đối chiếu**: `main`
- **Tác nhân thực hiện**: Nhân viên cửa hàng thời trang (`STAFF`)
- **Use cases đáp ứng**:
  - **UC-15**: Thêm sản phẩm mới
  - **UC-16**: Cập nhật / Xóa sản phẩm
  - **UC-16d**: Tra cứu sản phẩm trong trang quản trị
  - **UC-16e**: Cập nhật số lượng tồn kho

---

## 2. Tóm tắt chức năng đã thực hiện

Nhánh này đã xây dựng hoàn chỉnh phân hệ quản lý sản phẩm dành cho nhân viên cửa hàng với các nghiệp vụ chính:

1. **Xem danh sách sản phẩm**: Hỗ trợ phân trang, sắp xếp và lọc động theo nhiều điều kiện đồng thời (từ khóa tìm kiếm, danh mục sản phẩm, trạng thái hiển thị hoặc hết hàng/sắp hết hàng).
2. **Xem chi tiết sản phẩm**: Hiển thị đầy đủ thông tin mô tả, chất liệu, hướng dẫn bảo quản, danh sách hình ảnh được phân loại và danh sách các biến thể (size, màu sắc) cùng số lượng tồn kho thực tế.
3. **Thêm sản phẩm mới**: Nhân viên nhập thông tin sản phẩm, tải lên danh sách ảnh và cấu hình các biến thể. Hệ thống tự động sinh slug từ tên sản phẩm, sinh mã SKU chuẩn hóa cho từng biến thể, đồng thời tự động kiểm tra trùng lặp tên sản phẩm trong cùng danh mục và trùng lặp SKU hệ thống.
4. **Cập nhật thông tin**: Cho phép thay đổi thông tin sản phẩm, cập nhật lại hình ảnh và biến thể. Giá khuyến mãi mới cập nhật chỉ áp dụng cho các đơn hàng mới, không ảnh hưởng đến đơn hàng lịch sử.
5. **Cập nhật trạng thái hiển thị (Visibility)**: Bật/tắt trạng thái hoạt động (`isActive = true/false`) của sản phẩm để ẩn hoặc hiển thị nhanh trên trang Storefront.
6. **Xóa sản phẩm an toàn (Safe Delete)**: Tự động quyết định phương án xóa tối ưu. Nếu sản phẩm chưa từng phát sinh bất kỳ liên kết dữ liệu nào, hệ thống sẽ thực hiện xóa vật lý (**hard delete**). Nếu sản phẩm đã nằm trong đơn hàng, giỏ hàng, danh sách yêu thích, đánh giá hoặc phiên đặt giữ chỗ tồn kho, hệ thống sẽ tự động chuyển sang xóa mềm (**soft delete** / ẩn đi) để bảo toàn lịch sử dữ liệu.
7. **Cập nhật số lượng tồn kho**: Cập nhật trực tiếp số tồn cho từng biến thể, tự động kích hoạt cảnh báo "Sản phẩm sắp hết hàng" nếu số lượng tồn giảm xuống dưới ngưỡng quy định (< 10). Sử dụng cơ chế khóa giao dịch để đảm bảo nhất quán dữ liệu tồn kho khi có giao dịch mua sắm song song.
8. **Đồng bộ cache và sự kiện**: Tự động dọn dẹp các cache trang chủ (Hàng mới về, Hàng bán chạy) sau khi sản phẩm bị thay đổi và phát sự kiện thông báo giảm giá đến những khách hàng yêu thích sản phẩm đó.

---

## 3. Danh sách các API Endpoints đã thêm

Tất cả các API được đặt trong tiền tố bảo mật `/api/staff/products` và được phân quyền truy cập bắt buộc cho role `STAFF`:

| Chức năng | Phương thức HTTP | Endpoint | Mô tả |
| :--- | :---: | :--- | :--- |
| **Xem danh sách sản phẩm** | `GET` | `/api/staff/products` | Hỗ trợ tìm kiếm, lọc theo danh mục, trạng thái tồn kho, phân trang và sắp xếp. |
| **Xem chi tiết sản phẩm** | `GET` | `/api/staff/products/{id}` | Lấy chi tiết thuộc tính sản phẩm, hình ảnh và danh sách biến thể. |
| **Thêm sản phẩm mới** | `POST` | `/api/staff/products` | Tạo sản phẩm kèm theo danh sách hình ảnh và biến thể. |
| **Cập nhật sản phẩm** | `PUT` | `/api/staff/products/{id}` | Cập nhật toàn bộ thuộc tính, đồng bộ hóa danh sách ảnh và biến thể. |
| **Ẩn/Hiện sản phẩm nhanh** | `PATCH` | `/api/staff/products/{id}/visibility` | Bật hoặc tắt trạng thái hiển thị của sản phẩm trên Storefront. |
| **Xóa sản phẩm** | `DELETE` | `/api/staff/products/{id}` | Thực thi chính sách xóa an toàn (xóa cứng hoặc xóa mềm tùy thuộc ràng buộc). |
| **Cập nhật tồn kho biến thể** | `PATCH` | `/api/staff/products/{productId}/variants/{variantId}/stock` | Cập nhật số tồn kho của biến thể và trả về trạng thái cảnh báo nếu sắp hết hàng. |

---

## 4. Các Mẫu thiết kế (Design Patterns) áp dụng

Để đảm bảo kiến trúc sạch, dễ mở rộng và tuân thủ chặt chẽ nguyên lý OOP/SOLID, phân hệ đã được thiết kế dựa trên sự phối hợp của 10 mẫu thiết kế:

### 4.1. Mẫu chuỗi trách nhiệm (Chain of Responsibility)
- **Thiết kế**: Chuỗi handler thực thi kiểm tra hợp lệ gồm: `ProductPriceValidationHandler` -> `ProductImageValidationHandler` -> `ProductVariantValidationHandler` -> `CategoryExistsValidationHandler` -> `ProductNameUniqueValidationHandler`.
- **Giải thích**: Mỗi điều kiện xác thực (validate) được đóng gói thành một lớp xử lý độc lập kế thừa từ lớp cha `ProductValidationHandler`. Khi yêu cầu tạo hoặc sửa sản phẩm được gửi đến, nó sẽ được truyền qua chuỗi này. Handler nào phát hiện dữ liệu lỗi sẽ ném ra ngoại lệ phù hợp và ngắt chuỗi ngay lập tức. Điều này giúp loại bỏ hoàn toàn các khối lệnh `if-else` lồng nhau phức tạp trong lớp dịch vụ, giúp dễ dàng thêm hoặc bớt các quy tắc kiểm tra trong tương lai.

### 4.2. Mẫu chính sách / chiến lược (Policy / Strategy)
- **Thiết kế**: Lớp `ProductDeletionPolicy` định nghĩa chính sách xóa sản phẩm.
- **Giải thích**: Khi nhân viên yêu cầu xóa một sản phẩm, `ProductDeletionPolicy` sẽ đóng vai trò đưa ra quyết định xóa cứng hay xóa mềm bằng cách truy vấn đồng thời 6 nguồn liên kết ngoại tại CSDL. Chính sách xóa này hoạt động độc lập giúp Service không cần biết chi tiết ràng buộc của các bảng bên ngoài, bảo vệ lõi nghiệp vụ khỏi những thay đổi ở cấu trúc bảng dữ liệu phụ thuộc.

### 4.3. Mẫu Facade
- **Thiết kế**: `StaffProductServiceImpl` đóng vai trò Facade điều phối hệ thống con.
- **Giải thích**: Nghiệp vụ quản lý sản phẩm chạm đến nhiều thực thể phụ thuộc (`Product`, `ProductVariant`, `ProductImage`, `Category`), các hệ thống cache (Redis cache manager) và thông báo sự kiện (wishlist observer). `StaffProductServiceImpl` cung cấp một giao diện (interface) đơn giản cho Controller gọi, trong khi bản thân nó che giấu đi toàn bộ quy trình phức tạp gồm: xác thực dữ liệu qua chuỗi validation, sinh mã định danh, đồng bộ hóa các quan hệ thực thể, làm mới bộ nhớ đệm và phát sự kiện giảm giá.

### 4.4. Mẫu Factory Method / Simple Factory
- **Thiết kế**: Lớp `ProductIdentityFactory` chịu trách nhiệm sinh mã định danh.
- **Giải thích**: Tập trung hóa logic sinh slug tự động từ tên sản phẩm tiếng Việt có dấu và logic tạo mã SKU duy nhất cho từng biến thể dựa trên slug, size và màu sắc. Việc tách biệt này giúp tái sử dụng thuật toán tạo SKU/slug ở nhiều nơi (như luồng tạo mới, cập nhật hoặc import Excel sau này) và cô lập logic tạo chuỗi định danh khỏi mã nguồn nghiệp vụ chính.

### 4.5. Mẫu giải quyết trạng thái (State Resolver)
- **Thiết kế**: Lớp `ProductStockStateResolver` giải quyết trạng thái tồn kho của sản phẩm.
- **Giải thích**: Trạng thái hiển thị của sản phẩm đối với nhân viên không chỉ phụ thuộc vào cờ hoạt động mà còn phụ thuộc vào tổng số lượng tồn kho của các biến thể. Lớp này giải quyết trạng thái nghiệp vụ tĩnh (như `ACTIVE`, `INACTIVE`, `OUT_OF_STOCK`, `LOW_STOCK`) và ngưỡng cảnh báo dưới 10 dựa trên trạng thái tổng hợp của các biến thể con, giúp định hình trạng thái thực tế của sản phẩm một cách nhất quán trên giao diện quản lý.

### 4.6. Mẫu quan sát (Observer)
- **Thiết kế**: Đăng ký và thông báo qua `ProductPriceManager`.
- **Giải thích**: Khi nhân viên cập nhật giá bán khuyến mãi của sản phẩm giảm xuống so với giá cũ, hệ thống tự động kích hoạt thông báo gửi email/thông điệp đến những khách hàng đang lưu sản phẩm này trong danh sách yêu thích (`Wishlist`). Tác vụ gửi thông báo này được thực hiện thông qua mô hình Observer giúp dịch vụ quản lý sản phẩm không bị phụ thuộc trực tiếp vào module gửi email hay wishlist.

### 4.7. Mẫu đặc tả bộ lọc (Specification)
- **Thiết kế**: Lớp `StaffProductSpecification` cung cấp các bộ lọc JPA động.
- **Giải thích**: Tránh việc định nghĩa quá nhiều phương thức tìm kiếm thủ công trong Repository. Lớp này cho phép tạo các điều kiện lọc động (theo từ khóa, danh mục, trạng thái) và kết hợp (compose) chúng lại với nhau thông qua `JpaSpecificationExecutor` của Spring Data JPA tại thời điểm chạy.

### 4.8. Mẫu đơn vị công việc (Unit of Work)
- **Thiết kế**: Quản lý giao dịch thông qua `@Transactional` và Pessimistic Locking.
- **Giải thích**: Đảm bảo tất cả các hoạt động thay đổi dữ liệu sản phẩm, hình ảnh và biến thể được diễn ra thành công trọn vẹn (Atomic). Đặc biệt, khi nhân viên cập nhật kho hàng, hệ thống sử dụng khóa ghi (`PESSIMISTIC_WRITE`) trên biến thể sản phẩm để ngăn chặn xung đột dữ liệu (race condition) với luồng khách hàng đặt hàng song song.

### 4.9. Mẫu khởi tạo đối tượng (Builder)
- **Thiết kế**: Sử dụng `@Builder` của thư viện Lombok.
- **Giải thích**: Các thực thể sản phẩm, biến thể, hình ảnh và các đối tượng DTO phản hồi có số lượng thuộc tính lớn và nhiều thuộc tính tùy chọn (optional). Builder pattern giúp viết code tạo lập các đối tượng này một cách tường minh, dễ đọc và tránh nhầm lẫn thứ tự tham số.

### 4.10. Mẫu tầng biên nghiệp vụ (Service Layer)
- **Thiết kế**: Định nghĩa ranh giới qua interface `StaffProductService`.
- **Giải thích**: Thiết lập ranh giới rõ ràng giữa lớp Web Controller và lớp Domain Persistence. Tất cả logic nghiệp vụ, xử lý giao dịch và kiểm soát lỗi của Staff Product được gói gọn trong lớp này để phục vụ cho việc kiểm thử đơn vị độc lập.

---

## 5. Cấu trúc cây thư mục và danh sách file thay đổi

Các tệp tin được tổ chức khoa học theo mô hình phân tầng của dự án:

```text
src/
├── main/
│   └── java/vn/hcmute/edu/dp/nhom10/backend/
│       ├── controller/staff/
│       │   └── StaffProductController.java                        # [NEW] API Controller cho Staff
│       ├── dto/
│       │   ├── request/
│       │   │   ├── StaffCreateProductRequest.java                  # [NEW] Request DTO thêm sản phẩm
│       │   │   ├── StaffProductImageRequest.java                   # [NEW] Request DTO thông tin ảnh
│       │   │   ├── StaffProductSearchCriteria.java                 # [NEW] DTO chứa tham số tìm kiếm
│       │   │   ├── StaffProductVariantRequest.java                 # [NEW] Request DTO thông tin biến thể
│       │   │   ├── StaffUpdateProductRequest.java                  # [NEW] Request DTO sửa sản phẩm
│       │   │   ├── StaffUpdateProductVisibilityRequest.java        # [NEW] Request DTO ẩn/hiện sản phẩm
│       │   │   └── StaffUpdateStockRequest.java                    # [NEW] Request DTO sửa kho hàng
│       │   └── response/
│       │       ├── StaffProductDetailResponse.java                 # [NEW] Response DTO chi tiết sản phẩm
│       │       ├── StaffProductImageResponse.java                  # [NEW] Response DTO thông tin ảnh
│       │       ├── StaffProductListItemResponse.java               # [NEW] Response DTO dòng sản phẩm trong danh sách
│       │       ├── StaffProductVariantResponse.java                # [NEW] Response DTO thông tin biến thể
│       │       └── StaffStockUpdateResponse.java                   # [NEW] Response DTO sau khi sửa tồn kho
│       ├── enums/
│       │   └── StaffProductStatus.java                             # [NEW] Enum các trạng thái tồn kho/hiển thị
│       ├── pattern/
│       │   ├── chain/product/
│       │   │   ├── ProductValidationContext.java                   # [NEW] Context của chuỗi validate
│       │   │   ├── ProductValidationHandler.java                   # [NEW] Handler trừu tượng của chuỗi
│       │   │   ├── ProductPriceValidationHandler.java              # [NEW] Validate giá bán khuyến mãi
│       │   │   ├── ProductImageValidationHandler.java              # [NEW] Validate số lượng hình ảnh
│       │   │   ├── ProductVariantValidationHandler.java            # [NEW] Validate số lượng/sự trùng lặp biến thể
│       │   │   ├── CategoryExistsValidationHandler.java            # [NEW] Validate danh mục tồn tại & kích hoạt
│       │   │   └── ProductNameUniqueValidationHandler.java         # [NEW] Validate trùng tên sản phẩm trong danh mục
│       │   ├── factory/product/
│       │   │   └── ProductIdentityFactory.java                     # [NEW] Factory sinh SKU và slug
│       │   ├── policy/product/
│       │   │   ├── ProductDeletionDecision.java                    # [NEW] Enum kết quả quyết định xóa (HARD/SOFT)
│       │   │   └── ProductDeletionPolicy.java                      # [NEW] Policy quyết định xóa an toàn
│       │   ├── specification/
│       │   │   └── StaffProductSpecification.java                  # [NEW] Đặc tả JPA Specification lọc sản phẩm
│       │   └── state/product/
│       │       └── ProductStockStateResolver.java                  # [NEW] Resolver tính toán trạng thái tồn kho
│       ├── repository/
│       │   ├── CartItemRepository.java                             # [MODIFY] Thêm query check giỏ hàng
│       │   ├── CheckoutSessionItemRepository.java                  # [MODIFY] Thêm query check phiên thanh toán
│       │   ├── InventoryReservationRepository.java                 # [MODIFY] Thêm query check giữ chỗ tồn kho
│       │   ├── OrderItemRepository.java                            # [MODIFY] Thêm query check đơn hàng phát sinh
│       │   ├── ProductRepository.java                              # [MODIFY] Thêm query check slug và trùng tên
│       │   ├── ProductVariantRepository.java                       # [MODIFY] Thêm query check SKU và lock cập nhật kho
│       │   ├── ReviewRepository.java                               # [MODIFY] Thêm query check đánh giá sản phẩm
│       │   └── WishlistRepository.java                             # [MODIFY] Thêm query check danh sách yêu thích
│       └── service/
│           ├── StaffProductService.java                            # [NEW] Interface biên nghiệp vụ Staff Product
│           └── impl/
│               └── StaffProductServiceImpl.java                    # [NEW] Hiện thực hóa nghiệp vụ & Facade điều phối
└── test/
    └── java/vn/hcmute/edu/dp/nhom10/backend/
        ├── controller/staff/
        │   └── StaffProductControllerTest.java                     # [NEW] Unit test cho API Controller
        ├── pattern/
        │   ├── chain/product/
        │   │   └── ProductValidationChainTest.java                 # [NEW] Unit test cho chuỗi trách nhiệm validate
        │   ├── factory/product/
        │   │   └── ProductIdentityFactoryTest.java                 # [NEW] Unit test cho SKU/slug factory
        │   ├── policy/product/
        │   │   └── ProductDeletionPolicyTest.java                  # [NEW] Unit test cho chính sách xóa sản phẩm
        │   ├── specification/
        │   │   └── StaffProductSpecificationTest.java              # [NEW] Unit test cho đặc tả bộ lọc JPA
        │   └── state/product/
        │       └── ProductStockStateResolverTest.java              # [NEW] Unit test cho bộ tính trạng thái tồn kho
        └── service/
            └── StaffProductServiceImplTest.java                    # [NEW] Unit test cho lớp nghiệp vụ Service
```

---

## 6. Kết quả kiểm thử tự động (Unit Tests)

Bộ kiểm thử tự động của nhánh này bao gồm **39 kịch bản kiểm thử** độc lập chia thành 7 lớp kiểm thử chính. Kết quả thực thi thông qua Maven Surefire Plugin đạt tỷ lệ **thành công 100% (39/39 Passed)**:

1. **`StaffProductControllerTest` (7/7 Passed)**: Xác nhận ánh xạ định tuyến URL, tham số đầu vào, trạng thái HTTP trả về (`200 OK`, `201 Created`) và cấu trúc DTO phản hồi cho toàn bộ 7 endpoint của Controller.
2. **`ProductValidationChainTest` (7/7 Passed)**: Xác nhận chuỗi trách nhiệm bắt lỗi đúng các trường hợp ngoại lệ (giá sale lớn hơn giá gốc, thiếu ảnh, thiếu biến thể, trùng lặp thuộc tính biến thể, danh mục không tồn tại hoặc bị ẩn, trùng tên sản phẩm trong danh mục).
3. **`StaffProductServiceImplTest` (7/7 Passed)**: Xác nhận logic xử lý nghiệp vụ tại Service hoạt động chính xác (lưu sản phẩm, gộp biến thể/hình ảnh, cập nhật tồn kho an toàn, kích hoạt observer thông báo hạ giá và xóa sạch bộ nhớ đệm cache storefront).
4. **`ProductDeletionPolicyTest` (7/7 Passed)**: Xác nhận chính sách xóa hoạt động đúng (trả về kết quả `SOFT_DELETE` khi sản phẩm có bất kỳ liên kết nào trong 6 bảng dữ liệu liên quan, và trả về `HARD_DELETE` khi hoàn toàn độc lập).
5. **`StaffProductSpecificationTest` (4/4 Passed)**: Xác nhận bộ sinh câu lệnh JPA Specification tạo ra đúng các liên kết thực thể (Join) và biểu thức logic tìm kiếm (`LIKE`, `EQUAL`, `BETWEEN`, `EXISTS`).
6. **`ProductStockStateResolverTest` (4/4 Passed)**: Xác nhận thuật toán tính toán trạng thái tồn kho động hoạt động chính xác theo cờ hoạt động và số lượng tồn thực tế của các biến thể.
7. **`ProductIdentityFactoryTest` (3/3 Passed)**: Xác nhận thuật toán tạo chuỗi slug chuẩn hóa không dấu và SKU chuẩn hóa từ thuộc tính sản phẩm hoạt động chính xác.
