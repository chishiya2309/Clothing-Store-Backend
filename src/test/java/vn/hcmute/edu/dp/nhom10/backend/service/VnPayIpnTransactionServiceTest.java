package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.VnPayCallbackData;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.VnPayIpnTransactionResult;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.enums.CheckoutSessionStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentAttemptStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.VnPayAmountMatcher;
import vn.hcmute.edu.dp.nhom10.backend.repository.CartItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.InventoryReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentAttemptRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.internal.VnPayIpnTransactionService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VnPayIpnTransactionServiceTest {

    @Mock
    private PaymentAttemptRepository paymentAttemptRepository;

    @Mock
    private CheckoutSessionRepository checkoutSessionRepository;

    @Mock
    private CheckoutSessionItemRepository checkoutSessionItemRepository;

    @Mock
    private InventoryReservationRepository inventoryReservationRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private VoucherReservationRepository voucherReservationRepository;

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private VnPayAmountMatcher amountMatcher;

    @Mock
    private OrderStatusHistoryService orderStatusHistoryService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private VnPayIpnTransactionService transactionService;

    @Test
    void process_completedAttempt_returnsAlreadyProcessedWithoutCreatingOrder() {
        CheckoutSession checkoutSession = checkoutSession();
        PaymentAttempt attempt = attempt(checkoutSession, PaymentAttemptStatus.completed);
        mockLockedAttempt(checkoutSession, attempt);
        when(amountMatcher.matches("12000000", attempt.getAmount())).thenReturn(true);

        VnPayIpnTransactionResult result = transactionService.process(callbackData(true));

        assertEquals(VnPayIpnTransactionResult.Code.ALREADY_PROCESSED, result.code());
        verify(orderRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void process_successAfterExpiredAttempt_marksRequiresRefund() {
        CheckoutSession checkoutSession = checkoutSession();
        checkoutSession.setStatus(CheckoutSessionStatus.expired);
        checkoutSession.setExpiresAt(OffsetDateTime.now().minusMinutes(1));
        PaymentAttempt attempt = attempt(checkoutSession, PaymentAttemptStatus.expired);
        mockLockedAttempt(checkoutSession, attempt);
        when(amountMatcher.matches("12000000", attempt.getAmount())).thenReturn(true);

        VnPayIpnTransactionResult result = transactionService.process(callbackData(true));

        assertEquals(VnPayIpnTransactionResult.Code.CONFIRMED, result.code());
        ArgumentCaptor<PaymentAttempt> captor = ArgumentCaptor.forClass(PaymentAttempt.class);
        verify(paymentAttemptRepository).save(captor.capture());
        assertEquals(PaymentAttemptStatus.requires_refund, captor.getValue().getStatus());
        assertEquals("GTW-1", captor.getValue().getGatewayTransactionId());
        verify(orderRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private void mockLockedAttempt(CheckoutSession checkoutSession, PaymentAttempt attempt) {
        when(paymentAttemptRepository.findCheckoutSessionIdByPaymentReference("PAY-1")).thenReturn(Optional.of(1L));
        when(checkoutSessionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(checkoutSession));
        when(paymentAttemptRepository.findByPaymentReferenceForUpdate("PAY-1")).thenReturn(Optional.of(attempt));
    }

    private CheckoutSession checkoutSession() {
        return CheckoutSession.builder()
                .id(1L)
                .checkoutCode("CHK-1")
                .user(User.builder().id(10L).build())
                .paymentMethod(PaymentMethod.vnpay)
                .status(CheckoutSessionStatus.reserved)
                .expiresAt(OffsetDateTime.now().plusMinutes(10))
                .totalAmount(new BigDecimal("120000.00"))
                .build();
    }

    private PaymentAttempt attempt(CheckoutSession checkoutSession, PaymentAttemptStatus status) {
        return PaymentAttempt.builder()
                .paymentReference("PAY-1")
                .checkoutSession(checkoutSession)
                .method(PaymentMethod.vnpay)
                .amount(new BigDecimal("120000.00"))
                .status(status)
                .build();
    }

    private VnPayCallbackData callbackData(boolean success) {
        return new VnPayCallbackData(
                "12000000",
                null,
                null,
                null,
                null,
                null,
                success ? "00" : "24",
                "TEST_TMN_CODE",
                "GTW-1",
                success ? "00" : "02",
                "PAY-1",
                "hash",
                Map.of("vnp_TxnRef", "PAY-1")
        );
    }
}
