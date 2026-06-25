# Kế hoạch xây dựng chức năng Staff Review Moderation

## 1. Thông tin nhanh về branch

- **Branch**: `feature/staff-review-moderation`
- **Phạm vi chính**: 
  - Quản lý & kiểm duyệt đánh giá sản phẩm (Duyệt đánh giá chờ duyệt, Phản hồi đánh giá, Xóa đánh giá vi phạm kèm lý do).
- **Tác nhân**: Nhân viên (`STAFF`)
- **Tài liệu căn cứ**:
  - `Nhom10_Project2.docx` (mục UC-26, QĐ9, QĐ13)
  - Source code hiện có: `Review`, `ReviewImage`, `ReviewRepository`, `ReviewController`, `ReviewServiceImpl`.

---

## 2. Phạm vi trong branch này (Chỉ tập trung vào UC-26)

Tập trung vào phân hệ kiểm duyệt đánh giá của nhân viên, loại trừ việc tạo review (UC-24) và hiển thị storefront (UC-25) đã có sẵn:
1. **Xem danh sách đánh giá**:
   * Phân trang, lọc theo trạng thái kiểm duyệt thông qua 3 tab:
     * **Chờ duyệt** (`PENDING`): Đánh giá mới tạo chưa duyệt, `isActive = true`.
     * **Đã duyệt** (`APPROVED`): Đánh giá đã được duyệt hiển thị ngoài storefront (`isActive = true` và `isApproved = true`).
     * **Đã xóa** (`DELETED`): Đánh giá vi phạm bị ẩn khỏi storefront (`isActive = false`).
   * Tự động quét và gắn cờ cảnh báo (`isFlagged = true`) đối với các đánh giá nghi ngờ là spam hoặc chứa từ ngữ thô tục để hỗ trợ nhân viên ưu tiên xử lý.
2. **Duyệt đánh giá**:
   * Cập nhật trạng thái review thành "Đã duyệt" (`APPROVED`).
   * Tự động kích hoạt tính toán lại điểm đánh giá trung bình của sản phẩm (`averageRating` của `Product`).
3. **Xóa đánh giá vi phạm**:
   * Lưu lý do xóa và đổi trạng thái `isActive = false` (soft-delete).
   * Review bị ẩn khỏi storefront.
   * Tự động tính toán lại điểm đánh giá trung bình của sản phẩm.
4. **Phản hồi đánh giá**:
   * Nhân viên viết phản hồi cho đánh giá đã duyệt.
   * Phản hồi hiển thị trực tiếp bên dưới đánh giá ngoài storefront.

---

## 3. Hiện trạng code liên quan

### 3.1. Thực thể có sẵn
- `Review`: Có các trường `id`, `user`, `product`, `order`, `rating`, `content`, `isApproved`, `adminReply`, `repliedAt`, `createdAt`, `updatedAt`, `images`.
  * *Hạn chế*: Chưa có các trường `isActive` và `deleteReason` phục vụ soft-delete.
- `ReviewImage`: Lưu trữ danh sách ảnh đính kèm của review.

### 3.2. Controller và Service có sẵn
- `ReviewController` & `ReviewServiceImpl`:
  * Có sẵn tính năng của Customer: `createReview`, `getProductReviews` (xem review đã duyệt), `canReview`, `getEligibleOrders`, `uploadReviewImage`.
  * Có sẵn các method nghiệp vụ nền tảng: `getPendingReviews`, `approveReview`, `replyToReview`, `deleteReview` (hiện tại đang dùng hard-delete).
  * *Hạn chế*: Chưa có API endpoint riêng cho Staff kiểm duyệt, chưa có phân tab cụ thể cho danh sách kiểm duyệt, chưa lưu lý do xóa, chưa có các mẫu thiết kế (Design Patterns) để đảm bảo kiến trúc sạch.

---

## 4. API Endpoints đề xuất dành cho Staff

Các API này sẽ được định vị dưới tiền tố bảo mật `/api/staff`:

```text
# Danh sách đánh giá theo Tab kiểm duyệt (PENDING, APPROVED, DELETED)
GET    /api/staff/reviews?tab={tab}&page={page}&size={size}

# Duyệt đánh giá
PUT    /api/staff/reviews/{id}/approve

# Phản hồi đánh giá
PUT    /api/staff/reviews/{id}/reply

# Xóa đánh giá (vi phạm) kèm lý do
PUT    /api/staff/reviews/{id}/delete
```

---

## 5. DTOs đề xuất dành cho Staff

### 5.1. Request DTOs
- `StaffDeleteReviewRequest`:
  * `reason` (Bắt buộc, dùng để nhập lý do xóa review vi phạm).
- `StaffReplyReviewRequest`:
  * `replyText` (Bắt buộc, nội dung phản hồi của cửa hàng).

