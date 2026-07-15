package vn.hcmute.edu.dp.nhom10.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class ClientIpResolver {

    private static final int MAX_IP_LENGTH = 45;
    private static final String LOCAL_FALLBACK_IP = "127.0.0.1";

    public String resolve(HttpServletRequest request) {
        if (request == null) {
            return LOCAL_FALLBACK_IP;
        }
        String remoteAddr = normalize(request.getRemoteAddr());
        if (shouldTrustProxyHeaders(remoteAddr)) {
            String forwardedFor = firstForwardedForToken(request.getHeader("X-Forwarded-For"));
            if (hasText(forwardedFor)) {
                return truncate(forwardedFor);
            }
            String realIp = normalize(request.getHeader("X-Real-IP"));
            if (hasText(realIp)) {
                return truncate(realIp);
            }
        }
        if (hasText(remoteAddr)) {
            return truncate(remoteAddr);
        }
        String forwardedFor = firstForwardedForToken(request.getHeader("X-Forwarded-For"));
        if (hasText(forwardedFor)) {
            return truncate(forwardedFor);
        }
        String realIp = normalize(request.getHeader("X-Real-IP"));
        if (hasText(realIp)) {
            return truncate(realIp);
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

    private boolean shouldTrustProxyHeaders(String remoteAddr) {
        if (!hasText(remoteAddr)) {
            return true;
        }
        try {
            InetAddress address = InetAddress.getByName(remoteAddr);
            return address.isAnyLocalAddress()
                    || address.isLoopbackAddress()
                    || address.isSiteLocalAddress()
                    || address.isLinkLocalAddress()
                    || isUniqueLocalIpv6(address);
        } catch (UnknownHostException ex) {
            return false;
        }
    }

    private boolean isUniqueLocalIpv6(InetAddress address) {
        byte[] rawAddress = address.getAddress();
        return rawAddress.length == 16 && (rawAddress[0] & 0xFE) == 0xFC;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
