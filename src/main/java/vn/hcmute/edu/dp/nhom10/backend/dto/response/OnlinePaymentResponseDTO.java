package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OnlinePaymentResponseDTO(
        String paymentReference,
        String paymentUrl,
        BigDecimal amount,
        OffsetDateTime expiresAt
) implements Serializable {
}
