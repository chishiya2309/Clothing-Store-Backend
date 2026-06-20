package vn.hcmute.edu.dp.nhom10.backend.dto.payment;

import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PendingPaymentContext(
        Long checkoutSessionId,
        String checkoutCode,
        String paymentReference,
        PaymentMethod paymentMethod,
        BigDecimal amount,
        OffsetDateTime expiresAt,
        String paymentUrl
) {
    public boolean hasPaymentUrl() {
        return paymentUrl != null && !paymentUrl.isBlank();
    }
}
