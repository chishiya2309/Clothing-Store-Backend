package vn.hcmute.edu.dp.nhom10.backend.pattern.state.voucher;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher.VoucherApplyContext;

@Component
public class InactiveVoucherState implements VoucherState {

    @Override
    public void validate(Voucher voucher, VoucherApplyContext context) {
        throw new InvalidDataException("Voucher is inactive");
    }
}
