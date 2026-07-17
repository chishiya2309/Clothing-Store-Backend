package vn.hcmute.edu.dp.nhom10.backend.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private JwtTokenProvider tokenProvider;
    private CustomUserDetailsService customUserDetailsService;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        tokenProvider = mock(JwtTokenProvider.class);
        customUserDetailsService = mock(CustomUserDetailsService.class);
        filter = new JwtAuthenticationFilter(tokenProvider, customUserDetailsService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_authenticatesWithCaseInsensitiveTrimmedBearerHeader() throws Exception {
        when(tokenProvider.validateToken("valid-token")).thenReturn(true);
        when(tokenProvider.getUsernameFromJWT("valid-token")).thenReturn("alice@example.com");
        when(customUserDetailsService.loadUserByUsername("alice@example.com")).thenReturn(userDetails());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "   bearer valid-token   ");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean(false);

        filter.doFilter(request, response, (req, res) -> invoked.set(true));

        assertTrue(invoked.get());
        assertEquals("alice@example.com", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(tokenProvider).validateToken("valid-token");
        verify(customUserDetailsService).loadUserByUsername("alice@example.com");
    }

    @Test
    void doFilter_skipsJwtLookupWhenSecurityContextAlreadyHasAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("existing@example.com", null)
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean(false);

        filter.doFilter(request, response, (req, res) -> invoked.set(true));

        assertTrue(invoked.get());
        assertEquals("existing@example.com", SecurityContextHolder.getContext().getAuthentication().getName());
        verifyNoInteractions(tokenProvider, customUserDetailsService);
    }

    @Test
    void doFilter_ignoresBlankBearerValue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer     ");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean(false);

        filter.doFilter(request, response, (req, res) -> invoked.set(true));

        assertTrue(invoked.get());
        verifyNoInteractions(tokenProvider, customUserDetailsService);
    }

    private UserDetails userDetails() {
        return User.withUsername("alice@example.com")
                .password("secret")
                .authorities("ROLE_customer")
                .build();
    }
}
