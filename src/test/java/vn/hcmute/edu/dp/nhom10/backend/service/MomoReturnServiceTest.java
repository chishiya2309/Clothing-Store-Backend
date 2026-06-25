package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoIpnRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.MomoReturnResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentAttemptStatus;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.MomoCallbackParser;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.MomoCallbackVerifier;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentAttemptRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.MomoIpnService;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.MomoReturnService;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MomoReturnServiceTest {

    @Mock
    private MomoCallbackParser callbackParser;

    @Mock
    private MomoCallbackVerifier callbackVerifier;

    @Mock
    private PaymentAttemptRepository paymentAttemptRepository;

    @Mock
    private MomoIpnService ipnService;

    @InjectMocks
    private MomoReturnService returnService;

    @Test
    void handleReturn_pendingAttemptReturnsProcessingAndDoesNotFinalize() {
        MomoIpnRequest request = request();
        PaymentAttempt attempt = PaymentAttempt.builder()
                .paymentReference("PAY-1")
                .amount(new BigDecimal("100000.00"))
                .status(PaymentAttemptStatus.pending)
                .checkoutSession(CheckoutSession.builder().checkoutCode("CHK-1").build())
                .build();
        when(callbackParser.parse(org.mockito.ArgumentMatchers.any())).thenReturn(request);
        when(callbackVerifier.hasValidSignature(request)).thenReturn(true);
        when(callbackVerifier.matchesConfiguredPartner(request)).thenReturn(true);
        when(paymentAttemptRepository.findByPaymentReferenceWithCheckoutSession("PAY-1")).thenReturn(Optional.of(attempt));
        when(callbackVerifier.matchesAttempt(request, attempt)).thenReturn(true);

        MomoReturnResponseDTO response = returnService.handleReturn(new LinkedMultiValueMap<>());

        assertThat(response.signatureValid()).isTrue();
        assertThat(response.paymentStatus()).isEqualTo("processing");
        assertThat(response.checkoutCode()).isEqualTo("CHK-1");
        assertThat(response.gatewayTransactionId()).isEqualTo("TRANS-1");
        verify(ipnService).handleIpn(request);
    }

    @Test
    void handleReturn_invalidSignatureReturnsInvalidSignature() {
        MomoIpnRequest request = request();
        when(callbackParser.parse(org.mockito.ArgumentMatchers.any())).thenReturn(request);
        when(callbackVerifier.hasValidSignature(request)).thenReturn(false);

        MomoReturnResponseDTO response = returnService.handleReturn(new LinkedMultiValueMap<>());

        assertThat(response.signatureValid()).isFalse();
        assertThat(response.paymentStatus()).isEqualTo("invalid_signature");
        verify(ipnService, never()).handleIpn(any());
    }

    private MomoIpnRequest request() {
        return new MomoIpnRequest(
                "TEST_PARTNER",
                "PAY-1",
                "PAY-1",
                100000,
                "Thanh toan don hang CHK-1",
                "momo_wallet",
                "TRANS-1",
                0,
                "Successful.",
                "qr",
                1718770000000L,
                "",
                "signature"
        );
    }
}
