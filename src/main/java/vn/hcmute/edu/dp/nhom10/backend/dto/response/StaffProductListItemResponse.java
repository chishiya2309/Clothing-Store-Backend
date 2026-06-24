package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;
import vn.hcmute.edu.dp.nhom10.backend.enums.StaffProductStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record StaffProductListItemResponse(
        Long id,
        String name,
        String slug,
        Long categoryId,
        String categoryName,
        BigDecimal basePrice,
        BigDecimal salePrice,
        Boolean isActive,
        Boolean isFeatured,
        StaffProductStatus status,
        Integer totalStock,
        Integer variantCount,
        String thumbnailUrl,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
