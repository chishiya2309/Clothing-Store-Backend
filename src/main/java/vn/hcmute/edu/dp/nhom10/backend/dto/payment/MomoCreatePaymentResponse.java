package vn.hcmute.edu.dp.nhom10.backend.dto.payment;

public record MomoCreatePaymentResponse(
        String partnerCode,
        String requestId,
        long amount,
        String orderId,
        String message,
        int resultCode,
        String payUrl,
        Long responseTime,
        String signature
) {
}
