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
        String voucherMessage,
        Boolean productVoucherApplied,
        Long productVoucherId,
        String productVoucherCode,
        DiscountType productVoucherDiscountType,
        String productVoucherMessage,
        Boolean shippingVoucherApplied,
        Long shippingVoucherId,
        String shippingVoucherCode,
        DiscountType shippingVoucherDiscountType,
        String shippingVoucherMessage
) {
}
