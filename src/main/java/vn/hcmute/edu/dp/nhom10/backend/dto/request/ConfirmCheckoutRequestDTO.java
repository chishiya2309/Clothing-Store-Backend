package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;

public record ConfirmCheckoutRequestDTO(
        @NotNull(message = "Address ID is required")
        Long addressId,

        String voucherCode,

        String productVoucherCode,

        String shippingVoucherCode,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {
    public ConfirmCheckoutRequestDTO(Long addressId, String voucherCode, PaymentMethod paymentMethod) {
        this(addressId, voucherCode, null, null, paymentMethod);
    }
}
