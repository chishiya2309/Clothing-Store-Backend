package vn.hcmute.edu.dp.nhom10.backend.dto.checkout;

import java.math.BigDecimal;

public record CheckoutItemSnapshot(
        Long cartItemId,
        Long productVariantId,
        String productName,
        String variantInfo,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
