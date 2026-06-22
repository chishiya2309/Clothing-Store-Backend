package vn.hcmute.edu.dp.nhom10.backend.event;

import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentStatus;

import java.math.BigDecimal;
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
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        BigDecimal paidAmount,
        boolean requiresManualRefundReview,
        OffsetDateTime changedAt
) {
    public OrderStatusChangedEvent(
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
        this(
                orderId,
                orderCode,
                customerId,
                customerEmail,
                changedByStaffId,
                changedByStaffEmail,
                fromStatus,
                toStatus,
                reason,
                null,
                null,
                null,
                false,
                changedAt
        );
    }
}