### 5.2. Response DTOs
- `StaffReviewResponse`:
  * `id` (Mã review)
  * `reviewerName` (Tên khách hàng đánh giá)
  * `reviewerEmail` (Email khách hàng)
  * `productName` (Tên sản phẩm)
  * `productSku` (Mã SKU sản phẩm)
  * `rating` (Số sao: 1-5)
  * `content` (Nội dung đánh giá)
  * `imageUrls` (Danh sách ảnh đính kèm)
  * `adminReply` (Nội dung phản hồi)
  * `repliedAt` (Thời gian phản hồi)
  * `isApproved` (Đã duyệt hay chưa)
  * `isActive` (Trạng thái hoạt động, dùng cho xóa mềm)
  * `isFlagged` (Cờ cảnh báo tự động vi phạm spam/từ tục)
  * `flagReason` (Lý do bị gắn cờ cảnh báo)
  * `deleteReason` (Lý do xóa)
  * `createdAt` (Ngày tạo)

---

## 6. Thiết kế Services & Repositories

### 6.1. StaffReviewService
```java
PageResponse<StaffReviewResponse> getReviewsByTab(String tab, int page, int size);
StaffReviewResponse approveReview(Long id, String username);
StaffReviewResponse replyToReview(Long id, StaffReplyReviewRequest request, String username);
StaffReviewResponse deleteReview(Long id, StaffDeleteReviewRequest request, String username);
```

### 6.2. Cập nhật ReviewRepository
Thêm các query method có lọc điều kiện `isActive = true` để đảm bảo storefront không hiển thị review đã xóa:
*   Query tìm review đã duyệt hoạt động: `findByProductAndIsApprovedTrueAndIsActiveTrue(...)`
*   Query đếm review và tính rating trung bình của Product: Chỉ tính trên các review có `isApproved = true` và `isActive = true`.

---

## 7. Các Design Pattern áp dụng

Chi tiết thiết kế có tại [design_patterns_staff_review_moderation.md](file:///D:/MauThietKePhanMem/ProjectCuoiKy/Project/Clothing-Store-Backend/src/main/resources/docs/design-patterns/design_patterns_staff_review_moderation.md).

1. **Command Pattern**: Encapsulate các hành động duyệt, xóa, phản hồi thành các object lệnh độc lập để quản lý transaction và audit log dễ dàng.
2. **Template Method Pattern**: Định nghĩa khung (template) quy trình xử lý kiểm duyệt trong `BaseReviewModerationCommand`.
3. **State Pattern**: Quản lý vòng đời trạng thái của review (`PendingReviewState`, `ApprovedReviewState`, `DeletedReviewState`).
4. **Observer Pattern**: Bắn sự kiện `ReviewStatusChangedEvent` để lắng nghe cập nhật rating sản phẩm.
5. **Chain of Responsibility Pattern**: Thiết lập chuỗi quét tự động rà soát nội dung đánh giá (`SpamScanner`, `ProfanityScanner`) khi hiển thị trên trang quản trị.
6. **Strategy Pattern**: Thay đổi linh hoạt thuật toán lọc từ tục tiếng Việt (`ContentModerationStrategy`).
7. **Policy Pattern**: Đóng gói các quy tắc nghiệp vụ kiểm duyệt vào `ReviewModerationPolicy`.

---

## 8. Thay đổi Cơ sở dữ liệu (Database Changes)

Tạo file patch SQL `src/main/resources/db/phase4_review_moderation_schema_patch.sql` để cập nhật bảng `reviews` hiện tại:
```sql
ALTER TABLE reviews ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE reviews ADD COLUMN delete_reason TEXT;
```

---

## 9. Kịch bản kiểm thử (Test Plan)

### 9.1. Unit Test các Pattern
- **State Pattern**: Test đúng các quy tắc chuyển đổi trạng thái (ví dụ: APPROVED không cho duyệt lại, DELETED cho khôi phục).
- **Observer Pattern**: Test lắng nghe sự kiện để tự động cập nhật rating sản phẩm thành công khi review thay đổi trạng thái.
- **Chain of Responsibility & Strategy**: Test phát hiện đúng từ thô tục tiếng Việt và gắn cờ `isFlagged` thành công.
- **Command & Template Method**: Test lưu log audit thành công khi chạy lệnh kiểm duyệt.

### 9.2. Unit Test Service & Controller
- Test lấy danh sách đánh giá theo Tab.
- Test Duyệt/Xóa/Phản hồi đánh giá thành công và các lỗi ngoại lệ (review không tồn tại, lý do xóa bị trống, phản hồi rỗng).
- Test bảo mật: Chỉ cho phép tài khoản có role `ROLE_STAFF` truy cập.

---

## 10. Checklist triển khai

- [ ] Tạo file patch SQL `phase4_review_moderation_schema_patch.sql` và cập nhật file `database_schema.sql`.
- [ ] Cập nhật thực thể `Review.java` (thêm `isActive`, `deleteReason`).
- [ ] Cập nhật `ReviewRepository.java` (thêm bộ lọc `isActive = true` cho các truy vấn cũ).
- [ ] Định nghĩa các Request/Response DTOs dành cho Staff.
- [ ] Xây dựng các lớp core của Design Patterns (State, Observer, Command, Chain/Strategy, Policy).
- [ ] Viết interface `StaffReviewService` và implementation `StaffReviewServiceImpl`.
- [ ] Viết bộ điều khiển API `StaffReviewController`.
- [ ] Viết bộ Unit Test đầy đủ cho các Pattern, Service, và Controller (Mục tiêu 100% test pass).
- [ ] Chạy kiểm thử toàn bộ dự án (`mvn clean test`).
