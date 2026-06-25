# TÀI LIỆU THIẾT KẾ: DESIGN PATTERNS ÁP DỤNG CHO STAFF REVIEW MODERATION

## I. Thông tin phạm vi

- **Branch**: `feature/staff-review-moderation`
- **Module**: Quản lý & Kiểm duyệt đánh giá sản phẩm của Nhân viên (Staff Review Moderation)
- **Use case áp dụng**:
  - **UC-26: Duyệt / Xóa đánh giá sản phẩm (Staff)**: Nhân viên duyệt review chờ duyệt, phản hồi review, hoặc soft-delete review vi phạm kèm lý do.
- **Phạm vi loại trừ (Đã có sẵn)**:
  - UC-24: Đánh giá sản phẩm (Khách hàng tạo đánh giá mới).
  - UC-25: Xem đánh giá sản phẩm trên Storefront.
- **Tài liệu căn cứ**:
  - `Nhom10_Project2.docx` (mục UC-26, QĐ9, QĐ13)
  - GoF Design Patterns: `Design Patterns - Elements of Reusable Object-Oriented Software.chm`

---

## II. Nguyên tắc chọn các Pattern

1. **Decoupling (Giảm liên kết cứng)**: Tách biệt tính năng kiểm duyệt của nhân viên ra khỏi tính năng hiển thị sản phẩm. Khi đánh giá được duyệt/xóa, việc cập nhật điểm đánh giá trung bình của sản phẩm nên được xử lý tách biệt.
2. **Open-Closed Principle (OCP)**: Dễ dàng mở rộng thêm các bộ lọc nội dung (spam, chửi bậy, quảng cáo) hoặc các điều kiện kiểm duyệt mà không cần sửa đổi mã nguồn chính của Service.
3. **Single Responsibility Principle (SRP)**: Mỗi lớp xử lý một trách nhiệm duy nhất (quản lý trạng thái, kiểm duyệt văn bản, ghi nhật ký hoạt động, thực hiện lệnh).

---

## III. Ma trận các Pattern áp dụng cho UC-26

| Pattern | Nhóm | Trạng thái | Thành phần áp dụng | Vai trò chính |
|---|---|---|---|---|
| **Command** | GoF Behavioral | Áp dụng | `ReviewCommand`, `ReviewCommandExecutor` | Đóng gói các hành động duyệt, xóa, phản hồi thành các object lệnh độc lập để dễ dàng mở rộng và quản lý giao dịch. |
| **Template Method** | GoF Behavioral | Áp dụng | `BaseReviewModerationCommand` | Định nghĩa khung (template) các bước kiểm duyệt đánh giá (kiểm tra quyền, nạp review, thực thi cụ thể, lưu, ghi nhật ký, phát sự kiện). |
| **State** | GoF Behavioral | Áp dụng | `ReviewState` & các con (`PendingReviewState`, `ApprovedReviewState`, `DeletedReviewState`) | Encapsulate các quy tắc chuyển đổi trạng thái của review (ví dụ: chỉ review APPROVED mới được phản hồi, review DELETED không được duyệt lại). |
| **Observer** | GoF Behavioral | Áp dụng | `ReviewStatusChangedEvent`, `ProductRatingUpdater` | Lắng nghe sự kiện thay đổi trạng thái review để tự động cập nhật lại `averageRating` của sản phẩm một cách bất tuần tự và tách biệt. |
| **Chain of Responsibility** | GoF Behavioral | Áp dụng | `ReviewContentScanner` & các con (`SpamScanner`, `ProfanityScanner`) | Thiết lập chuỗi rà soát nội dung tự động giúp gắn cờ cảnh báo (flagged) cho các đánh giá nghi ngờ vi phạm khi hiển thị lên trang quản trị của Staff. |
| **Strategy** | GoF Behavioral | Áp dụng | `ContentModerationStrategy` | Cung cấp thuật toán kiểm tra từ ngữ thô tục hoặc spam một cách linh hoạt, dễ thay đổi cấu hình hoặc ngôn ngữ. |
| **Policy** | GoF/DDD | Áp dụng | `ReviewModerationPolicy` | Đóng gói các quy định kiểm duyệt của cửa hàng (character limits, lý do xóa bắt buộc, quyền kiểm duyệt). |

---

## IV. Chi tiết thiết kế các Pattern

