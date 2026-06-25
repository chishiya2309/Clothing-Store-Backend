package vn.hcmute.edu.dp.nhom10.backend.dto.projection;

import java.math.BigDecimal;

/**
 * Interface Projection để nhận kết quả native query thống kê khách hàng thân thiết.
 * Spring Data JPA tự động map alias trong SQL vào các getter tương ứng.
 * Áp dụng Projection Pattern để tách biệt raw SQL result khỏi DTO domain.
 */
public interface LoyaltyCustomerReportProjection {
    Long getUserId();
    String getFullName();
    String getEmail();
    String getMembershipTier();
    Long getTotalOrders();
    BigDecimal getTotalSpent();
    Integer getLoyaltyPoints();
}
