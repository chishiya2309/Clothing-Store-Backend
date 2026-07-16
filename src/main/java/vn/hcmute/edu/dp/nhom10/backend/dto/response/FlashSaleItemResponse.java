package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record FlashSaleItemResponse(
        Long id,
        Long productId,
        String productName,
        BigDecimal originalPrice,
        BigDecimal flashSalePrice,
        Integer quota,
        Integer reservedQuantity,
        Integer soldQuantity,
        Integer availableQuantity,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
