package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record StaffProductVariantRequest(
        Long id,

        @NotBlank
        @Size(max = 10)
        String size,

        @NotBlank
        @Size(max = 50)
        String color,

        @NotNull
        @Min(0)
        Integer stockQuantity,

        @NotNull
        @DecimalMin(value = "0.00")
        BigDecimal additionalPrice,

        Boolean isActive
) {
}
