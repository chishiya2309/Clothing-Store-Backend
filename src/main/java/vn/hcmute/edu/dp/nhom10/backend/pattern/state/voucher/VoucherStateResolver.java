package vn.hcmute.edu.dp.nhom10.backend.pattern.state.voucher;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class VoucherStateResolver {

    private final ActiveVoucherState activeVoucherState;
    private final InactiveVoucherState inactiveVoucherState;
    private final UpcomingVoucherState upcomingVoucherState;
    private final ExpiredVoucherState expiredVoucherState;
    private final ExhaustedVoucherState exhaustedVoucherState;

    public VoucherState resolve(Voucher voucher, OffsetDateTime now) {
        if (Boolean.FALSE.equals(voucher.getIsActive())) {
            return inactiveVoucherState;
        }
        if (now.isBefore(voucher.getStartDate())) {
            return upcomingVoucherState;
        }
        if (now.isAfter(voucher.getEndDate())) {
            return expiredVoucherState;
        }
        if (voucher.getTimesUsed() >= voucher.getUsageLimit()) {
            return exhaustedVoucherState;
        }
        return activeVoucherState;
    }
}
