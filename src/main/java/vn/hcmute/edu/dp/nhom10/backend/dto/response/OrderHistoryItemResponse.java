package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;

import java.io.Serial;
import java.io.Serializable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class OrderHistoryItemResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String orderCode;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private OrderStatus status;
    private OffsetDateTime createdAt;
    private int itemCount;
    private List<OrderHistoryProductImage> productImages;

    @Getter
    @Setter
    @Builder
    public static class OrderHistoryProductImage implements Serializable {
        @Serial
        private static final long serialVersionUID = 2L;
        private String imageUrl;
        private String productName;
    }
}
