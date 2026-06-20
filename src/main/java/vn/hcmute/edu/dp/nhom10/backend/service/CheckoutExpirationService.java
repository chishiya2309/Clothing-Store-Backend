package vn.hcmute.edu.dp.nhom10.backend.service;

import java.time.OffsetDateTime;

public interface CheckoutExpirationService {

    int expireDueCheckouts(OffsetDateTime now);
}
