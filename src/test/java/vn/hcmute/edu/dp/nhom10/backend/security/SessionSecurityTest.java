package vn.hcmute.edu.dp.nhom10.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.test.util.ReflectionTestUtils;
import vn.hcmute.edu.dp.nhom10.backend.config.*;
import vn.hcmute.edu.dp.nhom10.backend.controller.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.LoginRequest;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;
import vn.hcmute.edu.dp.nhom10.backend.exception.GlobalExceptionHandling;
import vn.hcmute.edu.dp.nhom10.backend.repository.*;
import vn.hcmute.edu.dp.nhom10.backend.service.*;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.AuthServiceImpl;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = SessionSecurityTest.Config.class)
@TestPropertySource(properties = {
        "jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "jwt.expiration=900000", "app.verification-token-ttl=900"
})
class SessionSecurityTest {
    @Autowired WebApplicationContext context;
    @Autowired UserRepository users;
    @Autowired RedisTemplate<String, Object> redis;
    @Autowired AuthService auth;
    @Autowired JwtTokenProvider jwt;
    @Autowired PlaceOrderService orders;
    @Autowired UserProfileService profiles;
    private MockMvc mvc;
    private User user;
    private Map<String, Object> values;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() {
        reset(users, redis, orders, profiles);
        user = user(1L, "a@test.com");
        User other = user(2L, "other@test.com");
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(users.findByEmail(other.getEmail())).thenReturn(Optional.of(other));
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(users.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        values = new HashMap<>();
        values.put("password_reset:reset", "1");
        values.put("refresh_token:A", "1:0");
        values.put("refresh_token:B", "1");
        ValueOperations<String, Object> ops = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.get(anyString())).thenAnswer(call -> values.get(call.getArgument(0)));
        doAnswer(call -> { values.put(call.getArgument(0), call.getArgument(1)); return null; })
                .when(ops).set(anyString(), any(), anyLong(), any(TimeUnit.class));
        when(redis.delete(anyString())).thenAnswer(call -> values.remove(call.getArgument(0)) != null);
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private User user(Long id, String email) {
        return User.builder().id(id).email(email).fullName("Test User").role(UserRole.customer)
                .passwordHash(context.getBean(org.springframework.security.crypto.password.PasswordEncoder.class).encode("OldPass123"))
                .emailVerified(true).isActive(true).build();
    }

    private void resetOnC() throws Exception {
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"reset\",\"newPassword\":\"NewPass123\",\"confirmPassword\":\"NewPass123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void resetOnCRevokesUnexpiredTokensOnAAndBAndPreventsCheckout() throws Exception {
        String tokenA = jwt.generateToken(user.getEmail(), 0);
        String tokenB = jwt.generateToken(user.getEmail(), 0);
        String other = jwt.generateToken("other@test.com", 0);
        mvc.perform(get("/api/auth/session").header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.role").value("customer"));
        mvc.perform(get("/api/customer/profile").header("Authorization", "Bearer " + tokenB)).andExpect(status().isOk());
        resetOnC();
        clearInvocations(profiles);
        assertTrue(jwt.validateToken(tokenA)); // Rejected for revocation, not expiration.
        for (String token : List.of(tokenA, tokenB)) {
            mvc.perform(get("/api/auth/session").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
            mvc.perform(get("/api/customer/profile").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
            mvc.perform(post("/api/checkouts/confirm").header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isUnauthorized());
        }
        assertThrows(org.springframework.security.authentication.BadCredentialsException.class, () -> auth.refreshToken("A"));
        assertThrows(org.springframework.security.authentication.BadCredentialsException.class, () -> auth.refreshToken("B"));
        verifyNoInteractions(orders, profiles);
        mvc.perform(get("/api/auth/session").header("Authorization", "Bearer " + other)).andExpect(status().isOk());
        verify(redis, never()).keys(anyString());
    }

    @Test
    void passwordLoginAndRememberMeWorkAfterResetWithTheNewVersion() throws Exception {
        resetOnC();
        assertThrows(org.springframework.security.authentication.BadCredentialsException.class,
                () -> auth.login(new LoginRequest(user.getEmail(), "OldPass123", false), null, null));
        var loggedIn = auth.login(new LoginRequest(user.getEmail(), "NewPass123", true), null, null);
        assertEquals(1, jwt.getSessionVersionFromJWT(loggedIn.getAccessToken()));
        assertEquals("1:1", values.get("refresh_token:" + loggedIn.getRefreshToken()));
        assertEquals(1, jwt.getSessionVersionFromJWT(auth.refreshToken(loggedIn.getRefreshToken()).getAccessToken()));
        mvc.perform(get("/api/auth/session").header("Authorization", "Bearer " + loggedIn.getAccessToken()))
                .andExpect(status().isOk());
        verify(redis.opsForValue()).set(eq("refresh_token:" + loggedIn.getRefreshToken()), eq("1:1"),
                eq(2592000L), eq(TimeUnit.SECONDS));
    }

    @Test
    void legacyTokensOnlyWorkBeforeReset() throws Exception {
        String legacy = JwtTokenProviderTest.legacyToken(user.getEmail());
        mvc.perform(get("/api/auth/session").header("Authorization", "Bearer " + legacy)).andExpect(status().isOk());
        assertNotNull(auth.refreshToken("B").getAccessToken());
        resetOnC();
        mvc.perform(get("/api/auth/session").header("Authorization", "Bearer " + legacy)).andExpect(status().isUnauthorized());
    }

    @Test
    void sessionEndpointRequiresAuthenticationButForgotPasswordRemainsPublic() throws Exception {
        mvc.perform(get("/api/auth/session")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"unknown@test.com\"}")).andExpect(status().isOk());
    }

    @Test
    void optimisticConflictDoesNotReportResetSuccessOrConsumeToken() throws Exception {
        doThrow(new org.springframework.orm.ObjectOptimisticLockingFailureException(User.class, 1L))
                .when(users).saveAndFlush(any(User.class));
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"reset\",\"newPassword\":\"NewPass123\",\"confirmPassword\":\"NewPass123\"}"))
                .andExpect(status().isConflict());
        verify(redis, never()).delete("password_reset:reset");
    }

    @Configuration
    @EnableWebMvc
    @Import({SecurityConfig.class, CorsConfig.class})
    static class Config {
        @Bean UserRepository users() { return mock(UserRepository.class); }
        @Bean JwtTokenProvider jwt() { return JwtTokenProviderTest.provider(); }
        @Bean CustomUserDetailsService details(UserRepository users) { return new CustomUserDetailsService(users); }
        @Bean JwtAuthenticationFilter filter(JwtTokenProvider jwt, CustomUserDetailsService details) { return new JwtAuthenticationFilter(jwt, details); }
        @Bean ApiErrorResponseWriter writer() { return new ApiErrorResponseWriter(new ObjectMapper()); }
        @Bean JwtAuthenticationEntryPoint entry(ApiErrorResponseWriter writer) { return new JwtAuthenticationEntryPoint(writer); }
        @Bean RateLimitingFilter rate(RateLimitProperties properties, ApiErrorResponseWriter writer) {
            properties.setEnabled(false);
            return new RateLimitingFilter(mock(RateLimitService.class), properties, mock(ClientIpResolver.class), writer);
        }
        @Bean @SuppressWarnings("unchecked") RedisTemplate<String, Object> redis() { return mock(RedisTemplate.class); }
        @Bean AuthService auth(UserRepository users, RedisTemplate<String, Object> redis, JwtTokenProvider jwt,
                               org.springframework.security.crypto.password.PasswordEncoder encoder) {
            AuthServiceImpl service = new AuthServiceImpl(users, mock(MembershipTierRepository.class),
                    mock(ActivityLogRepository.class), encoder, mock(ApplicationEventPublisher.class), redis, jwt);
            ReflectionTestUtils.setField(service, "passwordResetTokenTtl", 900L);
            ReflectionTestUtils.setField(service, "refreshTokenTtl", 604800L);
            ReflectionTestUtils.setField(service, "rememberMeTokenTtl", 2592000L);
            return service;
        }
        @Bean AuthController authController(AuthService service) { return new AuthController(service); }
        @Bean PlaceOrderService orders() { return mock(PlaceOrderService.class); }
        @Bean UserProfileService profiles() { return mock(UserProfileService.class); }
        @Bean UserProfileController profileController(UserProfileService service) { return new UserProfileController(service); }
        @Bean CheckoutController checkout(PlaceOrderService service, UserRepository users) {
            return new CheckoutController(service, new AuthenticatedUserProvider(users), mock(ClientIpResolver.class));
        }
        @Bean GlobalExceptionHandling advice() { return new GlobalExceptionHandling(); }
    }
}
