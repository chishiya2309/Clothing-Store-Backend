package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.OrderVoucherAdjustmentService;

@Service
@RequiredArgsConstructor
public class OrderVoucherAdjustmentServiceImpl implements OrderVoucherAdjustmentService {

    private final VoucherRepository voucherRepository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void restoreVoucherUsageForCancelledOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order is required");
        }
        Voucher voucher = order.getVoucher();
        if (voucher == null) {
            return;
        }
        if (voucher.getId() == null) {
            throw new IllegalStateException("Order voucher must be persisted before restoring usage");
        }

        Voucher lockedVoucher = voucherRepository.findByIdForUpdate(voucher.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with ID: " + voucher.getId()));
        Integer timesUsed = lockedVoucher.getTimesUsed();
        if (timesUsed == null || timesUsed <= 0) {
            throw new IllegalStateException("Voucher timesUsed is inconsistent and cannot be restored");
        }
        lockedVoucher.setTimesUsed(timesUsed - 1);
    }
}
