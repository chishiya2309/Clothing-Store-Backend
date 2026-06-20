package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.VnPayCallbackData;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.VnPayIpnTransactionResult;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.VnPayIpnResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentAttemptStatus;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.VnPayAmountMatcher;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.VnPayCallbackParser;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.VnPayCallbackVerifier;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentAttemptRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.VnPayIpnService;
import vn.hcmute.edu.dp.nhom10.backend.service.internal.VnPayIpnTransactionService;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VnPayIpnServiceTest {

    @Mock
    private VnPayCallbackParser callbackParser;

    @Mock
    private VnPayCallbackVerifier callbackVerifier;

    @Mock
    private VnPayAmountMatcher amountMatcher;

    @Mock
    private PaymentAttemptRepository paymentAttemptRepository;

    @Mock
    private VnPayIpnTransactionService ipnTransactionService;

    @InjectMocks
    private VnPayIpnService ipnService;

    @Test
    void handleIpn_invalidSignature_returns97() {
        VnPayCallbackData data = callbackData(true);
        when(callbackParser.parse(org.mockito.ArgumentMatchers.any())).thenReturn(data);
        when(callbackVerifier.hasValidSignature(data)).thenReturn(false);

        assertEquals("97", ipnService.handleIpn(new LinkedMultiValueMap<>()).rspCode());
    }

    @Test
    void handleIpn_attemptNotFound_returns01() {
        VnPayCallbackData data = callbackData(true);
        mockVerified(data);
        when(paymentAttemptRepository.findByPaymentReference("PAY-1")).thenReturn(Optional.empty());

        assertEquals("01", ipnService.handleIpn(new LinkedMultiValueMap<>()).rspCode());
    }

    @Test
    void handleIpn_amountMismatch_returns04() {
        VnPayCallbackData data = callbackData(true);
        PaymentAttempt attempt = attempt(PaymentAttemptStatus.pending);
        mockVerified(data);
        when(paymentAttemptRepository.findByPaymentReference("PAY-1")).thenReturn(Optional.of(attempt));
        when(amountMatcher.matches("12000000", attempt.getAmount())).thenReturn(false);

        assertEquals("04", ipnService.handleIpn(new LinkedMultiValueMap<>()).rspCode());
    }

    @Test
    void handleIpn_duplicateCompleted_returns02() {
        VnPayCallbackData data = callbackData(true);
        PaymentAttempt attempt = attempt(PaymentAttemptStatus.completed);
        mockVerified(data);
        when(paymentAttemptRepository.findByPaymentReference("PAY-1")).thenReturn(Optional.of(attempt));
        when(amountMatcher.matches("12000000", attempt.getAmount())).thenReturn(true);

        VnPayIpnResponse response = ipnService.handleIpn(new LinkedMultiValueMap<>());

        assertEquals("02", response.rspCode());
        assertEquals("Order already confirmed", response.message());
    }

    @Test
    void handleIpn_firstSuccess_returns00() {
        VnPayCallbackData data = callbackData(true);
        PaymentAttempt attempt = attempt(PaymentAttemptStatus.pending);
        mockVerified(data);
        when(paymentAttemptRepository.findByPaymentReference("PAY-1")).thenReturn(Optional.of(attempt));
        when(amountMatcher.matches("12000000", attempt.getAmount())).thenReturn(true);
        when(ipnTransactionService.process(data)).thenReturn(VnPayIpnTransactionResult.confirmed());

        assertEquals("00", ipnService.handleIpn(new LinkedMultiValueMap<>()).rspCode());
    }

    @Test
    void handleIpn_unexpectedTransactionError_returns99() {
        VnPayCallbackData data = callbackData(true);
        PaymentAttempt attempt = attempt(PaymentAttemptStatus.pending);
        mockVerified(data);
        when(paymentAttemptRepository.findByPaymentReference("PAY-1")).thenReturn(Optional.of(attempt));
        when(amountMatcher.matches("12000000", attempt.getAmount())).thenReturn(true);
        when(ipnTransactionService.process(data)).thenThrow(new IllegalStateException("database error"));

        assertEquals("99", ipnService.handleIpn(new LinkedMultiValueMap<>()).rspCode());
    }

    private void mockVerified(VnPayCallbackData data) {
        when(callbackParser.parse(org.mockito.ArgumentMatchers.any())).thenReturn(data);
        when(callbackVerifier.hasValidSignature(data)).thenReturn(true);
        when(callbackVerifier.hasValidTerminalCode(data)).thenReturn(true);
    }

    private PaymentAttempt attempt(PaymentAttemptStatus status) {
        return PaymentAttempt.builder()
                .paymentReference("PAY-1")
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
