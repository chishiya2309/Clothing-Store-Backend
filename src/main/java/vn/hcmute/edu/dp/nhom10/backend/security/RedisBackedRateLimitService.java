package vn.hcmute.edu.dp.nhom10.backend.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "RATE-LIMIT-SERVICE")
public class RedisBackedRateLimitService implements RateLimitService {

    private static final String KEY_PREFIX = "rate_limit:";
    private static final int CLEANUP_INTERVAL = 256;

    private final RedisTemplate<String, Object> redisTemplate;
    private final Clock clock;

    private final ConcurrentMap<String, InMemoryBucket> inMemoryBuckets = new ConcurrentHashMap<>();
    private final AtomicInteger cleanupTicker = new AtomicInteger();
    private final AtomicBoolean redisFallbackLogged = new AtomicBoolean(false);

    @Override
    public RateLimitDecision consume(String key, int limit, Duration window) {
        validateArguments(key, limit, window);

        String namespacedKey = KEY_PREFIX + key;
        try {
            return consumeWithRedis(namespacedKey, limit, window);
        } catch (RuntimeException ex) {
            if (redisFallbackLogged.compareAndSet(false, true)) {
                log.warn("Redis is unavailable. Falling back to in-memory rate limiting: {}", ex.getMessage());
            }
            log.debug("Redis rate limit access failed for key {}", namespacedKey, ex);
            return consumeInMemory(namespacedKey, limit, window);
        }
    }

    private RateLimitDecision consumeWithRedis(String key, int limit, Duration window) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Long currentCount = valueOperations.increment(key);
        if (currentCount == null) {
            throw new IllegalStateException("Redis did not return a counter value");
        }

        if (currentCount == 1L) {
            redisTemplate.expire(key, window);
        }

        Long ttlSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        long remaining = Math.max(0L, limit - currentCount);
        long retryAfter = normalizeRetryAfter(ttlSeconds, window);

        return new RateLimitDecision(currentCount <= limit, currentCount, limit, remaining, retryAfter);
    }

    private RateLimitDecision consumeInMemory(String key, int limit, Duration window) {
        Instant now = clock.instant();
        cleanupExpiredBuckets(now);

        InMemoryBucket bucket = inMemoryBuckets.compute(key, (ignored, existing) -> {
            if (existing == null || !existing.expiresAt().isAfter(now)) {
                return new InMemoryBucket(new AtomicLong(1L), now.plus(window));
            }
            existing.counter().incrementAndGet();
            return existing;
        });

        long currentCount = bucket.counter().get();
        long remaining = Math.max(0L, limit - currentCount);
        long retryAfter = normalizeRetryAfter(Duration.between(now, bucket.expiresAt()).getSeconds(), window);

        return new RateLimitDecision(currentCount <= limit, currentCount, limit, remaining, retryAfter);
    }

    private void cleanupExpiredBuckets(Instant now) {
        if (cleanupTicker.incrementAndGet() % CLEANUP_INTERVAL != 0) {
            return;
        }
        inMemoryBuckets.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
    }

    private long normalizeRetryAfter(Long ttlSeconds, Duration window) {
        if (ttlSeconds == null || ttlSeconds <= 0) {
            return Math.max(1L, window.toSeconds());
        }
        return ttlSeconds;
    }

    private void validateArguments(String key, int limit, Duration window) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Rate limit key must not be blank");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Rate limit limit must be greater than zero");
        }
        if (window == null || window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("Rate limit window must be greater than zero");
        }
    }

    private record InMemoryBucket(AtomicLong counter, Instant expiresAt) {
    }
}
