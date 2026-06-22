package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record VoucherApplyResult(
        BigDecimal discountAmount,
        BigDecimal shippingDiscountAmount,
        BigDecimal finalTotalAmount,
        String message
) {
}
