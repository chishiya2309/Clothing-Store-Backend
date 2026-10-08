package vn.hcmute.edu.dp.nhom10.backend.pattern.observer.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.service.OrderVoucherAdjustmentService;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderVoucherRestorationObserver implements OrderCancellationObserver {
    private final OrderVoucherAdjustmentService orderVoucherAdjustmentService;

    @Override
    public void onOrderCancelled(Order order) {
        log.info("Restoring voucher usage for cancelled order: {}", order.getOrderCode());
        orderVoucherAdjustmentService.restoreVoucherUsageForCancelledOrder(order);
    }
}
