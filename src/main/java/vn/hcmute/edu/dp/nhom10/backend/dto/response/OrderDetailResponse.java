package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentStatus;

import java.io.Serial;
import java.io.Serializable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class OrderDetailResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String orderCode;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private OffsetDateTime createdAt;
    private String note;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;

    // Shipping snapshot
    private String shippingName;
    private String shippingPhone;
    private String shippingProvince;
    private String shippingDistrict;
    private String shippingWard;
    private String shippingAddress;

    // Items
    private List<OrderDetailItemResponse> items;

    @Getter
    @Setter
    @Builder
    public static class OrderDetailItemResponse implements Serializable {
        @Serial
        private static final long serialVersionUID = 2L;
        private Long id;
        private String productName;
        private String variantInfo;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String imageUrl;
        private String productSlug;
    }
}
