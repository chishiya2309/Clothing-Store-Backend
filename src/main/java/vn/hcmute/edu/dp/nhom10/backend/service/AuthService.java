package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.request.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);
    void verifyEmail(String token);
    void resendVerificationEmail(String email);
}
