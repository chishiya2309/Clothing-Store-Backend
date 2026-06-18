package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.OnlinePaymentInitializationResult;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.PendingPaymentContext;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.enums.CheckoutSessionStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentAttemptStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.GatewayPaymentCreationResult;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentAttemptRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.internal.PaymentAttemptTransactionService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentAttemptTransactionServiceTest {

    @Mock
    private CheckoutSessionRepository checkoutSessionRepository;

    @Mock
    private PaymentAttemptRepository paymentAttemptRepository;

    @Mock
    private InventoryReservationService inventoryReservationService;

    @Mock
    private VoucherService voucherService;

    @InjectMocks
    private PaymentAttemptTransactionService paymentAttemptTransactionService;

    private final OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(15);

    @BeforeEach
    void setUp() {
        lenient().when(paymentAttemptRepository.save(any(PaymentAttempt.class))).thenAnswer(invocation -> {
            PaymentAttempt paymentAttempt = invocation.getArgument(0);
            if (paymentAttempt.getId() == null) {
                paymentAttempt.setId(100L);
            }
            return paymentAttempt;
        });
        lenient().when(checkoutSessionRepository.save(any(CheckoutSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createPendingAttempt_locksCheckoutByCode() {
        mockCheckoutForCreate(checkout(PaymentMethod.vnpay, CheckoutSessionStatus.reserved, 10L, money("120000.00"), null, expiresAt));

        paymentAttemptTransactionService.createPendingAttempt(" CHK-1 ", 10L);

        verify(checkoutSessionRepository).findByCheckoutCodeForUpdate("CHK-1");
    }

    @Test
    void createPendingAttempt_success_createsPendingAttempt() {
        CheckoutSession checkoutSession = checkout(PaymentMethod.vnpay, CheckoutSessionStatus.reserved, 10L,
                money("120000.00"), null, expiresAt);
        mockCheckoutForCreate(checkoutSession);

        PendingPaymentContext result = paymentAttemptTransactionService.createPendingAttempt("CHK-1", 10L);

        PaymentAttempt savedAttempt = captureSavedAttempt();
        assertNotNull(result.paymentReference());
        assertEquals(PaymentAttemptStatus.pending, savedAttempt.getStatus());
        assertEquals(checkoutSession, savedAttempt.getCheckoutSession());
        assertEquals(PaymentMethod.vnpay, savedAttempt.getMethod());
        assertEquals(money("120000.00"), savedAttempt.getAmount());
        assertEquals(expiresAt, savedAttempt.getExpiresAt());
        assertNull(savedAttempt.getPaymentUrl());
    }

    @Test
    void createPendingAttempt_retriesWhenPaymentReferenceCollides() {
        mockCheckoutForCreate(checkout(PaymentMethod.vnpay, CheckoutSessionStatus.reserved, 10L, money("120000.00"), null, expiresAt));
        when(paymentAttemptRepository.existsByPaymentReference(anyString()))
                .thenReturn(true)
                .thenReturn(false);

        paymentAttemptTransactionService.createPendingAttempt("CHK-1", 10L);

        verify(paymentAttemptRepository, org.mockito.Mockito.times(2)).existsByPaymentReference(anyString());
    }

    @Test
    void createPendingAttempt_nullCheckoutCode_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> paymentAttemptTransactionService.createPendingAttempt(null, 10L));
    }

    @Test
    void createPendingAttempt_nullUserId_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> paymentAttemptTransactionService.createPendingAttempt("CHK-1", null));
    }

    @Test
    void createPendingAttempt_checkoutNotFound_throwsException() {
        when(checkoutSessionRepository.findByCheckoutCodeForUpdate("CHK-1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentAttemptTransactionService.createPendingAttempt("CHK-1", 10L));
    }

    @Test
    void createPendingAttempt_wrongUser_throwsException() {
        mockCheckoutForCreate(checkout(PaymentMethod.vnpay, CheckoutSessionStatus.reserved, 20L, money("120000.00"), null, expiresAt));

        assertThrows(ResourceNotFoundException.class,
                () -> paymentAttemptTransactionService.createPendingAttempt("CHK-1", 10L));
    }

    @Test
    void createPendingAttempt_notReserved_throwsException() {
        mockCheckoutForCreate(checkout(PaymentMethod.vnpay, CheckoutSessionStatus.creating, 10L, money("120000.00"), null, expiresAt));

        assertThrows(InvalidDataException.class,
                () -> paymentAttemptTransactionService.createPendingAttempt("CHK-1", 10L));
    }

    @Test
    void createPendingAttempt_expiredCheckout_throwsException() {
        mockCheckoutForCreate(checkout(PaymentMethod.vnpay, CheckoutSessionStatus.reserved, 10L,
                money("120000.00"), null, OffsetDateTime.now().minusMinutes(1)));

        assertThrows(InvalidDataException.class,
                () -> paymentAttemptTransactionService.createPendingAttempt("CHK-1", 10L));
    }

    @Test
    void createPendingAttempt_cod_throwsException() {
        mockCheckoutForCreate(checkout(PaymentMethod.cod, CheckoutSessionStatus.reserved, 10L, money("120000.00"), null, expiresAt));

        assertThrows(InvalidDataException.class,
                () -> paymentAttemptTransactionService.createPendingAttempt("CHK-1", 10L));
    }

    @Test
    void createPendingAttempt_zeroAmount_throwsException() {
        mockCheckoutForCreate(checkout(PaymentMethod.vnpay, CheckoutSessionStatus.reserved, 10L, BigDecimal.ZERO, null, expiresAt));

        assertThrows(InvalidDataException.class,
                () -> paymentAttemptTransactionService.createPendingAttempt("CHK-1", 10L));
    }

    @Test
    void createPendingAttempt_existingPendingWithPaymentUrl_reusesAttempt() {
        CheckoutSession checkoutSession = checkout(PaymentMethod.vnpay, CheckoutSessionStatus.reserved, 10L,
                money("120000.00"), null, expiresAt);
        mockCheckoutForCreate(checkoutSession);
        when(paymentAttemptRepository.findTopByCheckoutSession_IdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(paymentAttempt(checkoutSession, PaymentAttemptStatus.pending, "https://pay.test/reused", expiresAt)));

        PendingPaymentContext result = paymentAttemptTransactionService.createPendingAttempt("CHK-1", 10L);

        assertEquals("https://pay.test/reused", result.paymentUrl());
        verify(paymentAttemptRepository, never()).existsByPaymentReference(anyString());
    }

    @Test
    void createPendingAttempt_existingPendingWithoutPaymentUrl_throwsException() {
        CheckoutSession checkoutSession = checkout(PaymentMethod.vnpay, CheckoutSessionStatus.reserved, 10L,
                money("120000.00"), null, expiresAt);
        mockCheckoutForCreate(checkoutSession);
        when(paymentAttemptRepository.findTopByCheckoutSession_IdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(paymentAttempt(checkoutSession, PaymentAttemptStatus.pending, null, expiresAt)));

        assertThrows(InvalidDataException.class,
                () -> paymentAttemptTransactionService.createPendingAttempt("CHK-1", 10L));
    }

    @Test
    void createPendingAttempt_completedLatestAttempt_throwsException() {
        CheckoutSession checkoutSession = checkout(PaymentMethod.vnpay, CheckoutSessionStatus.reserved, 10L,
                money("120000.00"), null, expiresAt);
        mockCheckoutForCreate(checkoutSession);
        when(paymentAttemptRepository.findTopByCheckoutSession_IdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(paymentAttempt(checkoutSession, PaymentAttemptStatus.completed, "https://pay.test/old", expiresAt)));

        assertThrows(InvalidDataException.class,
                () -> paymentAttemptTransactionService.createPendingAttempt("CHK-1", 10L));
    }

    @Test
    void createPendingAttempt_failedLatestAttempt_createsNewAttempt() {
        CheckoutSession checkoutSession = checkout(PaymentMethod.vnpay, CheckoutSessionStatus.reserved, 10L,
                money("120000.00"), null, expiresAt);
        mockCheckoutForCreate(checkoutSession);
        when(paymentAttemptRepository.findTopByCheckoutSession_IdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(paymentAttempt(checkoutSession, PaymentAttemptStatus.failed, null, expiresAt)));

        paymentAttemptTransactionService.createPendingAttempt("CHK-1", 10L);

        verify(paymentAttemptRepository).save(any(PaymentAttempt.class));
    }

    @Test
    void completeInitialization_locksCheckoutBeforeAttempt() {
        CheckoutSession checkoutSession = checkout(PaymentMethod.vnpay, CheckoutSessionStatus.reserved, 10L,
                money("120000.00"), null, expiresAt);
        mockAttemptForComplete(checkoutSession, paymentAttempt(checkoutSession, PaymentAttemptStatus.pending, null, expiresAt));

        paymentAttemptTransactionService.completeInitialization("PAY-1", gatewayResult("https://pay.test/checkout"));

        InOrder inOrder = inOrder(checkoutSessionRepository, paymentAttemptRepository);
        inOrder.verify(paymentAttemptRepository).findByPaymentReference("PAY-1");
        inOrder.verify(checkoutSessionRepository).findByIdForUpdate(1L);
        inOrder.verify(paymentAttemptRepository).findByPaymentReferenceForUpdate("PAY-1");
    }

    @Test
    void completeInitialization_success_savesGatewayData() {
        CheckoutSession checkoutSession = checkout(PaymentMethod.vnpay, CheckoutSessionStatus.reserved, 10L,
                money("120000.00"), null, expiresAt);
        PaymentAttempt paymentAttempt = paymentAttempt(checkoutSession, PaymentAttemptStatus.pending, null, expiresAt);
        mockAttemptForComplete(checkoutSession, paymentAttempt);

        OnlinePaymentInitializationResult result =
                paymentAttemptTransactionService.completeInitialization("PAY-1", gatewayResult("https://pay.test/checkout"));

        assertEquals("https://pay.test/checkout", result.paymentUrl());
        assertEquals(PaymentAttemptStatus.pending, paymentAttempt.getStatus());
        assertEquals("GTW-1", paymentAttempt.getGatewayTransactionId());
        assertEquals("visible", paymentAttempt.getGatewayPayload().get("publicKey"));
        assertFalse(paymentAttempt.getGatewayPayload().containsKey("secretHash"));
    }

    @Test
    void completeInitialization_attemptDoesNotBelongToCheckout_throwsException() {
        CheckoutSession checkoutSession = checkout(PaymentMethod.vnpay, CheckoutSessionStatus.reserved, 10L,
                money("120000.00"), null, expiresAt);
        CheckoutSession otherCheckout = checkout(PaymentMethod.vnpay, CheckoutSessionStatus.reserved, 10L,
                money("120000.00"), null, expiresAt);
        otherCheckout.setId(2L);
        PaymentAttempt existingAttempt = paymentAttempt(checkoutSession, PaymentAttemptStatus.pending, null, expiresAt);
        PaymentAttempt lockedAttempt = paymentAttempt(otherCheckout, PaymentAttemptStatus.pending, null, expiresAt);
        when(paymentAttemptRepository.findByPaymentReference("PAY-1")).thenReturn(Optional.of(existingAttempt));
        when(checkoutSessionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(checkoutSession));
        when(paymentAttemptRepository.findByPaymentReferenceForUpdate("PAY-1")).thenReturn(Optional.of(lockedAttempt));

        assertThrows(InvalidDataException.class,
                () -> paymentAttemptTransactionService.completeInitialization("PAY-1", gatewayResult("https://pay.test/checkout")));
    }

    @Test
    void completeInitialization_expiredCheckout_throwsException() {
        CheckoutSession checkoutSession = checkout(PaymentMethod.vnpay, CheckoutSessionStatus.reserved, 10L,
                money("120000.00"), null, OffsetDateTime.now().minusMinutes(1));
        mockAttemptForComplete(checkoutSession, paymentAttempt(checkoutSession, PaymentAttemptStatus.pending, null, expiresAt));

        assertThrows(InvalidDataException.class,
                () -> paymentAttemptTransactionService.completeInitialization("PAY-1", gatewayResult("https://pay.test/checkout")));
    }

    @Test
    void completeInitialization_blankPaymentUrl_throwsException() {
        CheckoutSession checkoutSession = checkout(PaymentMethod.vnpay, CheckoutSessionStatus.reserved, 10L,
                money("120000.00"), null, expiresAt);
        mockAttemptForComplete(checkoutSession, paymentAttempt(checkoutSession, PaymentAttemptStatus.pending, null, expiresAt));

        assertThrows(InvalidDataException.class,
                () -> paymentAttemptTransactionService.completeInitialization("PAY-1", gatewayResult(" ")));
    }

    @Test
    void failInitialization_pendingAttempt_marksFailedAndReleasesReservation() {
        Voucher voucher = Voucher.builder().id(50L).code("SAVE10").build();
        CheckoutSession checkoutSession = checkout(PaymentMethod.vnpay, CheckoutSessionStatus.reserved, 10L,
                money("120000.00"), voucher, expiresAt);
        PaymentAttempt paymentAttempt = paymentAttempt(checkoutSession, PaymentAttemptStatus.pending, null, expiresAt);
        mockAttemptForComplete(checkoutSession, paymentAttempt);

        paymentAttemptTransactionService.failInitialization("PAY-1", "Gateway unavailable");

        assertEquals(PaymentAttemptStatus.failed, paymentAttempt.getStatus());
        assertEquals("Gateway unavailable", paymentAttempt.getFailureReason());
        assertNotNull(paymentAttempt.getFailedAt());
        assertEquals(CheckoutSessionStatus.failed, checkoutSession.getStatus());
        verify(inventoryReservationService).releaseStockReservation("CHK-1");
        verify(voucherService).releaseVoucherReservation("CHK-1");
    }

    @Test
    void failInitialization_releasesInventoryBeforeVoucher() {
        Voucher voucher = Voucher.builder().id(50L).code("SAVE10").build();
        CheckoutSession checkoutSession = checkout(PaymentMethod.vnpay, CheckoutSessionStatus.reserved, 10L,
                money("120000.00"), voucher, expiresAt);
        PaymentAttempt paymentAttempt = paymentAttempt(checkoutSession, PaymentAttemptStatus.pending, null, expiresAt);
        mockAttemptForComplete(checkoutSession, paymentAttempt);

        paymentAttemptTransactionService.failInitialization("PAY-1", "Gateway unavailable");

        InOrder inOrder = inOrder(inventoryReservationService, voucherService);
        inOrder.verify(inventoryReservationService).releaseStockReservation("CHK-1");
        inOrder.verify(voucherService).releaseVoucherReservation("CHK-1");
    }

    @Test
    void failInitialization_withoutVoucher_doesNotReleaseVoucher() {
        CheckoutSession checkoutSession = checkout(PaymentMethod.vnpay, CheckoutSessionStatus.reserved, 10L,
                money("120000.00"), null, expiresAt);
        PaymentAttempt paymentAttempt = paymentAttempt(checkoutSession, PaymentAttemptStatus.pending, null, expiresAt);
        mockAttemptForComplete(checkoutSession, paymentAttempt);

        paymentAttemptTransactionService.failInitialization("PAY-1", "Gateway unavailable");

        verify(voucherService, never()).releaseVoucherReservation(anyString());
    }

    @Test
    void failInitialization_completedAttempt_doesNotReleaseReservation() {
        CheckoutSession checkoutSession = checkout(PaymentMethod.vnpay, CheckoutSessionStatus.reserved, 10L,
                money("120000.00"), null, expiresAt);
        PaymentAttempt paymentAttempt = paymentAttempt(checkoutSession, PaymentAttemptStatus.completed, "https://pay.test/checkout", expiresAt);
        mockAttemptForComplete(checkoutSession, paymentAttempt);

        paymentAttemptTransactionService.failInitialization("PAY-1", "Gateway unavailable");

        verify(inventoryReservationService, never()).releaseStockReservation(anyString());
        verify(checkoutSessionRepository, never()).save(any());
    }

    private void mockCheckoutForCreate(CheckoutSession checkoutSession) {
        when(checkoutSessionRepository.findByCheckoutCodeForUpdate("CHK-1")).thenReturn(Optional.of(checkoutSession));
        lenient().when(paymentAttemptRepository.findTopByCheckoutSession_IdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.empty());
        lenient().when(paymentAttemptRepository.existsByPaymentReference(anyString())).thenReturn(false);
    }

    private void mockAttemptForComplete(CheckoutSession checkoutSession, PaymentAttempt paymentAttempt) {
        when(paymentAttemptRepository.findByPaymentReference("PAY-1")).thenReturn(Optional.of(paymentAttempt));
        when(checkoutSessionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(checkoutSession));
        when(paymentAttemptRepository.findByPaymentReferenceForUpdate("PAY-1")).thenReturn(Optional.of(paymentAttempt));
    }

    private PaymentAttempt captureSavedAttempt() {
        ArgumentCaptor<PaymentAttempt> captor = ArgumentCaptor.forClass(PaymentAttempt.class);
        verify(paymentAttemptRepository).save(captor.capture());
        return captor.getValue();
    }

    private GatewayPaymentCreationResult gatewayResult(String paymentUrl) {
        return new GatewayPaymentCreationResult(
                paymentUrl,
                "GTW-1",
                Map.of(
                        "publicKey", "visible",
                        "secretHash", "hidden"
                )
        );
    }

    private CheckoutSession checkout(
            PaymentMethod paymentMethod,
            CheckoutSessionStatus status,
            Long userId,
            BigDecimal amount,
            Voucher voucher,
            OffsetDateTime expiresAt
    ) {
        return CheckoutSession.builder()
                .id(1L)
                .checkoutCode("CHK-1")
                .user(User.builder().id(userId).build())
                .paymentMethod(paymentMethod)
                .status(status)
                .totalAmount(amount)
                .voucher(voucher)
                .expiresAt(expiresAt)
                .build();
    }

    private PaymentAttempt paymentAttempt(
            CheckoutSession checkoutSession,
            PaymentAttemptStatus status,
            String paymentUrl,
            OffsetDateTime expiresAt
    ) {
        return PaymentAttempt.builder()
                .id(100L)
                .paymentReference("PAY-1")
                .checkoutSession(checkoutSession)
                .method(checkoutSession.getPaymentMethod())
                .amount(checkoutSession.getTotalAmount())
                .status(status)
                .paymentUrl(paymentUrl)
                .expiresAt(expiresAt)
                .build();
    }

    private BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
