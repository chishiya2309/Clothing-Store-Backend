package vn.hcmute.edu.dp.nhom10.backend.service;

public interface EmailService {
    void sendVerificationEmail(String toEmail, String fullName, String token);

    void sendPasswordResetEmail(String toEmail, String fullName, String token);

    void sendAccountStatusEmail(String toEmail, String fullName, Boolean isActive);
}

