package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentAttemptStatus;

import java.io.Serializable;

public record MomoReturnResponseDTO(
        boolean signatureValid,
        String paymentReference,
        Integer resultCode,
        String gatewayTransactionId,
        PaymentAttemptStatus attemptStatus,
        String paymentStatus,
        String message
) implements Serializable {
}
