package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.VnPayCallbackData;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.VnPayReturnResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentAttemptStatus;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.VnPayCallbackParser;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.VnPayCallbackVerifier;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentAttemptRepository;

@Service
@RequiredArgsConstructor
public class VnPayReturnService {

    private final VnPayCallbackParser callbackParser;
    private final VnPayCallbackVerifier callbackVerifier;
    private final PaymentAttemptRepository paymentAttemptRepository;

    public VnPayReturnResponseDTO handleReturn(MultiValueMap<String, String> parameters) {
        VnPayCallbackData callbackData;
        try {
            callbackData = callbackParser.parse(parameters);
        } catch (RuntimeException e) {
            return new VnPayReturnResponseDTO(
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "invalid_callback",
                    "Invalid VNPay callback"
            );
        }

        boolean signatureValid = callbackVerifier.hasValidSignature(callbackData)
                && callbackVerifier.hasValidTerminalCode(callbackData);
        if (!signatureValid) {
            return new VnPayReturnResponseDTO(
                    false,
                    callbackData.paymentReference(),
                    callbackData.responseCode(),
                    callbackData.transactionStatus(),
                    callbackData.transactionNumber(),
                    null,
                    "invalid_signature",
                    "Invalid VNPay callback signature"
            );
        }

        PaymentAttempt paymentAttempt = paymentAttemptRepository
                .findByPaymentReference(callbackData.paymentReference())
                .orElse(null);
        if (paymentAttempt == null) {
            return new VnPayReturnResponseDTO(
                    true,
                    callbackData.paymentReference(),
                    callbackData.responseCode(),
                    callbackData.transactionStatus(),
                    callbackData.transactionNumber(),
                    null,
                    "not_found",
                    "Payment attempt not found"
            );
        }

        return new VnPayReturnResponseDTO(
                true,
                callbackData.paymentReference(),
                callbackData.responseCode(),
                callbackData.transactionStatus(),
                paymentAttempt.getGatewayTransactionId(),
                paymentAttempt.getStatus(),
                toDisplayStatus(paymentAttempt.getStatus()),
                "Payment status loaded"
        );
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
