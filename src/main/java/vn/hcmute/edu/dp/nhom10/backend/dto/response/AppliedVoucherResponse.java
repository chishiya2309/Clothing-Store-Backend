package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Builder
public record AppliedVoucherResponse(
        Long voucherId,
        String code,
        DiscountType discountType,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal discountAmount,
        BigDecimal shippingDiscountAmount,
        BigDecimal totalAmount,
        String message
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
