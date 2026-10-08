package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;
import vn.hcmute.edu.dp.nhom10.backend.enums.VoucherSlot;

import java.math.BigDecimal;

@Builder
public record OrderVoucherResponse(
        Long voucherId,
        String voucherCode,
        DiscountType discountType,
        VoucherSlot slot,
        BigDecimal discountAmount
) {
}
