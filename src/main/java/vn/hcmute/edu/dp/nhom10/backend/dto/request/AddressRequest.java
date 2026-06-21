package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank(message = "Tên người nhận không được để trống")
        @Size(max = 100, message = "Tên người nhận tối đa 100 ký tự")
        String recipientName,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^(0[1-9])[0-9]{8}$", message = "Số điện thoại không hợp lệ")
        String phone,

        @NotBlank(message = "Tỉnh/Thành phố không được để trống")
        String province,

        @NotBlank(message = "Quận/Huyện không được để trống")
        String district,

        @NotBlank(message = "Phường/Xã không được để trống")
        String ward,

        @NotBlank(message = "Địa chỉ chi tiết không được để trống")
        @Size(max = 255, message = "Địa chỉ chi tiết tối đa 255 ký tự")
        String streetAddress,

        Boolean isDefault
) {}