### 1. Command & Template Method Pattern (Mẫu Lệnh & Phương thức Khuôn mẫu)

#### Vấn đề
Các hành động ghi dữ liệu kiểm duyệt của nhân viên (Duyệt, Phản hồi, Xóa) đều chia sẻ chung một quy trình nghiệp vụ:
1. Xác thực thông tin tài khoản nhân viên.
2. Kiểm tra thực thể `Review` có tồn tại trong hệ thống.
3. Áp dụng chính sách chuyển đổi trạng thái.
4. Lưu thay đổi vào CSDL.
5. Ghi nhận lịch sử hoạt động (`ActivityLog`) phục vụ audit.
6. Phát đi sự kiện thay đổi trạng thái để các dịch vụ khác đồng bộ.

#### Giải pháp
*   **Command Pattern**: Encapsulate mỗi hành động kiểm duyệt thành một đối tượng `ReviewCommand`.
*   **Template Method Pattern**: Định nghĩa phương thức khuôn mẫu `final T execute()` trong lớp trừu tượng `BaseReviewModerationCommand`. Phương thức này điều phối toàn bộ luồng quy trình chung và chừa lại phương thức trừu tượng `doExecute(Review review)` cho các lệnh cụ thể tự triển khai logic riêng.

```text
       BaseReviewModerationCommand (Template Method: execute())
                  ^
                  |---- ApproveReviewCommand (doExecute() -> duyệt review)
                  |---- DeleteReviewCommand (doExecute() -> soft-delete review)
                  |---- ReplyReviewCommand (doExecute() -> phản hồi review)
```

---

### 2. State Pattern (Mẫu Trạng thái)

#### Vấn đề
Một đánh giá có các trạng thái khác nhau: **Chờ duyệt** (`PENDING`), **Đã duyệt** (`APPROVED`), và **Đã xóa** (`DELETED`).
Tùy vào trạng thái hiện tại, review có các hành vi hợp lệ khác nhau:
*   Chỉ review ở trạng thái `PENDING` hoặc `DELETED` mới được phép duyệt (`approve()`).
*   Chỉ review ở trạng thái `APPROVED` mới được phép nhân viên phản hồi (`reply()`).
*   Chỉ review ở trạng thái `PENDING` hoặc `APPROVED` mới được phép xóa (`delete()`).

#### Giải pháp
Định nghĩa interface `ReviewState` với các phương thức chuyển đổi trạng thái:
*   `void approve(Review review)`
*   `void reject(Review review, String reason)`
*   `void reply(Review review, String replyText)`

Cài đặt các lớp trạng thái cụ thể:
*   `PendingReviewState`: Cho phép duyệt và xóa; không cho phép phản hồi.
*   `ApprovedReviewState`: Cho phép xóa và phản hồi; không cho phép duyệt lại.
*   `DeletedReviewState`: Cho phép duyệt lại (khôi phục); không cho phép phản hồi.

Thực thể `Review` sẽ nắm giữ trạng thái hiện tại dưới dạng một State Object để xử lý hành vi.

---

### 3. Observer Pattern (Mẫu Quan sát)

#### Vấn đề
Khi một đánh giá được nhân viên duyệt (`APPROVED`) hoặc xóa (`DELETED`), điểm đánh giá trung bình (`averageRating`) của sản phẩm liên quan bắt buộc phải tính toán lại và cập nhật.
Nếu viết trực tiếp logic tính toán rating này bên trong dịch vụ quản lý đánh giá (`ReviewService`), nó sẽ tạo ra sự phụ thuộc chặt chẽ (tight coupling) giữa `Review` và `Product`.

#### Giải pháp
*   Khi có bất kỳ thay đổi trạng thái kiểm duyệt nào, hệ thống phát đi một sự kiện `ReviewStatusChangedEvent` thông qua Spring `ApplicationEventPublisher`.
*   Bộ lắng nghe `ProductRatingUpdater` sẽ quan sát sự kiện này và tự động thực thi việc truy vấn tính điểm trung bình mới và lưu vào thực thể `Product`.

---

### 4. Chain of Responsibility & Strategy Pattern (Mẫu Chuỗi Trách Nhiệm & Chiến lược)

