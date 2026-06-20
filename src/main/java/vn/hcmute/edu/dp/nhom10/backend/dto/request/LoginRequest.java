package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Email is required") @Email(message = "Định dạng email không hợp lệ") String email,

        @NotBlank(message = "Password is required") @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự") String password,

        Boolean rememberMe) {
    public LoginRequest {
        if (rememberMe == null)
            rememberMe = false;
    }
}
