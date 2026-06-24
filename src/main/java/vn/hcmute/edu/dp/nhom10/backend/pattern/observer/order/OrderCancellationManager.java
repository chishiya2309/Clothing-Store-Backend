package vn.hcmute.edu.dp.nhom10.backend.pattern.observer.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderCancellationManager implements OrderCancellationSubject {
    private final List<OrderCancellationObserver> observers = new ArrayList<>();
    
    @Autowired(required = false)
    private List<OrderCancellationObserver> injectedObservers;

    @PostConstruct
    public void init() {
        if (injectedObservers != null) {
            for (OrderCancellationObserver obs : injectedObservers) {
                registerObserver(obs);
            }
        }
    }

    @Override
    public void registerObserver(OrderCancellationObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(OrderCancellationObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Order order) {
        for (OrderCancellationObserver observer : observers) {
            observer.onOrderCancelled(order);
        }
    }
}
