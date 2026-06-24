package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record StaffProductVariantResponse(
        Long id,
        String sku,
        String size,
        String color,
        Integer stockQuantity,
        BigDecimal additionalPrice,
        Boolean isActive,
        Boolean lowStock
) {
}
