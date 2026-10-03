package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.RegisterRequest;
import vn.hcmute.edu.dp.nhom10.backend.entity.MembershipTier;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.MembershipTierRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.AuthServiceImpl;

import vn.hcmute.edu.dp.nhom10.backend.repository.ActivityLogRepository;
import vn.hcmute.edu.dp.nhom10.backend.security.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.LoginRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ForgotPasswordRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ResetPasswordRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.TokenResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.ActivityLog;
import org.springframework.context.ApplicationEventPublisher;
import vn.hcmute.edu.dp.nhom10.backend.event.UserRegisteredEvent;
import vn.hcmute.edu.dp.nhom10.backend.event.PasswordResetRequestedEvent;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MembershipTierRepository membershipTierRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "verificationTokenTtl", 900L);
        ReflectionTestUtils.setField(authService, "refreshTokenTtl", 604800L);
        ReflectionTestUtils.setField(authService, "rememberMeTokenTtl", 2592000L);
        ReflectionTestUtils.setField(authService, "passwordResetTokenTtl", 900L);
        ReflectionTestUtils.setField(authService, "googleClientId", "test-google-client-id");
    }

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest("test@test.com", "Password123", "Test User");
        MembershipTier tier = new MembershipTier();
        tier.setId(1L);

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(membershipTierRepository.findById(1L)).thenReturn(Optional.of(tier));
        when(passwordEncoder.encode(request.password())).thenReturn("hashed_password");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        authService.register(request);

        verify(userRepository).save(any(User.class));
        verify(valueOperations).set(anyString(), eq("1"), eq(900L), eq(TimeUnit.SECONDS));
        verify(eventPublisher).publishEvent(any(UserRegisteredEvent.class));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        RegisterRequest request = new RegisterRequest("test@test.com", "Password123", "Test User");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(InvalidDataException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyEmail_validToken_success() {
        String token = "valid_token";
        String key = "email_verify:" + token;
        User user = new User();
        user.setId(1L);
        user.setEmailVerified(false);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        authService.verifyEmail(token);

        assertTrue(user.getEmailVerified());
        verify(userRepository).save(user);
        verify(redisTemplate).delete(key);
    }

    @Test
    void verifyEmail_expiredToken_throwsException() {
        String token = "invalid_token";
        String key = "email_verify:" + token;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);

        assertThrows(InvalidDataException.class, () -> authService.verifyEmail(token));
    }

    @Test
    void verifyEmail_alreadyVerified_throwsException() {
        String token = "valid_token";
        String key = "email_verify:" + token;
        User user = new User();
        user.setId(1L);
        user.setEmailVerified(true);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(InvalidDataException.class, () -> authService.verifyEmail(token));
        verify(userRepository, never()).save(any());
    }

    @Test
    void resendVerification_success() {
        String email = "test@test.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setFullName("Test User");
        user.setEmailVerified(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        authService.resendVerificationEmail(email);

        verify(valueOperations).set(anyString(), eq("1"), eq(900L), eq(TimeUnit.SECONDS));
        verify(eventPublisher).publishEvent(any(UserRegisteredEvent.class));
    }

    @Test
    void resendVerification_alreadyVerified_throwsException() {
        String email = "test@test.com";
        User user = new User();
        user.setId(1L);
        user.setEmailVerified(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(InvalidDataException.class, () -> authService.resendVerificationEmail(email));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest("test@test.com", "Password123", false);
        User user = new User();
        user.setId(1L);
        user.setEmail(request.email());
        user.setPasswordHash("hashed_password");
        user.setIsActive(true);
        user.setEmailVerified(true);

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);
        when(jwtTokenProvider.generateToken(user.getEmail(), user.getSessionVersion())).thenReturn("access_token");
        when(jwtTokenProvider.getJwtExpirationInMs()).thenReturn(900000L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        TokenResponse response = authService.login(request, "127.0.0.1", "userAgent");

        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals(900L, response.getExpiresIn());

        verify(userRepository).save(user);
        assertNotNull(user.getLastLoginAt());
        verify(activityLogRepository).save(any(ActivityLog.class));
        verify(valueOperations).set(anyString(), eq("1:0"), eq(604800L), eq(TimeUnit.SECONDS));
    }

    @Test
    void login_invalidEmail_throwsException() {
        LoginRequest request = new LoginRequest("wrong@test.com", "Password123", false);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(request, null, null));
    }

    @Test
    void login_inactiveAccount_throwsException() {
        LoginRequest request = new LoginRequest("test@test.com", "Password123", false);
        User user = new User();
        user.setIsActive(false);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> authService.login(request, null, null));
    }

    @Test
    void login_unverifiedEmail_throwsException() {
        LoginRequest request = new LoginRequest("test@test.com", "Password123", false);
        User user = new User();
        user.setIsActive(true);
        user.setEmailVerified(false);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> authService.login(request, null, null));
    }

    @Test
    void login_wrongPassword_throwsException() {
        LoginRequest request = new LoginRequest("test@test.com", "WrongPass", false);
        User user = new User();
        user.setId(1L);
        user.setPasswordHash("hashed_password");
        user.setIsActive(true);
        user.setEmailVerified(true);

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request, null, null));
        verify(activityLogRepository).save(any(ActivityLog.class)); // Verifies logActivity("login_failed")
    }

    @Test
    void refreshToken_success() {
        String token = "valid_refresh_token";
        String key = "refresh_token:" + token;
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setIsActive(true);
        user.setEmailVerified(true);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(user.getEmail(), user.getSessionVersion())).thenReturn("new_access_token");
        when(jwtTokenProvider.getJwtExpirationInMs()).thenReturn(900000L);

        TokenResponse response = authService.refreshToken(token);

        assertNotNull(response);
        assertEquals("new_access_token", response.getAccessToken());
        assertEquals(token, response.getRefreshToken());
    }

    @Test
    void refreshToken_invalidToken_throwsException() {
        String token = "invalid_refresh_token";
        String key = "refresh_token:" + token;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);

        assertThrows(BadCredentialsException.class, () -> authService.refreshToken(token));
    }

    @Test
    void logout_success() {
        String token = "refresh_token_to_delete";
        authService.logout(token);
        verify(redisTemplate).delete("refresh_token:" + token);
    }

    @Test
    void login_withRememberMe_usesLongerTtl() {
        LoginRequest request = new LoginRequest("test@test.com", "Password123", true);
        User user = new User();
        user.setId(1L);
        user.setEmail(request.email());
        user.setPasswordHash("hashed_password");
        user.setIsActive(true);
        user.setEmailVerified(true);

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);
        when(jwtTokenProvider.generateToken(user.getEmail(), user.getSessionVersion())).thenReturn("access_token");
        when(jwtTokenProvider.getJwtExpirationInMs()).thenReturn(900000L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        TokenResponse response = authService.login(request, "127.0.0.1", "userAgent");

        assertNotNull(response);
        // Verify that remember-me TTL (30 days = 2592000s) is used instead of default 7
        // days
        verify(valueOperations).set(anyString(), eq("1:0"), eq(2592000L), eq(TimeUnit.SECONDS));
    }

    @ParameterizedTest
    @CsvSource({
            "aki23092005@gmail.com, aki23092005@gmail.com",
            "AKI23092005@GMAIL.COM, aki23092005@gmail.com",
            "aKi23092005@gmail.com, aki23092005@gmail.com",
            "aki23092005@gMaIL.CoM, aki23092005@gmail.com",
            "aKi23092005@gMaIL.CoM, aki23092005@gmail.com",
            "aki23092005@gmail.com, aKi23092005@gMaIL.CoM"
    })
    void forgotPassword_emailCaseVariants_sendToStoredEmail(String inputEmail, String storedEmail) {
        ForgotPasswordRequest request = new ForgotPasswordRequest(inputEmail);
        User user = new User();
        user.setId(1L);
        user.setEmail(storedEmail);
        user.setFullName("Test User");

        when(userRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.of(user));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        authService.forgotPassword(request);

        ArgumentCaptor<PasswordResetRequestedEvent> eventCaptor =
                ArgumentCaptor.forClass(PasswordResetRequestedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        PasswordResetRequestedEvent event = eventCaptor.getValue();
        assertEquals(storedEmail, event.getEmail());
        assertEquals(user.getFullName(), event.getFullName());
        assertNotNull(event.getToken());
        assertFalse(event.getToken().isBlank());
        verify(valueOperations).set(eq("password_reset:" + event.getToken()), eq("1"), eq(900L), eq(TimeUnit.SECONDS));
        verify(userRepository).findByEmailIgnoreCase(inputEmail);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"notfound@test.com", "NotFound@TeSt.CoM"})
    void forgotPassword_emailNotFound_doesNothingSilently(String email) {
        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        when(userRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.empty());

        authService.forgotPassword(request);

        verifyNoInteractions(redisTemplate, valueOperations, eventPublisher);
    }

    @Test
    void resetPassword_success() {
        ResetPasswordRequest request = new ResetPasswordRequest("valid_token", "NewPass123", "NewPass123");
        String key = "password_reset:" + request.token();
        User user = new User();
        user.setId(1L);
        user.setPasswordHash("old_hash");
        user.setSessionVersion(3L);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn("1");
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(request.newPassword())).thenReturn("new_hash");

        authService.resetPassword(request);

        assertEquals("new_hash", user.getPasswordHash());
        assertEquals(4L, user.getSessionVersion());
        verify(userRepository).saveAndFlush(user);
        verify(redisTemplate).delete(key);
        verify(redisTemplate, never()).keys(anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "1:0", "1:2", "garbage", "1:-1", "1:3:4"})
    void refreshToken_revokedOrMalformed_cannotIssueAccessToken(String value) {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("refresh_token:old")).thenReturn(value);
        if (value.matches("1(?::[0-9]+)?")) {
            User user = new User();
            user.setSessionVersion(3L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        }
        assertThrows(BadCredentialsException.class, () -> authService.refreshToken("old"));
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    void refreshToken_currentVersion_issuesVersionedToken() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setSessionVersion(3L);
        user.setIsActive(true);
        user.setEmailVerified(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("refresh_token:current")).thenReturn("1:3");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(user.getEmail(), 3L)).thenReturn("new");
        assertEquals("new", authService.refreshToken("current").getAccessToken());
    }

    @Test
    void resetPassword_tokenConsumedWhileWaiting_doesNotChangePassword() {
        User user = new User();
        user.setPasswordHash("old_hash");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("password_reset:used")).thenReturn("1", null);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        assertThrows(InvalidDataException.class, () -> authService.resetPassword(
                new ResetPasswordRequest("used", "NewPass123", "NewPass123")));
        assertEquals("old_hash", user.getPasswordHash());
        assertEquals(0L, user.getSessionVersion());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void resetPassword_passwordsDoNotMatch_throwsException() {
        ResetPasswordRequest request = new ResetPasswordRequest("valid_token", "NewPass123", "DifferentPass");

        assertThrows(InvalidDataException.class, () -> authService.resetPassword(request));
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void googleLogin_bindsBothTokensToCurrentSessionVersion() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("google@test.com");
        user.setFullName("Google User");
        user.setRole(vn.hcmute.edu.dp.nhom10.backend.enums.UserRole.customer);
        user.setSessionVersion(3L);
        user.setIsActive(true);
        user.setEmailVerified(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(jwtTokenProvider.generateToken(user.getEmail(), 3L)).thenReturn("google_token");
        var payload = new com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload();
        payload.setEmail(user.getEmail());
        var token = mock(com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.class);
        when(token.getPayload()).thenReturn(payload);
        var verifier = mock(com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.class);
        when(verifier.verify("google-id-token")).thenReturn(token);
        try (var builders = mockConstruction(com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder.class,
                (builder, context) -> {
                    when(builder.setAudience(anyCollection())).thenReturn(builder);
                    when(builder.build()).thenReturn(verifier);
                })) {
            TokenResponse response = authService.loginWithGoogle(
                    new vn.hcmute.edu.dp.nhom10.backend.dto.request.GoogleAuthRequest("google-id-token"), null, null);
            assertEquals("google_token", response.getAccessToken());
            verify(valueOperations).set(startsWith("refresh_token:"), eq("1:3"), eq(604800L), eq(TimeUnit.SECONDS));
        }
    }

    @Test
    void resetPassword_invalidToken_throwsException() {
        ResetPasswordRequest request = new ResetPasswordRequest("invalid_token", "NewPass123", "NewPass123");
        String key = "password_reset:" + request.token();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);

        assertThrows(InvalidDataException.class, () -> authService.resetPassword(request));
    }
}
