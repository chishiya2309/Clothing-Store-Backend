# TÀI LIỆU THIẾT KẾ: DESIGN PATTERNS ÁP DỤNG CHO STAFF PRODUCT MANAGEMENT

## I. Thông tin phạm vi

- Branch: `feature/staff-product-management`
- Module: Quản lý sản phẩm của nhân viên
- Use case áp dụng:
  - UC-15: Thêm sản phẩm mới
  - UC-16: Cập nhật / Xóa sản phẩm
  - UC-16d: Tra cứu sản phẩm
  - UC-16e: Cập nhật số lượng tồn kho
- Tài liệu căn cứ:
  - `Nhom10_Project2.docx`
  - `usecase_detail_sanpham.md`
  - `ke_hoach_xay_dung_chuc_nang_staff_product_management.md`
  - GoF Design Patterns: `Design Patterns - Elements of Reusable Object-Oriented Software.chm`
  - Spring Data JPA Specification
  - Martin Fowler - Patterns of Enterprise Application Architecture
  - Microsoft Azure Architecture Patterns

## II. Nguyên tắc chọn pattern

Không áp dụng pattern chỉ để tăng số lượng. Pattern chỉ được đưa vào khi nó giải quyết một vấn đề thật trong chức năng:

1. Giảm độ phức tạp của luồng nghiệp vụ tạo/cập nhật sản phẩm.
2. Tách rõ trách nhiệm giữa controller, service, repository, validation, factory và event.
3. Giữ code dễ mở rộng khi sản phẩm có thêm biến thể, ảnh, lịch sử giá, cache, hoặc tích hợp upload ảnh.
4. Bảo vệ tính nhất quán dữ liệu khi thao tác với product, image, variant và stock.
5. Phù hợp kiến trúc Spring Boot/JPA hiện có của repository.

## III. Ma trận pattern áp dụng

| Pattern | Nhóm | Mức áp dụng | Thành phần dự kiến | Vai trò chính |
|---|---|---:|---|---|
| Service Layer | Enterprise Application | Bắt buộc | `StaffProductService` | Định nghĩa boundary nghiệp vụ cho API staff |
| Facade | GoF Structural | Bắt buộc | `StaffProductServiceImpl` hoặc `StaffProductManagementFacade` | Điều phối workflow nhiều bước |
| Repository | Enterprise Application | Bắt buộc | `ProductRepository`, `ProductVariantRepository`, `OrderItemRepository` | Tập trung truy vấn/persistence |
| Unit of Work | Enterprise Application | Bắt buộc | `@Transactional` | Gom nhiều thay đổi vào một transaction |
| Specification | DDD/Spring Data | Bắt buộc | `StaffProductSpecification` | Tạo bộ lọc động cho trang quản trị |
| Builder | GoF Creational | Bắt buộc | Entity/DTO builders | Tạo object phức tạp rõ ràng |
| Factory Method / Simple Factory | GoF Creational | Bắt buộc | `ProductIdentityFactory` | Sinh slug, mã sản phẩm, SKU |
| Chain of Responsibility | GoF Behavioral | Nên áp dụng | `ProductValidationHandler` chain | Tách các rule validate độc lập |
| Strategy / Policy | GoF Behavioral | Nên áp dụng | `ProductDeletionPolicy`, `ProductVariantMergePolicy` | Tách chính sách xóa/update |
| State | GoF Behavioral | Nên áp dụng nhẹ | `StaffProductStatus`, `ProductStockStateResolver` | Tính trạng thái active/inactive/out-of-stock |
| Observer / Domain Event | GoF Behavioral | Nên áp dụng có điều kiện | `ProductPriceDroppedEvent` | Kích hoạt thông báo wishlist khi giảm giá |
| Cache-Aside Eviction | Cloud Pattern | Nên áp dụng | Cache eviction sau mutate | Tránh dữ liệu storefront bị cũ |
| Adapter | GoF Structural | Có điều kiện | `ProductImageStorageAdapter` | Bọc S3/upload ảnh nếu branch xử lý upload trực tiếp |
| Retry | Cloud Pattern | Có điều kiện | Upload ảnh/S3 client | Xử lý lỗi tạm thời của remote service |
| Circuit Breaker | Cloud Pattern | Có điều kiện | Upload/email external dependency | Tránh lỗi kéo dài làm nghẽn hệ thống |
| Command | GoF Behavioral | Phase sau | `CreateProductCommand`, `UpdateStockCommand` | Audit/undo thao tác staff |
| Memento | GoF Behavioral | Phase sau | `ProductPriceSnapshot` | Lưu snapshot lịch sử thay đổi giá |

