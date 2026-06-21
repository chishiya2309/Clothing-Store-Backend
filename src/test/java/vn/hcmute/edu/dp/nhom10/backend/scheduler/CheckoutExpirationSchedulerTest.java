package vn.hcmute.edu.dp.nhom10.backend.scheduler;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.service.CheckoutExpirationService;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckoutExpirationSchedulerTest {

    @Test
    void expireDueCheckouts_delegatesToServiceWithClockTime() {
        CheckoutExpirationService checkoutExpirationService = mock(CheckoutExpirationService.class);
        Clock clock = Clock.fixed(Instant.parse("2026-06-18T16:00:00Z"), ZoneOffset.UTC);
        CheckoutExpirationScheduler scheduler = new CheckoutExpirationScheduler(checkoutExpirationService, clock);
        when(checkoutExpirationService.expireDueCheckouts(OffsetDateTime.now(clock))).thenReturn(2);

        scheduler.expireDueCheckouts();

        verify(checkoutExpirationService).expireDueCheckouts(OffsetDateTime.parse("2026-06-18T16:00:00Z"));
    }
}
