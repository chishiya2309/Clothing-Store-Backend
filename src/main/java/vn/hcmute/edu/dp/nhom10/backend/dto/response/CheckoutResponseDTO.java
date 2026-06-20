package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class CheckoutResponseDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String checkoutCode;
    private String paymentReference;
    private String paymentUrl;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private OffsetDateTime expiresAt;
}
