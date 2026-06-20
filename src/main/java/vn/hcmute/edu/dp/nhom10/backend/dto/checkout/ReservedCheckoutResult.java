package vn.hcmute.edu.dp.nhom10.backend.dto.checkout;

import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReservedCheckoutResult(
        Long checkoutSessionId,
        String checkoutCode,
        PaymentMethod paymentMethod,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        OffsetDateTime expiresAt
) {
}