## IV. Pattern bắt buộc áp dụng

### 1. Service Layer Pattern

#### Vấn đề

Controller staff không nên chứa nghiệp vụ tạo sản phẩm, kiểm tra category, sinh SKU, xử lý ảnh, xử lý biến thể, cập nhật stock, xóa mềm và clear cache.

#### Thiết kế

Tạo interface:

```text
StaffProductService
```

Các operation chính:

```text
getProducts(...)
getProductDetail(productId)
createProduct(request)
updateProduct(productId, request)
updateVisibility(productId, request)
deleteProduct(productId)
updateStock(productId, variantId, request)
```

#### Lợi ích

- Controller mỏng và dễ test.
- Transaction boundary nằm ở service.
- Các API staff có một điểm điều phối thống nhất.

### 2. Facade Pattern

#### Vấn đề

UC-15 và UC-16 không phải CRUD đơn giản. Một request tạo/cập nhật sản phẩm chạm đến:

- `Product`
- `Category`
- `ProductImage`
- `ProductVariant`
- slug/SKU generation
- validation
- cache invalidation
- event giảm giá nếu có

#### Thiết kế

`StaffProductServiceImpl` đóng vai trò một application facade. Nó trực tiếp đứng ra điều phối các hệ thống con và các bước nghiệp vụ phức tạp của phân hệ quản lý sản phẩm (bao gồm validation chain, identity factory, deletion policy, cache eviction, và events) mà không cần qua lớp facade trung gian, giúp giữ kiến trúc Spring Boot tối giản và dễ bảo trì.

Facade thực hiện thứ tự:

```text
validate request
load category/product
generate slug/SKU
map request -> entity
save aggregate
evict cache
publish event if needed
return response
```

#### Lợi ích

- Che giấu luồng nghiệp vụ phức tạp khỏi controller.
- Giảm coupling giữa API layer và domain/persistence details.
- Dễ thêm bước mới như audit log hoặc publish event.

### 3. Repository Pattern

#### Vấn đề

Staff search và delete cần nhiều query hơn storefront:

- tìm theo keyword
- lọc theo category
- lọc active/inactive
- lọc out-of-stock
- kiểm tra tên trùng trong cùng danh mục
- kiểm tra sản phẩm đã có đơn hàng chưa
- lock variant khi cập nhật stock

#### Thiết kế

Bổ sung method vào:

```text
ProductRepository
ProductVariantRepository
OrderItemRepository
```

Không viết query trong service nếu query có thể thuộc repository.

#### Lợi ích

- Query logic tập trung.
- Service đọc như nghiệp vụ hơn.
- Dễ mock repository trong unit test.

### 4. Unit of Work Pattern

#### Vấn đề

Tạo/cập nhật sản phẩm gồm nhiều thay đổi phải commit hoặc rollback cùng nhau:

- product
- images
- variants
- stock
- visibility

Nếu một phần fail mà phần khác đã lưu sẽ làm dữ liệu lệch.

#### Thiết kế

Dùng `@Transactional`:

```text
@Transactional
createProduct(...)

@Transactional
updateProduct(...)

@Transactional
deleteProduct(...)

@Transactional
updateStock(...)
```

Riêng `updateStock` cần lock variant bằng `PESSIMISTIC_WRITE`.

#### Lợi ích

- Dữ liệu nhất quán.
- Rollback tự động khi validation/persistence fail.
- Giảm rủi ro lost update khi thao tác tồn kho.

### 5. Specification Pattern

#### Vấn đề

Staff product search có nhiều tổ hợp filter. Nếu viết repository method cho từng tổ hợp sẽ bùng nổ số lượng method.

#### Thiết kế

Tạo:

```text
pattern/specification/StaffProductSpecification.java
```

Các predicate đề xuất:

```text
keywordContains(keyword)
belongsToCategory(categoryId)
hasActiveStatus(active)
isOutOfStock()
isLowStock(threshold)
```

Không dùng trực tiếp `ProductSpecification.fromCriteria` hiện có vì nó mặc định chỉ lấy sản phẩm active cho storefront.

#### Lợi ích

- Filter động, compose được.
- Bám đúng cách Spring Data JPA hỗ trợ `JpaSpecificationExecutor`.
- Dễ mở rộng thêm filter theo giá, size, màu, featured.

### 6. Builder Pattern

#### Vấn đề

`Product`, `ProductVariant`, `ProductImage` có nhiều field. Dùng constructor dài sẽ dễ nhầm tham số và khó đọc.

#### Thiết kế

Dùng Lombok builder hiện có:

```text
Product.builder()
ProductVariant.builder()
ProductImage.builder()
StaffProductDetailResponse.builder()
```

