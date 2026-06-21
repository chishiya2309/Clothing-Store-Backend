package vn.hcmute.edu.dp.nhom10.backend.dto.checkout;

import java.math.BigDecimal;
import java.util.List;

public record CheckoutData(
        Long userId,
        Long addressId,
        AddressSnapshot addressSnapshot,
        List<CheckoutItemSnapshot> items,
        BigDecimal subtotal,
        BigDecimal shippingFee
) {
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
}
