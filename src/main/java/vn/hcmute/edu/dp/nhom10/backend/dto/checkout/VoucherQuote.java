package vn.hcmute.edu.dp.nhom10.backend.dto.checkout;

import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;

import java.math.BigDecimal;

public record VoucherQuote(
        Long voucherId,
        String code,
        DiscountType discountType,
        BigDecimal voucherDiscountAmount,
        BigDecimal shippingDiscountAmount,
        String message
) {
    public static VoucherQuote empty() {
        return new VoucherQuote(null, null, null, BigDecimal.ZERO, BigDecimal.ZERO, null);
    }
}
