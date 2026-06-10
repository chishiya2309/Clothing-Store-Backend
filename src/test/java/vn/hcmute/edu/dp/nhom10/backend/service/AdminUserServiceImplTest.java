package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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
import vn.hcmute.edu.dp.nhom10.backend.pattern.observer.admin.event.UserStatusChangedEvent;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.AdminUserServiceImpl;

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

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    @Test
    void getUsers_success() {
        User user = User.builder()
                .id(1L)
                .email("customer@test.com")
                .fullName("Customer User")
                .role(UserRole.customer)
                .isActive(true)
                .build();

        Page<User> userPage = new PageImpl<>(Collections.singletonList(user));

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        Page<AdminUserResponse> response = adminUserService.getUsers(0, 10, "customer", UserRole.customer, true);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("customer@test.com", response.getContent().get(0).email());
        assertEquals("Customer User", response.getContent().get(0).fullName());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void updateUserStatus_success() {
        User targetUser = User.builder()
                .id(2L)
                .email("user@test.com")
                .isActive(true)
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(userRepository.save(any(User.class))).thenReturn(targetUser);

        // Mock SecurityContextHolder dùng MockedStatic của Mockito
        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn("admin@test.com"); // Email của Admin thực hiện
            when(securityContext.getAuthentication()).thenReturn(authentication);
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            AdminUserResponse response = adminUserService.updateUserStatus(2L, false);

            assertNotNull(response);
            assertFalse(response.isActive());
            verify(userRepository).save(targetUser);
            verify(eventPublisher).publishEvent(any(UserStatusChangedEvent.class));
        }
    }

    @Test
    void updateUserStatus_statusUnchanged_noEventFired() {
        User targetUser = User.builder()
                .id(2L)
                .email("user@test.com")
                .isActive(true)
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(userRepository.save(any(User.class))).thenReturn(targetUser);

        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn("admin@test.com");
            when(securityContext.getAuthentication()).thenReturn(authentication);
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            AdminUserResponse response = adminUserService.updateUserStatus(2L, true);

            assertNotNull(response);
            assertTrue(response.isActive());
            verify(userRepository).save(targetUser);
            verify(eventPublisher, never()).publishEvent(any(UserStatusChangedEvent.class));
        }
    }


    @Test
    void updateUserStatus_selfLock_throwsException() {
        User targetUser = User.builder()
                .id(1L)
                .email("admin@test.com")
                .isActive(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(targetUser));

        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn("admin@test.com"); // Trùng email
            when(securityContext.getAuthentication()).thenReturn(authentication);
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            assertThrows(InvalidDataException.class, () -> adminUserService.updateUserStatus(1L, false));
            verify(userRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Test
    void updateUserStatus_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminUserService.updateUserStatus(99L, false));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserRole_success() {
        User targetUser = User.builder()
                .id(3L)
                .email("staff@test.com")
                .role(UserRole.customer)
                .build();

        when(userRepository.findById(3L)).thenReturn(Optional.of(targetUser));
        when(userRepository.save(any(User.class))).thenReturn(targetUser);

        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn("admin@test.com");
            when(securityContext.getAuthentication()).thenReturn(authentication);
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            AdminUserResponse response = adminUserService.updateUserRole(3L, UserRole.staff);

            assertNotNull(response);
            assertEquals(UserRole.staff, response.role());
            verify(userRepository).save(targetUser);
        }
    }

    @Test
    void updateUserRole_selfChange_throwsException() {
        User targetUser = User.builder()
                .id(1L)
                .email("admin@test.com")
                .role(UserRole.admin)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(targetUser));

        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn("admin@test.com");
            when(securityContext.getAuthentication()).thenReturn(authentication);
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            assertThrows(InvalidDataException.class, () -> adminUserService.updateUserRole(1L, UserRole.staff));
            verify(userRepository, never()).save(any());
        }
    }
}
