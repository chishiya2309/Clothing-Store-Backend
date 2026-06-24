package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.OrderInventoryAdjustmentService;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderInventoryAdjustmentServiceImpl implements OrderInventoryAdjustmentService {

    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void restoreInventoryForCancelledOrder(Order order) {
        validateOrder(order);

        List<OrderItem> orderItems = orderItemRepository.findAllByOrderIdWithVariantOrderById(order.getId());
        Map<Long, Integer> quantityByVariantId = aggregateQuantityByVariantId(orderItems);
        if (quantityByVariantId.isEmpty()) {
            return;
        }

        List<Long> sortedVariantIds = quantityByVariantId.keySet().stream()
                .sorted()
                .toList();
        List<ProductVariant> lockedVariants = productVariantRepository.findAllByIdInForUpdate(sortedVariantIds);
        Map<Long, ProductVariant> variantsById = lockedVariants.stream()
                .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));
        if (variantsById.size() != sortedVariantIds.size()) {
            throw new IllegalStateException("Cannot restore inventory because one or more product variants are missing");
        }

        for (Long variantId : sortedVariantIds) {
            ProductVariant variant = variantsById.get(variantId);
            entityManager.refresh(variant);
            int currentStock = Objects.requireNonNullElse(variant.getStockQuantity(), 0);
            variant.setStockQuantity(currentStock + quantityByVariantId.get(variantId));
        }
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order is required");
        }
        if (order.getId() == null) {
            throw new IllegalStateException("Order must be persisted before restoring inventory");
        }
    }

    private Map<Long, Integer> aggregateQuantityByVariantId(List<OrderItem> orderItems) {
        return orderItems.stream()
                .sorted(Comparator.comparing(OrderItem::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.groupingBy(
                        this::variantId,
                        LinkedHashMap::new,
                        Collectors.summingInt(this::quantity)
                ));
    }

    private Long variantId(OrderItem orderItem) {
        if (orderItem.getProductVariant() == null || orderItem.getProductVariant().getId() == null) {
            throw new IllegalStateException("Order item product variant is required before restoring inventory");
        }
        return orderItem.getProductVariant().getId();
    }

    private int quantity(OrderItem orderItem) {
        if (orderItem.getQuantity() == null || orderItem.getQuantity() <= 0) {
            throw new IllegalStateException("Order item quantity must be positive before restoring inventory");
        }
        return orderItem.getQuantity();
    }
}
