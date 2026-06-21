package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ApplyVoucherRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.CreateVoucherRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpdateVoucherRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.AppliedVoucherResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.VoucherResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;
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
import vn.hcmute.edu.dp.nhom10.backend.service.VoucherService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final VoucherStateResolver voucherStateResolver;
    private final VoucherDiscountStrategyResolver voucherDiscountStrategyResolver;

    @Override
    @Transactional
    public VoucherResponse create(CreateVoucherRequest request) {
        String code = normalizeCode(request.code());
        if (voucherRepository.existsByCode(code)) {
            throw new InvalidDataException("Voucher code already exists");
        }
        validateVoucherData(request.discountType(), request.discountValue(), request.startDate(),
                request.endDate(), request.usageLimit());

        Voucher voucher = Voucher.builder()
                .code(code)
                .discountType(request.discountType())
                .discountValue(request.discountValue())
                .maxDiscountAmount(request.maxDiscountAmount())
                .minOrderAmount(defaultZero(request.minOrderAmount()))
                .startDate(request.startDate())
                .endDate(request.endDate())
                .usageLimit(request.usageLimit())
                .timesUsed(0)
                .isActive(request.isActive() == null || request.isActive())
                .build();

        return toResponse(voucherRepository.save(voucher));
    }

    @Override
    @Transactional
    public VoucherResponse update(Long id, UpdateVoucherRequest request) {
        Voucher voucher = getVoucher(id);
        validateVoucherData(request.discountType(), request.discountValue(), request.startDate(),
                request.endDate(), request.usageLimit());
        if (request.usageLimit() < voucher.getTimesUsed()) {
            throw new InvalidDataException("Usage limit cannot be lower than current used times");
        }

        voucher.setDiscountType(request.discountType());
        voucher.setDiscountValue(request.discountValue());
        voucher.setMaxDiscountAmount(request.maxDiscountAmount());
        voucher.setMinOrderAmount(defaultZero(request.minOrderAmount()));
        voucher.setStartDate(request.startDate());
        voucher.setEndDate(request.endDate());
        voucher.setUsageLimit(request.usageLimit());
        voucher.setIsActive(request.isActive());

        return toResponse(voucherRepository.save(voucher));
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherResponse getById(Long id) {
        return toResponse(getVoucher(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherResponse> getAll() {
        return voucherRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteOrDeactivate(Long id) {
        Voucher voucher = getVoucher(id);
        if (voucher.getTimesUsed() != null && voucher.getTimesUsed() > 0) {
            voucher.setIsActive(false);
            voucherRepository.save(voucher);
            return;
        }
        voucherRepository.delete(voucher);
    }

    @Override
    @Transactional(readOnly = true)
    public AppliedVoucherResponse apply(ApplyVoucherRequest request, String customerEmail) {
        String code = normalizeCode(request.code());
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher code is invalid"));

        OffsetDateTime now = OffsetDateTime.now();
        VoucherApplyContext context = new VoucherApplyContext(
                customer.getId(),
                request.subtotal(),
                defaultZero(request.shippingFee()),
                now
        );

        VoucherState state = voucherStateResolver.resolve(voucher, now);
        state.validate(voucher, context);

        VoucherDiscountStrategy strategy = voucherDiscountStrategyResolver.resolve(voucher.getDiscountType());
        VoucherApplyResult result = strategy.apply(voucher, context);

        return AppliedVoucherResponse.builder()
                .voucherId(voucher.getId())
                .code(voucher.getCode())
                .discountType(voucher.getDiscountType())
                .subtotal(context.subtotal())
                .shippingFee(context.normalizedShippingFee())
                .discountAmount(result.discountAmount())
                .shippingDiscountAmount(result.shippingDiscountAmount())
                .totalAmount(result.finalTotalAmount())
                .message(result.message())
                .build();
    }


    private Voucher getVoucher(Long id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with id: " + id));
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim();
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private void validateVoucherData(DiscountType discountType, BigDecimal discountValue,
                                     OffsetDateTime startDate, OffsetDateTime endDate, Integer usageLimit) {
        if (!endDate.isAfter(startDate)) {
            throw new InvalidDataException("End date must be after start date");
        }
        if (usageLimit == null || usageLimit < 1) {
            throw new InvalidDataException("Usage limit must be at least 1");
        }
        if (discountType == DiscountType.percentage
                && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new InvalidDataException("Percentage discount must not exceed 100");
        }
    }

    private VoucherResponse toResponse(Voucher voucher) {
        return VoucherResponse.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .maxDiscountAmount(voucher.getMaxDiscountAmount())
                .minOrderAmount(voucher.getMinOrderAmount())
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .usageLimit(voucher.getUsageLimit())
                .timesUsed(voucher.getTimesUsed())
                .isActive(voucher.getIsActive())
                .createdAt(voucher.getCreatedAt())
                .updatedAt(voucher.getUpdatedAt())
                .build();
    }
}
