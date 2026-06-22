package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCompleteOrderRequest;
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
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderCompletionSource;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;
import vn.hcmute.edu.dp.nhom10.backend.event.OrderStatusChangedEvent;
import vn.hcmute.edu.dp.nhom10.backend.exception.OrderStateConflictException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.policy.OrderStatusTransitionPolicy;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderStatusHistoryRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.LoyaltyPointAwardResult;
import vn.hcmute.edu.dp.nhom10.backend.service.LoyaltyPointService;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.StaffOrderServiceImpl;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
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

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderStatusHistoryService orderStatusHistoryService;

    @Mock
    private OrderStatusTransitionPolicy orderStatusTransitionPolicy;

    @Mock
    private LoyaltyPointService loyaltyPointService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private Clock clock;

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

    @Test
    void confirmOrder_success_locksValidatesRecordsHistoryPublishesEventAndReturnsDetail() {
        Order order = order("ORD-1", 1L);
        User staff = staff();
        OrderStatusHistory initialHistory = history(1L, order, null, OrderStatus.pending, null);
        OrderStatusHistory confirmHistory = history(2L, order, OrderStatus.pending, OrderStatus.processing, staff);
        when(userRepository.findById(5L)).thenReturn(Optional.of(staff));
        when(orderRepository.findByOrderCodeForUpdate("ORD-1")).thenReturn(Optional.of(order));
        when(orderStatusHistoryRepository.findAllByOrder_IdOrderByCreatedAtAscIdAsc(1L))
                .thenReturn(List.of(initialHistory, confirmHistory));
        when(orderItemRepository.findAllByOrderIdWithVariantOrderById(1L)).thenReturn(List.of());
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of());

        StaffOrderDetailResponse response = staffOrderService.confirmOrder(" ORD-1 ", 5L);

        assertEquals(OrderStatus.processing, order.getStatus());
        assertEquals(OrderStatus.processing, response.getStatus());
        assertEquals(2, response.getTimeline().size());
        InOrder inOrder = inOrder(
                userRepository,
                orderRepository,
                orderStatusTransitionPolicy,
                orderStatusHistoryService,
                eventPublisher
        );
        inOrder.verify(userRepository).findById(5L);
        inOrder.verify(orderRepository).findByOrderCodeForUpdate("ORD-1");
        inOrder.verify(orderStatusTransitionPolicy).validate(OrderStatus.pending, OrderStatus.processing);
        inOrder.verify(orderStatusHistoryService).recordTransition(
                order,
                OrderStatus.pending,
                OrderStatus.processing,
                staff,
                null,
                null
        );
        ArgumentCaptor<OrderStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(OrderStatusChangedEvent.class);
        inOrder.verify(eventPublisher).publishEvent(eventCaptor.capture());
        OrderStatusChangedEvent event = eventCaptor.getValue();
        assertEquals(1L, event.orderId());
        assertEquals("ORD-1", event.orderCode());
        assertEquals(10L, event.customerId());
        assertEquals("alice@test.com", event.customerEmail());
        assertEquals(5L, event.changedByStaffId());
        assertEquals("staff@test.com", event.changedByStaffEmail());
        assertEquals(OrderStatus.pending, event.fromStatus());
        assertEquals(OrderStatus.processing, event.toStatus());
    }

    @Test
    void shipOrder_success_movesProcessingToShipping() {
        Order order = order("ORD-1", 1L);
        order.setStatus(OrderStatus.processing);
        User staff = staff();
        when(userRepository.findById(5L)).thenReturn(Optional.of(staff));
        when(orderRepository.findByOrderCodeForUpdate("ORD-1")).thenReturn(Optional.of(order));
        when(orderStatusHistoryRepository.findAllByOrder_IdOrderByCreatedAtAscIdAsc(1L)).thenReturn(List.of(
                history(1L, order, null, OrderStatus.pending, null),
                history(2L, order, OrderStatus.pending, OrderStatus.processing, staff),
                history(3L, order, OrderStatus.processing, OrderStatus.shipping, staff)
        ));
        when(orderItemRepository.findAllByOrderIdWithVariantOrderById(1L)).thenReturn(List.of());
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of());

        StaffOrderDetailResponse response = staffOrderService.shipOrder("ORD-1", 5L);

        assertEquals(OrderStatus.shipping, order.getStatus());
        assertEquals(OrderStatus.shipping, response.getStatus());
        verify(orderStatusTransitionPolicy).validate(OrderStatus.processing, OrderStatus.shipping);
        verify(orderStatusHistoryService).recordTransition(
                order,
                OrderStatus.processing,
                OrderStatus.shipping,
                staff,
                null,
                null
        );
        verify(eventPublisher).publishEvent(any(OrderStatusChangedEvent.class));
    }

    @Test
    void confirmOrder_invalidCurrentStatus_doesNotRecordHistoryOrPublishEvent() {
        Order order = order("ORD-1", 1L);
        order.setStatus(OrderStatus.processing);
        User staff = staff();
        when(userRepository.findById(5L)).thenReturn(Optional.of(staff));
        when(orderRepository.findByOrderCodeForUpdate("ORD-1")).thenReturn(Optional.of(order));
        doThrow(new OrderStateConflictException("Không thể chuyển từ trạng thái processing sang processing"))
                .when(orderStatusTransitionPolicy).validate(OrderStatus.processing, OrderStatus.processing);

        assertThrows(OrderStateConflictException.class, () -> staffOrderService.confirmOrder("ORD-1", 5L));

        assertEquals(OrderStatus.processing, order.getStatus());
        verify(orderStatusHistoryService, never()).recordTransition(any(), any(), any(), any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shipOrder_invalidCurrentStatus_doesNotRecordHistoryOrPublishEvent() {
        Order order = order("ORD-1", 1L);
        User staff = staff();
        when(userRepository.findById(5L)).thenReturn(Optional.of(staff));
        when(orderRepository.findByOrderCodeForUpdate("ORD-1")).thenReturn(Optional.of(order));
        doThrow(new OrderStateConflictException("Không thể chuyển từ trạng thái pending sang shipping"))
                .when(orderStatusTransitionPolicy).validate(OrderStatus.pending, OrderStatus.shipping);

        assertThrows(OrderStateConflictException.class, () -> staffOrderService.shipOrder("ORD-1", 5L));

        assertEquals(OrderStatus.pending, order.getStatus());
        verify(orderStatusHistoryService, never()).recordTransition(any(), any(), any(), any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void completeOrder_shippingCodPending_updatesPaymentAwardsLoyaltyRecordsHistoryPublishesEventAndReturnsDetail() {
        stubClock();
        Order order = order("ORD-1", 1L);
        order.setStatus(OrderStatus.shipping);
        User staff = staff();
        Payment codPayment = payment(10L, order, PaymentMethod.cod, PaymentStatus.pending,
                OffsetDateTime.parse("2026-01-10T09:00:00+07:00"));
        LoyaltyPointAwardResult loyaltyResult = new LoyaltyPointAwardResult(
                250,
                100,
                350,
                "Äá»“ng",
                "Äá»“ng",
                false
        );
        Map<String, Object> metadata = Map.of("confirmationSource", "shipping_partner");
        OrderStatusHistory completeHistory = OrderStatusHistory.builder()
                .id(4L)
                .order(order)
                .fromStatus(OrderStatus.shipping)
                .toStatus(OrderStatus.completed)
                .changedBy(staff)
                .changedByRole(UserRole.staff)
                .reason("GHN confirmed")
                .metadata(metadata)
                .createdAt(OffsetDateTime.parse("2026-01-10T09:20:00+07:00"))
                .build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(staff));
        when(orderRepository.findByOrderCodeForUpdate("ORD-1")).thenReturn(Optional.of(order));
        when(paymentRepository.findAllByOrderIdForUpdate(1L)).thenReturn(List.of(codPayment));
        when(loyaltyPointService.awardForCompletedOrder(order)).thenReturn(loyaltyResult);
        when(orderItemRepository.findAllByOrderIdWithVariantOrderById(1L)).thenReturn(List.of());
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of(codPayment));
        when(orderStatusHistoryRepository.findAllByOrder_IdOrderByCreatedAtAscIdAsc(1L))
                .thenReturn(List.of(completeHistory));

        StaffOrderDetailResponse response = staffOrderService.completeOrder(" ORD-1 ", 5L, completeRequest());

        assertEquals(OrderStatus.completed, order.getStatus());
        assertEquals(PaymentStatus.completed, codPayment.getStatus());
        assertEquals(fixedNow(), codPayment.getPaidAt());
        assertEquals(OrderStatus.completed, response.getStatus());
        assertEquals(PaymentStatus.completed, response.getPayment().getStatus());
        assertEquals("GHN confirmed", response.getTimeline().get(0).getReason());
        assertEquals("shipping_partner", response.getTimeline().get(0).getMetadata().get("confirmationSource"));
        InOrder inOrder = inOrder(
                userRepository,
                orderRepository,
                orderStatusTransitionPolicy,
                paymentRepository,
                loyaltyPointService,
                orderStatusHistoryService,
                eventPublisher
        );
        inOrder.verify(userRepository).findById(5L);
        inOrder.verify(orderRepository).findByOrderCodeForUpdate("ORD-1");
        inOrder.verify(orderStatusTransitionPolicy).validate(OrderStatus.shipping, OrderStatus.completed);
        inOrder.verify(paymentRepository).findAllByOrderIdForUpdate(1L);
        inOrder.verify(loyaltyPointService).awardForCompletedOrder(order);
        ArgumentCaptor<Map<String, Object>> metadataCaptor = ArgumentCaptor.forClass(Map.class);
        inOrder.verify(orderStatusHistoryService).recordTransition(
                eq(order),
                eq(OrderStatus.shipping),
                eq(OrderStatus.completed),
                eq(staff),
                eq("GHN confirmed"),
                metadataCaptor.capture()
        );
        assertEquals("shipping_partner", metadataCaptor.getValue().get("confirmationSource"));
        assertEquals(true, metadataCaptor.getValue().get("codPaymentCompleted"));
        assertEquals(250, metadataCaptor.getValue().get("loyaltyPointsAwarded"));
        ArgumentCaptor<OrderStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(OrderStatusChangedEvent.class);
        inOrder.verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(OrderStatus.shipping, eventCaptor.getValue().fromStatus());
        assertEquals(OrderStatus.completed, eventCaptor.getValue().toStatus());
        assertEquals("GHN confirmed", eventCaptor.getValue().reason());
        assertEquals(fixedNow(), eventCaptor.getValue().changedAt());
    }

    @Test
    void completeOrder_onlineCompletedPayment_isNotModified() {
        stubClock();
        Order order = order("ORD-1", 1L);
        order.setStatus(OrderStatus.shipping);
        User staff = staff();
        OffsetDateTime paidAt = OffsetDateTime.parse("2026-01-10T08:00:00+07:00");
        Payment vnpayPayment = payment(10L, order, PaymentMethod.vnpay, PaymentStatus.completed, paidAt);
        when(userRepository.findById(5L)).thenReturn(Optional.of(staff));
        when(orderRepository.findByOrderCodeForUpdate("ORD-1")).thenReturn(Optional.of(order));
        when(paymentRepository.findAllByOrderIdForUpdate(1L)).thenReturn(List.of(vnpayPayment));
        when(loyaltyPointService.awardForCompletedOrder(order)).thenReturn(loyaltyResult());
        when(orderItemRepository.findAllByOrderIdWithVariantOrderById(1L)).thenReturn(List.of());
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of(vnpayPayment));
        when(orderStatusHistoryRepository.findAllByOrder_IdOrderByCreatedAtAscIdAsc(1L)).thenReturn(List.of());

        staffOrderService.completeOrder("ORD-1", 5L, completeRequest());

        assertEquals(PaymentStatus.completed, vnpayPayment.getStatus());
        assertEquals(paidAt, vnpayPayment.getPaidAt());
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"pending", "processing", "completed", "cancelled"})
    void completeOrder_invalidTransition_doesNotUpdatePaymentAwardLoyaltyRecordHistoryOrPublishEvent(OrderStatus status) {
        stubClock();
        Order order = order("ORD-1", 1L);
        order.setStatus(status);
        Payment codPayment = payment(10L, order, PaymentMethod.cod, PaymentStatus.pending,
                OffsetDateTime.parse("2026-01-10T09:00:00+07:00"));
        User staff = staff();
        when(userRepository.findById(5L)).thenReturn(Optional.of(staff));
        when(orderRepository.findByOrderCodeForUpdate("ORD-1")).thenReturn(Optional.of(order));
        doThrow(new OrderStateConflictException("KhÃ´ng thá»ƒ chuyá»ƒn tá»« tráº¡ng thÃ¡i " + status + " sang completed"))
                .when(orderStatusTransitionPolicy).validate(status, OrderStatus.completed);

        assertThrows(OrderStateConflictException.class,
                () -> staffOrderService.completeOrder("ORD-1", 5L, completeRequest()));

        assertEquals(status, order.getStatus());
        assertEquals(PaymentStatus.pending, codPayment.getStatus());
        verify(paymentRepository, never()).findAllByOrderIdForUpdate(any());
        verify(loyaltyPointService, never()).awardForCompletedOrder(any());
        verify(orderStatusHistoryService, never()).recordTransition(any(), any(), any(), any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus.class, names = {"failed", "refunded"})
    void completeOrder_codFailedOrRefunded_conflictsBeforeLoyaltyHistoryAndEvent(PaymentStatus paymentStatus) {
        stubClock();
        Order order = order("ORD-1", 1L);
        order.setStatus(OrderStatus.shipping);
        User staff = staff();
        Payment codPayment = payment(10L, order, PaymentMethod.cod, paymentStatus,
                OffsetDateTime.parse("2026-01-10T09:00:00+07:00"));
        when(userRepository.findById(5L)).thenReturn(Optional.of(staff));
        when(orderRepository.findByOrderCodeForUpdate("ORD-1")).thenReturn(Optional.of(order));
        when(paymentRepository.findAllByOrderIdForUpdate(1L)).thenReturn(List.of(codPayment));

        assertThrows(OrderStateConflictException.class,
                () -> staffOrderService.completeOrder("ORD-1", 5L, completeRequest()));

        assertEquals(OrderStatus.shipping, order.getStatus());
        assertEquals(paymentStatus, codPayment.getStatus());
        verify(loyaltyPointService, never()).awardForCompletedOrder(any());
        verify(orderStatusHistoryService, never()).recordTransition(any(), any(), any(), any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void completeOrder_withoutAnyPayment_conflictsBeforeLoyaltyHistoryAndEvent() {
        stubClock();
        Order order = order("ORD-1", 1L);
        order.setStatus(OrderStatus.shipping);
        User staff = staff();
        when(userRepository.findById(5L)).thenReturn(Optional.of(staff));
        when(orderRepository.findByOrderCodeForUpdate("ORD-1")).thenReturn(Optional.of(order));
        when(paymentRepository.findAllByOrderIdForUpdate(1L)).thenReturn(List.of());

        assertThrows(OrderStateConflictException.class,
                () -> staffOrderService.completeOrder("ORD-1", 5L, completeRequest()));

        assertEquals(OrderStatus.shipping, order.getStatus());
        verify(loyaltyPointService, never()).awardForCompletedOrder(any());
        verify(orderStatusHistoryService, never()).recordTransition(any(), any(), any(), any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void completeOrder_codAlreadyCompleted_doesNotOverwritePaidAt() {
        stubClock();
        Order order = order("ORD-1", 1L);
        order.setStatus(OrderStatus.shipping);
        User staff = staff();
        OffsetDateTime originalPaidAt = OffsetDateTime.parse("2026-01-10T08:00:00+07:00");
        Payment codPayment = payment(10L, order, PaymentMethod.cod, PaymentStatus.completed, originalPaidAt);
        when(userRepository.findById(5L)).thenReturn(Optional.of(staff));
        when(orderRepository.findByOrderCodeForUpdate("ORD-1")).thenReturn(Optional.of(order));
        when(paymentRepository.findAllByOrderIdForUpdate(1L)).thenReturn(List.of(codPayment));
        when(loyaltyPointService.awardForCompletedOrder(order)).thenReturn(loyaltyResult());
        when(orderItemRepository.findAllByOrderIdWithVariantOrderById(1L)).thenReturn(List.of());
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of(codPayment));
        when(orderStatusHistoryRepository.findAllByOrder_IdOrderByCreatedAtAscIdAsc(1L)).thenReturn(List.of());

        staffOrderService.completeOrder("ORD-1", 5L, completeRequest());

        assertEquals(PaymentStatus.completed, codPayment.getStatus());
        assertEquals(originalPaidAt, codPayment.getPaidAt());
    }

    @Test
    void completeOrder_loyaltyFailure_doesNotSetCompletedOrRecordHistoryOrPublishEvent() {
        stubClock();
        Order order = order("ORD-1", 1L);
        order.setStatus(OrderStatus.shipping);
        User staff = staff();
        Payment codPayment = payment(10L, order, PaymentMethod.cod, PaymentStatus.pending,
                OffsetDateTime.parse("2026-01-10T09:00:00+07:00"));
        when(userRepository.findById(5L)).thenReturn(Optional.of(staff));
        when(orderRepository.findByOrderCodeForUpdate("ORD-1")).thenReturn(Optional.of(order));
        when(paymentRepository.findAllByOrderIdForUpdate(1L)).thenReturn(List.of(codPayment));
        doThrow(new RuntimeException("Cannot award points"))
                .when(loyaltyPointService).awardForCompletedOrder(order);

        assertThrows(RuntimeException.class,
                () -> staffOrderService.completeOrder("ORD-1", 5L, completeRequest()));

        assertEquals(OrderStatus.shipping, order.getStatus());
        verify(orderStatusHistoryService, never()).recordTransition(any(), any(), any(), any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void completeOrder_historyFailure_doesNotPublishEvent() {
        stubClock();
        Order order = order("ORD-1", 1L);
        order.setStatus(OrderStatus.shipping);
        User staff = staff();
        Payment codPayment = payment(10L, order, PaymentMethod.cod, PaymentStatus.pending,
                OffsetDateTime.parse("2026-01-10T09:00:00+07:00"));
        when(userRepository.findById(5L)).thenReturn(Optional.of(staff));
        when(orderRepository.findByOrderCodeForUpdate("ORD-1")).thenReturn(Optional.of(order));
        when(paymentRepository.findAllByOrderIdForUpdate(1L)).thenReturn(List.of(codPayment));
        when(loyaltyPointService.awardForCompletedOrder(order)).thenReturn(loyaltyResult());
        doThrow(new RuntimeException("Cannot write history"))
                .when(orderStatusHistoryService)
                .recordTransition(eq(order), eq(OrderStatus.shipping), eq(OrderStatus.completed), eq(staff), eq("GHN confirmed"), any());

        assertThrows(RuntimeException.class,
                () -> staffOrderService.completeOrder("ORD-1", 5L, completeRequest()));

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void completeOrder_eventPublisherFailure_isPropagated() {
        stubClock();
        Order order = order("ORD-1", 1L);
        order.setStatus(OrderStatus.shipping);
        User staff = staff();
        Payment codPayment = payment(10L, order, PaymentMethod.cod, PaymentStatus.pending,
                OffsetDateTime.parse("2026-01-10T09:00:00+07:00"));
        when(userRepository.findById(5L)).thenReturn(Optional.of(staff));
        when(orderRepository.findByOrderCodeForUpdate("ORD-1")).thenReturn(Optional.of(order));
        when(paymentRepository.findAllByOrderIdForUpdate(1L)).thenReturn(List.of(codPayment));
        when(loyaltyPointService.awardForCompletedOrder(order)).thenReturn(loyaltyResult());
        doThrow(new RuntimeException("Cannot publish event")).when(eventPublisher).publishEvent(any());

        assertThrows(RuntimeException.class,
                () -> staffOrderService.completeOrder("ORD-1", 5L, completeRequest()));
    }

    @Test
    void confirmOrder_unknownOrder_doesNotRecordHistoryOrPublishEvent() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(staff()));
        when(orderRepository.findByOrderCodeForUpdate("ORD-404")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> staffOrderService.confirmOrder("ORD-404", 5L));

        verify(orderStatusTransitionPolicy, never()).validate(any(), any());
        verify(orderStatusHistoryService, never()).recordTransition(any(), any(), any(), any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void confirmOrder_unknownStaff_doesNotLockOrRecordHistoryOrPublishEvent() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> staffOrderService.confirmOrder("ORD-1", 5L));

        verify(orderRepository, never()).findByOrderCodeForUpdate(any());
        verify(orderStatusTransitionPolicy, never()).validate(any(), any());
        verify(orderStatusHistoryService, never()).recordTransition(any(), any(), any(), any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void confirmOrder_historyFailure_isPropagatedAndDoesNotPublishEvent() {
        Order order = order("ORD-1", 1L);
        User staff = staff();
        when(userRepository.findById(5L)).thenReturn(Optional.of(staff));
        when(orderRepository.findByOrderCodeForUpdate("ORD-1")).thenReturn(Optional.of(order));
        doThrow(new RuntimeException("Cannot write history"))
                .when(orderStatusHistoryService)
                .recordTransition(order, OrderStatus.pending, OrderStatus.processing, staff, null, null);

        assertThrows(RuntimeException.class, () -> staffOrderService.confirmOrder("ORD-1", 5L));

        verify(eventPublisher, never()).publishEvent(any());
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

    private User staff() {
        return User.builder()
                .id(5L)
                .fullName("Staff One")
                .email("staff@test.com")
                .role(UserRole.staff)
                .build();
    }

    private OrderStatusHistory history(Long id, Order order, OrderStatus fromStatus, OrderStatus toStatus, User changedBy) {
        return OrderStatusHistory.builder()
                .id(id)
                .order(order)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedBy(changedBy)
                .changedByRole(changedBy == null ? null : changedBy.getRole())
                .createdAt(OffsetDateTime.parse("2026-01-10T09:00:00+07:00").plusMinutes(id))
                .build();
    }

    private StaffCompleteOrderRequest completeRequest() {
        return new StaffCompleteOrderRequest(OrderCompletionSource.shipping_partner, "GHN confirmed");
    }

    private LoyaltyPointAwardResult loyaltyResult() {
        return new LoyaltyPointAwardResult(250, 0, 250, null, "Äá»“ng", true);
    }

    private OffsetDateTime fixedNow() {
        return OffsetDateTime.parse("2026-01-10T09:30:00Z");
    }

    private void stubClock() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-01-10T09:30:00Z"), ZoneOffset.UTC);
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }
}
