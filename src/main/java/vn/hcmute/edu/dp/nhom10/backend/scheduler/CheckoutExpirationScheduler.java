package vn.hcmute.edu.dp.nhom10.backend.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.service.CheckoutExpirationService;

import java.time.Clock;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "checkout.cleanup.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Slf4j(topic = "CHECKOUT-EXPIRATION-SCHEDULER")
public class CheckoutExpirationScheduler {

    private final CheckoutExpirationService checkoutExpirationService;
    private final Clock clock;

    @Scheduled(fixedDelayString = "${checkout.cleanup.fixed-delay-ms:60000}")
    public void expireDueCheckouts() {
        int expiredCount = checkoutExpirationService.expireDueCheckouts(OffsetDateTime.now(clock));
        if (expiredCount > 0) {
            log.info("Expired {} checkout sessions", expiredCount);
        }
    }
}
