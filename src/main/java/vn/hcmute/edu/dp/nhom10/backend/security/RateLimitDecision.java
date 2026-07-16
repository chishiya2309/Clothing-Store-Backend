package vn.hcmute.edu.dp.nhom10.backend.security;

public record RateLimitDecision(
        boolean allowed,
        long currentCount,
        int limit,
        long remaining,
        long retryAfterSeconds
) {
}
