package vn.hcmute.edu.dp.nhom10.backend.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenSessionTest {
    @Test
    void acceptsLegacyAndVersionedValues() {
        assertEquals(new RefreshTokenSession(12, 0), RefreshTokenSession.decode("12"));
        assertEquals(new RefreshTokenSession(12, 4), RefreshTokenSession.decode("12:4"));
        assertEquals("12:4", new RefreshTokenSession(12, 4).encode());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"0", "-1:0", "1:-1", "1:", "1:2:3", "abc", "1:99999999999999999999999"})
    void malformedValuesAreRejected(String value) {
        assertThrows(BadCredentialsException.class, () -> RefreshTokenSession.decode(value));
    }
}
