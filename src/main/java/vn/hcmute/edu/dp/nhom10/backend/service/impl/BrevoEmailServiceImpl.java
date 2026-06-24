package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.client.RestClient;
import vn.hcmute.edu.dp.nhom10.backend.service.EmailService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j(topic = "EMAIL-SERVICE")
@Service
public class BrevoEmailServiceImpl implements EmailService {

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.sender-email}")
    private String senderEmail;

    @Value("${brevo.sender-name}")
    private String senderName;

    @Value("${app.backend-url:http://localhost:8080}")
    private String backendUrl;

    private final RestClient restClient;

    public BrevoEmailServiceImpl() {
        this.restClient = RestClient.create();
    }

    @Async
    @Override
    public void sendVerificationEmail(String toEmail, String fullName, String token) {
        String htmlContent = buildVerificationEmailHtml(fullName, token);

        Map<String, Object> requestBody = Map.of(
                "sender", Map.of("name", senderName, "email", senderEmail),
                "to", List.of(Map.of("email", toEmail, "name", fullName)),
                "subject", "Clothing Store - Xác thực email của bạn",
                "htmlContent", htmlContent);

        try {
            restClient.post()
                    .uri("https://api.brevo.com/v3/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Verification email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}", toEmail, e);
        }
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String toEmail, String fullName, String token) {
        String htmlContent = buildPasswordResetEmailHtml(fullName, token);

        Map<String, Object> requestBody = Map.of(
                "sender", Map.of("name", senderName, "email", senderEmail),
                "to", List.of(Map.of("email", toEmail, "name", fullName)),
                "subject", "Clothing Store - Đặt lại mật khẩu của bạn",
                "htmlContent", htmlContent);

        try {
            restClient.post()
                    .uri("https://api.brevo.com/v3/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Password reset email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}", toEmail, e);
        }
    }

    @Async
    @Override
    public void sendAccountStatusEmail(String toEmail, String fullName, Boolean isActive) {
        String statusText = Boolean.TRUE.equals(isActive) ? "được kích hoạt lại" : "bị khóa";
        String htmlContent = String.format("""
                <html>
                <body>
                    <h2>Thông báo thay đổi trạng thái tài khoản</h2>
                    <p>Chào %s,</p>
                    <p>Tài khoản của bạn đã %s bởi Quản trị viên.</p>
                    <p>Nếu bạn có bất kỳ thắc mắc nào, vui lòng liên hệ bộ phận hỗ trợ.</p>
                </body>
                </html>
                """, fullName, statusText);

        Map<String, Object> requestBody = Map.of(
                "sender", Map.of("name", senderName, "email", senderEmail),
                "to", List.of(Map.of("email", toEmail, "name", fullName)),
                "subject", "Clothing Store - Trạng thái tài khoản",
                "htmlContent", htmlContent);

        try {
            restClient.post()
                    .uri("https://api.brevo.com/v3/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Account status email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send account status email to {}", toEmail, e);
        }
    }

    @Async
    @Override
    public void sendOrderCancellationEmailToCustomer(
            String toEmail,
            String orderCode,
            String reason,
            boolean requiresManualRefundReview
    ) {
        String htmlContent = """
                <html>
                <body>
                    <h2>Đơn hàng đã bị hủy</h2>
                    <p>Đơn hàng <strong>%s</strong> của bạn đã bị hủy.</p>
                    <p>Lý do: %s</p>
                    <p>Nếu bạn cần hỗ trợ, vui lòng liên hệ bộ phậm chăm sóc khách hàng để được hỗ trợ.</p>
                    %s
                </body>
                </html>
                """.formatted(
                HtmlUtils.htmlEscape(orderCode),
                HtmlUtils.htmlEscape(reason),
                requiresManualRefundReview
                        ? "<p>Cửa hàng sẽ xem xét riêng giao dịch thanh toán trực tuyến đã hoàn tất.</p>"
                        : ""
        );

        sendTransactionalEmail(toEmail, toEmail, "Clothing Store - Đơn hàng đã bị hủy", htmlContent);
    }

    @Async
    @Override
    public void sendOrderCancellationEmailToAdmin(
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
    ) {
        String htmlContent = """
                <html>
                <body>
                    <h2>Nhân viên đã hủy đơn hàng của khách</h2>
                    <p>Đơn hàng: <strong>%s</strong></p>
                    <p>Khách hàng: %s</p>
                    <p>Nhân viên: %s</p>
                    <p>Từ trạng thái: %s</p>
                    <p>Lý do: %s</p>
                    <p>Phương thức thanh toán: %s</p>
                    <p>Trạng thái thanh toán: %s</p>
                    <p>Số tiền đã thanh toán: %s</p>
                    <p>Việc nhân viên hủy đơn không bao gồm việc hoàn tiền.</p>
                    %s
                </body>
                </html>
                """.formatted(
                HtmlUtils.htmlEscape(orderCode),
                HtmlUtils.htmlEscape(customerEmail == null ? "" : customerEmail),
                HtmlUtils.htmlEscape(staffEmail == null ? "" : staffEmail),
                HtmlUtils.htmlEscape(fromStatus == null ? "" : fromStatus),
                HtmlUtils.htmlEscape(reason),
                HtmlUtils.htmlEscape(paymentMethod == null ? "" : paymentMethod),
                HtmlUtils.htmlEscape(paymentStatus == null ? "" : paymentStatus),
                paidAmount == null ? "" : paidAmount.toPlainString(),
                requiresManualRefundReview
                        ? "<p>Cần xem xét hoàn tiền thủ công đối với khoản thanh toán trực tuyến đã hoàn thành tất này.</p>"
                        : "<p>Việc xem xét hoàn tiền thủ công không bắt buộc với đơn hàng này.</p>"
        );

        sendTransactionalEmail(toEmail, toEmail, "Clothing Store - Staff cancelled order", htmlContent);
    }

    private void sendTransactionalEmail(String toEmail, String toName, String subject, String htmlContent) {
        Map<String, Object> requestBody = Map.of(
                "sender", Map.of("name", senderName, "email", senderEmail),
                "to", List.of(Map.of("email", toEmail, "name", toName == null ? toEmail : toName)),
                "subject", subject,
                "htmlContent", htmlContent);

        try {
            restClient.post()
                    .uri("https://api.brevo.com/v3/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Transactional email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send transactional email to {}", toEmail, e);
        }
    }


    @Async
    @Override
    public void sendOrderCancellationEmail(String toEmail, String fullName, String orderCode) {
        String htmlContent = String.format("""
                <html>
                <body>
                    <h2>Thông báo hủy đơn hàng thành công</h2>
                    <p>Chào %s,</p>
                    <p>Đơn hàng <strong>#%s</strong> của bạn đã được hủy thành công trên hệ thống của chúng tôi.</p>
                    <p>Số tiền thanh toán (nếu có) sẽ được xử lý hoàn trả theo chính sách của cửa hàng.</p>
                    <p>Cảm ơn bạn đã đồng hành cùng Clothing Store.</p>
                </body>
                </html>
                """, fullName, orderCode);

        Map<String, Object> requestBody = Map.of(
                "sender", Map.of("name", senderName, "email", senderEmail),
                "to", List.of(Map.of("email", toEmail, "name", fullName)),
                "subject", "Clothing Store - Hủy đơn hàng #" + orderCode,
                "htmlContent", htmlContent);

        try {
            restClient.post()
                    .uri("https://api.brevo.com/v3/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Order cancellation email sent successfully to {} for order {}", toEmail, orderCode);
        } catch (Exception e) {
            log.error("Failed to send order cancellation email to {} for order {}", toEmail, orderCode, e);
        }
    }


    @Async
    @Override
    public void sendProductSaleEmail(String toEmail, String fullName, vn.hcmute.edu.dp.nhom10.backend.entity.Product product) {
        String baseUrl = backendUrl.contains(":8080") ? backendUrl.replace(":8080", ":3000") : backendUrl;
        String productUrl = baseUrl + "/product/" + product.getSlug();
        String priceDisplay = product.getSalePrice() != null ? product.getSalePrice().toString() : "";
        
        String imageUrl = "";
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            imageUrl = product.getImages().get(0).getImageUrl();
        }
        
        String imageHtml = !imageUrl.isEmpty() 
            ? String.format("<div style='text-align: center; margin: 20px 0;'><a href='%s'><img src='%s' alt='%s' style='max-width: 100%%; max-height: 300px; border-radius: 8px;'/></a></div>", productUrl, imageUrl, product.getName())
            : "";

        String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; color: #333; line-height: 1.6;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 10px;">
                        <h2 style="color: #ba1a1a; text-align: center;">Sản phẩm bạn yêu thích đang giảm giá!</h2>
                        <p>Chào <strong>%s</strong>,</p>
                        <p>Sản phẩm <strong>%s</strong> mà bạn đã lưu vào danh sách yêu thích hiện đang có giá ưu đãi là <strong style="color: #ba1a1a; font-size: 1.2em;">%s đ</strong>.</p>
                        %s
                        <div style="text-align: center; margin-top: 30px;">
                            <a href="%s" style="background-color: #111827; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">Xem chi tiết và Mua ngay</a>
                        </div>
                        <p style="margin-top: 30px; font-size: 0.9em; color: #666; text-align: center;">Cảm ơn bạn đã luôn đồng hành cùng CLOTHY!</p>
                    </div>
                </body>
                </html>
                """, fullName, product.getName(), priceDisplay, imageHtml, productUrl);

        Map<String, Object> requestBody = Map.of(
                "sender", Map.of("name", senderName, "email", senderEmail),
                "to", List.of(Map.of("email", toEmail, "name", fullName)),
                "subject", "Clothing Store - " + product.getName() + " đang giảm giá!",
                "htmlContent", htmlContent);

        try {
            restClient.post()
                    .uri("https://api.brevo.com/v3/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Product sale email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send product sale email to {}", toEmail, e);
        }
    }

    private @NonNull String buildVerificationEmailHtml(String fullName, String token) {
        String escapedName = HtmlUtils.htmlEscape(fullName == null ? "" : fullName);
        String baseUrl = backendUrl.contains(":8080") ? backendUrl.replace(":8080", ":3000") : backendUrl;
        String encodedToken = java.net.URLEncoder.encode(token, java.nio.charset.StandardCharsets.UTF_8);
        String verificationLink = baseUrl + "/verify-email?token=" + encodedToken;

        return """
                <html>
                                <body style="margin:0;padding:0;background:#FAFAF8;">
                                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background:#FAFAF8;margin:0;padding:0;">
                                        <tr>
                                            <td align="center" style="padding:84px 24px;">
                                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="max-width:560px;background:#FFFFFF;border:1px solid #E5E5E0;border-radius:28px;box-shadow:0 18px 48px rgba(17,24,39,0.06);overflow:hidden;">
                                                    <tr>
                                                        <td align="center" style="padding:44px 44px 20px 44px;">
                                                            <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                                                                <tr>
                                                                    <td align="center" style="padding-bottom:14px;">
                                                                        <div style="width:56px;height:56px;border-radius:18px;border:1px solid #E5E5E0;background:#FAFAF8;display:inline-block;">
                                                                            <svg width="56" height="56" viewBox="0 0 56 56" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                                                                                <rect x="1" y="1" width="54" height="54" rx="18" stroke="#E5E5E0"/>
                                                                                <path d="M20 25.5C20 22.4624 22.4624 20 25.5 20C28.5376 20 31 22.4624 31 25.5V27.25L35 29.25V34H21V29.25L25 27.25V25.5C25 24.1193 26.1193 23 27.5 23C28.8807 23 30 24.1193 30 25.5" stroke="#111827" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"/>
                                                                                <path d="M23 35.5H33" stroke="#111827" stroke-width="1.9" stroke-linecap="round"/>
                                                                            </svg>
                                                                        </div>
                                                                    </td>
                                                                </tr>
                                                                <tr>
                                                                    <td align="center" style="font-family:Arial,Helvetica,sans-serif;font-size:13px;line-height:1.2;letter-spacing:0.42em;color:#111827;font-weight:700;padding-bottom:12px;">CLOTHY</td>
                                                                </tr>
                                                                <tr>
                                                                    <td align="center" style="font-family:Arial,Helvetica,sans-serif;font-size:28px;line-height:1.25;letter-spacing:-0.03em;color:#111827;font-weight:700;padding:0 8px 12px 8px;">Xác thực địa chỉ email của bạn</td>
                                                                </tr>
                                                                <tr>
                                                                    <td align="center" style="font-family:Arial,Helvetica,sans-serif;font-size:15px;line-height:1.8;color:#5B5B5B;padding:0 14px 26px 14px;">Chào %s, hãy xác thực email để hoàn tất quá trình đăng ký tài khoản của bạn.</td>
                                                                </tr>
                                                                <tr>
                                                                    <td align="center" style="padding-bottom:18px;">
                                                                        <a href="%s" style="display:inline-block;min-width:216px;padding:15px 28px;background:#111827;color:#FFFFFF;text-decoration:none;font-family:Arial,Helvetica,sans-serif;font-size:13px;line-height:1;font-weight:700;letter-spacing:0.18em;text-transform:uppercase;border-radius:14px;box-shadow:0 12px 24px rgba(17,24,39,0.16);">Xác thực email</a>
                                                                    </td>
                                                                </tr>
                                                                <tr>
                                                                    <td align="center" style="font-family:Arial,Helvetica,sans-serif;font-size:12px;line-height:1.7;color:#767676;padding:0 10px 8px 10px;">Liên kết này sẽ hết hạn sau 15 phút.</td>
                                                                </tr>
                                                                <tr>
                                                                    <td align="center" style="font-family:Arial,Helvetica,sans-serif;font-size:12px;line-height:1.7;color:#8A8A8A;padding:0 10px 6px 10px;">Nếu bạn không tạo tài khoản này, hãy bỏ qua email này.</td>
                                                                </tr>
                                                                <tr>
                                                                    <td align="center" style="padding:10px 0 0 0;">
                                                                        <div style="width:100%%;height:1px;background:#E9E7E2;line-height:1px;font-size:1px;">&nbsp;</div>
                                                                    </td>
                                                                </tr>
                                                                <tr>
                                                                    <td align="center" style="font-family:Arial,Helvetica,sans-serif;font-size:11px;line-height:1.7;color:#A3A3A3;padding:14px 10px 0 10px;word-break:break-all;">%s</td>
                                                                </tr>
                                                            </table>
                                                        </td>
                                                    </tr>
                                                </table>
                                            </td>
                                        </tr>
                                    </table>
                </body>
                </html>
                               \s"""
                .formatted(escapedName, verificationLink, verificationLink);
    }

    private @NonNull String buildPasswordResetEmailHtml(String fullName, String token) {
        String escapedName = HtmlUtils.htmlEscape(fullName == null ? "" : fullName);
        String baseUrl = backendUrl.contains(":8080") ? backendUrl.replace(":8080", ":3000") : backendUrl;
        String encodedToken = java.net.URLEncoder.encode(token, java.nio.charset.StandardCharsets.UTF_8);
        String resetLink = baseUrl + "/reset-password?token=" + encodedToken;
        return """
                <html>
                                <body style="margin:0;padding:0;background:#FAFAF8;">
                                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background:#FAFAF8;margin:0;padding:0;">
                                        <tr>
                                            <td align="center" style="padding:84px 24px;">
                                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="max-width:560px;background:#FFFFFF;border:1px solid #E5E5E0;border-radius:28px;box-shadow:0 18px 48px rgba(17,24,39,0.06);overflow:hidden;">
                                                    <tr>
                                                        <td align="center" style="padding:44px 44px 20px 44px;">
                                                            <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                                                                <tr>
                                                                    <td align="center" style="padding-bottom:14px;">
                                                                        <div style="width:56px;height:56px;border-radius:18px;border:1px solid #E5E5E0;background:#FAFAF8;display:inline-block;">
                                                                            <svg width="56" height="56" viewBox="0 0 56 56" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                                                                                <rect x="1" y="1" width="54" height="54" rx="18" stroke="#E5E5E0"/>
                                                                                <path d="M20 25.5C20 22.4624 22.4624 20 25.5 20C28.5376 20 31 22.4624 31 25.5V27.25L35 29.25V34H21V29.25L25 27.25V25.5C25 24.1193 26.1193 23 27.5 23C28.8807 23 30 24.1193 30 25.5" stroke="#111827" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"/>
                                                                                <path d="M23 35.5H33" stroke="#111827" stroke-width="1.9" stroke-linecap="round"/>
                                                                            </svg>
                                                                        </div>
                                                                    </td>
                                                                </tr>
                                                                <tr>
                                                                    <td align="center" style="font-family:Arial,Helvetica,sans-serif;font-size:13px;line-height:1.2;letter-spacing:0.42em;color:#111827;font-weight:700;padding-bottom:12px;">CLOTHY</td>
                                                                </tr>
                                                                <tr>
                                                                    <td align="center" style="font-family:Arial,Helvetica,sans-serif;font-size:28px;line-height:1.25;letter-spacing:-0.03em;color:#111827;font-weight:700;padding:0 8px 12px 8px;">Đặt lại mật khẩu</td>
                                                                </tr>
                                                                <tr>
                                                                    <td align="center" style="font-family:Arial,Helvetica,sans-serif;font-size:15px;line-height:1.8;color:#5B5B5B;padding:0 14px 26px 14px;">Chào %s, chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. Nhấn vào nút bên dưới để đổi mật khẩu.</td>
                                                                </tr>
                                                                <tr>
                                                                    <td align="center" style="padding-bottom:18px;">
                                                                        <a href="%s" style="display:inline-block;min-width:216px;padding:15px 28px;background:#111827;color:#FFFFFF;text-decoration:none;font-family:Arial,Helvetica,sans-serif;font-size:13px;line-height:1;font-weight:700;letter-spacing:0.18em;text-transform:uppercase;border-radius:14px;box-shadow:0 12px 24px rgba(17,24,39,0.16);">Đặt lại mật khẩu</a>
                                                                    </td>
                                                                </tr>
                                                                <tr>
                                                                    <td align="center" style="font-family:Arial,Helvetica,sans-serif;font-size:12px;line-height:1.7;color:#767676;padding:0 10px 8px 10px;">Liên kết này sẽ hết hạn sau 15 phút.</td>
                                                                </tr>
                                                                <tr>
                                                                    <td align="center" style="font-family:Arial,Helvetica,sans-serif;font-size:12px;line-height:1.7;color:#8A8A8A;padding:0 10px 6px 10px;">Nếu bạn không yêu cầu điều này, hãy bỏ qua email này.</td>
                                                                </tr>
                                                                <tr>
                                                                    <td align="center" style="padding:10px 0 0 0;">
                                                                        <div style="width:100%%;height:1px;background:#E9E7E2;line-height:1px;font-size:1px;">&nbsp;</div>
                                                                    </td>
                                                                </tr>
                                                                <tr>
                                                                    <td align="center" style="font-family:Arial,Helvetica,sans-serif;font-size:11px;line-height:1.7;color:#A3A3A3;padding:14px 10px 0 10px;word-break:break-all;">%s</td>
                                                                </tr>
                                                            </table>
                                                        </td>
                                                    </tr>
                                                </table>
                                            </td>
                                        </tr>
                                    </table>
                </body>
                </html>
                               \s"""
                .formatted(escapedName, resetLink, resetLink);
    }
}
