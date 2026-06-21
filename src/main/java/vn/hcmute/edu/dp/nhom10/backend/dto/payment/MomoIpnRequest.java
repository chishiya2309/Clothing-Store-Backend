package vn.hcmute.edu.dp.nhom10.backend.dto.payment;

public record MomoIpnRequest(
        String partnerCode,
        String orderId,
        String requestId,
        long amount,
        String orderInfo,
        String orderType,
        String transId,
        int resultCode,
        String message,
        String payType,
        long responseTime,
        String extraData,
        String signature
) {
}
