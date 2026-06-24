package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;

@Builder
public record StaffStockUpdateResponse(
        Long productId,
        Long variantId,
        String sku,
        Integer oldStockQuantity,
        Integer newStockQuantity,
        Boolean lowStock,
        String warningMessage
) {
}
