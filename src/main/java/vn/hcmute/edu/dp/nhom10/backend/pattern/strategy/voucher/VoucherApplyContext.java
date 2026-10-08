package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher;

import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.CheckoutItemSnapshot;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record VoucherApplyContext(
        Long customerId,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        List<CheckoutItemSnapshot> items,
        OffsetDateTime appliedAt
) {
    public VoucherApplyContext(Long customerId, BigDecimal subtotal, BigDecimal shippingFee,
                               OffsetDateTime appliedAt) {
        this(customerId, subtotal, shippingFee, List.of(), appliedAt);
    }

    public BigDecimal normalizedShippingFee() {
        return shippingFee != null ? shippingFee : BigDecimal.ZERO;
    }

    public List<CheckoutItemSnapshot> safeItems() {
        return items != null ? items : List.of();
    }
}