#### Vấn đề
Nhân viên khi kiểm duyệt đánh giá cần một công cụ hỗ trợ lọc nhanh hoặc gắn cờ cảnh báo (Auto-flagging / Pre-screening) đối với các đánh giá chứa từ ngữ thô tục, quảng cáo hoặc spam để ưu tiên xử lý.

#### Giải pháp
*   **Chain of Responsibility Pattern**: Định nghĩa bộ quét nội dung `ReviewContentScanner` để rà soát nội dung trước khi hiển thị cho nhân viên kiểm duyệt:
    1.  `SpamScanner`: Phát hiện nội dung spam (kẻ ký tự lặp lại quá nhiều, nội dung vô nghĩa).
    2.  `ProfanityScanner`: Quét từ ngữ thô tục nhạy cảm tiếng Việt.
*   **Strategy Pattern**: Tách thuật toán lọc từ ngữ thô tục ra thành interface `ContentModerationStrategy` (Ví dụ: `VietnameseProfanityStrategy`). Lớp response gửi về cho staff sẽ kèm theo cờ `isFlagged = true` nếu nội dung vi phạm chuỗi quét, giúp hiển thị cảnh báo đỏ trên giao diện Admin.

---

### 5. Policy Pattern (Mẫu Chính sách)

#### Vấn đề
Các quy tắc kiểm duyệt như: "Nhân viên bắt buộc phải nhập lý do khi xóa đánh giá", "Nội dung phản hồi không được quá 500 ký tự" cần được đóng gói rõ ràng.

#### Giải pháp
Tạo lớp chính sách `ReviewModerationPolicy` chịu trách nhiệm duy nhất là kiểm tra tính hợp lệ của các yêu cầu duyệt/xóa/phản hồi từ nhân viên.

---

## V. Cấu trúc thư mục các tệp tin Pattern mới

```text
src/main/java/vn/hcmute/edu/dp/nhom10/backend/
├── pattern/
│   ├── command/
│   │   └── review/
│   │       ├── ReviewCommand.java                   # Interface lệnh kiểm duyệt
│   │       ├── ReviewCommandExecutor.java           # Invoker ghi log audit ActivityLog
│   │       ├── BaseReviewModerationCommand.java     # Lớp khuôn mẫu (Template Method)
│   │       ├── ApproveReviewCommand.java            # Lệnh Duyệt đánh giá
│   │       ├── DeleteReviewCommand.java             # Lệnh Xóa đánh giá
│   │       └── ReplyReviewCommand.java              # Lệnh Phản hồi đánh giá
│   ├── state/
│   │   └── review/
│   │       ├── ReviewState.java                     # Interface trạng thái review
│   │       ├── PendingReviewState.java              # Trạng thái Chờ duyệt
│   │       ├── ApprovedReviewState.java             # Trạng thái Đã duyệt
│   │       ├── DeletedReviewState.java              # Trạng thái Đã xóa (Tạm ẩn)
│   │       └── ReviewStateContext.java              # Quản lý chuyển đổi trạng thái
│   ├── observer/
│   │   └── review/
│   │       ├── ReviewStatusChangedEvent.java        # Sự kiện thay đổi trạng thái review
│   │       └── ProductRatingUpdater.java            # Listener cập nhật rating trung bình Product
│   ├── chain/
│   │   └── review/
│   │       ├── ReviewContentScanner.java            # Handler cơ sở quét nội dung
│   │       ├── SpamScanner.java                     # Quét ký tự lặp lại (spam)
│   │       └── ProfanityScanner.java                # Quét từ ngữ thô tục nhạy cảm
│   ├── strategy/
│   │   └── moderation/
│   │       ├── ContentModerationStrategy.java       # Chiến lược lọc nội dung
│   │       └── VietnameseProfanityStrategy.java     # Chiến lược lọc từ tục tiếng Việt
│   └── policy/
│       └── review/
│           └── ReviewModerationPolicy.java          # Chính sách kiểm duyệt của Staff
```

---

## VI. Kết luận thiết kế

Việc áp dụng các pattern này giúp phân hệ **Duyệt / Xóa đánh giá sản phẩm của Nhân viên (UC-26)** có thiết kế cực kỳ chuyên nghiệp, tách biệt hoàn toàn giữa luồng xử lý chính và các tác vụ hỗ trợ (như gắn cờ nội dung xấu bằng Chain/Strategy và cập nhật rating sản phẩm bằng Observer).
