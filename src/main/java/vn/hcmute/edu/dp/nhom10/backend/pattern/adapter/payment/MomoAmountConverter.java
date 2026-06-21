package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;

import java.math.BigDecimal;

@Component
public class MomoAmountConverter {

    private static final long SANDBOX_MAX_AMOUNT = 50_000_000L;

    public long toMomoAmount(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidDataException("MoMo amount is required");
        }
        long converted;
        try {
            converted = amount.longValueExact();
        } catch (ArithmeticException e) {
            throw new InvalidDataException("MoMo amount must be a VND integer");
        }
        if (converted <= 0) {
            throw new InvalidDataException("MoMo amount must be greater than zero");
        }
        if (converted > SANDBOX_MAX_AMOUNT) {
            throw new InvalidDataException("MoMo amount exceeds sandbox limit");
        }
        return converted;
    }

    public boolean matches(long momoAmount, BigDecimal expectedAmount) {
        try {
            return momoAmount == toMomoAmount(expectedAmount);
        } catch (InvalidDataException e) {
            return false;
        }
    }
}
