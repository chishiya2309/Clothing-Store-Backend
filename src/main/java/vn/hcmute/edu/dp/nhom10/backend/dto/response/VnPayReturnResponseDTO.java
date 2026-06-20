package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentAttemptStatus;

public record VnPayReturnResponseDTO(
        boolean signatureValid,
        String paymentReference,
        String responseCode,
        String transactionStatus,
        String gatewayTransactionId,
        PaymentAttemptStatus paymentAttemptStatus,
        String paymentStatus,
        String message
) {
}
