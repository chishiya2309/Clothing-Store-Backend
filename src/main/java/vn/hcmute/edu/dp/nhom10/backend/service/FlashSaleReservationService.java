package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.CheckoutItemSnapshot;

import java.time.OffsetDateTime;
import java.util.List;

public interface FlashSaleReservationService {
    void reserveQuota(Long checkoutSessionId, List<CheckoutItemSnapshot> items, OffsetDateTime expiresAt);
    void consumeQuota(String checkoutCode);
    void releaseQuota(String checkoutCode);
    void expireQuota(Long checkoutSessionId);
}
