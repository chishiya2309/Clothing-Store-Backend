package vn.hcmute.edu.dp.nhom10.backend.security;

import org.springframework.security.authentication.BadCredentialsException;

public record RefreshTokenSession(long userId, long sessionVersion) {
    public String encode() {
        return userId + ":" + sessionVersion;
    }

    public static RefreshTokenSession decode(Object value) {
        if (value == null) {
            throw new BadCredentialsException("Refresh token expired or invalid");
        }
        String encoded = value.toString();
        if (!encoded.matches("[1-9][0-9]*(?::[0-9]+)?")) {
            throw new BadCredentialsException("Refresh token expired or invalid");
        }
        try {
            String[] parts = encoded.split(":");
            return new RefreshTokenSession(Long.parseLong(parts[0]),
                    parts.length == 1 ? 0 : Long.parseLong(parts[1]));
        } catch (NumberFormatException ex) {
            throw new BadCredentialsException("Refresh token expired or invalid", ex);
        }
    }
}
