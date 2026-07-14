package vn.hcmute.edu.dp.nhom10.backend.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import vn.hcmute.edu.dp.nhom10.backend.config.RateLimitProperties;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RateLimitingFilterTest {

    private RateLimitService rateLimitService;
    private RateLimitingFilter rateLimitingFilter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        rateLimitService = mock(RateLimitService.class);
        objectMapper = new ObjectMapper();
        ApiErrorResponseWriter apiErrorResponseWriter = new ApiErrorResponseWriter(objectMapper);

        rateLimitingFilter = new RateLimitingFilter(
                rateLimitService,
                buildProperties(),
                new ClientIpResolver(),
                apiErrorResponseWriter
        );
    }

    @Test
    void doFilter_allowsRequestAndAddsRateLimitHeaders() throws Exception {
        when(rateLimitService.consume(anyString(), anyInt(), any(Duration.class)))
                .thenReturn(new RateLimitDecision(true, 1L, 5, 4L, 60L));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr("203.0.113.10");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean(false);

        rateLimitingFilter.doFilter(request, response, (req, res) -> invoked.set(true));

        assertTrue(invoked.get());
        assertEquals("auth-login", response.getHeader("X-RateLimit-Rule"));
        assertEquals("5", response.getHeader("X-RateLimit-Limit"));
        assertEquals("4", response.getHeader("X-RateLimit-Remaining"));
        assertEquals("60", response.getHeader("X-RateLimit-Reset"));

        verify(rateLimitService).consume("auth-login:203.0.113.10", 5, Duration.ofSeconds(60));
    }

    @Test
    void doFilter_rejectsWhenLimitExceeded() throws Exception {
        when(rateLimitService.consume(anyString(), anyInt(), any(Duration.class)))
                .thenReturn(new RateLimitDecision(false, 6L, 5, 0L, 45L));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr("198.51.100.20");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean(false);

        rateLimitingFilter.doFilter(request, response, (req, res) -> invoked.set(true));

        assertFalse(invoked.get());
        assertEquals(429, response.getStatus());
        assertEquals("45", response.getHeader("Retry-After"));
        assertEquals("0", response.getHeader("X-RateLimit-Remaining"));

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertEquals(429, body.get("status").asInt());
        assertEquals("Too Many Requests", body.get("error").asText());
        assertEquals("Too many login attempts. Please try again in a minute.", body.get("message").asText());
        assertEquals("/api/auth/login", body.get("path").asText());
    }

    @Test
    void doFilter_skipsUnmatchedRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/users");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean(false);

        rateLimitingFilter.doFilter(request, response, (req, res) -> invoked.set(true));

        assertTrue(invoked.get());
        verifyNoInteractions(rateLimitService);
    }

    private RateLimitProperties buildProperties() {
        RateLimitProperties properties = new RateLimitProperties();
        RateLimitProperties.Rule rule = new RateLimitProperties.Rule();
        rule.setKey("auth-login");
        rule.setPathPatterns(List.of("/api/auth/login"));
        rule.setMethods(List.of("POST"));
        rule.setLimit(5);
        rule.setWindowSeconds(60);
        rule.setMessage("Too many login attempts. Please try again in a minute.");
        properties.setRules(List.of(rule));
        return properties;
    }
}