#### Lợi ích

- Code tạo object rõ nghĩa.
- Dễ bỏ qua field optional.
- Hợp với entity hiện tại đã có `@Builder`.

### 7. Factory Method / Simple Factory Pattern

#### Vấn đề

Tài liệu yêu cầu:

- mã/slug sản phẩm tự sinh
- SKU biến thể tự sinh
- SKU không trùng

Nếu để service tự nối chuỗi slug/SKU, logic này sẽ bị lặp ở create/update/import sau này.

#### Thiết kế

Tạo:

```text
pattern/factory/product/ProductIdentityFactory.java
```

Trách nhiệm:

```text
createSlug(productName)
createProductCode(productName/category)
createSku(productSlug, size, color)
createUniqueSku(..., skuExistsPredicate)
```

#### Lợi ích

- Tách logic khởi tạo identity khỏi service.
- Dễ thay đổi format SKU.
- Dễ test riêng.

## V. Pattern nên áp dụng

### 1. Chain of Responsibility Pattern

#### Áp dụng

Tạo validation chain cho create/update product:

```text
ProductValidationHandler
CategoryExistsValidationHandler
ProductNameUniqueValidationHandler
ProductPriceValidationHandler
ProductImageValidationHandler
ProductVariantValidationHandler
ProductVariantDuplicateValidationHandler
```

#### Vai trò

Mỗi handler kiểm một nhóm rule. Handler nào fail thì ném exception phù hợp.

#### Khi nào nên dùng

Nên dùng nếu validation nhiều hơn 4-5 rule và có khả năng tái sử dụng giữa create/update.

### 2. Strategy / Policy Pattern

#### Áp dụng 1: Chính sách xóa sản phẩm

```text
ProductDeletionPolicy
```

Quyết định:

- hard delete nếu sản phẩm chưa có liên kết nghiệp vụ
- soft delete nếu đã có đơn hàng/review/wishlist/cart/collection

#### Áp dụng 2: Chính sách merge variant/image

```text
ProductVariantMergePolicy
ProductImageMergePolicy
```

Quyết định:

- replace-all
- patch theo id
- deactivate thay vì remove nếu đã có liên kết đơn hàng

#### Lợi ích

- Tránh if/else dài trong service.
- Dễ đổi chính sách mà không đổi controller.

### 3. State Pattern hoặc State Resolver

#### Áp dụng

Tạo enum:

```text
StaffProductStatus
```

Giá trị:

```text
ACTIVE
INACTIVE
OUT_OF_STOCK
LOW_STOCK
```

Tạo resolver:

```text
ProductStockStateResolver
```

#### Vai trò

Tính trạng thái hiển thị cho staff từ:

- `product.isActive`
- tổng stock các variant active
- ngưỡng low stock `< 10`

#### Ghi chú

Phase đầu chỉ cần resolver. Nếu sau này trạng thái có hành vi riêng, có thể nâng cấp thành State Pattern đầy đủ.

### 4. Observer / Domain Event Pattern

#### Áp dụng

Khi staff cập nhật `salePrice` thấp hơn giá cũ, publish:

```text
ProductPriceDroppedEvent
```

Listener/observer:

```text
WishlistNotificationObserver
```

#### Vai trò

Tách luồng cập nhật sản phẩm khỏi luồng gửi email cho khách hàng đã wishlist.

#### Điều kiện dùng

Chỉ nên kích hoạt nếu branch có scope cập nhật giá giảm. Nếu branch cần gọn, để phase sau nhưng thiết kế sẵn điểm mở rộng.

### 5. Cache-Aside Eviction Pattern

#### Áp dụng

Các dữ liệu có thể đang cache:

- `newArrivals`
- `bestSellers`
- product detail
- category product listing nếu sau này cache

Sau các thao tác mutate:

```text
createProduct
updateProduct
updateVisibility
deleteProduct
updateStock
```

cần evict cache liên quan.

#### Vai trò

Repository hiện đã có Redis cache cho một số danh sách storefront. Nếu không clear cache, staff cập nhật xong nhưng storefront vẫn hiển thị dữ liệu cũ.

## VI. Pattern áp dụng có điều kiện

### 1. Adapter Pattern

Dùng nếu branch xử lý upload ảnh trực tiếp lên S3.

Thiết kế:

```text
ProductImageStorageAdapter
S3ProductImageStorageAdapter
```

Nếu frontend đã upload ảnh trước và backend chỉ nhận `imageUrl`, chưa cần tạo adapter.

### 2. Retry Pattern

