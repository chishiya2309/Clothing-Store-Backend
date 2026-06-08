package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ForgotPasswordRequest(
                @NotBlank(message = "Email cannot be blank") @Email(message = "Email should be valid") String email) {
}
