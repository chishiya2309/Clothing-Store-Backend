package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MomoAmountConverterTest {

    private final MomoAmountConverter converter = new MomoAmountConverter();

    @Test
    void toMomoAmount_requiresExactPositiveVndInteger() {
        assertThat(converter.toMomoAmount(new BigDecimal("100000"))).isEqualTo(100000L);
        assertThat(converter.toMomoAmount(new BigDecimal("100000.00"))).isEqualTo(100000L);

        assertThatThrownBy(() -> converter.toMomoAmount(new BigDecimal("100000.50")))
                .isInstanceOf(InvalidDataException.class);
        assertThatThrownBy(() -> converter.toMomoAmount(BigDecimal.ZERO))
                .isInstanceOf(InvalidDataException.class);
        assertThatThrownBy(() -> converter.toMomoAmount(new BigDecimal("50000001")))
                .isInstanceOf(InvalidDataException.class);
    }

    @Test
    void matches_comparesGatewayLongToAttemptAmountExactly() {
        assertThat(converter.matches(100000L, new BigDecimal("100000.00"))).isTrue();
        assertThat(converter.matches(100001L, new BigDecimal("100000.00"))).isFalse();
        assertThat(converter.matches(100000L, new BigDecimal("100000.10"))).isFalse();
    }
}
