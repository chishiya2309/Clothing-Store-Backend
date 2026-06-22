package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;

import java.math.BigDecimal;

@Component
public class FixedAmountDiscountStrategy implements VoucherDiscountStrategy {

    @Override
    public DiscountType supports() {
        return DiscountType.fixed_amount;
    }

    @Override
    public VoucherApplyResult apply(Voucher voucher, VoucherApplyContext context) {
        BigDecimal discount = voucher.getDiscountValue().min(context.subtotal());
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
