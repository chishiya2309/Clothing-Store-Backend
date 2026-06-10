# Tài Liệu Thiết Kế: Các Design Pattern Áp Dụng Trong Nghiệp Vụ Quản Lý User (Admin & Staff)

Tài liệu này chuẩn hóa và phân tích việc áp dụng các mẫu thiết kế (Design Patterns) cho các nghiệp vụ quản lý người dùng theo bảng phân công chức năng của **Quản trị viên (Admin - AD)** và **Nhân viên cửa hàng (Staff - NV)**:

*   **AD.1**: Quản lý tài khoản người dùng (Xem danh sách tài khoản, khóa/mở khóa tài khoản, cấp/thu hồi vai trò Nhân viên).
*   **AD.4**: Báo cáo khách hàng thân thiết (Danh sách khách hàng theo hạng, điểm tích lũy - Biểu mẫu BM5).
*   **NV.14**: Quản lý thông tin khách hàng (Xem, tìm kiếm, phân hạng thành viên).

---

## BẢNG MA TRẬN ÁP DỤNG DESIGN PATTERN THEO NGHIỆP VỤ

| Nghiệp vụ cụ thể | Design Pattern áp dụng | Mô tả cách triển khai thực tế |
| :--- | :--- | :--- |
| **AD.1: Xem danh sách tài khoản** | **Specification Pattern** | Xây dựng câu truy vấn động để tìm kiếm tài khoản theo keyword, lọc theo vai trò (`UserRole`) và trạng thái hoạt động (`isActive`). |
| **AD.1: Khóa/Mở khóa tài khoản** | **Observer Pattern** | Khi Admin thay đổi `isActive` của User, hệ thống phát đi `UserStatusChangedEvent`. Listener sẽ ghi chép `ActivityLog` và gửi email thông báo bất đồng bộ (`@Async`). |
| **AD.1: Cấp/Thu hồi vai trò Nhân viên** | **Proxy Pattern (AOP)** | Sử dụng `@PreAuthorize("hasRole('admin')")` tại tầng Controller để bảo vệ API, chỉ cho phép Quản trị viên (AD) thay đổi quyền hạn. |
| **NV.14: Xem, tìm kiếm khách hàng** | **DTO Pattern** | Sử dụng `CustomerResponse` (DTO) lọc sạch thông tin nhạy cảm của khách hàng trước khi trả về cho Nhân viên (Staff). |
| **NV.14 / AD.4: Phân hạng thành viên** | **Strategy Pattern** | Định nghĩa các chiến lược tính điểm tích lũy và xét duyệt nâng hạng thành viên (`MembershipTier`) khác nhau tùy theo mùa khuyến mãi hoặc cấu hình hệ thống. |
| **AD.4: Xuất báo cáo KH thân thiết (BM5)** | **Template Method Pattern** | Xây dựng bộ khung xuất báo cáo định dạng (Excel/PDF) chuẩn cho biểu mẫu BM5, cho phép lớp con ghi đè cách đổ dữ liệu khách hàng theo hạng và điểm. |

---

## 1. PHÂN TÍCH VÀ CƠ CHẾ TRIỂN KHAI CHI TIẾT

### 1.1. Proxy Pattern (Bảo vệ quyền hạn - AD.1)

#### Ngữ cảnh áp dụng
Chỉ **Admin (AD)** mới được phép thay đổi quyền nhân viên hoặc khóa tài khoản hệ thống. Nhân viên (Staff) hay Khách hàng (Customer) khi cố tình gọi API này sẽ lập tức bị hệ thống từ chối.

#### Cơ chế hoạt động
Spring Security áp dụng **Proxy Pattern (AOP)** để tự động bọc lớp Controller và kiểm tra chứng chỉ xác thực (JWT Token) trước khi cho phép đi vào hàm xử lý chính.

```java
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('admin')") // Proxy chặn và kiểm tra quyền AD ở mức lớp
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PatchMapping("/{id}/role")
    public ResponseEntity<AdminUserResponse> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(adminUserService.updateUserRole(id, request.role()));
    }
}
```

---

### 1.2. Specification Pattern (Truy vấn & Lọc động - AD.1 & NV.14)

#### Ngữ cảnh áp dụng
*   **Admin (AD.1)** cần tìm kiếm tài khoản hệ thống theo từ khóa, lọc theo vai trò (`admin`, `staff`, `customer`) và trạng thái (`isActive`).
*   **Nhân viên (NV.14)** cần tra cứu thông tin khách hàng, lọc theo hạng thành viên (`membershipTierId`) và điểm tích lũy.

#### Cơ chế hoạt động
Thay vì viết hàng chục hàm truy vấn SQL tĩnh, `UserSpecification` tạo các điều kiện nhỏ để ghép nối động tại Service Layer.

```java
package vn.hcmute.edu.dp.nhom10.backend.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;

public final class UserSpecification {

    public static Specification<User> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(root.get("phone"), pattern)
            );
        };
    }

    public static Specification<User> hasRole(UserRole role) {
        return (root, query, cb) -> role == null ? cb.conjunction() : cb.equal(root.get("role"), role);
    }

    public static Specification<User> hasActiveStatus(Boolean isActive) {
        return (root, query, cb) -> isActive == null ? cb.conjunction() : cb.equal(root.get("isActive"), isActive);
    }
}
```

---

### 1.3. Observer Pattern (Lưu vết & Thông báo - AD.1)

