package vn.hcmute.edu.dp.nhom10.backend.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;
import vn.hcmute.edu.dp.nhom10.backend.event.OrderStatusChangedEvent;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.EmailService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "ORDER-STATUS-EVENT-LISTENER")
public class OrderStatusChangedEventListener {

    private final EmailService emailService;
    private final UserRepository userRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        if (event.toStatus() != OrderStatus.cancelled) {
            return;
        }

        sendCustomerEmail(event);
        sendAdminEmails(event);
    }

    private void sendCustomerEmail(OrderStatusChangedEvent event) {
        if (event.customerEmail() == null || event.customerEmail().isBlank()) {
            log.warn("Cannot send cancellation email to customer because email is blank: orderCode={}", event.orderCode());
            return;
        }
        try {
            emailService.sendOrderCancellationEmailToCustomer(
                    event.customerEmail(),
                    event.orderCode(),
                    event.reason(),
                    event.requiresManualRefundReview()
            );
        } catch (Exception e) {
            log.error("Failed to send customer cancellation email: orderCode={}", event.orderCode(), e);
        }
    }

    private void sendAdminEmails(OrderStatusChangedEvent event) {
        List<User> admins = userRepository.findAllByRoleAndIsActiveTrue(UserRole.admin);
        Set<String> adminEmails = new LinkedHashSet<>();
        for (User admin : admins) {
            if (admin.getEmail() == null || admin.getEmail().isBlank()) {
                log.warn("Skipping admin cancellation email because admin email is blank: orderCode={}", event.orderCode());
                continue;
            }
            adminEmails.add(admin.getEmail());
        }
        if (adminEmails.isEmpty()) {
            log.warn("No active admin email found for cancellation notification: orderCode={}", event.orderCode());
            return;
        }

        for (String adminEmail : adminEmails) {
            try {
                emailService.sendOrderCancellationEmailToAdmin(
                        adminEmail,
                        event.orderCode(),
                        event.customerEmail(),
                        event.changedByStaffEmail(),
                        event.fromStatus() == null ? null : event.fromStatus().name(),
                        event.reason(),
                        event.paymentMethod() == null ? null : event.paymentMethod().name(),
                        event.paymentStatus() == null ? null : event.paymentStatus().name(),
                        event.paidAmount(),
                        event.requiresManualRefundReview()
                );
            } catch (Exception e) {
                log.error("Failed to send admin cancellation email: orderCode={}, adminEmail={}",
                        event.orderCode(), adminEmail, e);
            }
        }
    }
}
