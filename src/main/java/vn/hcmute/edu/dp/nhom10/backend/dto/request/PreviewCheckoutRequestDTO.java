package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record PreviewCheckoutRequestDTO(
        @NotNull(message = "Address ID is required")
        Long addressId,

        String voucherCode
) {
}
