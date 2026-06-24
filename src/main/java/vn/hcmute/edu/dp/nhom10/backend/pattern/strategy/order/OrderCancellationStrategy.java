package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.order;

import vn.hcmute.edu.dp.nhom10.backend.entity.Order;

public interface OrderCancellationStrategy {
    void cancel(Order order);
}
