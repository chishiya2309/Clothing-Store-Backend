package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.InventoryReservation;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.entity.VoucherReservation;
import vn.hcmute.edu.dp.nhom10.backend.enums.CheckoutSessionStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentAttemptStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReservationStatus;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.InventoryReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentAttemptRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.CheckoutExpirationServiceImpl;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutExpirationServiceImplTest {
    @Mock
    private FlashSaleReservationService flashSaleReservationService;

    @Mock
    private CheckoutSessionRepository checkoutSessionRepository;

    @Mock
    private InventoryReservationRepository inventoryReservationRepository;

    @Mock
    private VoucherReservationRepository voucherReservationRepository;

    @Mock
    private PaymentAttemptRepository paymentAttemptRepository;

    @InjectMocks
    private CheckoutExpirationServiceImpl checkoutExpirationService;

    private final OffsetDateTime now = OffsetDateTime.parse("2026-06-18T16:00:00Z");

    @Test
    void expireDueCheckouts_creatingExpiredCheckout_marksExpired() {
        CheckoutSession checkoutSession = checkout(CheckoutSessionStatus.creating, now.minusMinutes(1));
        mockExpiredCheckout(checkoutSession);

        int count = checkoutExpirationService.expireDueCheckouts(now);

        assertEquals(1, count);
        assertEquals(CheckoutSessionStatus.expired, checkoutSession.getStatus());
    }

    @Test
    void expireDueCheckouts_reservedExpiredCheckout_marksExpired() {
        CheckoutSession checkoutSession = checkout(CheckoutSessionStatus.reserved, now.minusMinutes(1));
        mockExpiredCheckout(checkoutSession);

        checkoutExpirationService.expireDueCheckouts(now);

        assertEquals(CheckoutSessionStatus.expired, checkoutSession.getStatus());
    }

    @Test
    void expireDueCheckouts_activeInventoryReservation_marksExpired() {
        InventoryReservation reservation = inventoryReservation(ReservationStatus.active);
        mockExpiredCheckoutWithInventory(reservation);

        checkoutExpirationService.expireDueCheckouts(now);

        assertEquals(ReservationStatus.expired, reservation.getStatus());
        verify(inventoryReservationRepository).saveAll(List.of(reservation));
    }

    @Test
    void expireDueCheckouts_consumedInventoryReservation_keepsStatus() {
        InventoryReservation reservation = inventoryReservation(ReservationStatus.consumed);
        mockExpiredCheckoutWithInventory(reservation);

        checkoutExpirationService.expireDueCheckouts(now);

        assertEquals(ReservationStatus.consumed, reservation.getStatus());
        verify(inventoryReservationRepository, never()).saveAll(any());
    }

    @Test
    void expireDueCheckouts_releasedInventoryReservation_keepsStatus() {
        InventoryReservation reservation = inventoryReservation(ReservationStatus.released);
        mockExpiredCheckoutWithInventory(reservation);

        checkoutExpirationService.expireDueCheckouts(now);

        assertEquals(ReservationStatus.released, reservation.getStatus());
        verify(inventoryReservationRepository, never()).saveAll(any());
    }

    @Test
    void expireDueCheckouts_expiredInventoryReservation_keepsStatus() {
        InventoryReservation reservation = inventoryReservation(ReservationStatus.expired);
        mockExpiredCheckoutWithInventory(reservation);

        checkoutExpirationService.expireDueCheckouts(now);

        assertEquals(ReservationStatus.expired, reservation.getStatus());
        verify(inventoryReservationRepository, never()).saveAll(any());
    }

    @Test
    void expireDueCheckouts_activeVoucherReservation_marksExpired() {
        VoucherReservation reservation = voucherReservation(ReservationStatus.active, Voucher.builder().timesUsed(0).build());
        mockExpiredCheckoutWithVoucher(reservation);

        checkoutExpirationService.expireDueCheckouts(now);

        assertEquals(ReservationStatus.expired, reservation.getStatus());
        verify(voucherReservationRepository).save(reservation);
    }

    @Test
    void expireDueCheckouts_consumedVoucherReservation_keepsStatus() {
        VoucherReservation reservation = voucherReservation(ReservationStatus.consumed, Voucher.builder().timesUsed(0).build());
        mockExpiredCheckoutWithVoucher(reservation);

        checkoutExpirationService.expireDueCheckouts(now);

        assertEquals(ReservationStatus.consumed, reservation.getStatus());
        verify(voucherReservationRepository, never()).save(any());
    }

    @Test
    void expireDueCheckouts_releasedVoucherReservation_keepsStatus() {
        VoucherReservation reservation = voucherReservation(ReservationStatus.released, Voucher.builder().timesUsed(0).build());
        mockExpiredCheckoutWithVoucher(reservation);

        checkoutExpirationService.expireDueCheckouts(now);

        assertEquals(ReservationStatus.released, reservation.getStatus());
        verify(voucherReservationRepository, never()).save(any());
    }

    @Test
    void expireDueCheckouts_expiredVoucherReservation_keepsStatus() {
        VoucherReservation reservation = voucherReservation(ReservationStatus.expired, Voucher.builder().timesUsed(0).build());
        mockExpiredCheckoutWithVoucher(reservation);

        checkoutExpirationService.expireDueCheckouts(now);

        assertEquals(ReservationStatus.expired, reservation.getStatus());
        verify(voucherReservationRepository, never()).save(any());
    }

    @Test
    void expireDueCheckouts_pendingPaymentAttempt_marksExpired() {
        PaymentAttempt paymentAttempt = paymentAttempt(PaymentAttemptStatus.pending);
        mockExpiredCheckoutWithPaymentAttempt(paymentAttempt);

        checkoutExpirationService.expireDueCheckouts(now);

        assertEquals(PaymentAttemptStatus.expired, paymentAttempt.getStatus());
        verify(paymentAttemptRepository).saveAll(List.of(paymentAttempt));
    }

    @Test
    void expireDueCheckouts_completedPaymentAttempt_keepsStatus() {
        PaymentAttempt paymentAttempt = paymentAttempt(PaymentAttemptStatus.completed);
        mockExpiredCheckoutWithPaymentAttempt(paymentAttempt);

        checkoutExpirationService.expireDueCheckouts(now);

        assertEquals(PaymentAttemptStatus.completed, paymentAttempt.getStatus());
        verify(paymentAttemptRepository, never()).saveAll(any());
    }

    @Test
    void expireDueCheckouts_failedPaymentAttempt_keepsStatus() {
        PaymentAttempt paymentAttempt = paymentAttempt(PaymentAttemptStatus.failed);
        mockExpiredCheckoutWithPaymentAttempt(paymentAttempt);

        checkoutExpirationService.expireDueCheckouts(now);

        assertEquals(PaymentAttemptStatus.failed, paymentAttempt.getStatus());
        verify(paymentAttemptRepository, never()).saveAll(any());
    }

    @Test
    void expireDueCheckouts_queriesOnlyCreatingAndReservedStatuses() {
        when(checkoutSessionRepository.findExpiredForUpdate(any(), eq(now))).thenReturn(List.of());

        checkoutExpirationService.expireDueCheckouts(now);

        ArgumentCaptor<Collection<CheckoutSessionStatus>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(checkoutSessionRepository).findExpiredForUpdate(captor.capture(), eq(now));
        assertEquals(List.of(CheckoutSessionStatus.creating, CheckoutSessionStatus.reserved), captor.getValue().stream().toList());
    }

    @Test
    void expireDueCheckouts_checkoutNotExpired_doesNotProcessReservations() {
        CheckoutSession checkoutSession = checkout(CheckoutSessionStatus.reserved, now.plusMinutes(1));
        when(checkoutSessionRepository.findExpiredForUpdate(any(), eq(now))).thenReturn(List.of(checkoutSession));

        int count = checkoutExpirationService.expireDueCheckouts(now);

        assertEquals(0, count);
        verifyNoInteractions(inventoryReservationRepository, voucherReservationRepository, paymentAttemptRepository);
        verify(checkoutSessionRepository, never()).save(any());
    }

    @Test
    void expireDueCheckouts_alreadyExpiredCheckout_doesNotProcessReservations() {
        CheckoutSession checkoutSession = checkout(CheckoutSessionStatus.expired, now.minusMinutes(1));
        when(checkoutSessionRepository.findExpiredForUpdate(any(), eq(now))).thenReturn(List.of(checkoutSession));

        int count = checkoutExpirationService.expireDueCheckouts(now);

        assertEquals(0, count);
        verifyNoInteractions(inventoryReservationRepository, voucherReservationRepository, paymentAttemptRepository);
        verify(checkoutSessionRepository, never()).save(any());
    }

    @Test
    void expireDueCheckouts_secondRunDoesNotChangeMoreRows() {
        CheckoutSession checkoutSession = checkout(CheckoutSessionStatus.reserved, now.minusMinutes(1));
        when(checkoutSessionRepository.findExpiredForUpdate(any(), eq(now)))
                .thenReturn(List.of(checkoutSession))
                .thenReturn(List.of());
        mockEmptyLockedChildren();

        int firstCount = checkoutExpirationService.expireDueCheckouts(now);
        int secondCount = checkoutExpirationService.expireDueCheckouts(now);

        assertEquals(1, firstCount);
        assertEquals(0, secondCount);
    }

    @Test
    void expireDueCheckouts_doesNotModifyStockQuantity() {
        InventoryReservation reservation = inventoryReservation(ReservationStatus.active);
        reservation.setQuantity(2);
        mockExpiredCheckoutWithInventory(reservation);

        checkoutExpirationService.expireDueCheckouts(now);

        assertEquals(2, reservation.getQuantity());
    }

    @Test
    void expireDueCheckouts_doesNotModifyVoucherTimesUsed() {
        Voucher voucher = Voucher.builder().timesUsed(5).build();
        VoucherReservation reservation = voucherReservation(ReservationStatus.active, voucher);
        mockExpiredCheckoutWithVoucher(reservation);

        checkoutExpirationService.expireDueCheckouts(now);

        assertEquals(5, voucher.getTimesUsed());
    }

    @Test
    void expireDueCheckouts_returnsExpiredCheckoutCount() {
        CheckoutSession first = checkout(CheckoutSessionStatus.creating, now.minusMinutes(2));
        CheckoutSession second = checkout(CheckoutSessionStatus.reserved, now.minusMinutes(1));
        second.setId(2L);
        when(checkoutSessionRepository.findExpiredForUpdate(any(), eq(now))).thenReturn(List.of(first, second));
        when(inventoryReservationRepository.findAllByCheckoutSessionIdForUpdate(any())).thenReturn(List.of());
        when(voucherReservationRepository.findByCheckoutSessionIdForUpdate(any())).thenReturn(Optional.empty());
        when(paymentAttemptRepository.findAllByCheckoutSessionIdForUpdate(any())).thenReturn(List.of());

        int count = checkoutExpirationService.expireDueCheckouts(now);

        assertEquals(2, count);
    }

    @Test
    void expireDueCheckouts_nullNow_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> checkoutExpirationService.expireDueCheckouts(null));
    }

    private void mockExpiredCheckout(CheckoutSession checkoutSession) {
        when(checkoutSessionRepository.findExpiredForUpdate(any(), eq(now))).thenReturn(List.of(checkoutSession));
        mockEmptyLockedChildren();
    }

    private void mockExpiredCheckoutWithInventory(InventoryReservation reservation) {
        when(checkoutSessionRepository.findExpiredForUpdate(any(), eq(now)))
                .thenReturn(List.of(checkout(CheckoutSessionStatus.reserved, now.minusMinutes(1))));
        when(inventoryReservationRepository.findAllByCheckoutSessionIdForUpdate(1L)).thenReturn(List.of(reservation));
        when(voucherReservationRepository.findByCheckoutSessionIdForUpdate(1L)).thenReturn(Optional.empty());
        when(paymentAttemptRepository.findAllByCheckoutSessionIdForUpdate(1L)).thenReturn(List.of());
    }

    private void mockExpiredCheckoutWithVoucher(VoucherReservation reservation) {
        when(checkoutSessionRepository.findExpiredForUpdate(any(), eq(now)))
                .thenReturn(List.of(checkout(CheckoutSessionStatus.reserved, now.minusMinutes(1))));
        when(inventoryReservationRepository.findAllByCheckoutSessionIdForUpdate(1L)).thenReturn(List.of());
        when(voucherReservationRepository.findByCheckoutSessionIdForUpdate(1L)).thenReturn(Optional.of(reservation));
        when(paymentAttemptRepository.findAllByCheckoutSessionIdForUpdate(1L)).thenReturn(List.of());
    }

    private void mockExpiredCheckoutWithPaymentAttempt(PaymentAttempt paymentAttempt) {
        when(checkoutSessionRepository.findExpiredForUpdate(any(), eq(now)))
                .thenReturn(List.of(checkout(CheckoutSessionStatus.reserved, now.minusMinutes(1))));
        when(inventoryReservationRepository.findAllByCheckoutSessionIdForUpdate(1L)).thenReturn(List.of());
        when(voucherReservationRepository.findByCheckoutSessionIdForUpdate(1L)).thenReturn(Optional.empty());
        when(paymentAttemptRepository.findAllByCheckoutSessionIdForUpdate(1L)).thenReturn(List.of(paymentAttempt));
    }

    private void mockEmptyLockedChildren() {
        when(inventoryReservationRepository.findAllByCheckoutSessionIdForUpdate(1L)).thenReturn(List.of());
        when(voucherReservationRepository.findByCheckoutSessionIdForUpdate(1L)).thenReturn(Optional.empty());
        when(paymentAttemptRepository.findAllByCheckoutSessionIdForUpdate(1L)).thenReturn(List.of());
    }

    private CheckoutSession checkout(CheckoutSessionStatus status, OffsetDateTime expiresAt) {
        return CheckoutSession.builder()
                .id(1L)
                .status(status)
                .expiresAt(expiresAt)
                .build();
    }

    private InventoryReservation inventoryReservation(ReservationStatus status) {
        return InventoryReservation.builder()
                .id(10L)
                .status(status)
                .quantity(1)
                .build();
    }

    private VoucherReservation voucherReservation(ReservationStatus status, Voucher voucher) {
        return VoucherReservation.builder()
                .id(20L)
                .status(status)
                .voucher(voucher)
                .build();
    }

    private PaymentAttempt paymentAttempt(PaymentAttemptStatus status) {
        return PaymentAttempt.builder()
                .id(30L)
                .status(status)
                .build();
    }
}
