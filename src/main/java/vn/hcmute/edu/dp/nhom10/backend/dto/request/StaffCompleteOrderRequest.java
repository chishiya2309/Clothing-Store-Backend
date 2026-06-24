package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderCompletionSource;

public record StaffCompleteOrderRequest(
        @NotNull
        OrderCompletionSource confirmationSource,

        @NotBlank
        @Size(max = 500)
        String note
) {
}
