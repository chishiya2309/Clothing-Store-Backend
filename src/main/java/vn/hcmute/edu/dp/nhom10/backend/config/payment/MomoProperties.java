package vn.hcmute.edu.dp.nhom10.backend.config.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
@ConfigurationProperties(prefix = "payment.momo")
public class MomoProperties {

    private boolean enabled = false;
    private String partnerCode;
    private String accessKey;
    private String secretKey;
    private String createUrl = "https://test-payment.momo.vn/v2/gateway/api/create";
    private String redirectUrl;
    private String ipnUrl;
    private String requestType = "captureWallet";
    private String lang = "vi";
    private boolean autoCapture = true;
    private int connectTimeoutSeconds = 10;
    private int readTimeoutSeconds = 30;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPartnerCode() {
        return partnerCode;
    }

    public void setPartnerCode(String partnerCode) {
        this.partnerCode = partnerCode;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getCreateUrl() {
        return createUrl;
    }

    public void setCreateUrl(String createUrl) {
        this.createUrl = createUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getIpnUrl() {
        return ipnUrl;
    }

    public void setIpnUrl(String ipnUrl) {
        this.ipnUrl = ipnUrl;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public boolean isAutoCapture() {
        return autoCapture;
    }

    public void setAutoCapture(boolean autoCapture) {
        this.autoCapture = autoCapture;
    }

    public int getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
        this.connectTimeoutSeconds = connectTimeoutSeconds;
    }

    public int getReadTimeoutSeconds() {
        return readTimeoutSeconds;
    }

    public void setReadTimeoutSeconds(int readTimeoutSeconds) {
        this.readTimeoutSeconds = readTimeoutSeconds;
    }

    public boolean isAvailable() {
        return enabled
                && hasText(partnerCode)
                && hasText(accessKey)
                && hasText(secretKey)
                && isHttpUrl(createUrl)
                && isHttpUrl(redirectUrl)
                && isHttpUrl(ipnUrl)
                && hasText(requestType)
                && hasText(lang)
                && connectTimeoutSeconds > 0
                && readTimeoutSeconds > 0;
    }

    public String unavailableReason() {
        if (!enabled) {
            return "payment.momo.enabled";
        }
        if (!hasText(partnerCode)) {
            return "payment.momo.partner-code";
        }
        if (!hasText(accessKey)) {
            return "payment.momo.access-key";
        }
        if (!hasText(secretKey)) {
            return "payment.momo.secret-key";
        }
        if (!isHttpUrl(createUrl)) {
            return "payment.momo.create-url";
        }
        if (!isHttpUrl(redirectUrl)) {
            return "payment.momo.redirect-url";
        }
        if (!isHttpUrl(ipnUrl)) {
            return "payment.momo.ipn-url";
        }
        if (!hasText(requestType)) {
            return "payment.momo.request-type";
        }
        if (!hasText(lang)) {
            return "payment.momo.lang";
        }
        if (connectTimeoutSeconds <= 0) {
            return "payment.momo.connect-timeout-seconds";
        }
        if (readTimeoutSeconds <= 0) {
            return "payment.momo.read-timeout-seconds";
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean isHttpUrl(String value) {
        if (!hasText(value)) {
            return false;
        }
        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    && uri.getHost() != null
                    && !uri.getHost().isBlank();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
