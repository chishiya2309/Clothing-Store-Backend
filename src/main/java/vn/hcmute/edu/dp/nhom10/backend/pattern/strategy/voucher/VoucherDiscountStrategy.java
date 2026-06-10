package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher;

import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;

public interface VoucherDiscountStrategy {
    DiscountType supports();

    VoucherApplyResult apply(Voucher voucher, VoucherApplyContext context);
}
