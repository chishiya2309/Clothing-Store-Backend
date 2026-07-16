package vn.hcmute.edu.dp.nhom10.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RedisBackedRateLimitServiceTest {

    private RedisTemplate<String, Object> redisTemplate;
    private ValueOperations<String, Object> valueOperations;
    private MutableClock clock;
    private RedisBackedRateLimitService service;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenThrow(new RuntimeException("Redis unavailable"));

        clock = new MutableClock(Instant.parse("2026-07-14T10:00:00Z"), ZoneOffset.UTC);
        service = new RedisBackedRateLimitService(redisTemplate, clock);
    }

    @Test
    void consume_blocksWhenLimitExceededAndResetsAfterWindow() {
        RateLimitDecision first = service.consume("auth-login:127.0.0.1", 2, Duration.ofSeconds(60));
        RateLimitDecision second = service.consume("auth-login:127.0.0.1", 2, Duration.ofSeconds(60));
        RateLimitDecision third = service.consume("auth-login:127.0.0.1", 2, Duration.ofSeconds(60));

        assertTrue(first.allowed());
        assertEquals(1L, first.currentCount());
        assertEquals(1L, first.remaining());

        assertTrue(second.allowed());
        assertEquals(2L, second.currentCount());
        assertEquals(0L, second.remaining());

        assertFalse(third.allowed());
        assertEquals(3L, third.currentCount());
        assertEquals(0L, third.remaining());
        assertEquals(60L, third.retryAfterSeconds());

        clock.advance(Duration.ofSeconds(61));

        RateLimitDecision afterReset = service.consume("auth-login:127.0.0.1", 2, Duration.ofSeconds(60));

        assertTrue(afterReset.allowed());
        assertEquals(1L, afterReset.currentCount());
        assertEquals(1L, afterReset.remaining());
    }

    @Test
    void consume_usesRedisCountersWhenRedisIsAvailable() {
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L, 2L, 3L);
        when(redisTemplate.getExpire(anyString(), eq(TimeUnit.SECONDS))).thenReturn(60L, 55L, 50L);

        RedisBackedRateLimitService redisOnlyService = new RedisBackedRateLimitService(redisTemplate, clock);

        RateLimitDecision first = redisOnlyService.consume("public-catalog:203.0.113.10", 2, Duration.ofSeconds(60));
        RateLimitDecision second = redisOnlyService.consume("public-catalog:203.0.113.10", 2, Duration.ofSeconds(60));
        RateLimitDecision third = redisOnlyService.consume("public-catalog:203.0.113.10", 2, Duration.ofSeconds(60));

        assertTrue(first.allowed());
        assertEquals(1L, first.currentCount());
        assertEquals(1L, first.remaining());
        assertEquals(60L, first.retryAfterSeconds());

        assertTrue(second.allowed());
        assertEquals(2L, second.currentCount());
        assertEquals(0L, second.remaining());
        assertEquals(55L, second.retryAfterSeconds());

        assertFalse(third.allowed());
        assertEquals(3L, third.currentCount());
        assertEquals(0L, third.remaining());
        assertEquals(50L, third.retryAfterSeconds());
    }

    private static final class MutableClock extends Clock {

        private Instant currentInstant;
        private final ZoneId zoneId;

        private MutableClock(Instant currentInstant, ZoneId zoneId) {
            this.currentInstant = currentInstant;
            this.zoneId = zoneId;
        }

        @Override
        public ZoneId getZone() {
            return zoneId;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(currentInstant, zone);
        }

        @Override
        public Instant instant() {
            return currentInstant;
        }

        private void advance(Duration duration) {
            currentInstant = currentInstant.plus(duration);
        }
    }
}
