# TÀI LIỆU THIẾT KẾ: DESIGN PATTERNS ÁP DỤNG CHO STAFF CATEGORY & COLLECTION MANAGEMENT

## I. Thông tin phạm vi

- **Branch**: `feature/staff-category-collection-management`
- **Module**: Quản lý danh mục & Bộ sưu tập của nhân viên
- **Use case áp dụng**:
  - UC-15 (QĐ5): Quản lý danh mục sản phẩm (CRUD, kiểm soát cây phả hệ tối đa 3 cấp, kiểm tra ràng buộc không xóa danh mục còn sản phẩm).
  - UC-16: Quản lý bộ sưu tập (CRUD, lập lịch thời gian áp dụng, liên kết/gỡ sản phẩm khỏi bộ sưu tập).
- **Tài liệu căn cứ**:
  - `Nhom10_Project2.docx`
  - `usecase_detail_sanpham.md`
  - GoF Design Patterns: `Design Patterns - Elements of Reusable Object-Oriented Software.chm`

---

## II. Nguyên tắc chọn pattern

1. **Giảm thiểu Coupling**: Giữ cho core logic của các Entity (`Category`, `Collection`) độc lập với các thuật toán kiểm tra ràng buộc hay logic định dạng dữ liệu.
2. **Khả năng mở rộng (OCP)**: Dễ dàng mở rộng thêm các thuật toán tạo SEO slug, các trạng thái thời gian của Collection, hoặc các điều kiện validation mà không làm thay đổi các Service cốt lõi.
3. **Phù hợp với kiến trúc Spring Boot/JPA**: Tích hợp mượt mà với Spring Data JPA repositories, Cache-Aside mechanism (`@CacheEvict`) và cơ chế Transaction (`@Transactional`).
4. **Đúng trách nhiệm (SRP)**: Phân rã rõ rệt giữa logic lưu trữ, logic kiểm tra nghiệp vụ phức tạp, và logic ghi log hoạt động (audit).

---

## III. Ma trận pattern áp dụng

| Pattern | Nhóm | Trạng thái | Thành phần áp dụng | Vai trò chính |
|---|---|---|---|---|
| **Composite** | GoF Structural | Bắt buộc | `Category` entity, `CategoryNode` | Biểu diễn cây thư mục cha-con đa cấp đồng nhất |
| **Visitor** | GoF Behavioral | Bắt buộc | `CategoryVisitor`, `CategoryDepthVisitor`, `CategoryProductCountVisitor` | Tách rời các logic duyệt cây (độ sâu, đếm sản phẩm, phát hiện vòng lặp) khỏi Entity |
| **Strategy** | GoF Behavioral | Bắt buộc | `SlugGenerationStrategy`, `VietnameseSlugGenerationStrategy` | Thuật toán tạo URL SEO slug tự động từ tên tiếng Việt |
| **Command** | GoF Behavioral | Bắt buộc | `CatalogCommand`, `CatalogCommandExecutor` | Encapsulate các action ghi dữ liệu, tự động kích hoạt audit log và evict cache |
| **State** | GoF Behavioral | Bắt buộc | `CollectionStateResolver`, `CollectionStatusState` | Giải quyết trạng thái hoạt động của Collection theo thời gian thực tế |
| **Policy** | GoF/DDD | Bắt buộc | `CategoryDeletionPolicy` | Đóng gói kiểm tra các ràng buộc an toàn trước khi thực hiện xóa cứng danh mục |
| **Observer** | GoF Behavioral | Nên áp dụng | Spring ApplicationEvent (`CategoryModifiedEvent`) | Đồng bộ hoặc xử lý phi tuần tự các tác vụ phụ sau khi catalog thay đổi |

---

## IV. Chi tiết thiết kế các Pattern

### 1. Composite Pattern (Mẫu Tổ hợp)

#### Vấn đề
Danh mục sản phẩm (`Category`) có cấu trúc cha-con không giới hạn ở mức cơ sở dữ liệu. Tuy nhiên, quy định nghiệp vụ (QĐ5) đặt giới hạn độ sâu tối đa là 3 cấp. Việc xử lý cả danh mục gốc (Root) và danh mục con (Sub-category) cần đồng nhất để Client có thể tương tác với bất kỳ danh mục nào mà không cần phân biệt cấp bậc.

#### Giải pháp
Treat `Category` entity như một Composite Object. Mỗi `Category` đều có:
- Thuộc tính `parent` kiểu `Category` (chỉ đến cha).
- Thuộc tính `children` kiểu `List<Category>` (chứa các con).

