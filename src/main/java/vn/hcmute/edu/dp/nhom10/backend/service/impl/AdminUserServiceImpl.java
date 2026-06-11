package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.AdminUserResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;
import vn.hcmute.edu.dp.nhom10.backend.pattern.observer.user.event.UserStatusChangedEvent;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.AdminUserService;
import vn.hcmute.edu.dp.nhom10.backend.pattern.specification.UserSpecification;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getUsers(int page, int size, String keyword, UserRole role, Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        //Specification Pattern: xây dựng câu truy vấn động
        Specification<User> spec = Specification.where(UserSpecification.hasKeyword(keyword))
                .and(UserSpecification.hasRole(role))
                .and(UserSpecification.hasActiveStatus(isActive));

        Page<User> userPage = userRepository.findAll(spec, pageable);
        return userPage.map(this::convertToResponse);
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserStatus(Long userId, Boolean isActive) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        // Bảo mật nghiệp vụ: Không cho phép admin tự khóa tài khoản của chính mình
        String currentAdminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (targetUser.getEmail().equals(currentAdminEmail)) {
            throw new InvalidDataException("Bạn không thể tự khóa tài khoản của chính mình.");
        }

        Boolean oldStatus = targetUser.getIsActive();
        targetUser.setIsActive(isActive);
        User savedUser = userRepository.save(targetUser);

        // Phát sự kiện (Observer Pattern)
        if (!oldStatus.equals(isActive)) {
            eventPublisher.publishEvent(new UserStatusChangedEvent(
                    this,
                    savedUser.getId(),
                    savedUser.getEmail(),
                    oldStatus,
                    isActive
            ));
        }

        return convertToResponse(savedUser);
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserRole(Long userId, UserRole role) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        // Bảo mật nghiệp vụ: Không cho phép admin tự thay đổi vai trò của chính mình
        String currentAdminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (targetUser.getEmail().equals(currentAdminEmail)) {
            throw new InvalidDataException("Bạn không thể tự thay đổi vai trò của chính mình.");
        }

        targetUser.setRole(role);
        User savedUser = userRepository.save(targetUser);

        return convertToResponse(savedUser);
    }

    /**
     * Hàm phụ trợ chuyển đổi Entity User sang AdminUserResponse DTO
     */
    private AdminUserResponse convertToResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getGender(),
                user.getDateOfBirth(),
                user.getAvatarUrl(),
                user.getRole(),
                user.getLoyaltyPoints(),
                user.getMembershipTier() != null ? user.getMembershipTier().getId() : null,
                user.getMembershipTier() != null ? user.getMembershipTier().getName() : null,
                user.getAuthProvider(),
                user.getEmailVerified(),
                user.getIsActive(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}