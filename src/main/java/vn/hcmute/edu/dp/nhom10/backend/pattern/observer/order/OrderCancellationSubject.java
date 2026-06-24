package vn.hcmute.edu.dp.nhom10.backend.pattern.observer.order;

import vn.hcmute.edu.dp.nhom10.backend.entity.Order;

public interface OrderCancellationSubject {
    void registerObserver(OrderCancellationObserver observer);
    void removeObserver(OrderCancellationObserver observer);
    void notifyObservers(Order order);
}
