package vn.hcmute.edu.dp.nhom10.backend.pattern.state.voucher;

import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher.VoucherApplyContext;

public interface VoucherState {
    void validate(Voucher voucher, VoucherApplyContext context);
}
