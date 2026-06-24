package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class StaffOrderItemResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long productVariantId;
    private String sku;
    private String productName;
    private String variantInfo;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
}
