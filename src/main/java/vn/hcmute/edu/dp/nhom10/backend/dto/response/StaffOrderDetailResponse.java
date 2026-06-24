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
public class StaffOrderDetailResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String orderCode;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    private String shippingName;
    private String shippingPhone;
    private String shippingProvince;
    private String shippingDistrict;
    private String shippingWard;
    private String shippingAddress;

    private Long voucherId;
    private String voucherCode;

    private List<StaffOrderItemResponse> items;
    private StaffPaymentSummaryResponse payment;
    private List<StaffOrderStatusTimelineResponse> timeline;
}