Dùng cho lỗi tạm thời khi gọi remote service như S3. Không dùng retry cho create product DB nếu operation không idempotent.

### 3. Circuit Breaker Pattern

Dùng nếu upload ảnh/email là dependency thường lỗi hoặc có nguy cơ làm nghẽn request. Có thể fallback bằng cách trả lỗi upload rõ ràng hoặc lưu product trước rồi xử lý ảnh sau nếu nghiệp vụ cho phép.

### 4. Command Pattern

Dùng nếu cần audit thao tác staff:

```text
CreateProductCommand
UpdateProductCommand
HideProductCommand
UpdateStockCommand
```

Phase đầu chưa bắt buộc vì service method hiện đủ rõ.

### 5. Memento Pattern

Dùng nếu triển khai lịch sử thay đổi giá:

```text
ProductPriceSnapshot
ProductPriceHistory
```

Tài liệu UC-16d có nhắc lịch sử thay đổi giá, nhưng code hiện chưa có entity. Không nên fake dữ liệu. Nếu cần, tạo phase riêng.

## VII. Pattern không nên áp dụng trong branch này

| Pattern | Lý do |
|---|---|
| Singleton | Spring bean đã singleton mặc định, tự viết Singleton làm khó test |
| Abstract Factory | Chưa có họ object liên quan đủ phức tạp |
| Prototype | Chưa có chức năng clone/nhân bản sản phẩm |
| Composite | Hợp với danh mục đa cấp, nhưng category thuộc branch khác |
| Decorator | Chưa có nhu cầu bọc thêm hành vi động quanh product |
| Visitor | Không có object structure cần duyệt nhiều loại node |
| Flyweight | Size/color/material chưa đủ lớn để cần chia sẻ object |
| Mediator | Chưa có nhiều component ngang hàng tương tác phức tạp |

## VIII. Cấu trúc file pattern thực tế đã áp dụng

```text
src/main/java/vn/hcmute/edu/dp/nhom10/backend/
├── enums/
│   └── StaffProductStatus.java                         # Enum dùng chung cho trạng thái
├── pattern/
│   ├── chain/
│   │   └── product/
│   │       ├── ProductValidationContext.java           # Context dùng trong chuỗi validate
│   │       ├── ProductValidationHandler.java           # Handler cơ sở cho chuỗi
│   │       ├── CategoryExistsValidationHandler.java   # Kiểm tra danh mục tồn tại & hoạt động
│   │       ├── ProductImageValidationHandler.java      # Kiểm tra số lượng hình ảnh tối thiểu
│   │       ├── ProductNameUniqueValidationHandler.java # Kiểm tra trùng tên sản phẩm trong danh mục
│   │       ├── ProductPriceValidationHandler.java      # Kiểm tra giá bán lẻ/khuyến mãi hợp lệ
│   │       └── ProductVariantValidationHandler.java    # Kiểm tra tính hợp lệ và không trùng lặp của biến thể
│   ├── factory/
│   │   └── product/
│   │       └── ProductIdentityFactory.java             # Factory sinh slug và SKU tự động
│   ├── policy/
│   │   └── product/
│   │       ├── ProductDeletionDecision.java            # Enum kết quả quyết định xóa (HARD/SOFT)
│   │       └── ProductDeletionPolicy.java              # Chiến lược kiểm tra ràng buộc & quyết định xóa an toàn
│   ├── specification/
│   │   └── StaffProductSpecification.java              # Bộ sinh đặc tả lọc JPA động
│   └── state/
│       └── product/
│           └── ProductStockStateResolver.java          # Bộ phân tích/giải quyết trạng thái tồn kho động
```

Toàn bộ các cấu trúc mẫu thiết kế trên đã được triển khai hoàn chỉnh và kiểm thử thành công 100% trên branch `feature/staff-product-management`.

## IX. Kết luận thiết kế

Tổ hợp pattern hợp lý nhất cho branch `feature/staff-product-management` là:

1. Service Layer + Facade để điều phối use case staff.
2. Repository + Unit of Work để bảo vệ persistence và transaction.
3. Specification để tìm kiếm/lọc sản phẩm linh hoạt.
4. Builder + Factory để tạo object và identity rõ ràng.
5. Chain of Responsibility để tách validation.
6. Strategy/Policy để tách quyết định xóa và merge dữ liệu con.
7. State Resolver để biểu diễn trạng thái sản phẩm/tồn kho.
8. Observer/Event và Cache-Aside Eviction để đồng bộ tác động phụ sau khi cập nhật sản phẩm.

Cách này vừa bám GoF, vừa bám enterprise patterns, vừa phù hợp cấu trúc Spring Boot hiện tại của dự án.
