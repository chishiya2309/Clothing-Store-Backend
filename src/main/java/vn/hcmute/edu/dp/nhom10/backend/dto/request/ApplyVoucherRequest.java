package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import vn.hcmute.edu.dp.nhom10.backend.enums.VoucherSlot;

import java.math.BigDecimal;

public record ApplyVoucherRequest(
        @NotBlank(message = "Voucher code must not be blank")
        @Size(max = 50, message = "Voucher code must not exceed 50 characters")
        String code,

        VoucherSlot slot,

        @NotNull(message = "Subtotal is required")
        @DecimalMin(value = "0.00", message = "Subtotal must not be negative")
        BigDecimal subtotal,

        @DecimalMin(value = "0.00", message = "Shipping fee must not be negative")
        BigDecimal shippingFee
) {
    public ApplyVoucherRequest(String code, BigDecimal subtotal, BigDecimal shippingFee) {
        this(code, VoucherSlot.product, subtotal, shippingFee);
    }
}
