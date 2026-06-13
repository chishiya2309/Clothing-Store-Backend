package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.exception.OrderCancellationException;

@Component
public class CancellationPolicyFactory {
    
    public CancellationPolicy getPolicy(OrderStatus status) {
        if (OrderStatus.pending.equals(status)) {
            return new PendingCancellationStrategy();
        }
        throw new OrderCancellationException("Không thể hủy đơn hàng ở trạng thái: " + status);
    }
}
