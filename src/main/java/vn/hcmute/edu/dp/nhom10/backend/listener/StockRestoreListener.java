package vn.hcmute.edu.dp.nhom10.backend.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.event.OrderCancelledEvent;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockRestoreListener {

    private final ProductVariantRepository productVariantRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStockRestore(OrderCancelledEvent event) {
        log.info("Khôi phục tồn kho cho đơn hàng bị hủy: {}", event.order().getOrderCode());
        for (OrderItem item : event.order().getOrderItems()) {
            ProductVariant variant = item.getProductVariant();
            int currentStock = variant.getStockQuantity();
            int restoredStock = currentStock + item.getQuantity();
            variant.setStockQuantity(restoredStock);
            productVariantRepository.save(variant);
            log.info("Biến thể ID {}: Khôi phục từ {} -> {}", variant.getId(), currentStock, restoredStock);
        }
    }
}
