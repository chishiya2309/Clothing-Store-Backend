package vn.hcmute.edu.dp.nhom10.backend.policy;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.exception.OrderStateConflictException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderStatusTransitionPolicyTest {

    private final OrderStatusTransitionPolicy policy = new OrderStatusTransitionPolicy();

    @Test
    void pendingToProcessing_isValid() {
        assertDoesNotThrow(() -> policy.validate(OrderStatus.pending, OrderStatus.processing));
    }

    @Test
    void pendingToCancelled_isValid() {
        assertDoesNotThrow(() -> policy.validate(OrderStatus.pending, OrderStatus.cancelled));
    }

    @Test
    void processingToShipping_isValid() {
        assertDoesNotThrow(() -> policy.validate(OrderStatus.processing, OrderStatus.shipping));
    }

    @Test
    void processingToCancelled_isValid() {
        assertDoesNotThrow(() -> policy.validate(OrderStatus.processing, OrderStatus.cancelled));
    }

    @Test
    void shippingToCompleted_isValid() {
        assertDoesNotThrow(() -> policy.validate(OrderStatus.shipping, OrderStatus.completed));
    }

    @Test
    void pendingToShipping_isRejected() {
        assertThrows(OrderStateConflictException.class,
                () -> policy.validate(OrderStatus.pending, OrderStatus.shipping));
    }

    @Test
    void processingToCompleted_isRejected() {
        assertThrows(OrderStateConflictException.class,
                () -> policy.validate(OrderStatus.processing, OrderStatus.completed));
    }

    @Test
    void shippingToCancelled_isRejected() {
        assertThrows(OrderStateConflictException.class,
                () -> policy.validate(OrderStatus.shipping, OrderStatus.cancelled));
    }

    @Test
    void completedToPending_isRejected() {
        assertThrows(OrderStateConflictException.class,
                () -> policy.validate(OrderStatus.completed, OrderStatus.pending));
    }

    @Test
    void cancelledToPending_isRejected() {
        assertThrows(OrderStateConflictException.class,
                () -> policy.validate(OrderStatus.cancelled, OrderStatus.pending));
    }

    @Test
    void sameStateTransition_isRejected() {
        assertThrows(OrderStateConflictException.class,
                () -> policy.validate(OrderStatus.processing, OrderStatus.processing));
    }

    @Test
    void currentStatusNull_isRejectedAsBadRequest() {
        assertThrows(IllegalArgumentException.class,
                () -> policy.validate(null, OrderStatus.processing));
    }

    @Test
    void targetStatusNull_isRejectedAsBadRequest() {
        assertThrows(IllegalArgumentException.class,
                () -> policy.validate(OrderStatus.pending, null));
    }

    @Test
    void conflictMessage_usesRequiredFormat() {
        OrderStateConflictException exception = assertThrows(OrderStateConflictException.class,
                () -> policy.validate(OrderStatus.processing, OrderStatus.processing));

        assertEquals("Không thể chuyển từ trạng thái processing sang processing", exception.getMessage());
    }
}
