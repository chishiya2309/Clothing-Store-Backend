package vn.hcmute.edu.dp.nhom10.backend.pattern.reservation.voucher;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.entity.VoucherReservation;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReservationStatus;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.VoucherService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class VoucherReservationServiceImpl implements VoucherService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final int MONEY_SCALE = 2;

    private final CheckoutSessionRepository checkoutSessionRepository;
    private final VoucherRepository voucherRepository;
    private final VoucherReservationRepository voucherReservationRepository;

    @Override
    @Transactional
    public BigDecimal reserveVoucher(
            Long checkoutSessionId,
            String code,
            BigDecimal subtotal,
            OffsetDateTime expiresAt
    ) {
        if (checkoutSessionId == null) {
            throw new IllegalArgumentException("Checkout session ID is required");
        }
        String normalizedCode = normalizeVoucherCode(code);
        validateSubtotal(subtotal);
        OffsetDateTime now = now();
        validateExpiresAt(expiresAt, now);

        CheckoutSession checkoutSession = checkoutSessionRepository.findByIdForUpdate(checkoutSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkout session not found with ID: " + checkoutSessionId));

        if (voucherReservationRepository.existsByCheckoutSession_Id(checkoutSessionId)) {
            throw new InvalidDataException("Voucher reservation already exists for checkout session: " + checkoutSessionId);
        }

        Voucher voucher = voucherRepository.findByCodeForUpdate(normalizedCode)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with code: " + normalizedCode));

        validateVoucherAvailable(voucher, subtotal, now);
        BigDecimal discountAmount = calculateDiscountAmount(voucher, subtotal);

        VoucherReservation reservation = VoucherReservation.builder()
                .checkoutSession(checkoutSession)
                .voucher(voucher)
                .discountAmount(discountAmount)
                .status(ReservationStatus.active)
                .expiresAt(expiresAt)
                .build();
        voucherReservationRepository.save(reservation);

        return discountAmount;
    }

    @Override
    @Transactional
    public void consumeVoucherReservation(String checkoutCode) {
        String normalizedCheckoutCode = normalizeCheckoutCode(checkoutCode);
        CheckoutSession checkoutSession = checkoutSessionRepository.findByCheckoutCodeForUpdate(normalizedCheckoutCode)
                .orElseThrow(() -> new ResourceNotFoundException("Checkout session not found with code: " + normalizedCheckoutCode));

        VoucherReservation reservation = voucherReservationRepository
                .findByCheckoutSessionIdForUpdate(checkoutSession.getId())
                .orElse(null);
        if (reservation == null || reservation.getStatus() == ReservationStatus.consumed) {
            return;
        }

        if (reservation.getStatus() == ReservationStatus.released || reservation.getStatus() == ReservationStatus.expired) {
            throw new InvalidDataException("Voucher reservation cannot be consumed because it is " + reservation.getStatus());
        }
        if (reservation.getStatus() != ReservationStatus.active) {
            throw new InvalidDataException("Voucher reservation status is invalid: " + reservation.getStatus());
        }

        OffsetDateTime now = now();
        if (reservation.getExpiresAt() == null || !reservation.getExpiresAt().isAfter(now)) {
            throw new InvalidDataException("Voucher reservation has expired");
        }

        Voucher voucher = lockVoucher(reservation);
        validateFiniteUsageLimitBeforeConsume(voucher);

        voucher.setTimesUsed(voucher.getTimesUsed() + 1);
        reservation.setStatus(ReservationStatus.consumed);

        voucherRepository.save(voucher);
        voucherReservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public void releaseVoucherReservation(String checkoutCode) {
        String normalizedCheckoutCode = normalizeCheckoutCode(checkoutCode);
        CheckoutSession checkoutSession = checkoutSessionRepository.findByCheckoutCodeForUpdate(normalizedCheckoutCode)
                .orElseThrow(() -> new ResourceNotFoundException("Checkout session not found with code: " + normalizedCheckoutCode));

        VoucherReservation reservation = voucherReservationRepository
                .findByCheckoutSessionIdForUpdate(checkoutSession.getId())
                .orElse(null);
        if (reservation == null || reservation.getStatus() != ReservationStatus.active) {
            return;
        }

        reservation.setStatus(ReservationStatus.released);
        voucherReservationRepository.save(reservation);
    }

    private void validateVoucherAvailable(Voucher voucher, BigDecimal subtotal, OffsetDateTime now) {
        if (!Boolean.TRUE.equals(voucher.getIsActive())) {
            throw new InvalidDataException("Voucher is inactive: " + voucher.getCode());
        }
        if (voucher.getStartDate() == null || voucher.getStartDate().isAfter(now)) {
            throw new InvalidDataException("Voucher is not active yet: " + voucher.getCode());
        }
        if (voucher.getEndDate() == null || voucher.getEndDate().isBefore(now)) {
            throw new InvalidDataException("Voucher has expired: " + voucher.getCode());
        }
        BigDecimal minOrderAmount = voucher.getMinOrderAmount();
        if (minOrderAmount != null && subtotal.compareTo(minOrderAmount) < 0) {
            throw new InvalidDataException("Order subtotal does not meet voucher minimum amount");
        }
        validateFiniteUsageAvailability(voucher, now);
    }

    private void validateFiniteUsageAvailability(Voucher voucher, OffsetDateTime now) {
        Integer usageLimit = voucher.getUsageLimit();
        Integer timesUsed = voucher.getTimesUsed();
        if (usageLimit == null || timesUsed == null) {
            throw new InvalidDataException("Voucher usage data is invalid: " + voucher.getId());
        }

        long activeReservations = voucherReservationRepository.countActiveReservations(
                voucher.getId(),
                ReservationStatus.active,
                now
        );
        long availableUses = usageLimit.longValue() - timesUsed.longValue() - activeReservations;
        if (availableUses <= 0) {
            throw new InvalidDataException("Voucher usage limit has been reached: " + voucher.getCode());
        }
    }

    private void validateFiniteUsageLimitBeforeConsume(Voucher voucher) {
        Integer usageLimit = voucher.getUsageLimit();
        Integer timesUsed = voucher.getTimesUsed();
        if (usageLimit == null || timesUsed == null) {
            throw new InvalidDataException("Voucher usage data is invalid: " + voucher.getId());
        }
        if (timesUsed >= usageLimit) {
            throw new InvalidDataException("Voucher usage limit has been reached: " + voucher.getCode());
        }
    }

    private BigDecimal calculateDiscountAmount(Voucher voucher, BigDecimal subtotal) {
        BigDecimal discountValue = voucher.getDiscountValue();
        if (discountValue == null || discountValue.signum() < 0) {
            throw new InvalidDataException("Voucher discount value is invalid: " + voucher.getId());
        }

        BigDecimal discountAmount;
        if (voucher.getDiscountType() == DiscountType.fixed_amount) {
            discountAmount = discountValue;
        } else if (voucher.getDiscountType() == DiscountType.percentage) {
            discountAmount = subtotal.multiply(discountValue)
                    .divide(ONE_HUNDRED, MONEY_SCALE, RoundingMode.HALF_UP);
        } else {
            throw new InvalidDataException("Voucher discount type is invalid: " + voucher.getDiscountType());
        }

        BigDecimal maxDiscountAmount = voucher.getMaxDiscountAmount();
        if (maxDiscountAmount != null && discountAmount.compareTo(maxDiscountAmount) > 0) {
            discountAmount = maxDiscountAmount;
        }
        if (discountAmount.compareTo(subtotal) > 0) {
            discountAmount = subtotal;
        }
        if (discountAmount.signum() < 0) {
            throw new InvalidDataException("Voucher discount amount is invalid: " + voucher.getId());
        }

        return discountAmount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private Voucher lockVoucher(VoucherReservation reservation) {
        if (reservation.getVoucher() == null || reservation.getVoucher().getId() == null) {
            throw new ResourceNotFoundException("Voucher not found for reservation: " + reservation.getId());
        }
        Long voucherId = reservation.getVoucher().getId();
        return voucherRepository.findByIdForUpdate(voucherId)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with ID: " + voucherId));
    }

    private String normalizeVoucherCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Voucher code is required");
        }
        return code.trim();
    }

    private String normalizeCheckoutCode(String checkoutCode) {
        if (checkoutCode == null || checkoutCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Checkout code is required");
        }
        return checkoutCode.trim();
    }

    private void validateSubtotal(BigDecimal subtotal) {
        if (subtotal == null) {
            throw new IllegalArgumentException("Subtotal is required");
        }
        if (subtotal.signum() < 0) {
            throw new IllegalArgumentException("Subtotal must not be negative");
        }
    }

    private void validateExpiresAt(OffsetDateTime expiresAt, OffsetDateTime now) {
        if (expiresAt == null || !expiresAt.isAfter(now)) {
            throw new IllegalArgumentException("Voucher reservation expiry time must be in the future");
        }
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now();
    }
}
