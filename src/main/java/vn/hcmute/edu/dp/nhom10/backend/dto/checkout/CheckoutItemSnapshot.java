package vn.hcmute.edu.dp.nhom10.backend.dto.checkout;

import java.math.BigDecimal;
import vn.hcmute.edu.dp.nhom10.backend.enums.PriceSource;

public record CheckoutItemSnapshot(
        Long cartItemId,
        Long productVariantId,
        String productName,
        String variantInfo,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        Long flashSaleItemId,
        PriceSource priceSource
) {
    public CheckoutItemSnapshot(Long cartItemId, Long productVariantId, String productName,
                                String variantInfo, Integer quantity, BigDecimal unitPrice,
                                BigDecimal subtotal) {
        this(cartItemId, productVariantId, productName, variantInfo, quantity, unitPrice,
                subtotal, null, PriceSource.REGULAR);
    }
}
