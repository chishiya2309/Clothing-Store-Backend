package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ResetPasswordRequest(
                @NotBlank(message = "Token cannot be blank") String token,

                @NotBlank(message = "New password cannot be blank") @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự") @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "Mật khẩu mới phải chứa ít nhất một chữ hoa, một chữ thường và một chữ số") String newPassword,

                @NotBlank(message = "Confirm password cannot be blank") String confirmPassword) {
}
