package vn.hcmute.edu.dp.nhom10.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {
    static final String SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    static JwtTokenProvider provider() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(provider, "jwtExpirationInMs", 900000L);
        return provider;
    }

    static String legacyToken(String email) {
        return Jwts.builder().subject(email).expiration(new Date(System.currentTimeMillis() + 900000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET))).compact();
    }

    @Test
    void tokensCarryTheirSessionVersion() {
        JwtTokenProvider provider = provider();
        String token = provider.generateToken("user@test.com", 4);
        assertTrue(provider.validateToken(token));
        assertEquals("user@test.com", provider.getUsernameFromJWT(token));
        assertEquals(4, provider.getSessionVersionFromJWT(token));
        assertEquals(0, provider.getSessionVersionFromJWT(legacyToken("user@test.com")));
    }

    @Test
    void rejectsInvalidTokensAndVersionClaims() {
        JwtTokenProvider provider = provider();
        assertFalse(provider.validateToken("broken"));
        assertThrows(IllegalArgumentException.class, () -> provider.generateToken("user@test.com", -1));
        String token = Jwts.builder().subject("user@test.com").claim("session_version", "bad")
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET))).compact();
        assertThrows(io.jsonwebtoken.JwtException.class, () -> provider.getSessionVersionFromJWT(token));
    }
}
