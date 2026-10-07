package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.VoucherQuote;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.VoucherQuoteRequest;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;
import vn.hcmute.edu.dp.nhom10.backend.enums.VoucherSlot;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
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
        return quote(new VoucherQuoteRequest(code, VoucherSlot.product, customerId, subtotal, shippingFee));
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherQuote quote(VoucherQuoteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Voucher quote request is required");
        }

        String normalizedCode = normalizeCode(request.code());
        VoucherSlot expectedSlot = defaultSlot(request.expectedSlot());
        if (request.customerId() == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        User customer = userRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Voucher voucher = voucherRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher code is invalid"));

        OffsetDateTime now = OffsetDateTime.now();
        VoucherApplyContext context = new VoucherApplyContext(
                customer.getId(),
                defaultZero(request.subtotal()),
                defaultZero(request.shippingFee()),
                request.items(),
                now
        );

        VoucherState state = voucherStateResolver.resolve(voucher, now);
        state.validate(voucher, context);
        validateSlot(voucher, expectedSlot);

        VoucherDiscountStrategy strategy = voucherDiscountStrategyResolver.resolve(voucher.getDiscountType());
        VoucherApplyResult result = strategy.apply(voucher, context);

        return new VoucherQuote(
                voucher.getId(),
                voucher.getCode(),
                voucher.getDiscountType(),
                expectedSlot,
                defaultZero(result.discountAmount()),
                defaultZero(result.shippingDiscountAmount()),
                result.message()
        );
    }

    private String normalizeCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Voucher code is required");
        }
        return code.trim();
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private VoucherSlot defaultSlot(VoucherSlot slot) {
        return slot != null ? slot : VoucherSlot.product;
    }

    private void validateSlot(Voucher voucher, VoucherSlot expectedSlot) {
        VoucherSlot actualSlot = slotOf(voucher.getDiscountType());
        if (actualSlot != expectedSlot) {
            throw new InvalidDataException("Voucher " + voucher.getCode() + " is not valid for " + expectedSlot + " slot");
        }
    }

    private VoucherSlot slotOf(DiscountType discountType) {
        return switch (discountType) {
            case percentage, fixed_amount, cheapest_item_free -> VoucherSlot.product;
            case shipping_fixed_amount -> VoucherSlot.shipping;
        };
    }
}
