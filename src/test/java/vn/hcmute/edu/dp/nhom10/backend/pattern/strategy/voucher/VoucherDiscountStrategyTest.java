package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.CheckoutItemSnapshot;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void shippingFixedAmountDiscount_appliesToShippingOnly() {
        Voucher voucher = Voucher.builder()
                .code("SHIP20")
                .discountType(DiscountType.shipping_fixed_amount)
                .discountValue(BigDecimal.valueOf(20000))
                .build();
        VoucherApplyContext context = new VoucherApplyContext(
                1L,
                BigDecimal.valueOf(300000),
                BigDecimal.valueOf(30000),
                OffsetDateTime.now()
        );

        VoucherApplyResult result = new ShippingFixedAmountDiscountStrategy().apply(voucher, context);

        assertEquals(0, BigDecimal.ZERO.compareTo(result.discountAmount()));
        assertEquals(0, BigDecimal.valueOf(20000).compareTo(result.shippingDiscountAmount()));
        assertEquals(0, BigDecimal.valueOf(310000).compareTo(result.finalTotalAmount()));
    }

    @Test
    void shippingFixedAmountDiscount_neverExceedsShippingFee() {
        Voucher voucher = Voucher.builder()
                .code("FREESHIP")
                .discountType(DiscountType.shipping_fixed_amount)
                .discountValue(BigDecimal.valueOf(50000))
                .build();
        VoucherApplyContext context = new VoucherApplyContext(
                1L,
                BigDecimal.valueOf(300000),
                BigDecimal.valueOf(30000),
                OffsetDateTime.now()
        );

        VoucherApplyResult result = new ShippingFixedAmountDiscountStrategy().apply(voucher, context);

        assertEquals(0, BigDecimal.ZERO.compareTo(result.discountAmount()));
        assertEquals(0, BigDecimal.valueOf(30000).compareTo(result.shippingDiscountAmount()));
        assertEquals(0, BigDecimal.valueOf(300000).compareTo(result.finalTotalAmount()));
    }

    @Test
    void cheapestItemFreeDiscount_discountsOneCheapestUnit() {
        Voucher voucher = Voucher.builder()
                .code("CHEAPFREE")
                .discountType(DiscountType.cheapest_item_free)
                .discountValue(BigDecimal.ONE)
                .build();
        VoucherApplyContext context = new VoucherApplyContext(
                1L,
                BigDecimal.valueOf(530000),
                BigDecimal.valueOf(30000),
                List.of(
                        item(1L, 1, BigDecimal.valueOf(200000)),
                        item(2L, 2, BigDecimal.valueOf(150000)),
                        item(3L, 1, BigDecimal.valueOf(30000))
                ),
                OffsetDateTime.now()
        );

        VoucherApplyResult result = new CheapestItemFreeDiscountStrategy().apply(voucher, context);

        assertEquals(0, BigDecimal.valueOf(30000).compareTo(result.discountAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.shippingDiscountAmount()));
        assertEquals(0, BigDecimal.valueOf(530000).compareTo(result.finalTotalAmount()));
    }

    @Test
    void cheapestItemFreeDiscount_respectsMaxDiscountAmount() {
        Voucher voucher = Voucher.builder()
                .code("CHEAPCAP")
                .discountType(DiscountType.cheapest_item_free)
                .discountValue(BigDecimal.ONE)
                .maxDiscountAmount(BigDecimal.valueOf(20000))
                .build();
        VoucherApplyContext context = new VoucherApplyContext(
                1L,
                BigDecimal.valueOf(530000),
                BigDecimal.valueOf(30000),
                List.of(
                        item(1L, 1, BigDecimal.valueOf(200000)),
                        item(2L, 2, BigDecimal.valueOf(30000))
                ),
                OffsetDateTime.now()
        );

        VoucherApplyResult result = new CheapestItemFreeDiscountStrategy().apply(voucher, context);

        assertEquals(0, BigDecimal.valueOf(20000).compareTo(result.discountAmount()));
        assertEquals(0, BigDecimal.valueOf(540000).compareTo(result.finalTotalAmount()));
    }

    @Test
    void cheapestItemFreeDiscount_requiresMoreThanOneItemQuantity() {
        Voucher voucher = Voucher.builder()
                .code("CHEAPFREE")
                .discountType(DiscountType.cheapest_item_free)
                .discountValue(BigDecimal.ONE)
                .build();
        VoucherApplyContext context = new VoucherApplyContext(
                1L,
                BigDecimal.valueOf(300000),
                BigDecimal.valueOf(30000),
                List.of(item(1L, 1, BigDecimal.valueOf(300000))),
                OffsetDateTime.now()
        );

        assertThrows(InvalidDataException.class,
                () -> new CheapestItemFreeDiscountStrategy().apply(voucher, context));
    }

    private CheckoutItemSnapshot item(Long productVariantId, Integer quantity, BigDecimal unitPrice) {
        return new CheckoutItemSnapshot(
                null,
                productVariantId,
                "Product " + productVariantId,
                null,
                quantity,
                unitPrice,
                unitPrice.multiply(BigDecimal.valueOf(quantity))
        );
    }
}
