package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateFlashSaleActivationRequest(
        @NotNull(message = "Active status is required")
        Boolean isActive
) {
}
