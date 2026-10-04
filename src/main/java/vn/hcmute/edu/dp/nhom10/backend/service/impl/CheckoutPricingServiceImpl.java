package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import org.springframework.stereotype.Service;
import vn.hcmute.edu.dp.nhom10.backend.dto.pricing.CheckoutPricingRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.pricing.CheckoutPricingResult;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.service.CheckoutPricingService;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CheckoutPricingServiceImpl implements CheckoutPricingService {

    private static final int MONEY_SCALE = 2;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    @Override
    public CheckoutPricingResult calculate(CheckoutPricingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Checkout pricing request is required");
        }

        BigDecimal subtotal = requireAmount(request.subtotal(), "Subtotal");
        BigDecimal shippingFee = requireAmount(request.shippingFee(), "Shipping fee");
        BigDecimal voucherDiscountAmount = normalizeDiscount(request.voucherDiscountAmount(), "Voucher discount amount");
        BigDecimal membershipDiscountAmount = calculateMembershipDiscount(request.user(), subtotal);
        BigDecimal discountAmount = membershipDiscountAmount.add(voucherDiscountAmount).setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        if (discountAmount.compareTo(subtotal) > 0) {
            throw new InvalidDataException("Discount amount must not exceed subtotal");
        }

        BigDecimal totalAmount = subtotal.add(shippingFee).subtract(discountAmount);
        if (totalAmount.signum() < 0) {
            throw new InvalidDataException("Checkout total amount must not be negative");
        }

        return new CheckoutPricingResult(
                subtotal,
                shippingFee,
                membershipDiscountAmount,
                voucherDiscountAmount,
                discountAmount,
                totalAmount
        );
    }

    private BigDecimal calculateMembershipDiscount(User user, BigDecimal subtotal) {
        if (user == null || user.getMembershipTier() == null
                || user.getMembershipTier().getDiscountPercent() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal percent = user.getMembershipTier().getDiscountPercent();
        if (percent.signum() < 0) {
            throw new InvalidDataException("Membership discount percent must not be negative");
        }
        if (percent.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return subtotal.multiply(percent).divide(ONE_HUNDRED, MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal requireAmount(BigDecimal amount, String fieldName) {
        if (amount == null) {
            throw new InvalidDataException(fieldName + " is required");
        }
        if (amount.signum() < 0) {
            throw new InvalidDataException(fieldName + " must not be negative");
        }
        return amount;
    }

    private BigDecimal normalizeDiscount(BigDecimal amount, String fieldName) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return requireAmount(amount, fieldName);
    }
}
