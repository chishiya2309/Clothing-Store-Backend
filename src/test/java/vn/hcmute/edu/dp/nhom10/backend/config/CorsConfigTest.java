package vn.hcmute.edu.dp.nhom10.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsConfigTest {

    @Test
    void corsConfigurationSource_usesConfiguredOriginsAndHeaders() {
        CorsProperties corsProperties = new CorsProperties();
        corsProperties.setAllowCredentials(true);
        corsProperties.setAllowedOriginPatterns(List.of(
                "https://shop.example.com",
                " http://localhost:3000 ",
                "https://shop.example.com"
        ));
        corsProperties.setExposedHeaders(List.of(
                "Retry-After",
                " X-RateLimit-Reset ",
                "Retry-After"
        ));

        CorsConfig corsConfig = new CorsConfig(corsProperties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products");

        CorsConfiguration configuration = corsConfig.corsConfigurationSource().getCorsConfiguration(request);

        assertEquals(List.of("https://shop.example.com", "http://localhost:3000"), configuration.getAllowedOriginPatterns());
        assertEquals(List.of("Retry-After", "X-RateLimit-Reset"), configuration.getExposedHeaders());
        assertTrue(Boolean.TRUE.equals(configuration.getAllowCredentials()));
        assertEquals(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"), configuration.getAllowedMethods());
    }
}
