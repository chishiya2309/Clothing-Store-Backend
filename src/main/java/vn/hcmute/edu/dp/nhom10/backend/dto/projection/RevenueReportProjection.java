package vn.hcmute.edu.dp.nhom10.backend.dto.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Interface Projection để nhận kết quả native query thống kê doanh thu theo ngày.
 * Spring Data JPA tự động map alias trong SQL vào các getter tương ứng.
 * Áp dụng Projection Pattern để tách biệt raw SQL result khỏi DTO domain.
 */
public interface RevenueReportProjection {
    LocalDate getDate();
    Long getTotalOrders();
    Long getCompletedOrders();
    Long getCancelledOrders();
    BigDecimal getTotalRevenue();
    BigDecimal getTotalDiscounts();
    BigDecimal getNetRevenue();
}
