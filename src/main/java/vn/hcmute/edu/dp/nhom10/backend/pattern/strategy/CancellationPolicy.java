package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy;

import vn.hcmute.edu.dp.nhom10.backend.entity.Order;

public interface CancellationPolicy {
    boolean canCancel(Order order);
    void cancel(Order order);
}
