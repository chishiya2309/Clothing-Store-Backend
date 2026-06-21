package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpdateProfileRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ChangePasswordRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.MembershipInfoResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.MembershipTierDto;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.UserProfileResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.MembershipTier;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.profile.DobValidationHandler;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.profile.NameValidationHandler;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.profile.PhoneValidationHandler;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.profile.ProfileValidationHandler;
import vn.hcmute.edu.dp.nhom10.backend.repository.MembershipTierRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.UserProfileService;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "USER-PROFILE-SERVICE")
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MembershipTierRepository membershipTierRepository;

    // Inject the handlers
    private final NameValidationHandler nameValidationHandler;
    private final PhoneValidationHandler phoneValidationHandler;
    private final DobValidationHandler dobValidationHandler;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email: " + email));

        return mapToResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(String email, UpdateProfileRequest request) {
        nameValidationHandler.setNext(phoneValidationHandler).setNext(dobValidationHandler);

        nameValidationHandler.handle(request);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email: " + email));

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }

        User savedUser = userRepository.save(user);
        log.info("Updated profile for user: {}", email);
        return mapToResponse(savedUser);
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email: " + email));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new InvalidDataException("Mật khẩu hiện tại không chính xác");
        }

        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new InvalidDataException("Mật khẩu xác nhận không khớp");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new InvalidDataException("Mật khẩu mới không được trùng mật khẩu hiện tại");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Changed password for user: {}", email);
    }

    @Override
    @Transactional(readOnly = true)
    public MembershipInfoResponse getMembershipInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email: " + email));

        List<MembershipTier> allTiers = membershipTierRepository.findAllByOrderByMinPointsAsc();
        MembershipTier currentTier = user.getMembershipTier();
        int loyaltyPoints = user.getLoyaltyPoints() != null ? user.getLoyaltyPoints() : 0;

        // Find next tier
        String nextTierName = null;
        Integer pointsNeeded = null;

        if (currentTier != null) {
            boolean foundCurrent = false;
            for (MembershipTier tier : allTiers) {
                if (foundCurrent) {
                    nextTierName = tier.getName();
                    pointsNeeded = tier.getMinPoints() - loyaltyPoints;
                    break;
                }
                if (tier.getId().equals(currentTier.getId())) {
                    foundCurrent = true;
                }
            }
        } else if (!allTiers.isEmpty()) {
            // User has no tier yet, next tier is the first one
            MembershipTier firstTier = allTiers.get(0);
            nextTierName = firstTier.getName();
            pointsNeeded = firstTier.getMinPoints() - loyaltyPoints;
        }

        List<MembershipTierDto> tierDtos = allTiers.stream()
                .map(t -> MembershipTierDto.builder()
                        .name(t.getName())
                        .minPoints(t.getMinPoints())
                        .discountPercent(t.getDiscountPercent())
                        .description(t.getDescription())
                        .build())
                .collect(Collectors.toList());

        log.info("Fetched membership info for user: {}", email);

        return MembershipInfoResponse.builder()
                .loyaltyPoints(loyaltyPoints)
                .currentTierName(currentTier != null ? currentTier.getName() : null)
                .currentTierDiscount(currentTier != null ? currentTier.getDiscountPercent() : BigDecimal.ZERO)
                .currentTierDescription(currentTier != null ? currentTier.getDescription() : null)
                .nextTierName(nextTierName)
                .pointsNeededForNextTier(pointsNeeded)
                .allTiers(tierDtos)
                .build();
    }

    private UserProfileResponse mapToResponse(User user) {
        MembershipTier tier = user.getMembershipTier();
        return UserProfileResponse.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .avatarUrl(user.getAvatarUrl())
                .loyaltyPoints(user.getLoyaltyPoints())
                .membershipTier(tier != null ? tier.getName() : null)
                .build();
    }
}
