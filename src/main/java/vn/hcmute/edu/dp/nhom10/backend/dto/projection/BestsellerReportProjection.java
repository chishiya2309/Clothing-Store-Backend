package vn.hcmute.edu.dp.nhom10.backend.dto.projection;

import java.math.BigDecimal;

/**
 * Interface Projection để nhận kết quả native query thống kê sản phẩm bán chạy.
 * Spring Data JPA tự động map alias trong SQL vào các getter tương ứng.
 * Áp dụng Projection Pattern để tách biệt raw SQL result khỏi DTO domain.
 */
public interface BestsellerReportProjection {
    Long getProductId();
    String getProductName();
    String getCategoryName();
    Long getTotalQuantitySold();
    BigDecimal getTotalRevenue();
}
