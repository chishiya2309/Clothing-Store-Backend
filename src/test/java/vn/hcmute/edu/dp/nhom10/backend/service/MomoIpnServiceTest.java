package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoIpnRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoIpnTransactionResult;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentAttemptStatus;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.MomoCallbackVerifier;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentAttemptRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.MomoIpnService;
import vn.hcmute.edu.dp.nhom10.backend.service.internal.MomoIpnTransactionService;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MomoIpnServiceTest {

    @Mock
    private MomoCallbackVerifier callbackVerifier;

    @Mock
    private PaymentAttemptRepository paymentAttemptRepository;

    @Mock
    private MomoIpnTransactionService ipnTransactionService;

    @InjectMocks
    private MomoIpnService ipnService;

    @Test
    void handleIpn_invalidSignatureReturnsFalseWithoutTransaction() {
        MomoIpnRequest request = request(0);
        when(callbackVerifier.hasValidSignature(request)).thenReturn(false);

        assertThat(ipnService.handleIpn(request)).isFalse();

        verify(ipnTransactionService, never()).process(request);
    }

    @Test
    void handleIpn_attemptMismatchReturnsFalseWithoutTransaction() {
        MomoIpnRequest request = request(0);
        PaymentAttempt attempt = attempt();
        verified(request);
        when(paymentAttemptRepository.findByPaymentReference("PAY-1")).thenReturn(Optional.of(attempt));
        when(callbackVerifier.matchesAttempt(request, attempt)).thenReturn(false);

        assertThat(ipnService.handleIpn(request)).isFalse();

        verify(ipnTransactionService, never()).process(request);
    }

    @Test
    void handleIpn_validCallbackDelegatesToTransactionAndReturnsAccepted() {
        MomoIpnRequest request = request(0);
        PaymentAttempt attempt = attempt();
        verified(request);
        when(paymentAttemptRepository.findByPaymentReference("PAY-1")).thenReturn(Optional.of(attempt));
        when(callbackVerifier.matchesAttempt(request, attempt)).thenReturn(true);
        when(ipnTransactionService.process(request)).thenReturn(MomoIpnTransactionResult.accepted("ok"));

        assertThat(ipnService.handleIpn(request)).isTrue();
    }

    private void verified(MomoIpnRequest request) {
        when(callbackVerifier.hasValidSignature(request)).thenReturn(true);
        when(callbackVerifier.matchesConfiguredPartner(request)).thenReturn(true);
    }

    private PaymentAttempt attempt() {
        return PaymentAttempt.builder()
                .paymentReference("PAY-1")
                .amount(new BigDecimal("100000.00"))
                .status(PaymentAttemptStatus.pending)
                .build();
    }

    private MomoIpnRequest request(int resultCode) {
        return new MomoIpnRequest(
                "TEST_PARTNER",
                "PAY-1",
                "PAY-1",
                100000,
                "Thanh toan don hang CHK-1",
                "momo_wallet",
                "TRANS-1",
                resultCode,
                "Successful.",
                "qr",
                1718770000000L,
                "",
                "signature"
        );
    }
}
