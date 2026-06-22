package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.enums.CheckoutSessionStatus;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.internal.CheckoutFailureTransactionService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutFailureTransactionServiceTest {

    @Mock
    private CheckoutSessionRepository checkoutSessionRepository;

    @Mock
    private InventoryReservationService inventoryReservationService;

    @Mock
    private VoucherReservationService voucherService;

    @InjectMocks
    private CheckoutFailureTransactionService checkoutFailureTransactionService;

    @Test
    void failAndReleaseReservedCheckout_reservedCheckout_releasesInventoryAndMarksFailed() {
        CheckoutSession checkoutSession = checkout(CheckoutSessionStatus.reserved, null);
        mockCheckout(checkoutSession);

        checkoutFailureTransactionService.failAndReleaseReservedCheckout("CHK-1");

        verify(inventoryReservationService).releaseStockReservation("CHK-1");
        verify(checkoutSessionRepository).save(checkoutSession);
        assertEquals(CheckoutSessionStatus.failed, checkoutSession.getStatus());
    }

    @Test
    void failAndReleaseReservedCheckout_reservedCheckoutWithVoucher_releasesVoucher() {
        CheckoutSession checkoutSession = checkout(CheckoutSessionStatus.reserved, Voucher.builder().id(1L).build());
        mockCheckout(checkoutSession);

        checkoutFailureTransactionService.failAndReleaseReservedCheckout("CHK-1");

        verify(voucherService).releaseVoucherReservation("CHK-1");
    }

    @Test
    void failAndReleaseReservedCheckout_withoutVoucher_doesNotReleaseVoucher() {
        CheckoutSession checkoutSession = checkout(CheckoutSessionStatus.reserved, null);
        mockCheckout(checkoutSession);

        checkoutFailureTransactionService.failAndReleaseReservedCheckout("CHK-1");

        verify(voucherService, never()).releaseVoucherReservation(anyString());
    }

    @Test
    void failAndReleaseReservedCheckout_completedCheckout_doesNotRelease() {
        CheckoutSession checkoutSession = checkout(CheckoutSessionStatus.completed, Voucher.builder().id(1L).build());
        mockCheckout(checkoutSession);

        checkoutFailureTransactionService.failAndReleaseReservedCheckout("CHK-1");

        verify(inventoryReservationService, never()).releaseStockReservation(anyString());
        verify(voucherService, never()).releaseVoucherReservation(anyString());
        verify(checkoutSessionRepository, never()).save(any());
        assertEquals(CheckoutSessionStatus.completed, checkoutSession.getStatus());
    }

    @Test
    void failAndReleaseReservedCheckout_failedCheckout_keepsFailedStatus() {
        CheckoutSession checkoutSession = checkout(CheckoutSessionStatus.failed, null);
        mockCheckout(checkoutSession);

        checkoutFailureTransactionService.failAndReleaseReservedCheckout("CHK-1");

        verify(inventoryReservationService).releaseStockReservation("CHK-1");
        verify(checkoutSessionRepository, never()).save(any());
        assertEquals(CheckoutSessionStatus.failed, checkoutSession.getStatus());
    }

    @Test
    void failAndReleaseReservedCheckout_checkoutNotFound_returnsSafely() {
        when(checkoutSessionRepository.findByCheckoutCodeForUpdate("CHK-1")).thenReturn(Optional.empty());

        checkoutFailureTransactionService.failAndReleaseReservedCheckout("CHK-1");

        verify(inventoryReservationService, never()).releaseStockReservation(anyString());
        verify(voucherService, never()).releaseVoucherReservation(anyString());
    }

    @Test
    void failAndReleaseReservedCheckout_releasesInventoryBeforeVoucher() {
        CheckoutSession checkoutSession = checkout(CheckoutSessionStatus.reserved, Voucher.builder().id(1L).build());
        mockCheckout(checkoutSession);

        checkoutFailureTransactionService.failAndReleaseReservedCheckout("CHK-1");

        InOrder inOrder = inOrder(inventoryReservationService, voucherService);
        inOrder.verify(inventoryReservationService).releaseStockReservation("CHK-1");
        inOrder.verify(voucherService).releaseVoucherReservation("CHK-1");
    }

    @Test
    void failAndReleaseReservedCheckout_blankCode_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> checkoutFailureTransactionService.failAndReleaseReservedCheckout(" "));
    }

    private void mockCheckout(CheckoutSession checkoutSession) {
        when(checkoutSessionRepository.findByCheckoutCodeForUpdate("CHK-1")).thenReturn(Optional.of(checkoutSession));
    }

    private CheckoutSession checkout(CheckoutSessionStatus status, Voucher voucher) {
        return CheckoutSession.builder()
                .id(1L)
                .checkoutCode("CHK-1")
                .status(status)
                .voucher(voucher)
                .build();
    }
}
