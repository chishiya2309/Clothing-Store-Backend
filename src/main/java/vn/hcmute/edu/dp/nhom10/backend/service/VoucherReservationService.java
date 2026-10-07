package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.CheckoutData;
import vn.hcmute.edu.dp.nhom10.backend.enums.VoucherSlot;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface VoucherReservationService {
    BigDecimal reserveVoucher(
            Long checkoutSessionId,
            String code,
            BigDecimal subtotal,
            OffsetDateTime expiresAt
    );

    BigDecimal reserveVoucher(
            Long checkoutSessionId,
            String code,
            VoucherSlot slot,
            CheckoutData checkoutData,
            OffsetDateTime expiresAt
    );

    void consumeVoucherReservation(String checkoutCode);

    void releaseVoucherReservation(String checkoutCode);
}
