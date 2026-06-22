package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderStatusHistory;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderStatusHistoryRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.OrderStatusHistoryService;

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
}
