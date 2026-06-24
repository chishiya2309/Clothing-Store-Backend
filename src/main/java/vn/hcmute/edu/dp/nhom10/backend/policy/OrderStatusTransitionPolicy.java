package vn.hcmute.edu.dp.nhom10.backend.policy;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.exception.OrderStateConflictException;

import java.util.Map;
import java.util.Set;

@Component
public class OrderStatusTransitionPolicy {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.pending, Set.of(OrderStatus.processing, OrderStatus.cancelled),
            OrderStatus.processing, Set.of(OrderStatus.shipping, OrderStatus.cancelled),
            OrderStatus.shipping, Set.of(OrderStatus.completed),
            OrderStatus.completed, Set.of(),
            OrderStatus.cancelled, Set.of()
    );

    public void validate(OrderStatus currentStatus, OrderStatus targetStatus) {
        if (currentStatus == null) {
            throw new IllegalArgumentException("Current order status is required");
        }
        if (targetStatus == null) {
            throw new IllegalArgumentException("Target order status is required");
        }
        if (!ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(targetStatus)) {
            throw new OrderStateConflictException(
                    "Không thể chuyển từ trạng thái " + currentStatus + " sang " + targetStatus
            );
        }
    }
}
