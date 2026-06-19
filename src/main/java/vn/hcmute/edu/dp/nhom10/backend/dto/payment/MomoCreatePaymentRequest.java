package vn.hcmute.edu.dp.nhom10.backend.dto.payment;

public record MomoCreatePaymentRequest(
        String partnerCode,
        String requestId,
        long amount,
        String orderId,
        String orderInfo,
        String redirectUrl,
        String ipnUrl,
        String requestType,
        String extraData,
        String lang,
        boolean autoCapture,
        String signature
) {
}
