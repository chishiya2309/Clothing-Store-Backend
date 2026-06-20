package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.ReservedCheckoutResult;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.OnlinePaymentInitializationResult;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ConfirmCheckoutRequestDTO;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.OrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PlaceOrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.PaymentGatewayUnavailableException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.PaymentGatewayAdapterFactory;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.PlaceOrderServiceImpl;
import vn.hcmute.edu.dp.nhom10.backend.service.internal.CheckoutFailureTransactionService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceOrderServiceImplTest {

    private static final String CLIENT_IP = "203.0.113.10";

    @Mock
    private CheckoutService checkoutService;

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentInitializationService paymentInitializationService;

    @Mock
    private PaymentGatewayAdapterFactory paymentGatewayAdapterFactory;

    @Mock
    private CheckoutFailureTransactionService checkoutFailureTransactionService;

    @InjectMocks
    private PlaceOrderServiceImpl placeOrderService;

    @Test
    void confirmCheckout_codSuccess() {
        when(checkoutService.prepareCheckout(request(PaymentMethod.cod), 10L)).thenReturn(reserved(PaymentMethod.cod));
        when(orderService.createCodOrder("CHK-1", 10L)).thenReturn(order());

        PlaceOrderResponseDTO response = placeOrderService.confirmCheckout(request(PaymentMethod.cod), 10L, CLIENT_IP);

        assertEquals("CHK-1", response.checkoutCode());
        assertEquals(PaymentMethod.cod, response.paymentMethod());
        assertEquals("ORD-1", response.order().getOrderCode());
        assertNull(response.onlinePayment());
    }

    @Test
    void confirmCheckout_codDoesNotCheckGatewayAvailability() {
        when(checkoutService.prepareCheckout(request(PaymentMethod.cod), 10L)).thenReturn(reserved(PaymentMethod.cod));
        when(orderService.createCodOrder("CHK-1", 10L)).thenReturn(order());

        placeOrderService.confirmCheckout(request(PaymentMethod.cod), 10L, CLIENT_IP);

        verifyNoInteractions(paymentGatewayAdapterFactory);
    }

    @Test
    void confirmCheckout_codPreparesCheckoutBeforeCreatingOrder() {
        ConfirmCheckoutRequestDTO request = request(PaymentMethod.cod);
        when(checkoutService.prepareCheckout(request, 10L)).thenReturn(reserved(PaymentMethod.cod));
        when(orderService.createCodOrder("CHK-1", 10L)).thenReturn(order());

        placeOrderService.confirmCheckout(request, 10L, CLIENT_IP);

        InOrder inOrder = inOrder(checkoutService, orderService);
        inOrder.verify(checkoutService).prepareCheckout(request, 10L);
        inOrder.verify(orderService).createCodOrder("CHK-1", 10L);
    }

    @Test
    void confirmCheckout_codDoesNotInitializeOnlinePayment() {
        when(checkoutService.prepareCheckout(request(PaymentMethod.cod), 10L)).thenReturn(reserved(PaymentMethod.cod));
        when(orderService.createCodOrder("CHK-1", 10L)).thenReturn(order());

        placeOrderService.confirmCheckout(request(PaymentMethod.cod), 10L, CLIENT_IP);

        verifyNoInteractions(paymentInitializationService);
    }

    @Test
    void confirmCheckout_prepareCheckoutFails_doesNotCreateCodOrder() {
        when(checkoutService.prepareCheckout(request(PaymentMethod.cod), 10L))
                .thenThrow(new InvalidDataException("Invalid checkout"));

        assertThrows(InvalidDataException.class,
                () -> placeOrderService.confirmCheckout(request(PaymentMethod.cod), 10L, CLIENT_IP));

        verifyNoInteractions(orderService, paymentInitializationService, checkoutFailureTransactionService);
    }

    @Test
    void confirmCheckout_createCodOrderFails_callsCompensationAndRethrowsOriginal() {
        RuntimeException originalException = new InvalidDataException("Cannot create order");
        when(checkoutService.prepareCheckout(request(PaymentMethod.cod), 10L)).thenReturn(reserved(PaymentMethod.cod));
        when(orderService.createCodOrder("CHK-1", 10L)).thenThrow(originalException);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> placeOrderService.confirmCheckout(request(PaymentMethod.cod), 10L, CLIENT_IP));

        assertSame(originalException, thrown);
        verify(checkoutFailureTransactionService).failAndReleaseReservedCheckout("CHK-1");
    }

    @Test
    void confirmCheckout_compensationFails_keepsOriginalException() {
        RuntimeException originalException = new InvalidDataException("Cannot create order");
        RuntimeException compensationException = new IllegalStateException("Cannot compensate");
        when(checkoutService.prepareCheckout(request(PaymentMethod.cod), 10L)).thenReturn(reserved(PaymentMethod.cod));
        when(orderService.createCodOrder("CHK-1", 10L)).thenThrow(originalException);
        doThrow(compensationException).when(checkoutFailureTransactionService).failAndReleaseReservedCheckout("CHK-1");

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> placeOrderService.confirmCheckout(request(PaymentMethod.cod), 10L, CLIENT_IP));

        assertSame(originalException, thrown);
        assertSame(compensationException, thrown.getSuppressed()[0]);
    }

    @Test
    void confirmCheckout_codSuccess_doesNotCallCompensation() {
        when(checkoutService.prepareCheckout(request(PaymentMethod.cod), 10L)).thenReturn(reserved(PaymentMethod.cod));
        when(orderService.createCodOrder("CHK-1", 10L)).thenReturn(order());

        placeOrderService.confirmCheckout(request(PaymentMethod.cod), 10L, CLIENT_IP);

        verifyNoInteractions(checkoutFailureTransactionService);
    }

    @Test
    void confirmCheckout_vnpayChecksAvailabilityBeforePrepare() {
        ConfirmCheckoutRequestDTO request = request(PaymentMethod.vnpay);
        when(checkoutService.prepareCheckout(request, 10L)).thenReturn(reserved(PaymentMethod.vnpay));
        when(paymentInitializationService.initializeOnlinePayment("CHK-1", 10L, CLIENT_IP)).thenReturn(payment(PaymentMethod.vnpay));

        placeOrderService.confirmCheckout(request, 10L, CLIENT_IP);

        InOrder inOrder = inOrder(paymentGatewayAdapterFactory, checkoutService, paymentInitializationService);
        inOrder.verify(paymentGatewayAdapterFactory).requireAvailable(PaymentMethod.vnpay);
        inOrder.verify(checkoutService).prepareCheckout(request, 10L);
        inOrder.verify(paymentInitializationService).initializeOnlinePayment("CHK-1", 10L, CLIENT_IP);
    }

    @Test
    void confirmCheckout_vnpayUnavailable_doesNotPrepareCheckout() {
        doThrow(new PaymentGatewayUnavailableException("VNPay unavailable"))
                .when(paymentGatewayAdapterFactory).requireAvailable(PaymentMethod.vnpay);

        assertThrows(PaymentGatewayUnavailableException.class,
                () -> placeOrderService.confirmCheckout(request(PaymentMethod.vnpay), 10L, CLIENT_IP));

        verifyNoInteractions(checkoutService, orderService, paymentInitializationService);
    }

    @Test
    void confirmCheckout_vnpaySuccess_mapsOnlinePayment() {
        when(checkoutService.prepareCheckout(request(PaymentMethod.vnpay), 10L)).thenReturn(reserved(PaymentMethod.vnpay));
        when(paymentInitializationService.initializeOnlinePayment("CHK-1", 10L, CLIENT_IP)).thenReturn(payment(PaymentMethod.vnpay));

        PlaceOrderResponseDTO response = placeOrderService.confirmCheckout(request(PaymentMethod.vnpay), 10L, CLIENT_IP);

        assertEquals(PaymentMethod.vnpay, response.paymentMethod());
        assertNull(response.order());
        assertEquals("PAY-1", response.onlinePayment().paymentReference());
        assertEquals("https://pay.test/checkout", response.onlinePayment().paymentUrl());
        verifyNoInteractions(orderService);
    }

    @Test
    void confirmCheckout_onlineInitializationFails_doesNotCallCodCompensation() {
        when(checkoutService.prepareCheckout(request(PaymentMethod.vnpay), 10L)).thenReturn(reserved(PaymentMethod.vnpay));
        when(paymentInitializationService.initializeOnlinePayment("CHK-1", 10L, CLIENT_IP))
                .thenThrow(new InvalidDataException("Payment failed"));

        assertThrows(InvalidDataException.class,
                () -> placeOrderService.confirmCheckout(request(PaymentMethod.vnpay), 10L, CLIENT_IP));

        verifyNoInteractions(checkoutFailureTransactionService, orderService);
    }

    @Test
    void confirmCheckout_momoChecksAvailabilityBeforePrepare() {
        ConfirmCheckoutRequestDTO request = request(PaymentMethod.momo);
        when(checkoutService.prepareCheckout(request, 10L)).thenReturn(reserved(PaymentMethod.momo));
        when(paymentInitializationService.initializeOnlinePayment("CHK-1", 10L, CLIENT_IP)).thenReturn(payment(PaymentMethod.momo));

        placeOrderService.confirmCheckout(request, 10L, CLIENT_IP);

        InOrder inOrder = inOrder(paymentGatewayAdapterFactory, checkoutService, paymentInitializationService);
        inOrder.verify(paymentGatewayAdapterFactory).requireAvailable(PaymentMethod.momo);
        inOrder.verify(checkoutService).prepareCheckout(request, 10L);
        inOrder.verify(paymentInitializationService).initializeOnlinePayment("CHK-1", 10L, CLIENT_IP);
    }

    @Test
    void confirmCheckout_momoUnavailable_doesNotPrepareCheckout() {
        doThrow(new PaymentGatewayUnavailableException("MoMo unavailable"))
                .when(paymentGatewayAdapterFactory).requireAvailable(PaymentMethod.momo);

        assertThrows(PaymentGatewayUnavailableException.class,
                () -> placeOrderService.confirmCheckout(request(PaymentMethod.momo), 10L, CLIENT_IP));

        verifyNoInteractions(checkoutService, orderService, paymentInitializationService);
    }

    @Test
    void confirmCheckout_momoSuccess_doesNotCallOrderService() {
        when(checkoutService.prepareCheckout(request(PaymentMethod.momo), 10L)).thenReturn(reserved(PaymentMethod.momo));
        when(paymentInitializationService.initializeOnlinePayment("CHK-1", 10L, CLIENT_IP)).thenReturn(payment(PaymentMethod.momo));

        placeOrderService.confirmCheckout(request(PaymentMethod.momo), 10L, CLIENT_IP);

        verifyNoInteractions(orderService);
    }

    @Test
    void confirmCheckout_nullRequest_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> placeOrderService.confirmCheckout(null, 10L, CLIENT_IP));

        verifyNoInteractions(checkoutService, orderService, paymentInitializationService);
    }

    @Test
    void confirmCheckout_nullUserId_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> placeOrderService.confirmCheckout(request(PaymentMethod.cod), null, CLIENT_IP));

        verifyNoInteractions(checkoutService, orderService, paymentInitializationService);
    }

    @Test
    void confirmCheckout_nullPaymentMethod_throwsException() {
        ConfirmCheckoutRequestDTO request = new ConfirmCheckoutRequestDTO(1L, null, null);

        assertThrows(IllegalArgumentException.class,
                () -> placeOrderService.confirmCheckout(request, 10L, CLIENT_IP));

        verifyNoInteractions(checkoutService, orderService, paymentInitializationService);
    }

    @Test
    void confirmCheckout_preparedPaymentMethodDiffersFromRequest_stopsFlow() {
        when(checkoutService.prepareCheckout(request(PaymentMethod.cod), 10L)).thenReturn(reserved(PaymentMethod.vnpay));

        assertThrows(InvalidDataException.class,
                () -> placeOrderService.confirmCheckout(request(PaymentMethod.cod), 10L, CLIENT_IP));

        verify(orderService, never()).createCodOrder(any(), anyLong());
        verifyNoInteractions(paymentInitializationService);
    }

    private ConfirmCheckoutRequestDTO request(PaymentMethod paymentMethod) {
        return new ConfirmCheckoutRequestDTO(1L, null, paymentMethod);
    }

    private ReservedCheckoutResult reserved(PaymentMethod paymentMethod) {
        return new ReservedCheckoutResult(
                1L,
                "CHK-1",
                paymentMethod,
                money("100000.00"),
                money("20000.00"),
                BigDecimal.ZERO,
                money("120000.00"),
                OffsetDateTime.now().plusMinutes(15)
        );
    }

    private OrderResponseDTO order() {
        return OrderResponseDTO.builder()
                .orderCode("ORD-1")
                .subtotal(money("100000.00"))
                .shippingFee(money("20000.00"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(money("120000.00"))
                .status(OrderStatus.pending)
                .build();
    }

    private OnlinePaymentInitializationResult payment(PaymentMethod paymentMethod) {
        return new OnlinePaymentInitializationResult(
                "CHK-1",
                "PAY-1",
                paymentMethod,
                "https://pay.test/checkout",
                money("120000.00"),
                OffsetDateTime.now().plusMinutes(15)
        );
    }

    private BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
