package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;

public record ConfirmCheckoutRequestDTO(
        @NotNull(message = "Address ID is required")
        Long addressId,

        String voucherCode,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {
}
