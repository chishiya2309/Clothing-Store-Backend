package vn.hcmute.edu.dp.nhom10.backend.service;

public interface EmailService {
    void sendVerificationEmail(String toEmail, String fullName, String token);
}
