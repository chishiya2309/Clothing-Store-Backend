package vn.hcmute.edu.dp.nhom10.backend.dto.checkout;

import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;
import vn.hcmute.edu.dp.nhom10.backend.enums.VoucherSlot;

import java.math.BigDecimal;

public record VoucherQuote(
        Long voucherId,
        String code,
        DiscountType discountType,
        VoucherSlot slot,
        BigDecimal voucherDiscountAmount,
        BigDecimal shippingDiscountAmount,
        String message
) {
    public VoucherQuote(Long voucherId, String code, DiscountType discountType,
                        BigDecimal voucherDiscountAmount, BigDecimal shippingDiscountAmount,
                        String message) {
        this(voucherId, code, discountType, null, voucherDiscountAmount, shippingDiscountAmount, message);
    }

    public static VoucherQuote empty() {
        return new VoucherQuote(null, null, null, null, BigDecimal.ZERO, BigDecimal.ZERO, null);
    }
}
