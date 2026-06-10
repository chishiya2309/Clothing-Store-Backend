package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record VoucherApplyContext(
        Long customerId,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        OffsetDateTime appliedAt
) {
    public BigDecimal normalizedShippingFee() {
        return shippingFee != null ? shippingFee : BigDecimal.ZERO;
    }
}
