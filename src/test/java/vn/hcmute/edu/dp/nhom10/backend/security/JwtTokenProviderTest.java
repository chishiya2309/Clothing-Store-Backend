package vn.hcmute.edu.dp.nhom10.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private static final String PRIMARY_SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
    private static final String SECONDARY_SECRET = "ZmVkY2JhOTg3NjU0MzIxMGZlZGNiYTk4NzY1NDMyMTA=";

    @Test
    void validateToken_returnsFalseForTokenSignedWithDifferentSecret() {
        JwtTokenProvider primaryProvider = provider(PRIMARY_SECRET);
        JwtTokenProvider secondaryProvider = provider(SECONDARY_SECRET);

        String token = secondaryProvider.generateToken("alice@example.com");

        assertFalse(primaryProvider.validateToken(token));
    }

    @Test
    void generateToken_roundTripsUsernameForValidSecret() {
        JwtTokenProvider provider = provider(PRIMARY_SECRET);

        String token = provider.generateToken("alice@example.com");

        assertTrue(provider.validateToken(token));
        assertEquals("alice@example.com", provider.getUsernameFromJWT(token));
    }

    private JwtTokenProvider provider(String secret) {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", secret);
        ReflectionTestUtils.setField(provider, "jwtExpirationInMs", 3600000L);
        return provider;
    }
}