Một interface logic hoặc các thao tác đệ quy trên cây sẽ cho phép thực thi đồng nhất trên cả nút lá (leaf - cấp 3) và nút nhánh (composite - cấp 1, 2).

---

### 2. Visitor Pattern (Mẫu Khách viếng thăm)

#### Vấn đề
Cấu trúc Composite của `Category` đòi hỏi nhiều hoạt động duyệt cây khác nhau:
1. Tính độ sâu tối đa hiện tại của một nhánh (phục vụ giới hạn 3 cấp).
2. Đếm tổng số sản phẩm thuộc về một danh mục và tất cả danh mục con của nó.
3. Phát hiện chu trình vòng lặp (vô tình gán cha là con của chính nó khi cập nhật).

Nếu viết tất cả logic đệ quy này trực tiếp vào `Category` entity hoặc viết dàn trải dưới dạng spaghetti code ở `CategoryServiceImpl`, code sẽ rất khó đọc, khó viết unit test biệt lập và vi phạm nguyên lý Single Responsibility.

#### Giải pháp
Áp dụng **Visitor Pattern** phối hợp với cấu trúc Composite:
- Định nghĩa interface `CategoryVisitor<R>` với hàm `R visit(Category category)`.
- Tạo các lớp thực thi Visitor chuyên biệt:
  - `CategoryDepthVisitor`: Tính toán độ sâu lớn nhất của nhánh danh mục.
  - `CategoryProductCountVisitor`: Đếm tổng số sản phẩm thuộc danh mục và các con.
  - `CategoryCycleDetectionVisitor`: Phát hiện vòng lặp phân cấp.

#### Minh họa sơ đồ lớp:
```text
  CategoryVisitor <---- visit() ---- CategoryDepthVisitor (Tính độ sâu)
         ^
         |----------- visit() ---- CategoryProductCountVisitor (Đếm sản phẩm đệ quy)
         ^
         |----------- visit() ---- CategoryCycleDetectionVisitor (Phát hiện vòng lặp)
```

---

### 3. Strategy Pattern (Mẫu Chiến lược)

#### Vấn đề
Mỗi danh mục hoặc bộ sưu tập đều yêu cầu một thuộc tính `slug` độc nhất để làm URL thân thiện với SEO. Nếu nhân viên không nhập slug thủ công, hệ thống phải tự động sinh slug từ tên (Name).
Việc tạo slug cần hỗ trợ nhiều chiến lược khác nhau:
1. Tạo slug chuẩn hóa tiếng Việt (loại bỏ dấu tiếng Việt, ví dụ: "Áo sơ mi nam" -> "ao-so-mi-nam").
2. Tạo slug đơn giản (chỉ chuyển chữ thường và thay dấu cách bằng gạch ngang).

#### Giải pháp
Định nghĩa interface `SlugGenerationStrategy` có hàm `String generate(String input)`.
Cài đặt các strategy tương ứng:
- `VietnameseSlugGenerationStrategy`: Dùng `Normalizer` và Regular Expression để xóa dấu tiếng Việt triệt để.
- `DefaultSlugGenerationStrategy`: Định dạng Latin đơn giản.

Staff service sẽ nắm giữ tham chiếu đến chiến lược phù hợp (mặc định là `VietnameseSlugGenerationStrategy`).

---

### 4. Policy Pattern (Mẫu Chính sách)

#### Vấn đề
Quy định QĐ5 nêu rõ: **"Không xóa danh mục còn sản phẩm"**. Quy tắc an toàn dữ liệu này cần được đóng gói độc lập để tránh rải rác nghiệp vụ check-exists khắp nơi trong service.

#### Giải pháp
Tạo lớp `CategoryDeletionPolicy`. Lớp này phối hợp với `CategoryProductCountVisitor` để đưa ra quyết định:
- Nếu đếm được sản phẩm > 0: Ném lỗi `BadRequestException` ngăn chặn xóa.
- Nếu không có sản phẩm: Cho phép xóa cứng.

---

### 5. Command Pattern (Mẫu Lệnh)

#### Vấn đề
Tất cả các hành động ghi dữ liệu (Tạo, Cập nhật, Xóa) danh mục và bộ sưu tập thực hiện bởi Staff cần đảm bảo các điều kiện sau:
1. Phải ghi log lịch sử thao tác vào bảng `ActivityLog` để phục vụ audit.
2. Phải xóa cache Redis của catalog (`categories`, `collections`) ngay lập tức để tránh tình trạng storefront hiển thị dữ liệu cũ.
3. Chạy trong một Transaction hoàn chỉnh.

