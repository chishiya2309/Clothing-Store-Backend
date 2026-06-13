package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy;

import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;

public class PendingCancellationStrategy implements CancellationPolicy {
    @Override
    public boolean canCancel(Order order) {
        return OrderStatus.pending.equals(order.getStatus());
    }

    @Override
    public void cancel(Order order) {
        order.setStatus(OrderStatus.cancelled);
    }
}
