package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PercentageDiscountStrategy implements VoucherDiscountStrategy {

    @Override
    public DiscountType supports() {
        return DiscountType.percentage;
    }

    @Override
    public VoucherApplyResult apply(Voucher voucher, VoucherApplyContext context) {
        BigDecimal rawDiscount = context.subtotal()
                .multiply(voucher.getDiscountValue())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal discount = rawDiscount;
        if (voucher.getMaxDiscountAmount() != null) {
            discount = discount.min(voucher.getMaxDiscountAmount());
        }
        discount = discount.min(context.subtotal());

        BigDecimal total = context.subtotal()
                .add(context.normalizedShippingFee())
                .subtract(discount);

        return VoucherApplyResult.builder()
                .discountAmount(discount)
                .shippingDiscountAmount(BigDecimal.ZERO)
                .finalTotalAmount(total)
                .message("Voucher applied successfully")
                .build();
    }
}
