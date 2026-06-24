package vn.hcmute.edu.dp.nhom10.backend.service;

import java.math.BigDecimal;

public interface EmailService {
    void sendVerificationEmail(String toEmail, String fullName, String token);

    void sendPasswordResetEmail(String toEmail, String fullName, String token);

    void sendAccountStatusEmail(String toEmail, String fullName, Boolean isActive);

    void sendOrderCancellationEmailToCustomer(
            String toEmail,
            String orderCode,
            String reason,
            boolean requiresManualRefundReview
    );

    void sendOrderCancellationEmailToAdmin(
            String toEmail,
            String orderCode,
            String customerEmail,
            String staffEmail,
            String fromStatus,
            String reason,
            String paymentMethod,
            String paymentStatus,
            BigDecimal paidAmount,
            boolean requiresManualRefundReview
    );

    void sendProductSaleEmail(String toEmail, String fullName, vn.hcmute.edu.dp.nhom10.backend.entity.Product product);
}

