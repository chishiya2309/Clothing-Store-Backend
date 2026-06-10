package vn.hcmute.edu.dp.nhom10.backend.pattern.observer.admin.listener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.hcmute.edu.dp.nhom10.backend.entity.ActivityLog;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.pattern.observer.admin.event.UserStatusChangedEvent;
import vn.hcmute.edu.dp.nhom10.backend.repository.ActivityLogRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.EmailService;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserStatusListenerTest {

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private UserStatusListener userStatusListener;

    @Test
    void handleAuditLog_lockUser_success() {
        UserStatusChangedEvent event = new UserStatusChangedEvent(this, 1L, "user@test.com", true, false);
        User adminUser = User.builder().id(2L).email("admin@test.com").build();

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));

        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn("admin@test.com");
            when(securityContext.getAuthentication()).thenReturn(authentication);
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            userStatusListener.handleAuditLog(event);

            ArgumentCaptor<ActivityLog> logCaptor = ArgumentCaptor.forClass(ActivityLog.class);
            verify(activityLogRepository).save(logCaptor.capture());

            ActivityLog savedLog = logCaptor.getValue();
            assertNotNull(savedLog);
            assertEquals("lock_user", savedLog.getAction());
            assertEquals("user", savedLog.getEntityType());
            assertEquals(1L, savedLog.getEntityId());
            assertEquals(adminUser, savedLog.getUser());
            assertEquals(true, savedLog.getOldData().get("isActive"));
            assertEquals(false, savedLog.getNewData().get("isActive"));
        }
    }

    @Test
    void handleAuditLog_unlockUser_success() {
        UserStatusChangedEvent event = new UserStatusChangedEvent(this, 1L, "user@test.com", false, true);
        User adminUser = User.builder().id(2L).email("admin@test.com").build();

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));

        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn("admin@test.com");
            when(securityContext.getAuthentication()).thenReturn(authentication);
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            userStatusListener.handleAuditLog(event);

            ArgumentCaptor<ActivityLog> logCaptor = ArgumentCaptor.forClass(ActivityLog.class);
            verify(activityLogRepository).save(logCaptor.capture());

            ActivityLog savedLog = logCaptor.getValue();
            assertNotNull(savedLog);
            assertEquals("unlock_user", savedLog.getAction());
            assertEquals(false, savedLog.getOldData().get("isActive"));
            assertEquals(true, savedLog.getNewData().get("isActive"));
        }
    }

    @Test
    void handleAuditLog_exceptionHandledGracefully() {
        UserStatusChangedEvent event = new UserStatusChangedEvent(this, 1L, "user@test.com", true, false);

        // This will trigger exception because SecurityContextHolder throws a NullPointerException
        // since we are not mocking it. We expect it to be caught and logged (meaning no exception thrown to caller).
        
        userStatusListener.handleAuditLog(event);
        
        verify(activityLogRepository, never()).save(any(ActivityLog.class));
    }

    @Test
    void handleSessionRevocation_lockUser_revokesSessions() {
        UserStatusChangedEvent event = new UserStatusChangedEvent(this, 1L, "user@test.com", true, false);
        Set<String> keys = Set.of("refresh_token:token123", "refresh_token:token456");

        when(redisTemplate.keys("refresh_token:*")).thenReturn(keys);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("refresh_token:token123")).thenReturn("1"); // locked user ID
        when(valueOperations.get("refresh_token:token456")).thenReturn("2"); // other user ID

        userStatusListener.handleSessionRevocation(event);

        verify(redisTemplate).delete("refresh_token:token123");
        verify(redisTemplate, never()).delete("refresh_token:token456");
    }

    @Test
    void handleSessionRevocation_unlockUser_doesNothing() {
        UserStatusChangedEvent event = new UserStatusChangedEvent(this, 1L, "user@test.com", false, true);

        userStatusListener.handleSessionRevocation(event);

        verify(redisTemplate, never()).keys(anyString());
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void handleSessionRevocation_exceptionHandledGracefully() {
        UserStatusChangedEvent event = new UserStatusChangedEvent(this, 1L, "user@test.com", true, false);

        when(redisTemplate.keys("refresh_token:*")).thenThrow(new RuntimeException("Redis connection error"));

        // No exception should escape
        userStatusListener.handleSessionRevocation(event);

        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void handleEmailNotification_success() {
        UserStatusChangedEvent event = new UserStatusChangedEvent(this, 1L, "user@test.com", true, false);
        User targetUser = User.builder().id(1L).email("user@test.com").fullName("Test User").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(targetUser));

        userStatusListener.handleEmailNotification(event);

        verify(emailService).sendAccountStatusEmail("user@test.com", "Test User", false);
    }

    @Test
    void handleEmailNotification_userNotFound_usesDefaultName() {
        UserStatusChangedEvent event = new UserStatusChangedEvent(this, 1L, "user@test.com", false, true);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        userStatusListener.handleEmailNotification(event);

        verify(emailService).sendAccountStatusEmail("user@test.com", "Khách hàng", true);
    }

    @Test
    void handleEmailNotification_exceptionHandledGracefully() {
        UserStatusChangedEvent event = new UserStatusChangedEvent(this, 1L, "user@test.com", true, false);

        when(userRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

        // No exception should be thrown by the listener method
        userStatusListener.handleEmailNotification(event);

        verify(emailService, never()).sendAccountStatusEmail(any(), any(), anyBoolean());
    }
}
