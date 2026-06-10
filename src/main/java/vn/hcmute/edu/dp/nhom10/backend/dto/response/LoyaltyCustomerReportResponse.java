package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import java.math.BigDecimal;

/**
 * DTO đại diện cho báo cáo thống kê khách hàng thân thiết
 * Áp dụng DTO Pattern (Java Record) để tổng hợp chi tiêu, số lượng đơn hàng
 *          và hạng thành viên của khách hàng hỗ trợ cho các chiến dịch loyalty.
 */
public record LoyaltyCustomerReportResponse(
        Long userId,
        String fullName,
        String email,
        String membershipTier,
        Long totalOrders,
        BigDecimal totalSpent,
        Integer loyaltyPoints
) {
    public LoyaltyCustomerReportResponse {
        if (membershipTier == null) membershipTier = "Thành viên";
        if (totalOrders == null) totalOrders = 0L;
        if (totalSpent == null) totalSpent = BigDecimal.ZERO;
        if (loyaltyPoints == null) loyaltyPoints = 0;
    }
}
