package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffOrderDetailResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffOrderListItemResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderStatusHistory;
import vn.hcmute.edu.dp.nhom10.backend.entity.Payment;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderStatusHistoryRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.StaffOrderServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffOrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @InjectMocks
    private StaffOrderServiceImpl staffOrderService;

    @Test
    void getOrders_mapsPageAndChoosesCompletedPayment() {
        Order order = order("ORD-1", 1L);
        OffsetDateTime older = OffsetDateTime.parse("2026-01-10T10:00:00+07:00");
        OffsetDateTime newer = OffsetDateTime.parse("2026-01-10T11:00:00+07:00");
        Payment latestFailed = payment(1L, order, PaymentMethod.vnpay, PaymentStatus.failed, newer);
        Payment completed = payment(2L, order, PaymentMethod.cod, PaymentStatus.completed, older);
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(order), PageRequest.of(0, 10), 1));
        when(paymentRepository.findAllByOrder_IdInOrderByCreatedAtDesc(List.of(1L)))
                .thenReturn(List.of(latestFailed, completed));

        PageResponse<StaffOrderListItemResponse> response = staffOrderService.getOrders(
                OrderStatus.pending,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                " ORD ",
                0,
                10,
                "createdAt",
                "desc"
        );

        assertEquals(0, response.getPageNumber());
        assertEquals(10, response.getPageSize());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        StaffOrderListItemResponse item = response.getContent().get(0);
        assertEquals("ORD-1", item.getOrderCode());
        assertEquals("Alice", item.getCustomerName());
        assertEquals("alice@test.com", item.getCustomerEmail());
        assertEquals("0909000001", item.getCustomerPhone());
        assertEquals(OrderStatus.pending, item.getStatus());
        assertEquals(PaymentMethod.cod, item.getPaymentMethod());
        assertEquals(PaymentStatus.completed, item.getPaymentStatus());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(orderRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(10, pageableCaptor.getValue().getPageSize());
        assertEquals("createdAt: DESC", pageableCaptor.getValue().getSort().toString());
    }

    @Test
    void getOrders_emptyPage_doesNotLoadPayments() {
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        PageResponse<StaffOrderListItemResponse> response = staffOrderService.getOrders(
                null,
                null,
                null,
                null,
                0,
                10,
                null,
                null
        );

        assertEquals(0, response.getContent().size());
        verify(paymentRepository, never()).findAllByOrder_IdInOrderByCreatedAtDesc(any());
    }

    @Test
    void getOrders_invalidDateRange_throwsBadRequestException() {
        assertThrows(IllegalArgumentException.class, () -> staffOrderService.getOrders(
                null,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 1, 1),
                null,
                0,
                10,
                "createdAt",
                "desc"
        ));
    }

    @Test
    void getOrders_invalidSort_throwsBadRequestException() {
        assertThrows(IllegalArgumentException.class, () -> staffOrderService.getOrders(
                null,
                null,
                null,
                null,
                0,
                10,
                "id",
                "desc"
        ));
    }

    @Test
    void getOrderDetail_mapsItemsPaymentAndSystemTimeline() {
        Order order = order(" ORD-1 ", 1L);
        order.setVoucher(Voucher.builder().id(3L).code("SAVE10").build());
        ProductVariant variant = ProductVariant.builder()
                .id(99L)
                .sku("SKU-99")
                .build();
        OrderItem item = OrderItem.builder()
                .id(8L)
                .order(order)
                .productVariant(variant)
                .productName("Snapshot Shirt")
                .variantInfo("Size M / Black")
                .quantity(2)
                .unitPrice(money("120000.00"))
                .subtotal(money("240000.00"))
                .build();
        Payment failed = payment(1L, order, PaymentMethod.vnpay, PaymentStatus.failed,
                OffsetDateTime.parse("2026-01-10T11:00:00+07:00"));
        Payment completed = payment(2L, order, PaymentMethod.momo, PaymentStatus.completed,
                OffsetDateTime.parse("2026-01-10T10:00:00+07:00"));
        OrderStatusHistory history = OrderStatusHistory.builder()
                .id(7L)
                .order(order)
                .fromStatus(null)
                .toStatus(OrderStatus.pending)
                .changedBy(null)
                .changedByRole(null)
                .createdAt(OffsetDateTime.parse("2026-01-10T09:00:00+07:00"))
                .build();
        when(orderRepository.findByOrderCode("ORD-1")).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrderIdWithVariantOrderById(1L)).thenReturn(List.of(item));
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of(failed, completed));
        when(orderStatusHistoryRepository.findAllByOrder_IdOrderByCreatedAtAscIdAsc(1L)).thenReturn(List.of(history));

        StaffOrderDetailResponse response = staffOrderService.getOrderDetail(" ORD-1 ");

        assertEquals("ORD-1", response.getOrderCode());
        assertEquals("Alice", response.getCustomerName());
        assertEquals("SAVE10", response.getVoucherCode());
        assertEquals(1, response.getItems().size());
        assertEquals(99L, response.getItems().get(0).getProductVariantId());
        assertEquals("SKU-99", response.getItems().get(0).getSku());
        assertEquals("Snapshot Shirt", response.getItems().get(0).getProductName());
        assertEquals(PaymentMethod.momo, response.getPayment().getMethod());
        assertEquals(PaymentStatus.completed, response.getPayment().getStatus());
        assertEquals(1, response.getTimeline().size());
        assertNull(response.getTimeline().get(0).getFromStatus());
        assertEquals(OrderStatus.pending, response.getTimeline().get(0).getToStatus());
        assertEquals("SYSTEM", response.getTimeline().get(0).getActorLabel());
    }

    @Test
    void getOrderDetail_staffHistoryUsesActorName() {
        Order order = order("ORD-1", 1L);
        User staff = User.builder()
                .id(5L)
                .fullName("Staff One")
                .email("staff@test.com")
                .role(UserRole.staff)
                .build();
        OrderStatusHistory history = OrderStatusHistory.builder()
                .id(7L)
                .order(order)
                .fromStatus(OrderStatus.pending)
                .toStatus(OrderStatus.processing)
                .changedBy(staff)
                .changedByRole(UserRole.staff)
                .createdAt(OffsetDateTime.parse("2026-01-10T09:00:00+07:00"))
                .build();
        when(orderRepository.findByOrderCode("ORD-1")).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrderIdWithVariantOrderById(1L)).thenReturn(List.of());
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of());
        when(orderStatusHistoryRepository.findAllByOrder_IdOrderByCreatedAtAscIdAsc(1L)).thenReturn(List.of(history));

        StaffOrderDetailResponse response = staffOrderService.getOrderDetail("ORD-1");

        assertEquals("Staff One", response.getTimeline().get(0).getActorLabel());
        assertEquals(UserRole.staff, response.getTimeline().get(0).getChangedByRole());
        assertEquals(5L, response.getTimeline().get(0).getChangedById());
    }

    @Test
    void getOrderDetail_unknownOrder_throwsNotFound() {
        when(orderRepository.findByOrderCode("ORD-404")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> staffOrderService.getOrderDetail(" ORD-404 "));
    }

    @Test
    void getOrderDetail_blankOrderCode_throwsBadRequestException() {
        assertThrows(IllegalArgumentException.class, () -> staffOrderService.getOrderDetail(" "));
    }

    private Order order(String orderCode, Long id) {
        return Order.builder()
                .id(id)
                .orderCode(orderCode.trim())
                .user(User.builder()
                        .id(10L)
                        .fullName("Alice")
                        .email("alice@test.com")
                        .phone("0909000001")
                        .build())
                .shippingName("Alice Shipping")
                .shippingPhone("0909000002")
                .shippingProvince("Ho Chi Minh")
                .shippingDistrict("Thu Duc")
                .shippingWard("Linh Trung")
                .shippingAddress("1 Vo Van Ngan")
                .subtotal(money("240000.00"))
                .shippingFee(money("20000.00"))
                .discountAmount(money("10000.00"))
                .totalAmount(money("250000.00"))
                .status(OrderStatus.pending)
                .createdAt(OffsetDateTime.parse("2026-01-10T09:00:00+07:00"))
                .updatedAt(OffsetDateTime.parse("2026-01-10T09:05:00+07:00"))
                .build();
    }

    private Payment payment(Long id, Order order, PaymentMethod method, PaymentStatus status, OffsetDateTime createdAt) {
        return Payment.builder()
                .id(id)
                .order(order)
                .method(method)
                .status(status)
                .amount(money("250000.00"))
                .transactionId("TXN-" + id)
                .paidAt(status == PaymentStatus.completed ? createdAt : null)
                .createdAt(createdAt)
                .build();
    }

    private BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
