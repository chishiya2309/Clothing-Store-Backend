package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StaffCancelOrderRequest(
        @NotBlank
        @Size(max = 500)
        String reason
) {
}
