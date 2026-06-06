package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.request.RegisterRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.LoginRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.GoogleAuthRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.TokenResponse;

public interface AuthService {
    void register(RegisterRequest request);

    void verifyEmail(String token);

    void resendVerificationEmail(String email);

    TokenResponse login(LoginRequest request, String ipAddress, String userAgent);

    TokenResponse loginWithGoogle(GoogleAuthRequest request, String ipAddress, String userAgent);

    TokenResponse refreshToken(String refreshToken);

    void logout(String refreshToken);
}
