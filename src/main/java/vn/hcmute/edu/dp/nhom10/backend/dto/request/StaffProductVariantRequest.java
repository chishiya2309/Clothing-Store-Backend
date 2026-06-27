package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record StaffProductVariantRequest(
        Long id,

        @NotBlank(message = "Kích thước không được để trống")
        @Size(max = 10, message = "Kích thước không được vượt quá 10 ký tự")
        String size,

        @NotBlank(message = "Màu sắc không được để trống")
        @Size(max = 50, message = "Màu sắc không được vượt quá 50 ký tự")
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
