package vn.hcmute.edu.dp.nhom10.backend.pattern.observer.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderVoucherRestorationObserver implements OrderCancellationObserver {
    private final VoucherRepository voucherRepository;

    @Override
    public void onOrderCancelled(Order order) {
        Voucher voucher = order.getVoucher();
        if (voucher != null && voucher.getTimesUsed() > 0) {
            log.info("Restoring voucher usage for voucher: {}", voucher.getCode());
            voucher.setTimesUsed(voucher.getTimesUsed() - 1);
            voucherRepository.save(voucher);
        }
    }
}
