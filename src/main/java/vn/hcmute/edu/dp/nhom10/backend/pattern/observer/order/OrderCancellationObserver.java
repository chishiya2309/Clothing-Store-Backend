package vn.hcmute.edu.dp.nhom10.backend.pattern.observer.order;

import vn.hcmute.edu.dp.nhom10.backend.entity.Order;

public interface OrderCancellationObserver {
    void onOrderCancelled(Order order);
}
