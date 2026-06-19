package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;

@Component
public class VnPayAmountMatcher {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    public boolean matches(String rawCallbackAmount, BigDecimal expectedAmount) {
        if (rawCallbackAmount == null || !rawCallbackAmount.matches("[1-9][0-9]*")
                || expectedAmount == null || expectedAmount.signum() <= 0) {
            return false;
        }
        try {
            BigInteger callbackAmount = new BigInteger(rawCallbackAmount);
            BigInteger expectedVnPayAmount = expectedAmount.multiply(ONE_HUNDRED).toBigIntegerExact();
            return expectedVnPayAmount.equals(callbackAmount);
        } catch (ArithmeticException | NumberFormatException e) {
            return false;
        }
    }
}
