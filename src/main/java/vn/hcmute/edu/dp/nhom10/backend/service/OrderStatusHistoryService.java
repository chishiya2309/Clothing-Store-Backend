package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderStatusHistory;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;

import java.util.Map;

public interface OrderStatusHistoryService {
    void recordInitialStatus(Order order);

    OrderStatusHistory recordTransition(
            Order order,
            OrderStatus fromStatus,
            OrderStatus toStatus,
            User changedBy,
            String reason,
            Map<String, Object> metadata
    );
}
