package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentStatus;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class StaffPaymentSummaryResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private PaymentMethod method;
    private PaymentStatus status;
    private BigDecimal amount;
    private String transactionId;
    private OffsetDateTime paidAt;
    private OffsetDateTime createdAt;
}
