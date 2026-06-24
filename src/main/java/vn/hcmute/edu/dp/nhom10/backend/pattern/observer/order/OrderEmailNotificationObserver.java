package vn.hcmute.edu.dp.nhom10.backend.pattern.observer.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.service.EmailService;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEmailNotificationObserver implements OrderCancellationObserver {
    private final EmailService emailService;

    @Override
    public void onOrderCancelled(Order order) {
        User user = order.getUser();
        if (user != null && user.getEmail() != null) {
            String toEmail = user.getEmail();
            String fullName = user.getFullName() != null ? user.getFullName() : "Khách hàng";
            log.info("Sending order cancellation email to {} for order {}", toEmail, order.getOrderCode());
            emailService.sendOrderCancellationEmail(toEmail, fullName, order.getOrderCode());
        } else {
            log.warn("Cannot send cancellation email, user email is null for order {}", order.getOrderCode());
        }
    }
}