Nếu viết code ghi log và xóa cache thủ công ở từng service method, code sẽ bị lặp lại rất nhiều (Code Duplication).

#### Giải pháp
Encapsulate các hành động thay đổi trạng thái danh mục/bộ sưu tập thành các đối tượng `CatalogCommand`.
- Interface `CatalogCommand<T>` định nghĩa phương thức `T execute()` và `String getDescription()`.
- Lớp điều phối `CatalogCommandExecutor` chịu trách nhiệm:
  - Mở transaction.
  - Chạy `execute()` của Command.
  - Tự động lấy thông tin người dùng từ Security Context để ghi `ActivityLog`.
  - Thực hiện các tác vụ phụ sau khi ghi thành công.

---

### 6. State Pattern (Mẫu Trạng thái)

#### Vấn đề
Một bộ sưu tập (`Collection`) có trạng thái hiển thị động phụ thuộc vào:
- Thuộc tính `isActive` (nhân viên bật/tắt thủ công).
- Thuộc tính `startDate` và `endDate` (lập lịch chạy chương trình bộ sưu tập).

Hệ thống cần xác định trạng thái thực tế thời gian thực của Collection (đang hoạt động, chưa đến giờ, hết hạn, hoặc bị vô hiệu hóa) để hiển thị nhãn phù hợp trên giao diện Admin hoặc ẩn/hiện ngoài storefront.

#### Giải pháp
Giải quyết trạng thái thông qua `CollectionStateResolver` và enum `CollectionStatusState`:
- `ACTIVE`: Bộ sưu tập đang hoạt động (isActive = true và thời gian hiện tại nằm trong khoảng [startDate, endDate]).
- `INACTIVE`: Bị vô hiệu hóa thủ công (isActive = false).
- `SCHEDULED`: Đã lập lịch (isActive = true nhưng chưa tới startDate).
- `EXPIRED`: Đã hết hạn (isActive = true nhưng đã qua endDate).

---

## V. Cấu trúc thư mục các tệp tin Pattern mới

```text
src/main/java/vn/hcmute/edu/dp/nhom10/backend/
├── enums/
│   └── CollectionStatusState.java                   # Các trạng thái của Collection
├── pattern/
│   ├── command/
│   │   └── catalog/
│   │       ├── CatalogCommand.java                  # Interface Command
│   │       └── CatalogCommandExecutor.java          # Invoker ghi log audit tự động
│   ├── composite/
│   │   └── category/
│   │       # Thực thể Category đóng vai trò Composite
│   ├── visitor/
│   │   └── category/
│   │       ├── CategoryVisitor.java                 # Interface Visitor duyệt cây
│   │       ├── CategoryDepthVisitor.java            # Visitor tính độ sâu tối đa
│   │       ├── CategoryProductCountVisitor.java     # Visitor đếm sản phẩm đệ quy
│   │       └── CategoryCycleDetectionVisitor.java   # Visitor check trùng lặp chu trình
│   ├── strategy/
│   │   └── slug/
│   │       ├── SlugGenerationStrategy.java          # Interface Strategy tạo slug
│   │       ├── VietnameseSlugGenerationStrategy.java# Tạo slug tiếng Việt
│   │       └── DefaultSlugGenerationStrategy.java   # Tạo slug mặc định
│   ├── policy/
│   │   └── category/
│   │       └── CategoryDeletionPolicy.java          # Chính sách xóa an toàn danh mục
│   └── state/
│       └── collection/
│           └── CollectionStateResolver.java         # Bộ phân giải trạng thái Collection
```

## VI. Kết luận thiết kế

Việc áp dụng đồng bộ các pattern này giúp phân hệ **Quản lý danh mục & Bộ sưu tập** đạt điểm chất lượng cao về mặt thiết kế hướng đối tượng:
- Logic nghiệp vụ phức tạp của cây danh mục được module hóa hoàn hảo bằng **Composite & Visitor**.
- Tính năng slug SEO linh hoạt nhờ **Strategy**.
- Ràng buộc an toàn dữ liệu được cô lập bằng **Policy**.
- Cơ chế kiểm toán hoạt động của nhân viên được tự động hóa triệt để qua **Command**.
- Trạng thái thời gian thực được biểu diễn rõ ràng qua **State**.
