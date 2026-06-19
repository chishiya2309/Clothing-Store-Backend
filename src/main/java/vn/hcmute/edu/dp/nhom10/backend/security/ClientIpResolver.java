package vn.hcmute.edu.dp.nhom10.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class ClientIpResolver {

    private static final int MAX_IP_LENGTH = 45;
    private static final String LOCAL_FALLBACK_IP = "127.0.0.1";

    public String resolve(HttpServletRequest request) {
        if (request == null) {
            return LOCAL_FALLBACK_IP;
        }
        String forwardedFor = firstForwardedForToken(request.getHeader("X-Forwarded-For"));
        if (hasText(forwardedFor)) {
            return truncate(forwardedFor);
        }
        String realIp = normalize(request.getHeader("X-Real-IP"));
        if (hasText(realIp)) {
            return truncate(realIp);
        }
        String remoteAddr = normalize(request.getRemoteAddr());
        if (hasText(remoteAddr)) {
            return truncate(remoteAddr);
        }
        return LOCAL_FALLBACK_IP;
    }

    private String firstForwardedForToken(String headerValue) {
        if (!hasText(headerValue)) {
            return null;
        }
        return normalize(headerValue.split(",", 2)[0]);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String truncate(String value) {
        return value.length() <= MAX_IP_LENGTH ? value : value.substring(0, MAX_IP_LENGTH);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
