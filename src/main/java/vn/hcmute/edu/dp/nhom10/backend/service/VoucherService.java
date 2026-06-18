package vn.hcmute.edu.dp.nhom10.backend.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface VoucherService {

    BigDecimal reserveVoucher(
            Long checkoutSessionId,
            String code,
            BigDecimal subtotal,
            OffsetDateTime expiresAt
    );

    void consumeVoucherReservation(String checkoutCode);

    void releaseVoucherReservation(String checkoutCode);
}
