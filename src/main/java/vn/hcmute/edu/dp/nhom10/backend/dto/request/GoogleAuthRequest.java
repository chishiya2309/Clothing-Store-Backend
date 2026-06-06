package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthRequest(
                @NotBlank(message = "Google ID token is required") String idToken) {
}
