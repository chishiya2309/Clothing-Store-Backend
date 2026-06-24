package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO cho chi tiết đầy đủ của một sản phẩm.
 */
@Getter
@Builder
public class ProductDetailResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String material;
    private String careInstructions;

    // Giá hiển thị (salePrice nếu có, ngược lại basePrice)
    private BigDecimal price;
    private BigDecimal originalPrice; // basePrice, null nếu không có khuyến mãi

    private BigDecimal averageRating;
    private Integer totalSold;

    private String categoryName;
    private String categorySlug;

    private List<ProductImageResponse> images;
    private List<ProductVariantResponse> variants;
}
