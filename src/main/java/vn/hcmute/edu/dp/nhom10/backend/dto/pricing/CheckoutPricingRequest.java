package vn.hcmute.edu.dp.nhom10.backend.dto.pricing;

import vn.hcmute.edu.dp.nhom10.backend.entity.User;

import java.math.BigDecimal;

public record CheckoutPricingRequest(
        User user,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal voucherDiscountAmount,
        BigDecimal shippingDiscountAmount
) {
}
