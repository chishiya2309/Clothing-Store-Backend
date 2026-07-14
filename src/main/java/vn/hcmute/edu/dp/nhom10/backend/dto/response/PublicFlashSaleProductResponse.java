package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Builder
public record PublicFlashSaleProductResponse(
        Long flashSaleItemId,
        Long productId,
        String productName,
        String productSlug,
        String thumbnailUrl,
        BigDecimal originalPrice,
        BigDecimal flashSalePrice,
        Integer quota,
        Integer soldQuantity,
        Integer availableQuantity,
        Boolean soldOut
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
