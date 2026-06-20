package vn.hcmute.edu.dp.nhom10.backend.event;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.event.listener.OrderCreatedEventListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class OrderCreatedEventListenerTest {

    @Test
    void handleOrderCreated_validEvent_doesNotThrow() {
        OrderCreatedEventListener listener = new OrderCreatedEventListener();
        OrderCreatedEvent event = new OrderCreatedEvent(
                1L,
                "ORD-1",
                10L,
                new BigDecimal("120000.00"),
                OffsetDateTime.now()
        );

        assertDoesNotThrow(() -> listener.handleOrderCreated(event));
    }
}
