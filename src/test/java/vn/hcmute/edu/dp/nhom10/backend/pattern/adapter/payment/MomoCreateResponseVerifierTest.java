package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.config.payment.MomoProperties;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoCreatePaymentRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoCreatePaymentResponse;

import static org.assertj.core.api.Assertions.assertThat;

class MomoCreateResponseVerifierTest {

    private final MomoSignatureService signatureService = new MomoSignatureService();
    private final MomoCreateResponseVerifier verifier = new MomoCreateResponseVerifier(signatureService);

    @Test
    void isValidSuccess_rejectsSignatureAndIdentityMismatches() {
        MomoProperties properties = properties();
        MomoCreatePaymentRequest request = request();
        MomoCreatePaymentResponse response = signedResponse(properties, 100000, "PAY-1", "PAY-1");

        assertThat(verifier.isValidSuccess(properties, request, response)).isTrue();
        assertThat(verifier.isValidSuccess(properties, request, responseWithSignature(response, null))).isTrue();
        assertThat(verifier.isValidSuccess(properties, request, signedResponse(properties, 100001, "PAY-1", "PAY-1"))).isFalse();
        assertThat(verifier.isValidSuccess(properties, request, signedResponse(properties, 100000, "PAY-2", "PAY-1"))).isFalse();
        assertThat(verifier.isValidSuccess(properties, request, signedResponse(properties, 100000, "PAY-1", "REQ-2"))).isFalse();
        assertThat(verifier.isValidSuccess(properties, request, responseWithSignature(response, "bad"))).isFalse();
    }

    private MomoCreatePaymentResponse signedResponse(
            MomoProperties properties,
            long amount,
            String orderId,
            String requestId
    ) {
        MomoCreatePaymentResponse unsigned = new MomoCreatePaymentResponse(
                "TEST_PARTNER",
                requestId,
                amount,
                orderId,
                "Successful.",
                0,
                "https://test-payment.momo.vn/pay/PAY-1",
                1718770000000L,
                null
        );
        String signature = signatureService.sign(
                properties.getSecretKey(),
                signatureService.createResponseRawSignature(properties.getAccessKey(), unsigned)
        );
        return responseWithSignature(unsigned, signature);
    }

    private MomoCreatePaymentResponse responseWithSignature(
            MomoCreatePaymentResponse response,
            String signature
    ) {
        return new MomoCreatePaymentResponse(
                response.partnerCode(),
                response.requestId(),
                response.amount(),
                response.orderId(),
                response.message(),
                response.resultCode(),
                response.payUrl(),
                response.responseTime(),
                signature
        );
    }

    private MomoCreatePaymentRequest request() {
        return new MomoCreatePaymentRequest(
                "TEST_PARTNER",
                "PAY-1",
                100000,
                "PAY-1",
                "Thanh toan don hang CHK-1",
                "https://example.test/api/payments/momo/return",
                "https://example.test/api/payments/momo/ipn",
                "captureWallet",
                "",
                "vi",
                true,
                "signature"
        );
    }

    private MomoProperties properties() {
        MomoProperties properties = new MomoProperties();
        properties.setPartnerCode("TEST_PARTNER");
        properties.setAccessKey("test-access");
        properties.setSecretKey("test-secret");
        return properties;
    }
}
