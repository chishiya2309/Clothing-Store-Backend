package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ResetPasswordRequest(
                @NotBlank(message = "Token cannot be blank") String token,

                @NotBlank(message = "New password cannot be blank") @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự") @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).*$", message = "New password must contain at least one uppercase letter and one number") String newPassword,

                @NotBlank(message = "Confirm password cannot be blank") String confirmPassword) {
}
