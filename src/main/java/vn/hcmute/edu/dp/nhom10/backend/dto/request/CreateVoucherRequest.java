package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateVoucherRequest(
        @NotBlank(message = "Voucher code must not be blank")
        @Size(max = 50, message = "Voucher code must not exceed 50 characters")
        String code,

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
        @Future(message = "End date must be in the future")
        OffsetDateTime endDate,

        @NotNull(message = "Usage limit is required")
        @Min(value = 1, message = "Usage limit must be at least 1")
        Integer usageLimit,

        Boolean isActive
) {
}
