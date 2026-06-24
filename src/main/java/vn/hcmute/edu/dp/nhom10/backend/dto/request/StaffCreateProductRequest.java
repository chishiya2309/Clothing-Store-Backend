package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record StaffCreateProductRequest(
        @NotBlank
        @Size(max = 255)
        String name,

        String description,

        @Size(max = 255)
        String material,

        String careInstructions,

        @NotNull
        Long categoryId,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal basePrice,

        @DecimalMin(value = "0.00")
        BigDecimal salePrice,

        Boolean isFeatured,

        @Valid
        @NotEmpty
        List<StaffProductImageRequest> images,

        @Valid
        @NotEmpty
        List<StaffProductVariantRequest> variants
) {
}
