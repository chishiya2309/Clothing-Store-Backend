package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddToCartRequest(
        @NotNull(message = "Product ID is required")
        Long productId,

        @NotBlank(message = "Size is required")
        String size,

        @NotBlank(message = "Color is required")
        String color,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity
) {
}
