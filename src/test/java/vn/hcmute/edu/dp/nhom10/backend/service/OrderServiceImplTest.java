package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.OrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSessionItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.Payment;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.enums.CheckoutSessionStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentStatus;
import vn.hcmute.edu.dp.nhom10.backend.event.OrderCreatedEvent;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.CartItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.OrderServiceImpl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private CheckoutSessionRepository checkoutSessionRepository;

    @Mock
    private CheckoutSessionItemRepository checkoutSessionItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private InventoryReservationService inventoryReservationService;

    @Mock
    private VoucherReservationService voucherService;

    @Mock
    private OrderStatusHistoryService orderStatusHistoryService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createCodOrder_withoutVoucher_success() {
        mockSuccessfulFlow(null);

        OrderResponseDTO response = orderService.createCodOrder("CHK-1", 10L);

        assertNotNull(response);
        assertEquals(OrderStatus.pending, response.getStatus());
        assertEquals(money("200000.00"), response.getSubtotal());
        assertEquals(money("20000.00"), response.getShippingFee());
        assertEquals(BigDecimal.ZERO, response.getDiscountAmount());
        assertEquals(money("220000.00"), response.getTotalAmount());
        verify(voucherService, never()).consumeVoucherReservation(any());
    }

    @Test
    void createCodOrder_withVoucher_success() {
        mockSuccessfulFlow(Voucher.builder().id(100L).code("SAVE10").build());

        OrderResponseDTO response = orderService.createCodOrder("CHK-1", 10L);

        assertEquals(money("190000.00"), response.getTotalAmount());
        verify(voucherService).consumeVoucherReservation("CHK-1");
    }

    @Test
    void createCodOrder_mapsShippingSnapshot() {
        mockSuccessfulFlow(null);

        orderService.createCodOrder("CHK-1", 10L);

        Order savedOrder = captureSavedOrder();
        assertEquals("Nguyen Van A", savedOrder.getShippingName());
        assertEquals("0900000000", savedOrder.getShippingPhone());
        assertEquals("Ho Chi Minh", savedOrder.getShippingProvince());
        assertEquals("District 1", savedOrder.getShippingDistrict());
        assertEquals("Ben Nghe", savedOrder.getShippingWard());
        assertEquals("1 Le Loi", savedOrder.getShippingAddress());
    }

    @Test
    void createCodOrder_mapsAmounts() {
        mockSuccessfulFlow(Voucher.builder().id(100L).build());

        orderService.createCodOrder("CHK-1", 10L);

        Order savedOrder = captureSavedOrder();
        assertEquals(money("200000.00"), savedOrder.getSubtotal());
        assertEquals(money("20000.00"), savedOrder.getShippingFee());
        assertEquals(money("30000.00"), savedOrder.getDiscountAmount());
        assertEquals(money("190000.00"), savedOrder.getTotalAmount());
    }

    @Test
    void createCodOrder_mapsOrderItemFromCheckoutSnapshot() {
        mockSuccessfulFlow(null);

        orderService.createCodOrder("CHK-1", 10L);

        OrderItem orderItem = captureSavedOrderItem();
        assertEquals("T-Shirt", orderItem.getProductName());
        assertEquals("Size: M, Color: Black", orderItem.getVariantInfo());
        assertEquals(2, orderItem.getQuantity());
        assertEquals(money("100000.00"), orderItem.getUnitPrice());
        assertEquals(money("200000.00"), orderItem.getSubtotal());
    }

    @Test
    void createCodOrder_doesNotReadCurrentProductPrice() {
        mockSuccessfulFlow(null);

        orderService.createCodOrder("CHK-1", 10L);

        ProductVariant variant = captureSavedOrderItem().getProductVariant();
        assertEquals(100L, variant.getId());
    }

    @Test
    void createCodOrder_createsCodPayment() {
        mockSuccessfulFlow(null);

        orderService.createCodOrder("CHK-1", 10L);

        Payment payment = captureSavedPayment();
        assertEquals(PaymentMethod.cod, payment.getMethod());
    }

    @Test
    void createCodOrder_paymentStatusPending() {
        mockSuccessfulFlow(null);

        orderService.createCodOrder("CHK-1", 10L);

        assertEquals(PaymentStatus.pending, captureSavedPayment().getStatus());
    }

    @Test
    void createCodOrder_paymentAmountEqualsCheckoutTotal() {
        mockSuccessfulFlow(Voucher.builder().id(100L).build());

        orderService.createCodOrder("CHK-1", 10L);

        assertEquals(money("190000.00"), captureSavedPayment().getAmount());
    }

    @Test
    void createCodOrder_orderStatusPending() {
        mockSuccessfulFlow(null);

        orderService.createCodOrder("CHK-1", 10L);

        assertEquals(OrderStatus.pending, captureSavedOrder().getStatus());
    }

    @Test
    void createCodOrder_consumesInventory() {
        mockSuccessfulFlow(null);

        orderService.createCodOrder("CHK-1", 10L);

        verify(inventoryReservationService).consumeStockReservation("CHK-1");
    }

    @Test
    void createCodOrder_withVoucherConsumesVoucher() {
        mockSuccessfulFlow(Voucher.builder().id(100L).build());

        orderService.createCodOrder("CHK-1", 10L);

        verify(voucherService).consumeVoucherReservation("CHK-1");
    }

    @Test
    void createCodOrder_withoutVoucherDoesNotConsumeVoucher() {
        mockSuccessfulFlow(null);

        orderService.createCodOrder("CHK-1", 10L);

        verifyNoInteractions(voucherService);
    }

    @Test
    void createCodOrder_deletesPurchasedProductVariantIds() {
        mockSuccessfulFlow(null);

        orderService.createCodOrder("CHK-1", 10L);

        ArgumentCaptor<Collection<Long>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(cartItemRepository).deletePurchasedItems(eq(10L), captor.capture());
        assertEquals(List.of(100L), captor.getValue().stream().toList());
    }

    @Test
    void createCodOrder_marksCheckoutCompleted() {
        mockSuccessfulFlow(null);

        orderService.createCodOrder("CHK-1", 10L);

        assertEquals(CheckoutSessionStatus.completed, captureCompletedCheckout().getStatus());
    }

    @Test
    void createCodOrder_returnsOrderResponse() {
        mockSuccessfulFlow(null);

        OrderResponseDTO response = orderService.createCodOrder("CHK-1", 10L);

        assertNotNull(response.getOrderCode());
        assertEquals(money("220000.00"), response.getTotalAmount());
    }

    @Test
    void createCodOrder_nullCheckoutCode_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> orderService.createCodOrder(null, 10L));

        verifyNoInteractions(checkoutSessionRepository, orderRepository, paymentRepository);
    }

    @Test
    void createCodOrder_blankCheckoutCode_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> orderService.createCodOrder(" ", 10L));

        verifyNoInteractions(checkoutSessionRepository, orderRepository, paymentRepository);
    }

    @Test
    void createCodOrder_nullUserId_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> orderService.createCodOrder("CHK-1", null));

        verifyNoInteractions(checkoutSessionRepository, orderRepository, paymentRepository);
    }

    @Test
    void createCodOrder_checkoutNotFound_throwsException() {
        when(checkoutSessionRepository.findByCheckoutCodeForUpdate("CHK-1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.createCodOrder("CHK-1", 10L));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createCodOrder_checkoutBelongsToAnotherUser_throwsException() {
        when(checkoutSessionRepository.findByCheckoutCodeForUpdate("CHK-1"))
                .thenReturn(Optional.of(checkoutSession(null, 20L)));

        assertThrows(ResourceNotFoundException.class, () -> orderService.createCodOrder("CHK-1", 10L));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createCodOrder_nonCodPaymentMethod_throwsException() {
        CheckoutSession checkoutSession = checkoutSession(null, 10L);
        checkoutSession.setPaymentMethod(PaymentMethod.vnpay);
        when(checkoutSessionRepository.findByCheckoutCodeForUpdate("CHK-1")).thenReturn(Optional.of(checkoutSession));

        assertThrows(InvalidDataException.class, () -> orderService.createCodOrder("CHK-1", 10L));
    }

    @Test
    void createCodOrder_statusNotReserved_throwsException() {
        CheckoutSession checkoutSession = checkoutSession(null, 10L);
        checkoutSession.setStatus(CheckoutSessionStatus.creating);
        when(checkoutSessionRepository.findByCheckoutCodeForUpdate("CHK-1")).thenReturn(Optional.of(checkoutSession));

        assertThrows(InvalidDataException.class, () -> orderService.createCodOrder("CHK-1", 10L));
    }

    @Test
    void createCodOrder_alreadyCompleted_throwsException() {
        CheckoutSession checkoutSession = checkoutSession(null, 10L);
        checkoutSession.setStatus(CheckoutSessionStatus.completed);
        when(checkoutSessionRepository.findByCheckoutCodeForUpdate("CHK-1")).thenReturn(Optional.of(checkoutSession));

        assertThrows(InvalidDataException.class, () -> orderService.createCodOrder("CHK-1", 10L));
    }

    @Test
    void createCodOrder_expiredCheckout_throwsException() {
        CheckoutSession checkoutSession = checkoutSession(null, 10L);
        checkoutSession.setExpiresAt(OffsetDateTime.now().minusMinutes(1));
        when(checkoutSessionRepository.findByCheckoutCodeForUpdate("CHK-1")).thenReturn(Optional.of(checkoutSession));

        assertThrows(InvalidDataException.class, () -> orderService.createCodOrder("CHK-1", 10L));
    }

    @Test
    void createCodOrder_withoutSnapshotItems_throwsException() {
        when(checkoutSessionRepository.findByCheckoutCodeForUpdate("CHK-1"))
                .thenReturn(Optional.of(checkoutSession(null, 10L)));
        when(checkoutSessionItemRepository.findAllByCheckoutSessionIdWithVariant(1L)).thenReturn(List.of());

        assertThrows(InvalidDataException.class, () -> orderService.createCodOrder("CHK-1", 10L));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createCodOrder_saveOrderFails_stopsFlow() {
        mockCheckoutAndItems(null);
        when(orderRepository.existsByOrderCode(any())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenThrow(new IllegalArgumentException("Cannot save order"));

        assertThrows(IllegalArgumentException.class, () -> orderService.createCodOrder("CHK-1", 10L));

        verify(orderItemRepository, never()).saveAll(any());
        verify(paymentRepository, never()).save(any());
        verifyNoInteractions(inventoryReservationService, voucherService);
    }

    @Test
    void createCodOrder_saveOrderItemFails_stopsFlow() {
        mockSuccessfulFlow(null);
        when(orderItemRepository.saveAll(any())).thenThrow(new IllegalArgumentException("Cannot save order item"));

        assertThrows(IllegalArgumentException.class, () -> orderService.createCodOrder("CHK-1", 10L));

        verify(paymentRepository, never()).save(any());
        verifyNoInteractions(inventoryReservationService, voucherService);
    }

    @Test
    void createCodOrder_savePaymentFails_stopsBeforeConsume() {
        mockSuccessfulFlow(null);
        when(paymentRepository.save(any(Payment.class))).thenThrow(new IllegalArgumentException("Cannot save payment"));

        assertThrows(IllegalArgumentException.class, () -> orderService.createCodOrder("CHK-1", 10L));

        verifyNoInteractions(inventoryReservationService, voucherService);
    }

    @Test
    void createCodOrder_consumeInventoryFails_stopsFlow() {
        mockSuccessfulFlow(Voucher.builder().id(100L).build());
        doThrow(new IllegalArgumentException("Cannot consume inventory"))
                .when(inventoryReservationService).consumeStockReservation("CHK-1");

        assertThrows(IllegalArgumentException.class, () -> orderService.createCodOrder("CHK-1", 10L));

        verify(voucherService, never()).consumeVoucherReservation(any());
        verify(cartItemRepository, never()).deletePurchasedItems(any(), anyCollection());
        verify(checkoutSessionRepository, never()).save(any());
    }

    @Test
    void createCodOrder_consumeVoucherFails_stopsFlow() {
        mockSuccessfulFlow(Voucher.builder().id(100L).build());
        doThrow(new IllegalArgumentException("Cannot consume voucher"))
                .when(voucherService).consumeVoucherReservation("CHK-1");

        assertThrows(IllegalArgumentException.class, () -> orderService.createCodOrder("CHK-1", 10L));

        verify(cartItemRepository, never()).deletePurchasedItems(any(), anyCollection());
        verify(checkoutSessionRepository, never()).save(any());
    }

    @Test
    void createCodOrder_deleteCartFails_doesNotCompleteCheckout() {
        mockSuccessfulFlow(null);
        when(cartItemRepository.deletePurchasedItems(eq(10L), anyCollection()))
                .thenThrow(new IllegalArgumentException("Cannot delete cart"));

        assertThrows(IllegalArgumentException.class, () -> orderService.createCodOrder("CHK-1", 10L));

        verify(checkoutSessionRepository, never()).save(any());
    }

    @Test
    void createCodOrder_createsOnlyCodPaymentRecord() {
        mockSuccessfulFlow(null);

        orderService.createCodOrder("CHK-1", 10L);

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createCodOrder_doesNotCallGateway() {
        mockSuccessfulFlow(null);

        orderService.createCodOrder("CHK-1", 10L);

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createCodOrder_orderCodeCollisionRetries() {
        mockCheckoutAndItems(null);
        when(orderRepository.existsByOrderCode(any())).thenReturn(true, false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(500L);
            return order;
        });

        orderService.createCodOrder("CHK-1", 10L);

        verify(orderRepository, org.mockito.Mockito.times(2)).existsByOrderCode(any());
    }

    @Test
    void createCodOrder_success_recordsInitialHistory() {
        mockSuccessfulFlow(null);

        orderService.createCodOrder("CHK-1", 10L);

        verify(orderStatusHistoryService).recordInitialStatus(org.mockito.ArgumentMatchers.argThat(order ->
                order != null && order.getId().equals(500L) && order.getStatus() == OrderStatus.pending
        ));
    }

    @Test
    void createCodOrder_callsDependenciesInRequiredOrder() {
        mockSuccessfulFlow(Voucher.builder().id(100L).build());

        orderService.createCodOrder("CHK-1", 10L);

        InOrder inOrder = inOrder(orderRepository, orderStatusHistoryService, orderItemRepository, paymentRepository,
                inventoryReservationService, voucherService, cartItemRepository, checkoutSessionRepository);
        inOrder.verify(orderRepository).save(any(Order.class));
        inOrder.verify(orderStatusHistoryService).recordInitialStatus(any(Order.class));
        inOrder.verify(orderItemRepository).saveAll(any());
        inOrder.verify(paymentRepository).save(any(Payment.class));
        inOrder.verify(inventoryReservationService).consumeStockReservation("CHK-1");
        inOrder.verify(voucherService).consumeVoucherReservation("CHK-1");
        inOrder.verify(cartItemRepository).deletePurchasedItems(eq(10L), anyCollection());
        inOrder.verify(checkoutSessionRepository).save(any(CheckoutSession.class));
    }

    @Test
    void createCodOrder_success_publishesOrderCreatedEvent() {
        mockSuccessfulFlow(null);

        orderService.createCodOrder("CHK-1", 10L);

        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        OrderCreatedEvent event = captor.getValue();
        assertEquals(500L, event.orderId());
        assertEquals(10L, event.userId());
        assertEquals(money("220000.00"), event.totalAmount());
        assertNotNull(event.orderCode());
        assertNotNull(event.occurredAt());
    }

    @Test
    void createCodOrder_publishesEventAfterMainBusinessSteps() {
        mockSuccessfulFlow(Voucher.builder().id(100L).build());

        orderService.createCodOrder("CHK-1", 10L);

        InOrder inOrder = inOrder(orderRepository, orderStatusHistoryService, orderItemRepository, paymentRepository,
                inventoryReservationService, voucherService, cartItemRepository, checkoutSessionRepository, eventPublisher);
        inOrder.verify(orderRepository).save(any(Order.class));
        inOrder.verify(orderStatusHistoryService).recordInitialStatus(any(Order.class));
        inOrder.verify(orderItemRepository).saveAll(any());
        inOrder.verify(paymentRepository).save(any(Payment.class));
        inOrder.verify(inventoryReservationService).consumeStockReservation("CHK-1");
        inOrder.verify(voucherService).consumeVoucherReservation("CHK-1");
        inOrder.verify(cartItemRepository).deletePurchasedItems(eq(10L), anyCollection());
        inOrder.verify(checkoutSessionRepository).save(any(CheckoutSession.class));
        inOrder.verify(eventPublisher).publishEvent(any(OrderCreatedEvent.class));
    }

    @Test
    void createCodOrder_saveOrderFails_doesNotPublishEvent() {
        mockCheckoutAndItems(null);
        when(orderRepository.existsByOrderCode(any())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenThrow(new IllegalArgumentException("Cannot save order"));

        assertThrows(IllegalArgumentException.class, () -> orderService.createCodOrder("CHK-1", 10L));

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void createCodOrder_saveOrderItemFails_doesNotPublishEvent() {
        mockSuccessfulFlow(null);
        when(orderItemRepository.saveAll(any())).thenThrow(new IllegalArgumentException("Cannot save order item"));

        assertThrows(IllegalArgumentException.class, () -> orderService.createCodOrder("CHK-1", 10L));

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void createCodOrder_savePaymentFails_doesNotPublishEvent() {
        mockSuccessfulFlow(null);
        when(paymentRepository.save(any(Payment.class))).thenThrow(new IllegalArgumentException("Cannot save payment"));

        assertThrows(IllegalArgumentException.class, () -> orderService.createCodOrder("CHK-1", 10L));

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void createCodOrder_consumeInventoryFails_doesNotPublishEvent() {
        mockSuccessfulFlow(Voucher.builder().id(100L).build());
        doThrow(new IllegalArgumentException("Cannot consume inventory"))
                .when(inventoryReservationService).consumeStockReservation("CHK-1");

        assertThrows(IllegalArgumentException.class, () -> orderService.createCodOrder("CHK-1", 10L));

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void createCodOrder_consumeVoucherFails_doesNotPublishEvent() {
        mockSuccessfulFlow(Voucher.builder().id(100L).build());
        doThrow(new IllegalArgumentException("Cannot consume voucher"))
                .when(voucherService).consumeVoucherReservation("CHK-1");

        assertThrows(IllegalArgumentException.class, () -> orderService.createCodOrder("CHK-1", 10L));

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void createCodOrder_deleteCartFails_doesNotPublishEvent() {
        mockSuccessfulFlow(null);
        when(cartItemRepository.deletePurchasedItems(eq(10L), anyCollection()))
                .thenThrow(new IllegalArgumentException("Cannot delete cart"));

        assertThrows(IllegalArgumentException.class, () -> orderService.createCodOrder("CHK-1", 10L));

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void createCodOrder_checkoutNotCompleted_doesNotPublishEvent() {
        CheckoutSession checkoutSession = checkoutSession(null, 10L);
        checkoutSession.setStatus(CheckoutSessionStatus.creating);
        when(checkoutSessionRepository.findByCheckoutCodeForUpdate("CHK-1")).thenReturn(Optional.of(checkoutSession));

        assertThrows(InvalidDataException.class, () -> orderService.createCodOrder("CHK-1", 10L));

        verify(eventPublisher, never()).publishEvent(any());
    }

    private void mockSuccessfulFlow(Voucher voucher) {
        mockCheckoutAndItems(voucher);
        when(orderRepository.existsByOrderCode(any())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(500L);
            return order;
        });
    }

    private void mockCheckoutAndItems(Voucher voucher) {
        CheckoutSession checkoutSession = checkoutSession(voucher, 10L);
        when(checkoutSessionRepository.findByCheckoutCodeForUpdate("CHK-1")).thenReturn(Optional.of(checkoutSession));
        when(checkoutSessionItemRepository.findAllByCheckoutSessionIdWithVariant(1L))
                .thenReturn(List.of(checkoutSessionItem()));
    }

    private CheckoutSession checkoutSession(Voucher voucher, Long userId) {
        return CheckoutSession.builder()
                .id(1L)
                .checkoutCode("CHK-1")
                .user(User.builder().id(userId).build())
                .shippingName("Nguyen Van A")
                .shippingPhone("0900000000")
                .shippingProvince("Ho Chi Minh")
                .shippingDistrict("District 1")
                .shippingWard("Ben Nghe")
                .shippingAddress("1 Le Loi")
                .subtotal(money("200000.00"))
                .shippingFee(money("20000.00"))
                .discountAmount(voucher == null ? BigDecimal.ZERO : money("30000.00"))
                .totalAmount(voucher == null ? money("220000.00") : money("190000.00"))
                .voucher(voucher)
                .paymentMethod(PaymentMethod.cod)
                .status(CheckoutSessionStatus.reserved)
                .expiresAt(OffsetDateTime.now().plusMinutes(15))
                .build();
    }

    private CheckoutSessionItem checkoutSessionItem() {
        return CheckoutSessionItem.builder()
                .id(10L)
                .productVariant(ProductVariant.builder().id(100L).build())
                .productName("T-Shirt")
                .variantInfo("Size: M, Color: Black")
                .quantity(2)
                .unitPrice(money("100000.00"))
                .subtotal(money("200000.00"))
                .build();
    }

    private Order captureSavedOrder() {
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        return captor.getValue();
    }

    private OrderItem captureSavedOrderItem() {
        ArgumentCaptor<Iterable<OrderItem>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(orderItemRepository).saveAll(captor.capture());
        return captor.getValue().iterator().next();
    }

    private Payment captureSavedPayment() {
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        return captor.getValue();
    }

    private CheckoutSession captureCompletedCheckout() {
        ArgumentCaptor<CheckoutSession> captor = ArgumentCaptor.forClass(CheckoutSession.class);
        verify(checkoutSessionRepository).save(captor.capture());
        return captor.getValue();
    }

    private BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
