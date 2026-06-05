package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.RegisterRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.LoginRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.TokenResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.ActivityLog;
import vn.hcmute.edu.dp.nhom10.backend.entity.MembershipTier;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.ActivityLogRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.MembershipTierRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.security.JwtTokenProvider;
import vn.hcmute.edu.dp.nhom10.backend.service.AuthService;
import vn.hcmute.edu.dp.nhom10.backend.service.EmailService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "AUTHENTICATION-SERVICE")
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final MembershipTierRepository membershipTierRepository;
    private final ActivityLogRepository activityLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.verification-token-ttl}")
    private long verificationTokenTtl;

@Value("${app.refresh-token-ttl:604800}")
private long refreshTokenTtl;

    private static final String VERIFY_PREFIX = "email_verify:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    @Transactional
    @Override
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new InvalidDataException("Email is already registered");
        }

        MembershipTier bronzeTier = membershipTierRepository.findById(1L)
                .orElse(null);

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(UserRole.customer)
                .membershipTier(bronzeTier)
                .authProvider("email")
                .emailVerified(false)
                .isActive(true)
                .loyaltyPoints(0)
                .build();

        userRepository.save(user);

        String token = generateAndSaveVerificationToken(user.getId());
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), token);
    }

    @Transactional
    @Override
    public void verifyEmail(String token) {
        String key = VERIFY_PREFIX + token;
        Object userIdObj = redisTemplate.opsForValue().get(key);

        if (userIdObj == null) {
            throw new InvalidDataException("Token expired or invalid");
        }

        Long userId = Long.valueOf(userIdObj.toString());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new InvalidDataException("Email is already verified");
        }

        user.setEmailVerified(true);
        userRepository.save(user);

        redisTemplate.delete(key);
    }

    @Override
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new InvalidDataException("Email is already verified");
        }

        String token = generateAndSaveVerificationToken(user.getId());
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), token);
    }

    private String generateAndSaveVerificationToken(Long userId) {
        String token = UUID.randomUUID().toString();
        String key = VERIFY_PREFIX + token;
        redisTemplate.opsForValue().set(key, userId.toString(), verificationTokenTtl, TimeUnit.SECONDS);
        return token;
    }

    @Transactional
    @Override
    public TokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
User user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new BadCredentialsException("Email or password is incorrect"));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new AccessDeniedException("Account is locked");
        }

        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            throw new AccessDeniedException("Email not verified. Please verify your email first.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            logActivity(user, "login_failed", ipAddress, userAgent);
            throw new BadCredentialsException("Email or password is incorrect");
        }

        String accessToken = jwtTokenProvider.generateToken(user.getEmail());
        String refreshToken = UUID.randomUUID().toString();
        
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(key, user.getId().toString(), refreshTokenTtl, TimeUnit.SECONDS);

        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);

        logActivity(user, "login", ipAddress, userAgent);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getJwtExpirationInMs() / 1000)
                .build();
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        Object userIdObj = redisTemplate.opsForValue().get(key);

        if (userIdObj == null) {
            throw new InvalidDataException("Refresh token expired or invalid");
        }

        Long userId = Long.valueOf(userIdObj.toString());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getIsActive() || !user.getEmailVerified()) {
            throw new AccessDeniedException("Account is inactive or email not verified");
        }

        String newAccessToken = jwtTokenProvider.generateToken(user.getEmail());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Keep the old refresh token
                .expiresIn(jwtTokenProvider.getJwtExpirationInMs() / 1000)
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken != null) {
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + refreshToken);
        }
    }

    private void logActivity(User user, String action, String ipAddress, String userAgent) {
        Map<String, Object> newData = new HashMap<>();
        if (ipAddress != null) newData.put("ip", ipAddress);
        if (userAgent != null) newData.put("userAgent", userAgent);

        ActivityLog log = ActivityLog.builder()
                .user(user)
                .action(action)
                .entityType("user")
                .entityId(user.getId())
                .newData(newData)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
                
        activityLogRepository.save(log);
    }
}
