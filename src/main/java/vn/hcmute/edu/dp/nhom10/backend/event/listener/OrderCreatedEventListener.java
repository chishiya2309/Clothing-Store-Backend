package vn.hcmute.edu.dp.nhom10.backend.event.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.hcmute.edu.dp.nhom10.backend.event.OrderCreatedEvent;

@Component
@Slf4j(topic = "ORDER-CREATED-LISTENER")
public class OrderCreatedEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        try {
            log.info("Order created event received after commit: orderId={}, orderCode={}",
                    event.orderId(),
                    event.orderCode());
        } catch (RuntimeException e) {
            log.error("Failed to handle order created event for orderCode={}", event.orderCode(), e);
        }
    }
}
