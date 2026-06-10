package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record VoucherResponse(
        Long id,
        String code,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal maxDiscountAmount,
        BigDecimal minOrderAmount,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        Integer usageLimit,
        Integer timesUsed,
        Boolean isActive,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
