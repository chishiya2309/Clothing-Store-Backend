package vn.hcmute.edu.dp.nhom10.backend.dto.pricing;

import java.math.BigDecimal;

public record CheckoutPricingResult(
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal membershipDiscountAmount,
        BigDecimal voucherDiscountAmount,
        BigDecimal shippingDiscountAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount
) {}