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
                "subject", "Clothing Store - Verify your email",
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

        private @NonNull String buildVerificationEmailHtml(String fullName, String token) {
                String escapedName = HtmlUtils.htmlEscape(fullName == null ? "" : fullName);
                String verificationLink = backendUrl + "/api/auth/verify-email?token=" + token;

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
                               \s""".formatted(escapedName, verificationLink, verificationLink);
    }
}