#### Ngữ cảnh áp dụng
Khi Admin thực hiện khóa (`isActive = false`) hoặc mở khóa tài khoản, hệ thống cần thực hiện đồng thời các hành vi:
1.  Ghi lại lịch sử hoạt động vào bảng `activity_logs`.
2.  Gửi email cảnh báo/thông báo cho khách hàng.

#### Cơ chế hoạt động
`AdminUserServiceImpl` bắn sự kiện `UserStatusChangedEvent`. Lớp `UserStatusListener` lắng nghe sự kiện này và phân phối tác vụ.

```java
// Lớp lắng nghe (Observer) xử lý các luồng nghiệp vụ phụ độc lập
@Component
@RequiredArgsConstructor
@Slf4j
public class UserStatusListener {

    private final ActivityLogRepository activityLogRepository;
    private final EmailService emailService;

    @EventListener
    public void handleAuditLog(UserStatusChangedEvent event) {
        // Ghi log đồng bộ để lưu vết chính xác tài khoản Admin thực hiện
        ActivityLog auditLog = ActivityLog.builder()
                .action(Boolean.FALSE.equals(event.getNewStatus()) ? "lock_user" : "unlock_user")
                .entityType("user")
                .entityId(event.getUserId())
                .build();
        activityLogRepository.save(auditLog);
    }

    @Async
    @EventListener
    public void handleEmailNotification(UserStatusChangedEvent event) {
        // Gửi email bất đồng bộ ngầm để tránh gây treo API chính
        emailService.sendAccountStatusEmail(event.getEmail(), event.getNewStatus());
    }
}
```

---

### 1.4. DTO Pattern (An toàn thông tin - NV.14)

#### Ngữ cảnh áp dụng
Nhân viên (Staff) khi quản lý thông tin khách hàng cần xem danh sách để hỗ trợ khách hàng, nhưng hệ thống phải ẩn đi các dữ liệu nhạy cảm của khách hàng như mật khẩu, lịch sử token đăng nhập.

#### Cơ chế hoạt động
Thiết kế `CustomerResponse` (chứa các thông tin hiển thị cơ bản, thứ hạng thành viên, điểm tích lũy) và map dữ liệu thủ công từ thực thể `User` trước khi phản hồi về Client.

```java
public record CustomerResponse(
        Long id,
        String email,
        String fullName,
        String phone,
        Integer loyaltyPoints,
        String membershipTierName,
        Boolean isActive
) {}
```

---

### 1.5. Strategy Pattern (Tính điểm & Nâng hạng - NV.14 & AD.4)

#### Ngữ cảnh áp dụng
Hệ thống tính điểm tích lũy và xét hạng khách hàng (`MembershipTier`) cần linh hoạt. Ví dụ:
*   *Chiến lược tiêu chuẩn*: Tiêu 100,000 VND tích 1 điểm.
*   *Chiến lược sự kiện (Black Friday)*: Tiêu 100,000 VND tích 2 điểm.

#### Cơ chế hoạt động
Định nghĩa interface `TierUpgradeStrategy` để hoán đổi linh hoạt thuật toán nâng hạng dựa trên điểm tích lũy của khách hàng.

```java
public interface TierUpgradeStrategy {
    MembershipTier calculateTier(int points, List<MembershipTier> allTiers);
}

// Chiến lược kiểm tra nâng hạng tuần tự tiêu chuẩn
@Component
public class StandardUpgradeStrategy implements TierUpgradeStrategy {
    @Override
    public MembershipTier calculateTier(int points, List<MembershipTier> allTiers) {
        return allTiers.stream()
                .filter(tier -> points >= tier.getMinPoints())
                .max(Comparator.comparingInt(MembershipTier::getMinPoints))
                .orElse(null);
    }
}
```

---

### 1.6. Template Method Pattern (Kết xuất báo cáo BM5 - AD.4)

#### Ngữ cảnh áp dụng
Admin cần kết xuất báo cáo Khách hàng thân thiết (**Biểu mẫu BM5**). Việc xuất dữ liệu ra file (Excel/PDF) thường có quy trình chung cố định:
`Mở file template -> Ghi tiêu đề báo cáo -> Đổ dữ liệu bảng -> Tính toán dòng tổng cộng -> Xuất file`.

#### Cơ chế hoạt động
Sử dụng **Template Method Pattern** để định nghĩa khung quy trình xuất file trong class trừu tượng, để các class con tự cấu hình phần đổ dữ liệu cụ thể cho biểu mẫu BM5 hoặc BM1, BM2.

```java
public abstract class ReportExporterTemplate<T> {

    // Template Method định nghĩa khung quy trình cố định
    public final byte[] exportReport(List<T> data, String title) {
        Workbook workbook = createWorkbook();
        writeTitle(workbook, title);
        writeTableHeader(workbook);
        writeDataRows(workbook, data); // Lớp con bắt buộc triển khai cụ thể
        writeSummaryRow(workbook, data);
        return convertToBytes(workbook);
    }

    protected abstract void writeTableHeader(Workbook workbook);
    protected abstract void writeDataRows(Workbook workbook, List<T> data);
}
```

---

## 2. KẾT LUẬN

Bằng cách bám sát danh mục chức năng thực tế của Quản trị viên (Admin) và Nhân viên (Staff):
*   Các Design Pattern không đứng độc lập mà bổ trợ trực tiếp cho từng công việc lưu trữ, tra cứu và kết xuất báo cáo.
*   Việc ứng dụng giúp tách biệt rõ ràng trách nhiệm phân quyền (**Proxy**), truy vấn động (**Specification**), xử lý tác vụ nền (**Observer**), và đồng nhất giao diện báo cáo mẫu (**Template Method**).
