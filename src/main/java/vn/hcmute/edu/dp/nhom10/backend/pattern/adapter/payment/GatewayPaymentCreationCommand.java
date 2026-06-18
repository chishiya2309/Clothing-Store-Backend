package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record GatewayPaymentCreationCommand(
        String paymentReference,
        String checkoutCode,
        BigDecimal amount,
        OffsetDateTime expiresAt,
        String returnUrl,
        String callbackUrl
) {
}
