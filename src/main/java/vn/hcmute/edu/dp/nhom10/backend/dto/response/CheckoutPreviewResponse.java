package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;

import java.math.BigDecimal;

@Builder
public record CheckoutPreviewResponse(
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal membershipDiscountAmount,
        BigDecimal voucherDiscountAmount,
        BigDecimal shippingDiscountAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        Boolean voucherApplied,
        Long voucherId,
        String voucherCode,
        DiscountType voucherDiscountType,
        String voucherMessage
) {
}
