package vn.hcmute.edu.dp.nhom10.backend.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.event.OrderCancelledEvent;
import vn.hcmute.edu.dp.nhom10.backend.service.EmailService;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancellationEmailListener {

    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmailNotification(OrderCancelledEvent event) {
        Order order = event.order();
        log.info("Bắt đầu gửi email thông báo hủy đơn {} cho khách hàng {}", order.getOrderCode(), order.getUser().getEmail());
        try {
            emailService.sendOrderCancelledEmail(
                    order.getUser().getEmail(),
                    order.getUser().getFullName(),
                    order.getOrderCode()
            );
        } catch (Exception e) {
            // Chỉ ghi log lỗi gửi email, không làm rollback transaction hủy đơn
            log.error("Lỗi khi gửi email thông báo hủy đơn {}: {}", order.getOrderCode(), e.getMessage());
        }
    }
}
