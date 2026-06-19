package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.config.payment.MomoProperties;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoIpnRequest;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;

@Component
@RequiredArgsConstructor
public class MomoCallbackVerifier {

    private final MomoProperties properties;
    private final MomoSignatureService signatureService;
    private final MomoAmountConverter amountConverter;

    public boolean hasValidSignature(MomoIpnRequest request) {
        if (request == null || request.signature() == null || request.signature().isBlank()) {
            return false;
        }
        String expected = signatureService.sign(
                properties.getSecretKey(),
                signatureService.ipnRawSignature(properties.getAccessKey(), request)
        );
        return signatureService.matches(expected, request.signature());
    }

    public boolean matchesConfiguredPartner(MomoIpnRequest request) {
        return request != null && properties.getPartnerCode() != null
                && properties.getPartnerCode().equals(request.partnerCode());
    }

    public boolean matchesAttempt(MomoIpnRequest request, PaymentAttempt paymentAttempt) {
        if (request == null || paymentAttempt == null) {
            return false;
        }
        return equals(paymentAttempt.getPaymentReference(), request.orderId())
                && equals(paymentAttempt.getPaymentReference(), request.requestId())
                && amountConverter.matches(request.amount(), paymentAttempt.getAmount());
    }

    private boolean equals(String expected, String actual) {
        return expected != null && expected.equals(actual);
    }
}
