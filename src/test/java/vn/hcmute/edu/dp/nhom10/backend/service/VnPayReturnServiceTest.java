package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.VnPayCallbackData;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.VnPayReturnResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentAttemptStatus;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.VnPayCallbackParser;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.VnPayCallbackVerifier;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentAttemptRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.VnPayIpnService;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.VnPayReturnService;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VnPayReturnServiceTest {

    @Mock
    private VnPayCallbackParser callbackParser;

    @Mock
    private VnPayCallbackVerifier callbackVerifier;

    @Mock
    private PaymentAttemptRepository paymentAttemptRepository;

    @Mock
    private VnPayIpnService ipnService;

    @InjectMocks
    private VnPayReturnService returnService;

    @Test
    void handleReturn_invalidSignature_returnsInvalidSignatureWithoutLookup() {
        VnPayCallbackData data = callbackData();
        when(callbackParser.parse(org.mockito.ArgumentMatchers.any())).thenReturn(data);
        when(callbackVerifier.hasValidSignature(data)).thenReturn(false);

        VnPayReturnResponseDTO response = returnService.handleReturn(new LinkedMultiValueMap<>());

        assertFalse(response.signatureValid());
        assertEquals("invalid_signature", response.paymentStatus());
        verify(paymentAttemptRepository, never()).findByPaymentReferenceWithCheckoutSession(org.mockito.ArgumentMatchers.any());
        verify(ipnService, never()).handleIpn(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void handleReturn_pendingAttempt_returnsProcessing() {
        VnPayCallbackData data = callbackData();
        PaymentAttempt attempt = PaymentAttempt.builder()
                .paymentReference("PAY-1")
                .checkoutSession(CheckoutSession.builder().checkoutCode("CHK-1").build())
                .status(PaymentAttemptStatus.pending)
                .build();
        when(callbackParser.parse(org.mockito.ArgumentMatchers.any())).thenReturn(data);
        when(callbackVerifier.hasValidSignature(data)).thenReturn(true);
        when(callbackVerifier.hasValidTerminalCode(data)).thenReturn(true);
        when(paymentAttemptRepository.findByPaymentReferenceWithCheckoutSession("PAY-1")).thenReturn(Optional.of(attempt));

        VnPayReturnResponseDTO response = returnService.handleReturn(new LinkedMultiValueMap<>());

        assertTrue(response.signatureValid());
        assertEquals("processing", response.paymentStatus());
        assertEquals("CHK-1", response.checkoutCode());
        verify(ipnService).handleIpn(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void handleReturn_completedAttempt_returnsSuccess() {
        VnPayCallbackData data = callbackData();
        PaymentAttempt attempt = PaymentAttempt.builder()
                .paymentReference("PAY-1")
                .checkoutSession(CheckoutSession.builder().checkoutCode("CHK-1").build())
                .status(PaymentAttemptStatus.completed)
                .gatewayTransactionId("GTW-1")
                .build();
        when(callbackParser.parse(org.mockito.ArgumentMatchers.any())).thenReturn(data);
        when(callbackVerifier.hasValidSignature(data)).thenReturn(true);
        when(callbackVerifier.hasValidTerminalCode(data)).thenReturn(true);
        when(paymentAttemptRepository.findByPaymentReferenceWithCheckoutSession("PAY-1")).thenReturn(Optional.of(attempt));

        VnPayReturnResponseDTO response = returnService.handleReturn(new LinkedMultiValueMap<>());

        assertEquals("success", response.paymentStatus());
        assertEquals("GTW-1", response.gatewayTransactionId());
        assertEquals("CHK-1", response.checkoutCode());
    }

    private VnPayCallbackData callbackData() {
        return new VnPayCallbackData(
                "12000000",
                null,
                null,
                null,
                null,
                null,
                "00",
                "TEST_TMN_CODE",
                "GTW-1",
                "00",
                "PAY-1",
                "hash",
                Map.of("vnp_TxnRef", "PAY-1")
        );
    }
}
