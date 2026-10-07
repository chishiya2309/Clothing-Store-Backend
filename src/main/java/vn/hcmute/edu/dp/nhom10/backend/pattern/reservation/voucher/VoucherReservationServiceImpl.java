package vn.hcmute.edu.dp.nhom10.backend.pattern.reservation.voucher;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.CheckoutData;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.CheckoutItemSnapshot;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.entity.VoucherReservation;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReservationStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.VoucherSlot;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.VoucherReservationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherReservationServiceImpl implements VoucherReservationService {

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
        return reserveVoucher(
                checkoutSessionId,
                code,
                VoucherSlot.product,
                new CheckoutData(null, null, null, List.of(), subtotal, BigDecimal.ZERO),
                expiresAt,
                true
        );
    }

    @Override
    @Transactional
    public BigDecimal reserveVoucher(
            Long checkoutSessionId,
            String code,
            VoucherSlot slot,
            CheckoutData checkoutData,
            OffsetDateTime expiresAt
    ) {
        if (checkoutData == null) {
            throw new IllegalArgumentException("Checkout data is required");
        }
        return reserveVoucher(checkoutSessionId, code, defaultSlot(slot), checkoutData, expiresAt, false);
    }

    private BigDecimal reserveVoucher(
            Long checkoutSessionId,
            String code,
            VoucherSlot slot,
            CheckoutData checkoutData,
            OffsetDateTime expiresAt,
            boolean legacySingleVoucherMode
    ) {
        if (checkoutSessionId == null) {
            throw new IllegalArgumentException("Checkout session ID is required");
        }
        String normalizedCode = normalizeVoucherCode(code);
        BigDecimal subtotal = requireAmount(checkoutData.subtotal(), "Subtotal");
        BigDecimal shippingFee = defaultZero(checkoutData.shippingFee());
        List<CheckoutItemSnapshot> items = checkoutData.items() != null ? checkoutData.items() : List.of();
        OffsetDateTime now = now();
        validateExpiresAt(expiresAt, now);

        CheckoutSession checkoutSession = checkoutSessionRepository.findByIdForUpdate(checkoutSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkout session not found with ID: " + checkoutSessionId));

        boolean reservationExists = legacySingleVoucherMode
                ? voucherReservationRepository.existsByCheckoutSession_Id(checkoutSessionId)
                : voucherReservationRepository.existsByCheckoutSession_IdAndVoucherSlot(checkoutSessionId, slot);
        if (reservationExists) {
            throw new InvalidDataException("Voucher reservation already exists for checkout session: " + checkoutSessionId);
        }

        Voucher voucher = voucherRepository.findByCodeForUpdate(normalizedCode)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with code: " + normalizedCode));

        validateVoucherAvailable(voucher, subtotal, now);
        validateSlot(voucher, slot);
        BigDecimal discountAmount = calculateDiscountAmount(voucher, subtotal, shippingFee, items);

        VoucherReservation reservation = VoucherReservation.builder()
                .checkoutSession(checkoutSession)
                .voucher(voucher)
                .discountAmount(discountAmount)
                .voucherSlot(slot)
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

    private BigDecimal calculateDiscountAmount(
            Voucher voucher,
            BigDecimal subtotal,
            BigDecimal shippingFee,
            List<CheckoutItemSnapshot> items
    ) {
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
        } else if (voucher.getDiscountType() == DiscountType.shipping_fixed_amount) {
            discountAmount = discountValue.min(shippingFee);
        } else if (voucher.getDiscountType() == DiscountType.cheapest_item_free) {
            discountAmount = calculateCheapestItemDiscount(items);
        } else {
            throw new InvalidDataException("Voucher discount type is invalid: " + voucher.getDiscountType());
        }

        if (voucher.getDiscountType() != DiscountType.shipping_fixed_amount) {
            BigDecimal maxDiscountAmount = voucher.getMaxDiscountAmount();
            if (maxDiscountAmount != null && discountAmount.compareTo(maxDiscountAmount) > 0) {
                discountAmount = maxDiscountAmount;
            }
        }
        if (voucher.getDiscountType() != DiscountType.shipping_fixed_amount
                && discountAmount.compareTo(subtotal) > 0) {
            discountAmount = subtotal;
        }
        if (discountAmount.signum() < 0) {
            throw new InvalidDataException("Voucher discount amount is invalid: " + voucher.getId());
        }

        return discountAmount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCheapestItemDiscount(List<CheckoutItemSnapshot> items) {
        int totalQuantity = items.stream()
                .mapToInt(this::safeQuantity)
                .sum();
        if (totalQuantity <= 1) {
            throw new InvalidDataException("Cheapest item voucher requires at least two items");
        }

        return items.stream()
                .filter(item -> safeQuantity(item) > 0)
                .map(CheckoutItemSnapshot::unitPrice)
                .filter(price -> price != null && price.compareTo(BigDecimal.ZERO) >= 0)
                .min(BigDecimal::compareTo)
                .orElseThrow(() -> new InvalidDataException("Checkout items are required for cheapest item voucher"));
    }

    private int safeQuantity(CheckoutItemSnapshot item) {
        return item.quantity() != null ? item.quantity() : 0;
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

    private void validateExpiresAt(OffsetDateTime expiresAt, OffsetDateTime now) {
        if (expiresAt == null || !expiresAt.isAfter(now)) {
            throw new IllegalArgumentException("Voucher reservation expiry time must be in the future");
        }
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now();
    }

    private BigDecimal requireAmount(BigDecimal amount, String fieldName) {
        if (amount == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        return amount;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private VoucherSlot defaultSlot(VoucherSlot slot) {
        return slot != null ? slot : VoucherSlot.product;
    }

    private void validateSlot(Voucher voucher, VoucherSlot expectedSlot) {
        VoucherSlot actualSlot = slotOf(voucher.getDiscountType());
        if (actualSlot != expectedSlot) {
            throw new InvalidDataException("Voucher " + voucher.getCode() + " is not valid for " + expectedSlot + " slot");
        }
    }

    private VoucherSlot slotOf(DiscountType discountType) {
        return switch (discountType) {
            case percentage, fixed_amount, cheapest_item_free -> VoucherSlot.product;
            case shipping_fixed_amount -> VoucherSlot.shipping;
        };
    }
}
