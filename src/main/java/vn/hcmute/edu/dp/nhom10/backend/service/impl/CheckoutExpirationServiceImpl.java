package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.InventoryReservation;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;
import vn.hcmute.edu.dp.nhom10.backend.entity.VoucherReservation;
import vn.hcmute.edu.dp.nhom10.backend.enums.CheckoutSessionStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentAttemptStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReservationStatus;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.InventoryReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentAttemptRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.CheckoutExpirationService;
import vn.hcmute.edu.dp.nhom10.backend.service.FlashSaleReservationService;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckoutExpirationServiceImpl implements CheckoutExpirationService {

    private static final List<CheckoutSessionStatus> EXPIRABLE_CHECKOUT_STATUSES = List.of(
            CheckoutSessionStatus.creating,
            CheckoutSessionStatus.reserved
    );

    private final CheckoutSessionRepository checkoutSessionRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final VoucherReservationRepository voucherReservationRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final FlashSaleReservationService flashSaleReservationService;

    @Override
    @Transactional
    public int expireDueCheckouts(OffsetDateTime now) {
        if (now == null) {
            throw new IllegalArgumentException("Current time is required");
        }

        List<CheckoutSession> dueCheckouts = checkoutSessionRepository.findExpiredForUpdate(
                EXPIRABLE_CHECKOUT_STATUSES,
                now
        );

        int expiredCount = 0;
        for (CheckoutSession checkoutSession : dueCheckouts) {
            if (!isExpirable(checkoutSession, now)) {
                continue;
            }
            expireInventoryReservations(checkoutSession.getId());
            flashSaleReservationService.expireQuota(checkoutSession.getId());
            expireVoucherReservation(checkoutSession.getId());
            expirePaymentAttempts(checkoutSession.getId());

            checkoutSession.setStatus(CheckoutSessionStatus.expired);
            checkoutSessionRepository.save(checkoutSession);
            expiredCount++;
        }
        return expiredCount;
    }

    private boolean isExpirable(CheckoutSession checkoutSession, OffsetDateTime now) {
        return checkoutSession != null
                && EXPIRABLE_CHECKOUT_STATUSES.contains(checkoutSession.getStatus())
                && checkoutSession.getExpiresAt() != null
                && !checkoutSession.getExpiresAt().isAfter(now);
    }

    private void expireInventoryReservations(Long checkoutSessionId) {
        List<InventoryReservation> reservations =
                inventoryReservationRepository.findAllByCheckoutSessionIdForUpdate(checkoutSessionId);
        List<InventoryReservation> changedReservations = reservations.stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.active)
                .peek(reservation -> reservation.setStatus(ReservationStatus.expired))
                .toList();
        if (!changedReservations.isEmpty()) {
            inventoryReservationRepository.saveAll(changedReservations);
        }
    }

    private void expireVoucherReservation(Long checkoutSessionId) {
        voucherReservationRepository.findByCheckoutSessionIdForUpdate(checkoutSessionId)
                .filter(reservation -> reservation.getStatus() == ReservationStatus.active)
                .ifPresent(reservation -> {
                    reservation.setStatus(ReservationStatus.expired);
                    voucherReservationRepository.save(reservation);
                });
    }

    private void expirePaymentAttempts(Long checkoutSessionId) {
        List<PaymentAttempt> paymentAttempts =
                paymentAttemptRepository.findAllByCheckoutSessionIdForUpdate(checkoutSessionId);
        List<PaymentAttempt> changedAttempts = paymentAttempts.stream()
                .filter(attempt -> attempt.getStatus() == PaymentAttemptStatus.pending)
                .peek(attempt -> attempt.setStatus(PaymentAttemptStatus.expired))
                .toList();
        if (!changedAttempts.isEmpty()) {
            paymentAttemptRepository.saveAll(changedAttempts);
        }
    }
}
