package vn.hcmute.edu.dp.nhom10.backend.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.OrderInventoryAdjustmentServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderInventoryAdjustmentServiceImplTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private OrderInventoryAdjustmentServiceImpl service;

    @Test
    void restoreInventoryForCancelledOrder_aggregatesDuplicateVariantsAndLocksInVariantIdOrder() {
        Order order = Order.builder().id(1L).build();
        ProductVariant variant2 = ProductVariant.builder().id(2L).stockQuantity(3).build();
        ProductVariant variant5 = ProductVariant.builder().id(5L).stockQuantity(10).build();
        ProductVariant variant7 = ProductVariant.builder().id(7L).stockQuantity(null).build();
        when(orderItemRepository.findAllByOrderIdWithVariantOrderById(1L)).thenReturn(List.of(
                item(3L, variant5, 2),
                item(1L, variant2, 4),
                item(2L, variant5, 1),
                item(4L, variant7, 6)
        ));
        when(productVariantRepository.findAllByIdInForUpdate(List.of(2L, 5L, 7L)))
                .thenReturn(List.of(variant2, variant5, variant7));

        service.restoreInventoryForCancelledOrder(order);

        assertEquals(7, variant2.getStockQuantity());
        assertEquals(13, variant5.getStockQuantity());
        assertEquals(6, variant7.getStockQuantity());
        verify(productVariantRepository).findAllByIdInForUpdate(List.of(2L, 5L, 7L));
        verify(entityManager).refresh(variant2);
        verify(entityManager).refresh(variant5);
        verify(entityManager).refresh(variant7);
    }

    @Test
    void restoreInventoryForCancelledOrder_withoutItemsDoesNotLockVariants() {
        Order order = Order.builder().id(1L).build();
        when(orderItemRepository.findAllByOrderIdWithVariantOrderById(1L)).thenReturn(List.of());

        service.restoreInventoryForCancelledOrder(order);

        verify(productVariantRepository, never()).findAllByIdInForUpdate(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void restoreInventoryForCancelledOrder_missingVariantThrowsBeforeLocking() {
        Order order = Order.builder().id(1L).build();
        when(orderItemRepository.findAllByOrderIdWithVariantOrderById(1L)).thenReturn(List.of(
                OrderItem.builder().id(1L).quantity(1).productVariant(null).build()
        ));

        assertThrows(IllegalStateException.class, () -> service.restoreInventoryForCancelledOrder(order));

        verify(productVariantRepository, never()).findAllByIdInForUpdate(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void restoreInventoryForCancelledOrder_nonPositiveQuantityThrows() {
        Order order = Order.builder().id(1L).build();
        ProductVariant variant = ProductVariant.builder().id(2L).stockQuantity(3).build();
        when(orderItemRepository.findAllByOrderIdWithVariantOrderById(1L)).thenReturn(List.of(
                item(1L, variant, 0)
        ));

        assertThrows(IllegalStateException.class, () -> service.restoreInventoryForCancelledOrder(order));

        verify(productVariantRepository, never()).findAllByIdInForUpdate(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void restoreInventoryForCancelledOrder_missingLockedVariantThrows() {
        Order order = Order.builder().id(1L).build();
        ProductVariant variant = ProductVariant.builder().id(2L).stockQuantity(3).build();
        when(orderItemRepository.findAllByOrderIdWithVariantOrderById(1L)).thenReturn(List.of(
                item(1L, variant, 1)
        ));
        when(productVariantRepository.findAllByIdInForUpdate(List.of(2L))).thenReturn(List.of());

        assertThrows(IllegalStateException.class, () -> service.restoreInventoryForCancelledOrder(order));
    }

    private OrderItem item(Long id, ProductVariant variant, Integer quantity) {
        return OrderItem.builder()
                .id(id)
                .productVariant(variant)
                .quantity(quantity)
                .build();
    }
}
