package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email là bắt buộc")
        @Email(message = "Định dạng email không hợp lệ")
        String email,

        @NotBlank(message = "Mật khẩu là bắt buộc")
        @Size(min = 8, max = 100, message = "Mật khẩu phải có từ 8 đến 100 ký tự")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
                message = "Mật khẩu phải chứa ít nhất một chữ hoa và một chữ số")
        String password,

        @NotBlank(message = "Họ và tên là bắt buộc")
        @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
        String fullName
) {
}
