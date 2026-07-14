package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpsertFlashSaleItemRequest(
        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "Flash sale price is required")
        @DecimalMin(value = "0.00", message = "Flash sale price must not be negative")
        BigDecimal flashSalePrice,

        @NotNull(message = "Quota is required")
        @Min(value = 1, message = "Quota must be at least 1")
        Integer quota
) {
}
