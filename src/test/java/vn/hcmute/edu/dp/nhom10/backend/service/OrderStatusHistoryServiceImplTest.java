package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderStatusHistory;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderStatusHistoryRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.OrderStatusHistoryServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OrderStatusHistoryServiceImplTest {

    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @InjectMocks
    private OrderStatusHistoryServiceImpl orderStatusHistoryService;

    @Test
    void recordInitialStatus_savesSystemPendingHistory() {
        Order order = Order.builder()
                .id(1L)
                .status(OrderStatus.pending)
                .build();

        orderStatusHistoryService.recordInitialStatus(order);

        ArgumentCaptor<OrderStatusHistory> captor = ArgumentCaptor.forClass(OrderStatusHistory.class);
        verify(orderStatusHistoryRepository).save(captor.capture());
        OrderStatusHistory history = captor.getValue();
        assertSame(order, history.getOrder());
        assertNull(history.getFromStatus());
        assertEquals(OrderStatus.pending, history.getToStatus());
        assertNull(history.getChangedBy());
        assertNull(history.getChangedByRole());
        assertNull(history.getReason());
        assertNull(history.getMetadata());
    }

    @Test
    void recordInitialStatus_nullOrder_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> orderStatusHistoryService.recordInitialStatus(null));

        verifyNoInteractions(orderStatusHistoryRepository);
    }

    @Test
    void recordInitialStatus_unsavedOrder_throwsException() {
        Order order = Order.builder()
                .status(OrderStatus.pending)
                .build();

        assertThrows(IllegalStateException.class, () -> orderStatusHistoryService.recordInitialStatus(order));

        verifyNoInteractions(orderStatusHistoryRepository);
    }

    @Test
    void recordInitialStatus_orderWithoutStatus_throwsException() {
        Order order = Order.builder()
                .id(1L)
                .build();

        assertThrows(IllegalStateException.class, () -> orderStatusHistoryService.recordInitialStatus(order));

        verifyNoInteractions(orderStatusHistoryRepository);
    }
}
