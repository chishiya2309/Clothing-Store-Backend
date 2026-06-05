package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import vn.hcmute.edu.dp.nhom10.backend.dto.response.TokenResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.ActivityLog;

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
    private EmailService emailService;

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
        verify(emailService).sendVerificationEmail(eq("test@test.com"), eq("Test User"), anyString());
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
        verify(emailService).sendVerificationEmail(eq(email), eq("Test User"), anyString());
    }

    @Test
    void resendVerification_alreadyVerified_throwsException() {
        String email = "test@test.com";
        User user = new User();
        user.setId(1L);
        user.setEmailVerified(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(InvalidDataException.class, () -> authService.resendVerificationEmail(email));
        verify(emailService, never()).sendVerificationEmail(any(), any(), any());
    }

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest("test@test.com", "Password123");
        User user = new User();
        user.setId(1L);
        user.setEmail(request.email());
        user.setPasswordHash("hashed_password");
        user.setIsActive(true);
        user.setEmailVerified(true);

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);
        when(jwtTokenProvider.generateToken(user.getEmail())).thenReturn("access_token");
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
        verify(valueOperations).set(anyString(), eq("1"), eq(604800L), eq(TimeUnit.SECONDS));
    }

    @Test
    void login_invalidEmail_throwsException() {
        LoginRequest request = new LoginRequest("wrong@test.com", "Password123");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.login(request, null, null));
    }

    @Test
    void login_inactiveAccount_throwsException() {
        LoginRequest request = new LoginRequest("test@test.com", "Password123");
        User user = new User();
        user.setIsActive(false);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> authService.login(request, null, null));
    }

    @Test
    void login_unverifiedEmail_throwsException() {
        LoginRequest request = new LoginRequest("test@test.com", "Password123");
        User user = new User();
        user.setIsActive(true);
        user.setEmailVerified(false);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> authService.login(request, null, null));
    }

    @Test
    void login_wrongPassword_throwsException() {
        LoginRequest request = new LoginRequest("test@test.com", "WrongPass");
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
        when(jwtTokenProvider.generateToken(user.getEmail())).thenReturn("new_access_token");
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

        assertThrows(InvalidDataException.class, () -> authService.refreshToken(token));
    }

    @Test
    void logout_success() {
        String token = "refresh_token_to_delete";
        authService.logout(token);
        verify(redisTemplate).delete("refresh_token:" + token);
    }
}
