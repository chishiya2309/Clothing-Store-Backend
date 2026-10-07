package vn.hcmute.edu.dp.nhom10.backend.dto.checkout;

import vn.hcmute.edu.dp.nhom10.backend.enums.VoucherSlot;

import java.math.BigDecimal;
import java.util.List;

public record VoucherQuoteRequest(
        String code,
        VoucherSlot expectedSlot,
        Long customerId,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        List<CheckoutItemSnapshot> items
) {
    public VoucherQuoteRequest(String code, VoucherSlot expectedSlot, Long customerId,
                               BigDecimal subtotal, BigDecimal shippingFee) {
        this(code, expectedSlot, customerId, subtotal, shippingFee, List.of());
    }
}
