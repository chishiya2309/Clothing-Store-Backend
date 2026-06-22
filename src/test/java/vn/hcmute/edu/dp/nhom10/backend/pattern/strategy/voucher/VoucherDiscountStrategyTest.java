package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VoucherDiscountStrategyTest {

    @Test
    void percentageDiscount_respectsMaxDiscountAmount() {
        Voucher voucher = Voucher.builder()
                .code("SALE10")
                .discountType(DiscountType.percentage)
                .discountValue(BigDecimal.TEN)
                .maxDiscountAmount(BigDecimal.valueOf(30000))
                .build();
        VoucherApplyContext context = new VoucherApplyContext(
                1L,
                BigDecimal.valueOf(500000),
                BigDecimal.valueOf(30000),
                OffsetDateTime.now()
        );

        VoucherApplyResult result = new PercentageDiscountStrategy().apply(voucher, context);

        assertEquals(0, BigDecimal.valueOf(30000).compareTo(result.discountAmount()));
        assertEquals(0, BigDecimal.valueOf(500000).compareTo(result.finalTotalAmount()));
    }

    @Test
    void fixedAmountDiscount_neverExceedsSubtotal() {
        Voucher voucher = Voucher.builder()
                .code("FIX500")
                .discountType(DiscountType.fixed_amount)
                .discountValue(BigDecimal.valueOf(500000))
                .build();
        VoucherApplyContext context = new VoucherApplyContext(
                1L,
                BigDecimal.valueOf(300000),
                BigDecimal.valueOf(30000),
                OffsetDateTime.now()
        );

        VoucherApplyResult result = new FixedAmountDiscountStrategy().apply(voucher, context);

        assertEquals(0, BigDecimal.valueOf(300000).compareTo(result.discountAmount()));
        assertEquals(0, BigDecimal.valueOf(30000).compareTo(result.finalTotalAmount()));
    }
}
