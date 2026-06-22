package vn.hcmute.edu.dp.nhom10.backend.event;

import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;

import java.time.OffsetDateTime;

public record OrderStatusChangedEvent(
        Long orderId,
        String orderCode,
        Long customerId,
        String customerEmail,
        Long changedByStaffId,
        String changedByStaffEmail,
        OrderStatus fromStatus,
        OrderStatus toStatus,
        String reason,
        OffsetDateTime changedAt
) {
}
