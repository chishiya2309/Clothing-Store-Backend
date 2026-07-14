package vn.hcmute.edu.dp.nhom10.backend.security;

import java.time.Duration;

public interface RateLimitService {

    RateLimitDecision consume(String key, int limit, Duration window);
}
