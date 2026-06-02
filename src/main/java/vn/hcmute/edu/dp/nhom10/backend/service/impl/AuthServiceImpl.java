package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.RegisterRequest;
import vn.hcmute.edu.dp.nhom10.backend.entity.MembershipTier;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.MembershipTierRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.AuthService;
import vn.hcmute.edu.dp.nhom10.backend.service.EmailService;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "AUTHENTICATION-SERVICE")
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final MembershipTierRepository membershipTierRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.verification-token-ttl}")
    private long verificationTokenTtl;

    private static final String VERIFY_PREFIX = "email_verify:";

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

        if (user.getEmailVerified()) {
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

        if (user.getEmailVerified()) {
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
}
