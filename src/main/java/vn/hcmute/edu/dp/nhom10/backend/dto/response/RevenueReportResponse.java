package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import java.math.BigDecimal;
import java.util.Date;

/**
 * DTO đại diện cho dữ liệu báo cáo thống kê doanh thu theo ngày
 * Áp dụng DTO Pattern (Java Record) để đảm bảo tính bất biến (immutable)
 *          và an toàn khi truyền tải dữ liệu doanh số tổng, chiết khấu và doanh số thực nhận.
 */
public record RevenueReportResponse(
        Date date,
        Long totalOrders,
        Long completedOrders,
        Long cancelledOrders,
        BigDecimal totalRevenue,   // Doanh thu tổng = subtotal + shippingFee
        BigDecimal totalDiscounts, // Chiết khấu giảm giá từ voucher
        BigDecimal netRevenue      // Doanh thu thực tế = totalAmount
) {
    public RevenueReportResponse {
        if (totalOrders == null) totalOrders = 0L;
        if (completedOrders == null) completedOrders = 0L;
        if (cancelledOrders == null) cancelledOrders = 0L;
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        if (totalDiscounts == null) totalDiscounts = BigDecimal.ZERO;
        if (netRevenue == null) netRevenue = BigDecimal.ZERO;
    }
}
