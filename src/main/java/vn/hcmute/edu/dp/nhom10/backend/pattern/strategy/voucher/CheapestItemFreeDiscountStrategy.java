package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.CheckoutItemSnapshot;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;

import java.math.BigDecimal;

@Component
public class CheapestItemFreeDiscountStrategy implements VoucherDiscountStrategy {

    @Override
    public DiscountType supports() {
        return DiscountType.cheapest_item_free;
    }

    @Override
    public VoucherApplyResult apply(Voucher voucher, VoucherApplyContext context) {
        if (totalQuantity(context) <= 1) {
            throw new InvalidDataException("Cheapest item voucher requires at least two items");
        }

        BigDecimal discount = context.safeItems()
                .stream()
                .filter(item -> safeQuantity(item) > 0)
                .map(CheckoutItemSnapshot::unitPrice)
                .filter(price -> price != null && price.compareTo(BigDecimal.ZERO) >= 0)
                .min(BigDecimal::compareTo)
                .orElseThrow(() -> new InvalidDataException("Checkout items are required for cheapest item voucher"));

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
                .finalTotalAmount(total.max(BigDecimal.ZERO))
                .message("Voucher applied successfully")
                .build();
    }

    private int totalQuantity(VoucherApplyContext context) {
        return context.safeItems()
                .stream()
                .mapToInt(this::safeQuantity)
                .sum();
    }

    private int safeQuantity(CheckoutItemSnapshot item) {
        return item.quantity() != null ? item.quantity() : 0;
    }
}
