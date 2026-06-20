package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VnPayAmountMatcherTest {

    private final VnPayAmountMatcher matcher = new VnPayAmountMatcher();

    @Test
    void matches_exactVndAmountTimesOneHundred_returnsTrue() {
        assertTrue(matcher.matches("12000000", new BigDecimal("120000.00")));
    }

    @Test
    void matches_mismatch_returnsFalse() {
        assertFalse(matcher.matches("12000100", new BigDecimal("120000.00")));
    }

    @Test
    void matches_decimalSeparatorScientificNotationOrZero_returnsFalse() {
        assertFalse(matcher.matches("120000.00", new BigDecimal("120000.00")));
        assertFalse(matcher.matches("1.2E7", new BigDecimal("120000.00")));
        assertFalse(matcher.matches("0", new BigDecimal("120000.00")));
    }
}
