package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record PreviewCheckoutRequestDTO(
        @NotNull(message = "Address ID is required")
        Long addressId,

        String voucherCode,

        String productVoucherCode,

        String shippingVoucherCode
) {
    public PreviewCheckoutRequestDTO(Long addressId, String voucherCode) {
        this(addressId, voucherCode, null, null);
    }
}
