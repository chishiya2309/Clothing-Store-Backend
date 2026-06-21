package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.config.payment.MomoProperties;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoCreatePaymentRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoCreatePaymentResponse;

@Component
@RequiredArgsConstructor
public class MomoCreateResponseVerifier {

    private final MomoSignatureService signatureService;

    public boolean isValidSuccess(
            MomoProperties properties,
            MomoCreatePaymentRequest request,
            MomoCreatePaymentResponse response
    ) {
        if (response == null || response.signature() == null || response.signature().isBlank()) {
            return false;
        }
        if (response.resultCode() != 0 || response.payUrl() == null || response.payUrl().isBlank()) {
            return false;
        }
        if (!equals(properties.getPartnerCode(), response.partnerCode())
                || !equals(request.orderId(), response.orderId())
                || !equals(request.requestId(), response.requestId())
                || request.amount() != response.amount()) {
            return false;
        }
        String expectedSignature = signatureService.sign(
                properties.getSecretKey(),
                signatureService.createResponseRawSignature(properties.getAccessKey(), response)
        );
        return signatureService.matches(expectedSignature, response.signature());
    }

    private boolean equals(String expected, String actual) {
        return expected != null && expected.equals(actual);
    }
}
