package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.order;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;

@Component
public class CustomerOrderCancellationStrategy implements OrderCancellationStrategy {
    @Override
    public void cancel(Order order) {
        if (!OrderStatus.pending.equals(order.getStatus())) {
            throw new InvalidDataException("Only pending orders can be cancelled by the customer");
        }
        order.setStatus(OrderStatus.cancelled);
    }
}
