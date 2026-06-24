# Kế hoạch xây dựng chức năng Staff Category & Collection Management

## 1. Thông tin nhanh về branch

- **Branch**: `feature/staff-category-collection-management`
- **Phạm vi chính**: 
  - Quản lý danh mục sản phẩm (CRUD, phân cấp tối đa 3 cấp, kiểm soát ràng buộc an toàn sản phẩm).
  - Quản lý bộ sưu tập (CRUD, lên lịch áp dụng, liên kết/gỡ sản phẩm).
- **Tác nhân**: Nhân viên (`STAFF`)
- **Tài liệu căn cứ**:
  - `Nhom10_Project2.docx`
  - `usecase_detail_khac.md`
  - Source code hiện có: `Category`, `Collection`, `CollectionProduct`, `CategoryRepository`, `CollectionRepository`, `GuestCategoryController`, `GuestCollectionController`.

---

## 2. Phạm vi trong branch này

Tập trung vào phân hệ tổ chức danh mục và bộ sưu tập bán hàng:
1. **Quản lý danh mục**:
   - Lấy sơ đồ cây danh mục phả hệ (mức độ sâu 1-3).
   - Tạo danh mục mới (hỗ trợ chọn danh mục cha, tự sinh SEO slug).
   - Cập nhật danh mục (đổi tên, mô tả, hiển thị, thứ tự, thay đổi cha).
   - Xóa danh mục (kiểm tra an toàn: không được phép xóa nếu danh mục hoặc danh mục con của nó còn sản phẩm liên kết).
2. **Quản lý bộ sưu tập**:
   - Xem danh sách bộ sưu tập (có phân trang, tìm kiếm).
   - Lấy chi tiết bộ sưu tập (gồm thông tin bộ sưu tập và danh sách sản phẩm thuộc về nó).
   - Tạo bộ sưu tập (tên, slug, mô tả, ảnh banner, ngày bắt đầu, ngày kết thúc).
   - Cập nhật bộ sưu tập.
   - Xóa bộ sưu tập (chỉ xóa liên kết sản phẩm, không xóa sản phẩm vật lý).
   - Thêm sản phẩm vào bộ sưu tập.
   - Gỡ sản phẩm khỏi bộ sưu tập.

---

## 3. Hiện trạng code liên quan

### 3.1. Entity có sẵn
- `Category`
  - Có các trường: `id`, `name`, `slug`, `description`, `displayOrder`, `isActive`, `createdAt`, `updatedAt`.
  - Có trường tự liên kết cây: `parent` (`Category`) và `children` (`List<Category>`).
  - Có liên kết với `Product`: `products` (`List<Product>`).
- `Collection`
  - Có các trường: `id`, `name`, `slug`, `description`, `bannerUrl`, `startDate`, `endDate`, `isActive`, `createdAt`, `updatedAt`.
  - Có liên kết trung gian với sản phẩm: `collectionProducts` (`List<CollectionProduct>`).
- `CollectionProduct`
  - Thực thể trung gian Many-to-Many giữa `Collection` và `Product`.
  - Có các trường: `id`, `collection`, `product`, `displayOrder`.

### 3.2. Controller và Service có sẵn
- `GuestCategoryController` & `CategoryService` / `CategoryServiceImpl`
  - Chỉ có 1 hàm public duy nhất là `getCategoryHierarchy()` để hiển thị danh mục dạng cây ngoài trang chủ.
- `GuestCollectionController` & `CollectionService` / `CollectionServiceImpl`
  - Chỉ có 1 hàm public duy nhất là `getCollectionBySlug(String slug)` để lấy thông tin hiển thị trang bộ sưu tập ngoài trang chủ.

### 3.3. Repository có sẵn
- `CategoryRepository`
  - Có hàm tìm kiếm danh mục gốc: `findByIsActiveTrueAndParentIsNullOrderByDisplayOrderAsc()`.
  - Có hàm tìm kiếm theo slug: `findBySlugAndIsActiveTrue(...)`.
  - Có native query tìm ID tất cả danh mục con: `findAllDescendantIds(@Param("categoryId") Long categoryId)`.
- `CollectionRepository`
  - Có hàm tìm kiếm theo slug hoạt động: `findBySlugAndIsActiveTrue(String slug)`.

---

## 4. Yêu cầu nghiệp vụ rút ra từ `Nhom10_Project2.docx`

