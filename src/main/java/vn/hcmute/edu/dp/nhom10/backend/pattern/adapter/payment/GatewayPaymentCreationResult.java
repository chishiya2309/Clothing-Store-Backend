package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import java.util.Map;

public record GatewayPaymentCreationResult(
        String paymentUrl,
        String gatewayTransactionId,
        Map<String, Object> gatewayPayload
) {
}
