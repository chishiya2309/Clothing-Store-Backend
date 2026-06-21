package vn.hcmute.edu.dp.nhom10.backend.dto.payment;

import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OnlinePaymentInitializationResult(
        String checkoutCode,
        String paymentReference,
        PaymentMethod paymentMethod,
        String paymentUrl,
        BigDecimal amount,
        OffsetDateTime expiresAt
) {
}