### 4.1. Quy tắc Danh mục (QĐ5)
- Danh mục hỗ trợ đa cấp, tối đa là 3 cấp.
- Không được phép xóa danh mục nếu danh mục đó hoặc các danh mục con trực thuộc còn chứa sản phẩm đang hoạt động.
- Khi tạo/cập nhật danh mục, slug không được trùng lặp.
- Khi đổi danh mục cha của một danh mục hiện tại, phải kiểm tra:
  1. Không tạo chu trình vòng lặp (ví dụ: gán cha của A là B, trong khi B lại là con của A).
  2. Tổng độ sâu của nhánh danh mục sau khi chuyển không vượt quá 3 cấp (ví dụ: A có nhánh con sâu 2 cấp, nếu chuyển A làm con của B có cấp là 2, tổng cấp sẽ là 2 + 2 = 4, vi phạm quy định).

### 4.2. Quy tắc Bộ sưu tập
- Bộ sưu tập có hiệu lực hiển thị theo thời gian đặt lịch (`startDate` đến `endDate`).
- Bộ sưu tập cần có ảnh banner để hiển thị slider.
- Nhân viên có thể thêm nhiều sản phẩm vào bộ sưu tập, gỡ sản phẩm, hoặc thiết lập thứ tự hiển thị (`display_order`) của các sản phẩm trong bộ sưu tập.
- Khi xóa bộ sưu tập, các sản phẩm liên quan không bị xóa, chỉ các dòng liên kết trong bảng `collection_products` bị loại bỏ.

---

## 5. API Endpoints đề xuất

Chúng tôi tuân thủ tiền tố `/api/staff` của nhân viên:

```text
# Danh mục
GET    /api/staff/categories/hierarchy    # Lấy sơ đồ cây danh mục phục vụ trang quản trị
POST   /api/staff/categories              # Tạo danh mục mới
PUT    /api/staff/categories/{id}         # Cập nhật danh mục
DELETE /api/staff/categories/{id}         # Xóa danh mục (kiểm tra ràng buộc)

# Bộ sưu tập
GET    /api/staff/collections             # Xem danh sách bộ sưu tập (phân trang, search)
GET    /api/staff/collections/{id}        # Xem chi tiết bộ sưu tập + danh sách sản phẩm liên kết
POST   /api/staff/collections             # Tạo bộ sưu tập mới
PUT    /api/staff/collections/{id}        # Cập nhật thông tin bộ sưu tập
DELETE /api/staff/collections/{id}        # Xóa bộ sưu tập
POST   /api/staff/collections/{id}/products # Gán danh sách sản phẩm vào bộ sưu tập
DELETE /api/staff/collections/{id}/products # Gỡ danh sách sản phẩm khỏi bộ sưu tập
```

---

## 6. DTOs đề xuất

### 6.1. Request DTOs
- `StaffCategoryRequest`: Dùng cho cả POST (tạo) và PUT (cập nhật) danh mục.
  - `name` (Bắt buộc, tối đa 100)
  - `slug` (Không bắt buộc, tự sinh nếu trống)
  - `parentId` (Không bắt buộc)
  - `displayOrder` (Mặc định 0)
  - `description` (Mô tả chi tiết)
  - `isActive` (Mặc định true)
- `StaffCollectionRequest`: Dùng tạo/cập nhật thông tin bộ sưu tập.
  - `name` (Bắt buộc)
  - `slug` (Không bắt buộc)
  - `description`
  - `bannerUrl`
  - `startDate` (Lập lịch chạy)
  - `endDate`
  - `isActive`
- `StaffCollectionProductsRequest`:
  - `productIds` (Danh sách các ID sản phẩm cần gán/gỡ)

### 6.2. Response DTOs
- `StaffCategoryResponse`: Trả về thông tin chi tiết danh mục, kèm độ sâu (`depth`), tổng số sản phẩm đệ quy (`recursiveProductCount`), và mảng con `children`.
- `StaffCollectionResponse`: Trả về thông tin bộ sưu tập, trạng thái giải quyết động (`statusState`: ACTIVE/INACTIVE/SCHEDULED/EXPIRED), và tổng số sản phẩm hiện có.
- `StaffCollectionDetailResponse`: Bọc `StaffCollectionResponse` cùng danh sách `ProductGridResponse` biểu thị các sản phẩm đang có trong bộ sưu tập.

---

## 7. Thiết kế Services

