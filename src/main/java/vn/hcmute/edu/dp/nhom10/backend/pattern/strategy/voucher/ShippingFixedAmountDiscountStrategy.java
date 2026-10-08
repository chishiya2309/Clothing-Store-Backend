package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;

import java.math.BigDecimal;

@Component
public class ShippingFixedAmountDiscountStrategy implements VoucherDiscountStrategy {

    @Override
    public DiscountType supports() {
        return DiscountType.shipping_fixed_amount;
    }

    @Override
    public VoucherApplyResult apply(Voucher voucher, VoucherApplyContext context) {
        BigDecimal discountValue = voucher.getDiscountValue();
        if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidDataException("Shipping discount value must be greater than 0");
        }

        BigDecimal shippingFee = context.normalizedShippingFee();
        BigDecimal shippingDiscount = discountValue.min(shippingFee);
        BigDecimal total = context.subtotal()
                .add(shippingFee)
                .subtract(shippingDiscount);

        return VoucherApplyResult.builder()
                .discountAmount(BigDecimal.ZERO)
                .shippingDiscountAmount(shippingDiscount)
                .finalTotalAmount(total.max(BigDecimal.ZERO))
                .message("Voucher applied successfully")
                .build();
    }
}
