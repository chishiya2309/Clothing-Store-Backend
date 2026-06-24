package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;
import vn.hcmute.edu.dp.nhom10.backend.enums.StaffProductStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record StaffProductDetailResponse(
        Long id,
        String name,
        String slug,
        String description,
        String material,
        String careInstructions,
        Long categoryId,
        String categoryName,
        BigDecimal basePrice,
        BigDecimal salePrice,
        Boolean isActive,
        Boolean isFeatured,
        StaffProductStatus status,
        Integer totalStock,
        Boolean stockWarning,
        List<StaffProductImageResponse> images,
        List<StaffProductVariantResponse> variants,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
