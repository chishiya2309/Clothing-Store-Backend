package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StaffUpdateStockRequest(
        @NotNull
        @Min(0)
        Integer stockQuantity
) {
}
