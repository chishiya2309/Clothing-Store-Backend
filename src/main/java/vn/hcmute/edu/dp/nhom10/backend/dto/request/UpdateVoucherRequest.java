package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record UpdateVoucherRequest(
        @NotNull(message = "Discount type is required")
        DiscountType discountType,

        @NotNull(message = "Discount value is required")
        @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
        BigDecimal discountValue,

        @DecimalMin(value = "0.00", message = "Max discount amount must not be negative")
        BigDecimal maxDiscountAmount,

        @DecimalMin(value = "0.00", message = "Min order amount must not be negative")
        BigDecimal minOrderAmount,

        @NotNull(message = "Start date is required")
        OffsetDateTime startDate,

        @NotNull(message = "End date is required")
        OffsetDateTime endDate,

        @NotNull(message = "Usage limit is required")
        @Min(value = 1, message = "Usage limit must be at least 1")
        Integer usageLimit,

        @NotNull(message = "Active status is required")
        Boolean isActive
) {
}
