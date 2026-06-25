package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoIpnRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.MomoReturnResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentAttemptStatus;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.MomoCallbackParser;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.MomoCallbackVerifier;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentAttemptRepository;

@Service
@RequiredArgsConstructor
public class MomoReturnService {

    private final MomoCallbackParser callbackParser;
    private final MomoCallbackVerifier callbackVerifier;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final MomoIpnService ipnService;

    public MomoReturnResponseDTO handleReturn(MultiValueMap<String, String> parameters) {
        MomoIpnRequest request;
        try {
            request = callbackParser.parse(parameters);
        } catch (RuntimeException e) {
            return response(false, null, null, null, null, null, "invalid_callback", "Invalid MoMo callback");
        }

        if (!callbackVerifier.hasValidSignature(request) || !callbackVerifier.matchesConfiguredPartner(request)) {
            return response(
                    false,
                    request.orderId(),
                    request.resultCode(),
                    request.transId(),
                    null,
                    null,
                    "invalid_signature",
                    "Invalid MoMo callback signature"
            );
        }

        ipnService.handleIpn(request);

        PaymentAttempt paymentAttempt = paymentAttemptRepository
                .findByPaymentReferenceWithCheckoutSession(request.orderId())
                .orElse(null);
        if (paymentAttempt == null) {
            return response(
                    true,
                    request.orderId(),
                    request.resultCode(),
                    request.transId(),
                    null,
                    null,
                    "not_found",
                    "Payment attempt not found"
            );
        }
        if (!callbackVerifier.matchesAttempt(request, paymentAttempt)) {
            return response(
                    true,
                    request.orderId(),
                    request.resultCode(),
                    request.transId(),
                    paymentAttempt.getCheckoutSession().getCheckoutCode(),
                    paymentAttempt.getStatus(),
                    "invalid_callback",
                    "MoMo callback does not match payment attempt"
            );
        }

        return response(
                true,
                request.orderId(),
                request.resultCode(),
                firstNonBlank(paymentAttempt.getGatewayTransactionId(), request.transId()),
                paymentAttempt.getCheckoutSession().getCheckoutCode(),
                paymentAttempt.getStatus(),
                toDisplayStatus(paymentAttempt.getStatus()),
                "Payment status loaded"
        );
    }

    private MomoReturnResponseDTO response(
            boolean signatureValid,
            String paymentReference,
            Integer resultCode,
            String gatewayTransactionId,
            String checkoutCode,
            PaymentAttemptStatus attemptStatus,
            String paymentStatus,
            String message
    ) {
        return new MomoReturnResponseDTO(
                signatureValid,
                paymentReference,
                resultCode,
                gatewayTransactionId,
                checkoutCode,
                attemptStatus,
                paymentStatus,
                message
        );
    }

    private String firstNonBlank(String primary, String fallback) {
        return primary == null || primary.isBlank() ? fallback : primary;
    }

    private String toDisplayStatus(PaymentAttemptStatus status) {
        if (status == PaymentAttemptStatus.completed) {
            return "success";
        }
        if (status == PaymentAttemptStatus.failed) {
            return "failed";
        }
        if (status == PaymentAttemptStatus.requires_refund) {
            return "requires_refund";
        }
        if (status == PaymentAttemptStatus.pending) {
            return "processing";
        }
        return status == null ? "not_found" : status.name();
    }
}
