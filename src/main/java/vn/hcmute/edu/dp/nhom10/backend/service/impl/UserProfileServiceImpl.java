package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpdateProfileRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.UserProfileResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.MembershipTier;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.profile.DobValidationHandler;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.profile.NameValidationHandler;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.profile.PhoneValidationHandler;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.profile.ProfileValidationHandler;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.UserProfileService;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "USER-PROFILE-SERVICE")
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;

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
