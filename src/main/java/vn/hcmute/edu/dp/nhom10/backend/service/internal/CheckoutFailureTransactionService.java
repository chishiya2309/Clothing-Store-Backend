package vn.hcmute.edu.dp.nhom10.backend.service.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.enums.CheckoutSessionStatus;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.InventoryReservationService;
import vn.hcmute.edu.dp.nhom10.backend.service.VoucherReservationService;

@Service
@RequiredArgsConstructor
public class CheckoutFailureTransactionService {

    private final CheckoutSessionRepository checkoutSessionRepository;
    private final InventoryReservationService inventoryReservationService;
    private final VoucherReservationService voucherService;

    @Transactional
    public void failAndReleaseReservedCheckout(String checkoutCode) {
        String normalizedCheckoutCode = normalizeCheckoutCode(checkoutCode);
        CheckoutSession checkoutSession = checkoutSessionRepository.findByCheckoutCodeForUpdate(normalizedCheckoutCode)
                .orElse(null);
        if (checkoutSession == null || checkoutSession.getStatus() == CheckoutSessionStatus.completed) {
            return;
        }

        inventoryReservationService.releaseStockReservation(normalizedCheckoutCode);
        if (checkoutSession.getVoucher() != null) {
            voucherService.releaseVoucherReservation(normalizedCheckoutCode);
        }
        if (checkoutSession.getStatus() != CheckoutSessionStatus.failed) {
            checkoutSession.setStatus(CheckoutSessionStatus.failed);
            checkoutSessionRepository.save(checkoutSession);
        }
    }

    private String normalizeCheckoutCode(String checkoutCode) {
        if (checkoutCode == null || checkoutCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Checkout code is required");
        }
        return checkoutCode.trim();
    }
}
