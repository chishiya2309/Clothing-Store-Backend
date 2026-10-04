package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.dto.pricing.CheckoutPricingRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.pricing.CheckoutPricingResult;
import vn.hcmute.edu.dp.nhom10.backend.entity.MembershipTier;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.CheckoutPricingServiceImpl;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CheckoutPricingServiceImplTest {

    private final CheckoutPricingServiceImpl service = new CheckoutPricingServiceImpl();

    @Test
    void calculate_withoutDiscount_returnsSubtotalPlusShipping() {
        CheckoutPricingResult result = service.calculate(request(null, "200000.00", "20000.00", null));

        assertEquals(new BigDecimal("200000.00"), result.subtotal());
        assertEquals(new BigDecimal("20000.00"), result.shippingFee());
        assertEquals(new BigDecimal("0.00"), result.discountAmount());
        assertEquals(new BigDecimal("220000.00"), result.totalAmount());
    }

    @Test
    void calculate_appliesMembershipAndVoucherDiscounts() {
        User user = User.builder()
                .membershipTier(MembershipTier.builder()
                        .discountPercent(new BigDecimal("10.00"))
                        .build())
                .build();

        CheckoutPricingResult result = service.calculate(request(user, "200000.00", "20000.00", "30000.00"));

        assertEquals(new BigDecimal("20000.00"), result.membershipDiscountAmount());
        assertEquals(new BigDecimal("30000.00"), result.voucherDiscountAmount());
        assertEquals(new BigDecimal("50000.00"), result.discountAmount());
        assertEquals(new BigDecimal("170000.00"), result.totalAmount());
    }

    @Test
    void calculate_roundsMembershipDiscountToMoneyScale() {
        User user = User.builder()
                .membershipTier(MembershipTier.builder()
                        .discountPercent(new BigDecimal("12.345"))
                        .build())
                .build();

        CheckoutPricingResult result = service.calculate(request(user, "999.00", "0.00", null));

        assertEquals(new BigDecimal("123.33"), result.membershipDiscountAmount());
    }

    @Test
    void calculate_discountGreaterThanSubtotal_capsProductPayableAtZero() {
        CheckoutPricingResult result = service.calculate(request(null, "100000.00", "20000.00", "100001.00"));

        assertEquals(new BigDecimal("100000.00"), result.discountAmount());
        assertEquals(new BigDecimal("20000.00"), result.totalAmount());
    }

    @Test
    void calculate_appliesShippingDiscountWithMaxZeroPayable() {
        CheckoutPricingResult result = service.calculate(new CheckoutPricingRequest(
                null,
                new BigDecimal("100000.00"),
                new BigDecimal("20000.00"),
                BigDecimal.ZERO,
                new BigDecimal("25000.00")
        ));

        assertEquals(new BigDecimal("20000.00"), result.shippingDiscountAmount());
        assertEquals(new BigDecimal("20000.00"), result.discountAmount());
        assertEquals(new BigDecimal("100000.00"), result.totalAmount());
    }

    @Test
    void calculate_negativeMembershipPercent_throwsException() {
        User user = User.builder()
                .membershipTier(MembershipTier.builder()
                        .discountPercent(new BigDecimal("-1.00"))
                        .build())
                .build();

        assertThrows(InvalidDataException.class,
                () -> service.calculate(request(user, "100000.00", "0.00", null)));
    }

    private CheckoutPricingRequest request(
            User user,
            String subtotal,
            String shippingFee,
            String voucherDiscountAmount
    ) {
        return new CheckoutPricingRequest(
                user,
                new BigDecimal(subtotal),
                new BigDecimal(shippingFee),
                voucherDiscountAmount == null ? null : new BigDecimal(voucherDiscountAmount),
                null
        );
    }
}
