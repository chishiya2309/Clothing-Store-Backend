package vn.hcmute.edu.dp.nhom10.backend.pattern.observer.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStockRestorationObserver implements OrderCancellationObserver {
    private final ProductVariantRepository productVariantRepository;

    @Override
    public void onOrderCancelled(Order order) {
        log.info("Restoring stock for cancelled order: {}", order.getOrderCode());
        if (order.getOrderItems() != null) {
            for (OrderItem oi : order.getOrderItems()) {
                ProductVariant pv = oi.getProductVariant();
                if (pv != null) {
                    pv.setStockQuantity(pv.getStockQuantity() + oi.getQuantity());
                    productVariantRepository.save(pv);
                    log.info("Restored stock for variant id {}: +{}", pv.getId(), oi.getQuantity());
                }
            }
        }
    }
}
