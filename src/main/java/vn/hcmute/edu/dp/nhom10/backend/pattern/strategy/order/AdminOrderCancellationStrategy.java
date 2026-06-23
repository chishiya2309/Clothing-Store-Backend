package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.order;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;

@Component
public class AdminOrderCancellationStrategy implements OrderCancellationStrategy {
    @Override
    public void cancel(Order order) {
        if (OrderStatus.completed.equals(order.getStatus())) {
            throw new InvalidDataException("Completed orders cannot be cancelled");
        }
        if (OrderStatus.cancelled.equals(order.getStatus())) {
            throw new InvalidDataException("Order is already cancelled");
        }
        order.setStatus(OrderStatus.cancelled);
    }
}
