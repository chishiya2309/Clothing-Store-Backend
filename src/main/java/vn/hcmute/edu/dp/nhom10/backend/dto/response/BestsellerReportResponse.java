package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import java.math.BigDecimal;

/**
 * DTO đại diện cho dữ liệu thống kê sản phẩm bán chạy nhất
 * Áp dụng DTO Pattern (Java Record) để đóng gói dữ liệu thống kê doanh số bán
 *          và thông tin danh mục sản phẩm phục vụ hiển thị bảng xếp hạng.
 */
public record BestsellerReportResponse(
        Long productId,
        String productName,
        String categoryName,
        Long totalQuantitySold,
        BigDecimal totalRevenue,
        String thumbnailUrl
) {
    public BestsellerReportResponse {
        if (categoryName == null) categoryName = "Không rõ";
        if (totalQuantitySold == null) totalQuantitySold = 0L;
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
    }
}
