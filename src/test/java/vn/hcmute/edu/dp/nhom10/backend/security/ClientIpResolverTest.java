package vn.hcmute.edu.dp.nhom10.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientIpResolverTest {

    private final ClientIpResolver resolver = new ClientIpResolver();

    @Test
    void resolve_usesFirstForwardedForToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", " 203.0.113.10 , 10.0.0.1");
        request.setRemoteAddr("127.0.0.1");

        assertEquals("203.0.113.10", resolver.resolve(request));
    }

    @Test
    void resolve_ignoresSpoofedProxyHeadersWhenRemoteAddressIsPublic() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.10");
        request.addHeader("X-Real-IP", "198.51.100.20");
        request.setRemoteAddr("198.51.100.7");

        assertEquals("198.51.100.7", resolver.resolve(request));
    }

    @Test
    void resolve_usesRealIpWhenForwardedForBlank() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", " ");
        request.addHeader("X-Real-IP", " 198.51.100.20 ");

        assertEquals("198.51.100.20", resolver.resolve(request));
    }

    @Test
    void resolve_usesRemoteAddrWhenProxyHeadersMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.30");

        assertEquals("192.0.2.30", resolver.resolve(request));
    }

    @Test
    void resolve_truncatesVeryLongValue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "12345678901234567890123456789012345678901234567890");

        assertEquals(45, resolver.resolve(request).length());
    }

    @Test
    void resolve_nullRequestUsesLocalFallback() {
        assertEquals("127.0.0.1", resolver.resolve(null));
    }
}
