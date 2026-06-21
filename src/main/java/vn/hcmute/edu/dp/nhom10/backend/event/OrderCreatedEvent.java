package vn.hcmute.edu.dp.nhom10.backend.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrderCreatedEvent(
        Long orderId,
        String orderCode,
        Long userId,
        BigDecimal totalAmount,
        OffsetDateTime occurredAt
) {
}