### 7.1. StaffCategoryService
```java
List<StaffCategoryResponse> getCategoryHierarchy();
StaffCategoryResponse createCategory(StaffCategoryRequest request, String username);
StaffCategoryResponse updateCategory(Long id, StaffCategoryRequest request, String username);
void deleteCategory(Long id, String username);
```

### 7.2. StaffCollectionService
```java
PageResponse<StaffCollectionResponse> getCollections(int page, int size, String keyword);
StaffCollectionDetailResponse getCollectionDetail(Long id);
StaffCollectionResponse createCollection(StaffCollectionRequest request, String username);
StaffCollectionResponse updateCollection(Long id, StaffCollectionRequest request, String username);
void deleteCollection(Long id, String username);
StaffCollectionDetailResponse addProductsToCollection(Long id, StaffCollectionProductsRequest request, String username);
StaffCollectionDetailResponse removeProductsFromCollection(Long id, StaffCollectionProductsRequest request, String username);
```

---

## 8. Thiết kế Repositories

### 8.1. CategoryRepository
- Sử dụng JPA mặc định cho các nghiệp vụ CRUD cơ bản.
- Query tìm danh mục theo slug hoặc cha.
- Sử dụng native query đệ quy có sẵn `findAllDescendantIds` để hỗ trợ tối ưu tìm kiếm con cháu trong DB nếu không muốn duyệt cây trên JVM (tuy nhiên JVM đệ quy qua Composite Visitor được khuyến khích khi cây danh mục nhỏ và được lưu bộ đệm).

### 8.2. CollectionRepository
- Query phân trang danh sách bộ sưu tập.
- Query kiểm tra trùng slug bộ sưu tập.

---

## 9. Design Patterns áp dụng

