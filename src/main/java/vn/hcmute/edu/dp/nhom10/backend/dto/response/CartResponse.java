package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class CartResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<CartItemResponse> items;
    private BigDecimal totalAmount;
}
