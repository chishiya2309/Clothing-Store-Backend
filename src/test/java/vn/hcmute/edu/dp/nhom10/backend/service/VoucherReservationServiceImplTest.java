package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.entity.VoucherReservation;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReservationStatus;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.reservation.voucher.VoucherReservationServiceImpl;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherReservationRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class VoucherReservationServiceImplTest {

    @Mock
    private CheckoutSessionRepository checkoutSessionRepository;

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private VoucherReservationRepository voucherReservationRepository;

    @InjectMocks
    private VoucherReservationServiceImpl voucherService;

    @Test
    void reserveVoucher_fixed_success() {
        CheckoutSession session = checkoutSession();
        Voucher voucher = voucher(DiscountType.fixed_amount, "50000.00");
        mockReserve(session, voucher, 0L);

        BigDecimal discount = voucherService.reserveVoucher(1L, " SAVE10 ", money("200000.00"), future());

        VoucherReservation saved = captureSavedReservation();
        assertEquals(money("50000.00"), discount);
        assertEquals(session, saved.getCheckoutSession());
        assertEquals(voucher, saved.getVoucher());
        assertEquals(money("50000.00"), saved.getDiscountAmount());
        assertEquals(ReservationStatus.active, saved.getStatus());
    }

    @Test
    void reserveVoucher_percentage_success() {
        mockReserve(checkoutSession(), voucher(DiscountType.percentage, "10.00"), 0L);

        BigDecimal discount = voucherService.reserveVoucher(1L, "SAVE10", money("200000.00"), future());

        assertEquals(money("20000.00"), discount);
    }

    @Test
    void reserveVoucher_percentageAppliesMaxDiscountAmount() {
        Voucher voucher = voucher(DiscountType.percentage, "50.00");
        voucher.setMaxDiscountAmount(money("30000.00"));
        mockReserve(checkoutSession(), voucher, 0L);

        BigDecimal discount = voucherService.reserveVoucher(1L, "SAVE10", money("100000.00"), future());

        assertEquals(money("30000.00"), discount);
    }

    @Test
    void reserveVoucher_discountDoesNotExceedSubtotal() {
        mockReserve(checkoutSession(), voucher(DiscountType.fixed_amount, "50000.00"), 0L);

        BigDecimal discount = voucherService.reserveVoucher(1L, "SAVE10", money("30000.00"), future());

        assertEquals(money("30000.00"), discount);
    }

    @Test
    void reserveVoucher_blankCode_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> voucherService.reserveVoucher(1L, " ", money("100000.00"), future()));

        verifyNoInteractions(checkoutSessionRepository, voucherRepository, voucherReservationRepository);
    }

    @Test
    void reserveVoucher_nullSubtotal_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> voucherService.reserveVoucher(1L, "SAVE10", null, future()));

        verifyNoInteractions(checkoutSessionRepository, voucherRepository, voucherReservationRepository);
    }

    @Test
    void reserveVoucher_negativeSubtotal_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> voucherService.reserveVoucher(1L, "SAVE10", money("-1.00"), future()));

        verifyNoInteractions(checkoutSessionRepository, voucherRepository, voucherReservationRepository);
    }

    @Test
    void reserveVoucher_invalidExpiresAt_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> voucherService.reserveVoucher(1L, "SAVE10", money("100000.00"), past()));

        verifyNoInteractions(checkoutSessionRepository, voucherRepository, voucherReservationRepository);
    }

    @Test
    void reserveVoucher_checkoutNotFound_throwsException() {
        when(checkoutSessionRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> voucherService.reserveVoucher(1L, "SAVE10", money("100000.00"), future()));

        verify(voucherRepository, never()).findByCodeForUpdate(any());
        verify(voucherReservationRepository, never()).save(any());
    }

    @Test
    void reserveVoucher_existingReservation_throwsException() {
        when(checkoutSessionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(checkoutSession()));
        when(voucherReservationRepository.existsByCheckoutSession_Id(1L)).thenReturn(true);

        assertThrows(InvalidDataException.class,
                () -> voucherService.reserveVoucher(1L, "SAVE10", money("100000.00"), future()));

        verify(voucherRepository, never()).findByCodeForUpdate(any());
        verify(voucherReservationRepository, never()).save(any());
    }

    @Test
    void reserveVoucher_voucherNotFound_throwsException() {
        when(checkoutSessionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(checkoutSession()));
        when(voucherReservationRepository.existsByCheckoutSession_Id(1L)).thenReturn(false);
        when(voucherRepository.findByCodeForUpdate("SAVE10")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> voucherService.reserveVoucher(1L, "SAVE10", money("100000.00"), future()));

        verify(voucherReservationRepository, never()).save(any());
    }

    @Test
    void reserveVoucher_inactiveVoucher_throwsException() {
        Voucher voucher = voucher(DiscountType.fixed_amount, "10000.00");
        voucher.setIsActive(false);
        mockReserve(checkoutSession(), voucher, 0L);

        assertThrows(InvalidDataException.class,
                () -> voucherService.reserveVoucher(1L, "SAVE10", money("100000.00"), future()));

        verify(voucherReservationRepository, never()).save(any());
    }

    @Test
    void reserveVoucher_beforeStartDate_throwsException() {
        Voucher voucher = voucher(DiscountType.fixed_amount, "10000.00");
        voucher.setStartDate(OffsetDateTime.now().plusDays(1));
        mockReserve(checkoutSession(), voucher, 0L);

        assertThrows(InvalidDataException.class,
                () -> voucherService.reserveVoucher(1L, "SAVE10", money("100000.00"), future()));
    }

    @Test
    void reserveVoucher_afterEndDate_throwsException() {
        Voucher voucher = voucher(DiscountType.fixed_amount, "10000.00");
        voucher.setEndDate(past());
        mockReserve(checkoutSession(), voucher, 0L);

        assertThrows(InvalidDataException.class,
                () -> voucherService.reserveVoucher(1L, "SAVE10", money("100000.00"), future()));
    }

    @Test
    void reserveVoucher_minOrderAmountNotMet_throwsException() {
        Voucher voucher = voucher(DiscountType.fixed_amount, "10000.00");
        voucher.setMinOrderAmount(money("200000.00"));
        mockReserve(checkoutSession(), voucher, 0L);

        assertThrows(InvalidDataException.class,
                () -> voucherService.reserveVoucher(1L, "SAVE10", money("100000.00"), future()));
    }

    @Test
    void reserveVoucher_hasAvailableUses_success() {
        Voucher voucher = voucher(DiscountType.fixed_amount, "10000.00");
        voucher.setUsageLimit(2);
        voucher.setTimesUsed(1);
        mockReserve(checkoutSession(), voucher, 0L);

        BigDecimal discount = voucherService.reserveVoucher(1L, "SAVE10", money("100000.00"), future());

        assertEquals(money("10000.00"), discount);
    }

    @Test
    void reserveVoucher_noAvailableUsesAfterActiveReservation_throwsException() {
        Voucher voucher = voucher(DiscountType.fixed_amount, "10000.00");
        voucher.setUsageLimit(2);
        voucher.setTimesUsed(1);
        mockReserve(checkoutSession(), voucher, 1L);

        assertThrows(InvalidDataException.class,
                () -> voucherService.reserveVoucher(1L, "SAVE10", money("100000.00"), future()));

        verify(voucherReservationRepository, never()).save(any());
    }

    @Test
    void reserveVoucher_expiredActiveReservationIsNotCounted_success() {
        Voucher voucher = voucher(DiscountType.fixed_amount, "10000.00");
        voucher.setUsageLimit(1);
        voucher.setTimesUsed(0);
        mockReserve(checkoutSession(), voucher, 0L);

        voucherService.reserveVoucher(1L, "SAVE10", money("100000.00"), future());

        verify(voucherReservationRepository).countActiveReservations(eq(100L), eq(ReservationStatus.active), any(OffsetDateTime.class));
        verify(voucherReservationRepository).save(any(VoucherReservation.class));
    }

    @Test
    void reserveVoucher_negativeDiscountValue_throwsException() {
        mockReserve(checkoutSession(), voucher(DiscountType.fixed_amount, "-10000.00"), 0L);

        assertThrows(InvalidDataException.class,
                () -> voucherService.reserveVoucher(1L, "SAVE10", money("100000.00"), future()));

        verify(voucherReservationRepository, never()).save(any());
    }

    @Test
    void reserveVoucher_doesNotIncreaseTimesUsed() {
        Voucher voucher = voucher(DiscountType.fixed_amount, "10000.00");
        mockReserve(checkoutSession(), voucher, 0L);

        voucherService.reserveVoucher(1L, "SAVE10", money("100000.00"), future());

        assertEquals(0, voucher.getTimesUsed());
    }

    @Test
    void reserveVoucher_validationFailureDoesNotSave() {
        assertThrows(IllegalArgumentException.class,
                () -> voucherService.reserveVoucher(null, "SAVE10", money("100000.00"), future()));

        verify(voucherReservationRepository, never()).save(any());
    }

    @Test
    void consumeVoucherReservation_active_success() {
        Voucher voucher = voucher(DiscountType.fixed_amount, "10000.00");
        VoucherReservation reservation = reservation(voucher, ReservationStatus.active, future());
        mockConsume(reservation);
        when(voucherRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(voucher));

        voucherService.consumeVoucherReservation("CHK-1");

        assertEquals(ReservationStatus.consumed, reservation.getStatus());
        verify(voucherRepository).save(voucher);
        verify(voucherReservationRepository).save(reservation);
    }

    @Test
    void consumeVoucherReservation_increasesTimesUsedOnce() {
        Voucher voucher = voucher(DiscountType.fixed_amount, "10000.00");
        VoucherReservation reservation = reservation(voucher, ReservationStatus.active, future());
        mockConsume(reservation);
        when(voucherRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(voucher));

        voucherService.consumeVoucherReservation("CHK-1");

        assertEquals(1, voucher.getTimesUsed());
    }

    @Test
    void consumeVoucherReservation_calledAgain_doesNotIncreaseTimesUsed() {
        Voucher voucher = voucher(DiscountType.fixed_amount, "10000.00");
        VoucherReservation reservation = reservation(voucher, ReservationStatus.consumed, future());
        mockConsume(reservation);

        voucherService.consumeVoucherReservation("CHK-1");

        assertEquals(0, voucher.getTimesUsed());
        verify(voucherRepository, never()).findByIdForUpdate(any());
        verify(voucherRepository, never()).save(any());
        verify(voucherReservationRepository, never()).save(any());
    }

    @Test
    void consumeVoucherReservation_releasedReservation_throwsException() {
        mockConsume(reservation(voucher(DiscountType.fixed_amount, "10000.00"), ReservationStatus.released, future()));

        assertThrows(InvalidDataException.class, () -> voucherService.consumeVoucherReservation("CHK-1"));

        verify(voucherRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void consumeVoucherReservation_expiredReservation_throwsException() {
        mockConsume(reservation(voucher(DiscountType.fixed_amount, "10000.00"), ReservationStatus.expired, future()));

        assertThrows(InvalidDataException.class, () -> voucherService.consumeVoucherReservation("CHK-1"));

        verify(voucherRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void consumeVoucherReservation_activeButExpired_throwsException() {
        mockConsume(reservation(voucher(DiscountType.fixed_amount, "10000.00"), ReservationStatus.active, past()));

        assertThrows(InvalidDataException.class, () -> voucherService.consumeVoucherReservation("CHK-1"));

        verify(voucherRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void consumeVoucherReservation_usageLimitReached_throwsException() {
        Voucher voucher = voucher(DiscountType.fixed_amount, "10000.00");
        voucher.setUsageLimit(1);
        voucher.setTimesUsed(1);
        mockConsume(reservation(voucher, ReservationStatus.active, future()));
        when(voucherRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(voucher));

        assertThrows(InvalidDataException.class, () -> voucherService.consumeVoucherReservation("CHK-1"));

        verify(voucherRepository, never()).save(any());
        verify(voucherReservationRepository, never()).save(any());
    }

    @Test
    void consumeVoucherReservation_voucherNotFound_throwsException() {
        Voucher voucher = voucher(DiscountType.fixed_amount, "10000.00");
        mockConsume(reservation(voucher, ReservationStatus.active, future()));
        when(voucherRepository.findByIdForUpdate(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> voucherService.consumeVoucherReservation("CHK-1"));

        verify(voucherRepository, never()).save(any());
        verify(voucherReservationRepository, never()).save(any());
    }

    @Test
    void releaseVoucherReservation_active_setsReleased() {
        VoucherReservation reservation = reservation(voucher(DiscountType.fixed_amount, "10000.00"), ReservationStatus.active, future());
        mockRelease(Optional.of(reservation));

        voucherService.releaseVoucherReservation("CHK-1");

        assertEquals(ReservationStatus.released, reservation.getStatus());
        verify(voucherReservationRepository).save(reservation);
    }

    @Test
    void releaseVoucherReservation_released_keepsState() {
        VoucherReservation reservation = reservation(voucher(DiscountType.fixed_amount, "10000.00"), ReservationStatus.released, future());
        mockRelease(Optional.of(reservation));

        voucherService.releaseVoucherReservation("CHK-1");

        assertEquals(ReservationStatus.released, reservation.getStatus());
        verify(voucherReservationRepository, never()).save(any());
    }

    @Test
    void releaseVoucherReservation_expired_keepsState() {
        VoucherReservation reservation = reservation(voucher(DiscountType.fixed_amount, "10000.00"), ReservationStatus.expired, past());
        mockRelease(Optional.of(reservation));

        voucherService.releaseVoucherReservation("CHK-1");

        assertEquals(ReservationStatus.expired, reservation.getStatus());
        verify(voucherReservationRepository, never()).save(any());
    }

    @Test
    void releaseVoucherReservation_consumed_keepsState() {
        VoucherReservation reservation = reservation(voucher(DiscountType.fixed_amount, "10000.00"), ReservationStatus.consumed, future());
        mockRelease(Optional.of(reservation));

        voucherService.releaseVoucherReservation("CHK-1");

        assertEquals(ReservationStatus.consumed, reservation.getStatus());
        verify(voucherReservationRepository, never()).save(any());
    }

    @Test
    void releaseVoucherReservation_withoutReservation_returns() {
        mockRelease(Optional.empty());

        voucherService.releaseVoucherReservation("CHK-1");

        verify(voucherReservationRepository, never()).save(any());
    }

    @Test
    void releaseVoucherReservation_doesNotChangeTimesUsed() {
        Voucher voucher = voucher(DiscountType.fixed_amount, "10000.00");
        voucher.setTimesUsed(3);
        mockRelease(Optional.of(reservation(voucher, ReservationStatus.active, future())));

        voucherService.releaseVoucherReservation("CHK-1");

        assertEquals(3, voucher.getTimesUsed());
        verifyNoInteractions(voucherRepository);
    }

    private void mockReserve(CheckoutSession checkoutSession, Voucher voucher, long activeReservations) {
        when(checkoutSessionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(checkoutSession));
        when(voucherReservationRepository.existsByCheckoutSession_Id(1L)).thenReturn(false);
        when(voucherRepository.findByCodeForUpdate("SAVE10")).thenReturn(Optional.of(voucher));
        lenient().when(voucherReservationRepository.countActiveReservations(eq(voucher.getId()), eq(ReservationStatus.active), any(OffsetDateTime.class)))
                .thenReturn(activeReservations);
    }

    private void mockConsume(VoucherReservation reservation) {
        when(checkoutSessionRepository.findByCheckoutCodeForUpdate("CHK-1")).thenReturn(Optional.of(checkoutSession()));
        when(voucherReservationRepository.findByCheckoutSessionIdForUpdate(1L)).thenReturn(Optional.of(reservation));
    }

    private void mockRelease(Optional<VoucherReservation> reservation) {
        when(checkoutSessionRepository.findByCheckoutCodeForUpdate("CHK-1")).thenReturn(Optional.of(checkoutSession()));
        when(voucherReservationRepository.findByCheckoutSessionIdForUpdate(1L)).thenReturn(reservation);
    }

    private VoucherReservation captureSavedReservation() {
        ArgumentCaptor<VoucherReservation> captor = ArgumentCaptor.forClass(VoucherReservation.class);
        verify(voucherReservationRepository).save(captor.capture());
        return captor.getValue();
    }

    private CheckoutSession checkoutSession() {
        return CheckoutSession.builder()
                .id(1L)
                .checkoutCode("CHK-1")
                .build();
    }

    private Voucher voucher(DiscountType discountType, String discountValue) {
        return Voucher.builder()
                .id(100L)
                .code("SAVE10")
                .discountType(discountType)
                .discountValue(money(discountValue))
                .minOrderAmount(BigDecimal.ZERO)
                .startDate(OffsetDateTime.now().minusDays(1))
                .endDate(OffsetDateTime.now().plusDays(1))
                .usageLimit(10)
                .timesUsed(0)
                .isActive(true)
                .build();
    }

    private VoucherReservation reservation(Voucher voucher, ReservationStatus status, OffsetDateTime expiresAt) {
        return VoucherReservation.builder()
                .id(200L)
                .checkoutSession(checkoutSession())
                .voucher(voucher)
                .discountAmount(money("10000.00"))
                .status(status)
                .expiresAt(expiresAt)
                .build();
    }

    private BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    private OffsetDateTime future() {
        return OffsetDateTime.now().plusHours(1);
    }

    private OffsetDateTime past() {
        return OffsetDateTime.now().minusHours(1);
    }
}
