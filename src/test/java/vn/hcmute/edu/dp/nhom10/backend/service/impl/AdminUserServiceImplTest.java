package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.AdminUserResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.observer.user.event.UserStatusChangedEvent;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    private User adminUser;
    private User targetUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(UserRole.admin);
        adminUser.setIsActive(true);

        targetUser = new User();
        targetUser.setId(2L);
        targetUser.setEmail("user@example.com");
        targetUser.setRole(UserRole.customer);
        targetUser.setIsActive(true);
    }

    private void mockSecurityContext(String email) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getUsers_Success() {
        Page<User> userPage = new PageImpl<>(Collections.singletonList(targetUser));
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        Page<AdminUserResponse> result = adminUserService.getUsers(0, 10, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(targetUser.getEmail(), result.getContent().get(0).email());
    }

    @Test
    void updateUserStatus_Success() {
        mockSecurityContext("admin@example.com");
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(userRepository.save(any(User.class))).thenReturn(targetUser);

        AdminUserResponse response = adminUserService.updateUserStatus(2L, false);

        assertNotNull(response);
        assertFalse(response.isActive());
        verify(eventPublisher, times(1)).publishEvent(any(UserStatusChangedEvent.class));
    }

    @Test
    void updateUserStatus_ThrowsException_WhenLockingOwnAccount() {
        mockSecurityContext("admin@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        InvalidDataException exception = assertThrows(InvalidDataException.class, () -> {
            adminUserService.updateUserStatus(1L, false);
        });

        assertEquals("Bạn không thể tự khóa tài khoản của chính mình.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserStatus_ThrowsException_WhenUserNotFound() {
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            adminUserService.updateUserStatus(3L, false);
        });
    }

    @Test
    void updateUserRole_Success() {
        mockSecurityContext("admin@example.com");
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(userRepository.save(any(User.class))).thenReturn(targetUser);

        AdminUserResponse response = adminUserService.updateUserRole(2L, UserRole.staff);

        assertNotNull(response);
        assertEquals(UserRole.staff, response.role());
    }

    @Test
    void updateUserRole_ThrowsException_WhenChangingOwnRole() {
        mockSecurityContext("admin@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        InvalidDataException exception = assertThrows(InvalidDataException.class, () -> {
            adminUserService.updateUserRole(1L, UserRole.customer);
        });

        assertEquals("Bạn không thể tự thay đổi vai trò của chính mình.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }
}
