package vn.hcmute.edu.dp.nhom10.backend.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.event.OrderCancelledEvent;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherRepository;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoucherRestoreListener {

    private final VoucherRepository voucherRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleVoucherRestore(OrderCancelledEvent event) {
        Voucher voucher = event.order().getVoucher();
        if (voucher == null) {
            return;
        }

        log.info("Đơn hàng {} có áp dụng voucher: {}. Bắt đầu xử lý hoàn trả.", event.order().getOrderCode(), voucher.getCode());

        // Kiểm tra Voucher còn hoạt động và chưa hết hạn không
        OffsetDateTime now = OffsetDateTime.now();
        if (Boolean.FALSE.equals(voucher.getIsActive()) || now.isAfter(voucher.getEndDate())) {
            log.warn("Voucher {} đã hết hạn hoặc bị vô hiệu hóa. Bỏ qua việc khôi phục lượt dùng.", voucher.getCode());
            return;
        }

        // Khôi phục lượt dùng
        if (voucher.getTimesUsed() > 0) {
            int currentUsed = voucher.getTimesUsed();
            voucher.setTimesUsed(currentUsed - 1);
            voucherRepository.save(voucher);
            log.info("Khôi phục thành công lượt sử dụng Voucher {}: {} -> {}", voucher.getCode(), currentUsed, currentUsed - 1);
        }
    }
}
