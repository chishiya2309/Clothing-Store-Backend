package vn.hcmute.edu.dp.nhom10.backend.pattern.observer.admin.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.entity.ActivityLog;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.pattern.observer.admin.event.UserStatusChangedEvent;
import vn.hcmute.edu.dp.nhom10.backend.repository.ActivityLogRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "USER-STATUS-LISTENER")
public class UserStatusListener {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final vn.hcmute.edu.dp.nhom10.backend.service.EmailService emailService;

    /**
     * Đồng bộ: Ghi log lịch sử hoạt động khi trạng thái User thay đổi.
     * Chạy trên cùng thread để đảm bảo lấy được thông tin người thực hiện từ SecurityContext.
     */
    @EventListener
    public void handleAuditLog(UserStatusChangedEvent event) {
        log.info("Handling status changed audit log for user id: {}", event.getUserId());
        try {
            // Lấy thông tin Admin thực hiện hành động từ SecurityContext
            String adminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            User admin = userRepository.findByEmail(adminEmail).orElse(null);

            Map<String, Object> oldData = new HashMap<>();
            oldData.put("isActive", event.getOldStatus());

            Map<String, Object> newData = new HashMap<>();
            newData.put("isActive", event.getNewStatus());

            ActivityLog auditLog = ActivityLog.builder()
                    .user(admin)
                    .action(Boolean.FALSE.equals(event.getNewStatus()) ? "lock_user" : "unlock_user")
                    .entityType("user")
                    .entityId(event.getUserId())
                    .oldData(oldData)
                    .newData(newData)
                    .build();

            activityLogRepository.save(auditLog);
            log.info("Audit log for user status change saved successfully.");
        } catch (Exception e) {
            log.error("Failed to save audit log for user status change", e);
        }
    }

    /**
     * Bất đồng bộ: Gửi email thông báo cho User khi tài khoản bị khóa/mở khóa.
     * Đóng vai trò là một Observer chạy ngầm để tối ưu hóa thời gian phản hồi API.
     */
    @Async
    @EventListener
    public void handleEmailNotification(UserStatusChangedEvent event) {
        log.info("Asynchronously handling email notification for user status change: {} -> {}", 
                event.getEmail(), event.getNewStatus());
        try {
            User targetUser = userRepository.findById(event.getUserId()).orElse(null);
            String fullName = targetUser != null ? targetUser.getFullName() : "Khách hàng";
            emailService.sendAccountStatusEmail(event.getEmail(), fullName, event.getNewStatus());
            log.info("Notification email queued for user: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send notification email for user: {}", event.getEmail(), e);
        }
    }
}
