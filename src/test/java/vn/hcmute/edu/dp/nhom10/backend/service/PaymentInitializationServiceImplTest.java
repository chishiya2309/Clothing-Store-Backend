package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.OnlinePaymentInitializationResult;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.PendingPaymentContext;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.PaymentInitializationException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.GatewayPaymentCreationCommand;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.GatewayPaymentCreationResult;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.PaymentGatewayAdapter;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.PaymentGatewayAdapterFactory;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.PaymentInitializationServiceImpl;
import vn.hcmute.edu.dp.nhom10.backend.service.internal.PaymentAttemptTransactionService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentInitializationServiceImplTest {

    @Mock
    private PaymentAttemptTransactionService paymentAttemptTransactionService;

    @Mock
    private PaymentGatewayAdapterFactory paymentGatewayAdapterFactory;

    @Mock
    private PaymentGatewayAdapter paymentGatewayAdapter;

    @InjectMocks
    private PaymentInitializationServiceImpl paymentInitializationService;

    private final OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(15);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentInitializationService, "returnUrl", "https://shop.test/payment-return");
        ReflectionTestUtils.setField(paymentInitializationService, "callbackUrl", "https://shop.test/payment-callback");
    }

    @Test
    void initializeOnlinePayment_vnpay_success() {
        mockGatewaySuccess(PaymentMethod.vnpay);

        OnlinePaymentInitializationResult result =
                paymentInitializationService.initializeOnlinePayment("CHK-1", 10L, "203.0.113.10");

        assertEquals("https://pay.test/checkout", result.paymentUrl());
        assertEquals(PaymentMethod.vnpay, result.paymentMethod());
    }

    @Test
    void initializeOnlinePayment_momo_success() {
        mockGatewaySuccess(PaymentMethod.momo);

        OnlinePaymentInitializationResult result =
                paymentInitializationService.initializeOnlinePayment("CHK-1", 10L, "203.0.113.10");

        assertEquals("https://pay.test/checkout", result.paymentUrl());
        assertEquals(PaymentMethod.momo, result.paymentMethod());
    }

    @Test
    void initializeOnlinePayment_passesPaymentReferenceToGateway() {
        mockGatewaySuccess(PaymentMethod.vnpay);

        paymentInitializationService.initializeOnlinePayment("CHK-1", 10L, "203.0.113.10");

        GatewayPaymentCreationCommand command = captureGatewayCommand();
        assertEquals("PAY-1", command.paymentReference());
    }

    @Test
    void initializeOnlinePayment_passesAmountToGateway() {
        mockGatewaySuccess(PaymentMethod.vnpay);

        paymentInitializationService.initializeOnlinePayment("CHK-1", 10L, "203.0.113.10");

        GatewayPaymentCreationCommand command = captureGatewayCommand();
        assertEquals(money("120000.00"), command.amount());
    }

    @Test
    void initializeOnlinePayment_passesExpiresAtToGateway() {
        mockGatewaySuccess(PaymentMethod.vnpay);

        paymentInitializationService.initializeOnlinePayment("CHK-1", 10L, "203.0.113.10");

        GatewayPaymentCreationCommand command = captureGatewayCommand();
        assertEquals(expiresAt, command.expiresAt());
    }

    @Test
    void initializeOnlinePayment_passesConfiguredUrlsToGateway() {
        mockGatewaySuccess(PaymentMethod.vnpay);

        paymentInitializationService.initializeOnlinePayment("CHK-1", 10L, "203.0.113.10");

        GatewayPaymentCreationCommand command = captureGatewayCommand();
        assertEquals("https://shop.test/payment-return", command.returnUrl());
        assertEquals("https://shop.test/payment-callback", command.callbackUrl());
    }

    @Test
    void initializeOnlinePayment_passesClientIpToGateway() {
        mockGatewaySuccess(PaymentMethod.vnpay);

        paymentInitializationService.initializeOnlinePayment("CHK-1", 10L, "203.0.113.10");

        GatewayPaymentCreationCommand command = captureGatewayCommand();
        assertEquals("203.0.113.10", command.clientIp());
    }

    @Test
    void initializeOnlinePayment_returnsCompletedInitializationResult() {
        mockGatewaySuccess(PaymentMethod.vnpay);

        OnlinePaymentInitializationResult result =
                paymentInitializationService.initializeOnlinePayment("CHK-1", 10L, "203.0.113.10");

        assertEquals("CHK-1", result.checkoutCode());
        assertEquals("PAY-1", result.paymentReference());
        assertEquals(money("120000.00"), result.amount());
        assertEquals(expiresAt, result.expiresAt());
    }

    @Test
    void initializeOnlinePayment_existingPendingUrl_reusesResultWithoutGateway() {
        when(paymentAttemptTransactionService.createPendingAttempt("CHK-1", 10L))
                .thenReturn(pendingContext(PaymentMethod.vnpay, "https://pay.test/reused"));

        OnlinePaymentInitializationResult result =
                paymentInitializationService.initializeOnlinePayment("CHK-1", 10L, "203.0.113.10");

        assertEquals("https://pay.test/reused", result.paymentUrl());
        verifyNoInteractions(paymentGatewayAdapterFactory, paymentGatewayAdapter);
        verify(paymentAttemptTransactionService, never()).completeInitialization(any(), any());
    }

    @Test
    void initializeOnlinePayment_createsPendingAttemptBeforeCallingGateway() {
        mockGatewaySuccess(PaymentMethod.vnpay);

        paymentInitializationService.initializeOnlinePayment("CHK-1", 10L, "203.0.113.10");

        InOrder inOrder = inOrder(paymentAttemptTransactionService, paymentGatewayAdapterFactory, paymentGatewayAdapter);
        inOrder.verify(paymentAttemptTransactionService).createPendingAttempt("CHK-1", 10L);
        inOrder.verify(paymentGatewayAdapterFactory).getAdapter(PaymentMethod.vnpay);
        inOrder.verify(paymentGatewayAdapter).createPayment(any(GatewayPaymentCreationCommand.class));
        inOrder.verify(paymentAttemptTransactionService).completeInitialization(eq("PAY-1"), any(GatewayPaymentCreationResult.class));
    }

    @Test
    void initializeOnlinePayment_gatewayThrows_marksAttemptFailed() {
        when(paymentAttemptTransactionService.createPendingAttempt("CHK-1", 10L))
                .thenReturn(pendingContext(PaymentMethod.vnpay, null));
        when(paymentGatewayAdapterFactory.getAdapter(PaymentMethod.vnpay)).thenReturn(paymentGatewayAdapter);
        when(paymentGatewayAdapter.createPayment(any(GatewayPaymentCreationCommand.class)))
                .thenThrow(new IllegalStateException("Gateway unavailable"));

        assertThrows(PaymentInitializationException.class,
                () -> paymentInitializationService.initializeOnlinePayment("CHK-1", 10L, "203.0.113.10"));

        verify(paymentAttemptTransactionService).failInitialization("PAY-1", "Gateway unavailable");
        verify(paymentAttemptTransactionService, never()).completeInitialization(any(), any());
    }

    @Test
    void initializeOnlinePayment_nullGatewayResult_marksAttemptFailed() {
        when(paymentAttemptTransactionService.createPendingAttempt("CHK-1", 10L))
                .thenReturn(pendingContext(PaymentMethod.vnpay, null));
        when(paymentGatewayAdapterFactory.getAdapter(PaymentMethod.vnpay)).thenReturn(paymentGatewayAdapter);
        when(paymentGatewayAdapter.createPayment(any(GatewayPaymentCreationCommand.class))).thenReturn(null);

        assertThrows(PaymentInitializationException.class,
                () -> paymentInitializationService.initializeOnlinePayment("CHK-1", 10L, "203.0.113.10"));

        verify(paymentAttemptTransactionService).failInitialization(eq("PAY-1"), any(String.class));
        verify(paymentAttemptTransactionService, never()).completeInitialization(any(), any());
    }

    @Test
    void initializeOnlinePayment_blankPaymentUrl_marksAttemptFailed() {
        when(paymentAttemptTransactionService.createPendingAttempt("CHK-1", 10L))
                .thenReturn(pendingContext(PaymentMethod.vnpay, null));
        when(paymentGatewayAdapterFactory.getAdapter(PaymentMethod.vnpay)).thenReturn(paymentGatewayAdapter);
        when(paymentGatewayAdapter.createPayment(any(GatewayPaymentCreationCommand.class)))
                .thenReturn(new GatewayPaymentCreationResult(" ", null, Map.of()));

        assertThrows(PaymentInitializationException.class,
                () -> paymentInitializationService.initializeOnlinePayment("CHK-1", 10L, "203.0.113.10"));

        verify(paymentAttemptTransactionService).failInitialization(eq("PAY-1"), any(String.class));
        verify(paymentAttemptTransactionService, never()).completeInitialization(any(), any());
    }

    @Test
    void initializeOnlinePayment_missingGlobalReturnUrl_stillPassesCommandToGateway() {
        ReflectionTestUtils.setField(paymentInitializationService, "returnUrl", " ");
        mockGatewaySuccess(PaymentMethod.vnpay);

        paymentInitializationService.initializeOnlinePayment("CHK-1", 10L, "203.0.113.10");

        GatewayPaymentCreationCommand command = captureGatewayCommand();
        assertEquals(" ", command.returnUrl());
    }

    @Test
    void initializeOnlinePayment_missingGlobalCallbackUrl_stillPassesCommandToGateway() {
        ReflectionTestUtils.setField(paymentInitializationService, "callbackUrl", " ");
        mockGatewaySuccess(PaymentMethod.vnpay);

        paymentInitializationService.initializeOnlinePayment("CHK-1", 10L, "203.0.113.10");

        GatewayPaymentCreationCommand command = captureGatewayCommand();
        assertEquals(" ", command.callbackUrl());
    }

    @Test
    void initializeOnlinePayment_createPendingFails_doesNotCallGatewayOrFailHandler() {
        when(paymentAttemptTransactionService.createPendingAttempt("CHK-1", 10L))
                .thenThrow(new InvalidDataException("Checkout is invalid"));

        assertThrows(InvalidDataException.class,
                () -> paymentInitializationService.initializeOnlinePayment("CHK-1", 10L, "203.0.113.10"));

        verifyNoInteractions(paymentGatewayAdapterFactory, paymentGatewayAdapter);
        verify(paymentAttemptTransactionService, never()).failInitialization(any(), any());
    }

    private void mockGatewaySuccess(PaymentMethod paymentMethod) {
        when(paymentAttemptTransactionService.createPendingAttempt("CHK-1", 10L))
                .thenReturn(pendingContext(paymentMethod, null));
        when(paymentGatewayAdapterFactory.getAdapter(paymentMethod)).thenReturn(paymentGatewayAdapter);
        GatewayPaymentCreationResult gatewayResult = new GatewayPaymentCreationResult(
                "https://pay.test/checkout",
                "GTW-1",
                Map.of("gateway", paymentMethod.name())
        );
        when(paymentGatewayAdapter.createPayment(any(GatewayPaymentCreationCommand.class))).thenReturn(gatewayResult);
        when(paymentAttemptTransactionService.completeInitialization("PAY-1", gatewayResult))
                .thenReturn(result(paymentMethod, "https://pay.test/checkout"));
    }

    private GatewayPaymentCreationCommand captureGatewayCommand() {
        ArgumentCaptor<GatewayPaymentCreationCommand> captor =
                ArgumentCaptor.forClass(GatewayPaymentCreationCommand.class);
        verify(paymentGatewayAdapter).createPayment(captor.capture());
        return captor.getValue();
    }

    private PendingPaymentContext pendingContext(PaymentMethod paymentMethod, String paymentUrl) {
        return new PendingPaymentContext(
                1L,
                "CHK-1",
                "PAY-1",
                paymentMethod,
                money("120000.00"),
                expiresAt,
                paymentUrl
        );
    }

    private OnlinePaymentInitializationResult result(PaymentMethod paymentMethod, String paymentUrl) {
        return new OnlinePaymentInitializationResult(
                "CHK-1",
                "PAY-1",
                paymentMethod,
                paymentUrl,
                money("120000.00"),
                expiresAt
        );
    }

    private BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
