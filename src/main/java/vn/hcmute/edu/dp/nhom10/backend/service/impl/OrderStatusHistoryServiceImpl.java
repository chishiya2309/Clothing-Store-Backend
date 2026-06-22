package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderStatusHistory;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderStatusHistoryRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.OrderStatusHistoryService;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderStatusHistoryServiceImpl implements OrderStatusHistoryService {

    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Override
    public void recordInitialStatus(Order order) {
        validateOrder(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(null)
                .toStatus(order.getStatus())
                .changedBy(null)
                .changedByRole(null)
                .reason(null)
                .metadata(null)
                .build();

        orderStatusHistoryRepository.save(history);
    }

    @Override
    public OrderStatusHistory recordTransition(
            Order order,
            OrderStatus fromStatus,
            OrderStatus toStatus,
            User changedBy,
            String reason,
            Map<String, Object> metadata
    ) {
        validateTransition(order, fromStatus, toStatus, changedBy);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedBy(changedBy)
                .changedByRole(changedBy.getRole())
                .reason(reason)
                .metadata(metadata)
                .build();

        return orderStatusHistoryRepository.save(history);
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order is required");
        }
        if (order.getId() == null) {
            throw new IllegalStateException("Order must be persisted before recording initial status");
        }
        if (order.getStatus() == null) {
            throw new IllegalStateException("Order status is required");
        }
    }

    private void validateTransition(Order order, OrderStatus fromStatus, OrderStatus toStatus, User changedBy) {
        validateOrderIdentity(order);
        if (fromStatus == null) {
            throw new IllegalArgumentException("From status is required");
        }
        if (toStatus == null) {
            throw new IllegalArgumentException("To status is required");
        }
        if (changedBy == null) {
            throw new IllegalArgumentException("Changed by user is required");
        }
        if (changedBy.getRole() == null) {
            throw new IllegalStateException("Changed by user role is required");
        }
    }

    private void validateOrderIdentity(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order is required");
        }
        if (order.getId() == null) {
            throw new IllegalStateException("Order must be persisted before recording status transition");
        }
    }
}
