package vn.hcmute.edu.dp.nhom10.backend.pattern.state.voucher;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher.VoucherApplyContext;

@Component
public class ActiveVoucherState implements VoucherState {

    @Override
    public void validate(Voucher voucher, VoucherApplyContext context) {
        if (context.subtotal().compareTo(voucher.getMinOrderAmount()) < 0) {
            throw new InvalidDataException(
                    "Order must be at least " + voucher.getMinOrderAmount() + " to apply this voucher");
        }
    }
}
