package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.VoucherQuote;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.state.voucher.VoucherState;
import vn.hcmute.edu.dp.nhom10.backend.pattern.state.voucher.VoucherStateResolver;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher.VoucherApplyContext;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher.VoucherApplyResult;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher.VoucherDiscountStrategy;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher.VoucherDiscountStrategyResolver;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.VoucherQuoteService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class VoucherQuoteServiceImpl implements VoucherQuoteService {

    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final VoucherStateResolver voucherStateResolver;
    private final VoucherDiscountStrategyResolver voucherDiscountStrategyResolver;

    @Override
    @Transactional(readOnly = true)
    public VoucherQuote quote(String code, Long customerId, BigDecimal subtotal, BigDecimal shippingFee) {
        String normalizedCode = normalizeCode(code);
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Voucher voucher = voucherRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher code is invalid"));

        OffsetDateTime now = OffsetDateTime.now();
        VoucherApplyContext context = new VoucherApplyContext(
                customer.getId(),
                subtotal,
                defaultZero(shippingFee),
                now
        );

        VoucherState state = voucherStateResolver.resolve(voucher, now);
        state.validate(voucher, context);

        VoucherDiscountStrategy strategy = voucherDiscountStrategyResolver.resolve(voucher.getDiscountType());
        VoucherApplyResult result = strategy.apply(voucher, context);

        return new VoucherQuote(
                voucher.getId(),
                voucher.getCode(),
                voucher.getDiscountType(),
                defaultZero(result.discountAmount()),
                defaultZero(result.shippingDiscountAmount()),
                result.message()
        );
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim();
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