Xem mô tả chi tiết tại [design_patterns_staff_category_collection_management.md](file:///D:/MauThietKePhanMem/ProjectCuoiKy/Project/Clothing-Store-Backend/src/main/resources/docs/design-patterns/design_patterns_staff_category_collection_management.md).

1. **Composite**: Áp dụng cho cấu trúc cây `Category` để truy xuất, quản lý cấu trúc phả hệ.
2. **Visitor**: Áp dụng duyệt cây danh mục để tính độ sâu (`CategoryDepthVisitor`), đếm sản phẩm (`CategoryProductCountVisitor`), và chống vòng lặp (`CategoryCycleDetectionVisitor`).
3. **Strategy**: Tạo slug tự động từ tên tiếng Việt không dấu (`VietnameseSlugGenerationStrategy`).
4. **Policy**: Tránh xóa danh mục khi có sản phẩm liên kết (`CategoryDeletionPolicy`).
5. **Command**: Đóng gói các lệnh ghi dữ liệu của nhân viên (`CatalogCommand`) để tự động ghi log vào bảng `ActivityLog` và làm trống cache Redis.
6. **State**: Resolver xác định trạng thái Collection hoạt động theo thời gian biểu (`CollectionStateResolver`).

---

## 10. Quy tắc Validation chi tiết

### 10.1. Danh mục sản phẩm (Category)
- Tên danh mục không trùng lặp trong cùng một nhánh cha (tức là hai danh mục con của cùng một cha không được trùng tên).
- Slug không được trùng lặp trên toàn hệ thống.
- Độ sâu cây danh mục không vượt quá 3.
- Chu trình phả hệ: Một danh mục không được làm cha của chính nó, hoặc làm cha của các danh mục tổ tiên của nó.

### 10.2. Bộ sưu tập (Collection)
- Ngày bắt đầu (`startDate`) phải trước ngày kết thúc (`endDate`).
- Tên bộ sưu tập không được để trống.
- Slug không được trùng lặp.

---

## 11. Transaction và Đồng thời (Concurrency)

- Tất cả các tác vụ ghi (`create`, `update`, `delete`, `addProducts`, `removeProducts`) đều bắt buộc cấu hình `@Transactional` trên Service.
- Sử dụng `@CacheEvict(value = {"categories", "collections"}, allEntries = true)` để xóa cache Redis sau khi cập nhật dữ liệu thành công.

---

## 12. Kịch bản kiểm thử (Test Plan)

### 12.1. Unit Test cho các Pattern
- **Composite & Visitor**:
  - Test `CategoryDepthVisitor` tính chính xác độ sâu.
  - Test `CategoryProductCountVisitor` đếm đệ quy số sản phẩm của danh mục gốc và danh mục con.
  - Test `CategoryCycleDetectionVisitor` phát hiện đúng vòng lặp phân cấp.
- **Strategy**:
  - Test `VietnameseSlugGenerationStrategy` xóa chính xác tất cả các loại dấu tiếng Việt phức tạp và ký tự đặc biệt.
- **State**:
  - Test `CollectionStateResolver` phân loại đúng các trạng thái hoạt động: chưa chạy (`SCHEDULED`), đang chạy (`ACTIVE`), kết thúc (`EXPIRED`), bị tắt (`INACTIVE`).
- **Policy**:
  - Test `CategoryDeletionPolicy` ngăn chặn thành công việc xóa danh mục còn chứa sản phẩm trực thuộc.

### 12.2. Unit Test cho Service & Controller
- Test CRUD danh mục thành công, thất bại khi vượt quá 3 cấp.
- Test CRUD bộ sưu tập, gán/gỡ sản phẩm thành công.
- Test bảo mật phân quyền: Chỉ tài khoản có vai trò `ROLE_STAFF` mới được gọi các endpoint của API này. Các khách hàng (`CUSTOMER`) hoặc khách vãng lai (`GUEST`) gọi vào phải nhận mã lỗi `403 Forbidden` hoặc `401 Unauthorized`.

---

## 13. File dự kiến thêm mới và chỉnh sửa

### 13.1. Thêm mới nguồn (Main Source)
- **DTOs**:
  - `StaffCategoryRequest.java`, `StaffCollectionRequest.java`, `StaffCollectionProductsRequest.java`
  - `StaffCategoryResponse.java`, `StaffCollectionResponse.java`, `StaffCollectionDetailResponse.java`
- **Enums**:
  - `CollectionStatusState.java`
- **Patterns**:
  - `pattern/visitor/category/CategoryVisitor.java`
  - `pattern/visitor/category/CategoryDepthVisitor.java`
  - `pattern/visitor/category/CategoryProductCountVisitor.java`
  - `pattern/visitor/category/CategoryCycleDetectionVisitor.java`
  - `pattern/strategy/slug/SlugGenerationStrategy.java`
  - `pattern/strategy/slug/VietnameseSlugGenerationStrategy.java`
  - `pattern/strategy/slug/DefaultSlugGenerationStrategy.java`
  - `pattern/policy/category/CategoryDeletionPolicy.java`
  - `pattern/command/catalog/CatalogCommand.java`
  - `pattern/command/catalog/CatalogCommandExecutor.java`
  - `pattern/state/collection/CollectionStateResolver.java`
- **Services & Impl**:
  - `StaffCategoryService.java`, `StaffCategoryServiceImpl.java`
  - `StaffCollectionService.java`, `StaffCollectionServiceImpl.java`
- **Controllers**:
  - `StaffCategoryController.java`, `StaffCollectionController.java`

### 13.2. Chỉnh sửa nguồn
- `CategoryRepository.java` (thêm phương thức kiểm tra trùng slug/tên nếu cần).
- `CollectionRepository.java` (thêm phương thức tìm kiếm).

### 13.3. File test dự kiến
- `CategoryCompositeTest.java` (Test composite & visitors).
- `SlugGenerationStrategyTest.java` (Test strategy slug).
- `CategoryDeletionPolicyTest.java` (Test chính sách xóa).
- `CollectionStateTest.java` (Test phân giải trạng thái).
- `StaffCategoryServiceImplTest.java` (Test nghiệp vụ danh mục).
- `StaffCollectionServiceImplTest.java` (Test nghiệp vụ bộ sưu tập).

---

## 14. Checklist triển khai

- [ ] Định nghĩa các Request/Response DTOs và các Enum trạng thái.
- [ ] Triển khai các lớp Pattern: Composite & Visitor, Strategy Slug, Deletion Policy, State Resolver.
- [ ] Viết interface `StaffCategoryService` và `StaffCollectionService`.
- [ ] Thực thi `CatalogCommand` và `CatalogCommandExecutor` phục vụ Logging & Cache.
- [ ] Implement chi tiết nghiệp vụ `StaffCategoryServiceImpl` và `StaffCollectionServiceImpl`.
- [ ] Viết bộ điều khiển API `StaffCategoryController` và `StaffCollectionController`.
- [ ] Viết và hoàn thiện các Unit Test cho tất cả các Pattern và Services.
- [ ] Kiểm tra lỗi bảo mật phân quyền (STAFF Role).
- [ ] Chạy kiểm thử toàn bộ dự án (`mvn clean test`) để đảm bảo không phát sinh lỗi bất kỳ.
