package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.CheckoutItemSnapshot;

import java.time.OffsetDateTime;
import java.util.List;

public interface InventoryReservationService {

    void reserveStock(
            Long checkoutSessionId,
            List<CheckoutItemSnapshot> items,
            OffsetDateTime expiresAt
    );

    void consumeStockReservation(String checkoutCode);

    void releaseStockReservation(String checkoutCode);
}
