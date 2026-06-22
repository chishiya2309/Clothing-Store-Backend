package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO chứa các tiêu chí tìm kiếm, lọc và sắp xếp sản phẩm.
 * Áp dụng cho UC-11 (Tìm kiếm), UC-12 (Lọc), UC-13 (Sắp xếp).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchCriteria {

    /** Từ khóa tìm kiếm theo tên sản phẩm (UC-11) */
    private String keyword;

    /** Slug danh mục để lọc theo category tree (UC-10 + UC-12) */
    private String categorySlug;

    /** Lọc theo danh sách màu sắc (UC-12) */
    private List<String> colors;

    /** Lọc theo danh sách kích cỡ (UC-12) */
    private List<String> sizes;

    /** Giá tối thiểu (UC-12) */
    private BigDecimal minPrice;

    /** Giá tối đa (UC-12) */
    private BigDecimal maxPrice;

    /**
     * Tiêu chí sắp xếp (UC-13).
     * Giá trị hợp lệ: "latest", "price_asc", "price_desc", "best_selling"
     */
    private String sortBy;
}
