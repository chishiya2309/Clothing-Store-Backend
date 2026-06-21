package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import vn.hcmute.edu.dp.nhom10.backend.pattern.state.voucher.ActiveVoucherState;
import vn.hcmute.edu.dp.nhom10.backend.pattern.state.voucher.ExhaustedVoucherState;
import vn.hcmute.edu.dp.nhom10.backend.pattern.state.voucher.ExpiredVoucherState;
import vn.hcmute.edu.dp.nhom10.backend.pattern.state.voucher.InactiveVoucherState;
import vn.hcmute.edu.dp.nhom10.backend.pattern.state.voucher.UpcomingVoucherState;
import vn.hcmute.edu.dp.nhom10.backend.pattern.state.voucher.VoucherStateResolver;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher.FixedAmountDiscountStrategy;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher.PercentageDiscountStrategy;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher.VoucherDiscountStrategyResolver;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.VoucherServiceImpl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherServiceImplTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private UserRepository userRepository;

    private VoucherService voucherService;

    @BeforeEach
    void setUp() {
        VoucherStateResolver stateResolver = new VoucherStateResolver(
                new ActiveVoucherState(),
                new InactiveVoucherState(),
                new UpcomingVoucherState(),
                new ExpiredVoucherState(),
                new ExhaustedVoucherState()
        );
        VoucherDiscountStrategyResolver strategyResolver = new VoucherDiscountStrategyResolver(
                List.of(new PercentageDiscountStrategy(), new FixedAmountDiscountStrategy())
        );
        voucherService = new VoucherServiceImpl(voucherRepository, userRepository, stateResolver, strategyResolver);
    }

    @Test
    void create_percentageVoucher_success() {
        CreateVoucherRequest request = new CreateVoucherRequest(
                " SALE10 ",
                DiscountType.percentage,
                BigDecimal.TEN,
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(100000),
                OffsetDateTime.now().plusMinutes(1),
                OffsetDateTime.now().plusDays(7),
                100,
                true
        );

        when(voucherRepository.existsByCode("SALE10")).thenReturn(false);
        when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> {
            Voucher voucher = invocation.getArgument(0);
            voucher.setId(1L);
            return voucher;
        });

        VoucherResponse response = voucherService.create(request);

        assertEquals(1L, response.id());
        assertEquals("SALE10", response.code());
        assertEquals(DiscountType.percentage, response.discountType());
        assertEquals(0, response.timesUsed());
        verify(voucherRepository).save(any(Voucher.class));
    }

    @Test
    void create_duplicateCode_throwsException() {
        CreateVoucherRequest request = new CreateVoucherRequest(
                "SALE10",
                DiscountType.percentage,
                BigDecimal.TEN,
                null,
                BigDecimal.ZERO,
                OffsetDateTime.now().plusMinutes(1),
                OffsetDateTime.now().plusDays(7),
                100,
                true
        );

        when(voucherRepository.existsByCode("SALE10")).thenReturn(true);

        assertThrows(InvalidDataException.class, () -> voucherService.create(request));
        verify(voucherRepository, never()).save(any());
    }

    @Test
    void create_percentageGreaterThan100_throwsException() {
        CreateVoucherRequest request = new CreateVoucherRequest(
                "SALE110",
                DiscountType.percentage,
                BigDecimal.valueOf(110),
                null,
                BigDecimal.ZERO,
                OffsetDateTime.now().plusMinutes(1),
                OffsetDateTime.now().plusDays(7),
                100,
                true
        );

        when(voucherRepository.existsByCode("SALE110")).thenReturn(false);

        assertThrows(InvalidDataException.class, () -> voucherService.create(request));
        verify(voucherRepository, never()).save(any());
    }

    @Test
    void update_usageLimitLowerThanTimesUsed_throwsException() {
        Voucher voucher = activeVoucher("SALE10", DiscountType.percentage, BigDecimal.TEN);
        voucher.setId(1L);
        voucher.setTimesUsed(5);

        UpdateVoucherRequest request = new UpdateVoucherRequest(
                DiscountType.percentage,
                BigDecimal.TEN,
                null,
                BigDecimal.ZERO,
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().plusDays(7),
                4,
                true
        );

        when(voucherRepository.findById(1L)).thenReturn(Optional.of(voucher));

        assertThrows(InvalidDataException.class, () -> voucherService.update(1L, request));
        verify(voucherRepository, never()).save(any());
    }

    @Test
    void apply_percentageVoucher_success() {
        User customer = new User();
        customer.setId(10L);
        customer.setEmail("customer@test.com");
        Voucher voucher = activeVoucher("SALE10", DiscountType.percentage, BigDecimal.TEN);
        voucher.setId(1L);
        voucher.setMaxDiscountAmount(BigDecimal.valueOf(40000));
        voucher.setMinOrderAmount(BigDecimal.valueOf(100000));

        ApplyVoucherRequest request = new ApplyVoucherRequest(
                "SALE10",
                BigDecimal.valueOf(500000),
                BigDecimal.valueOf(30000)
        );

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));
        when(voucherRepository.findByCode("SALE10")).thenReturn(Optional.of(voucher));

        AppliedVoucherResponse response = voucherService.apply(request, "customer@test.com");

        assertEquals(1L, response.voucherId());
        assertEquals("SALE10", response.code());
        assertEquals(0, BigDecimal.valueOf(40000).compareTo(response.discountAmount()));
        assertEquals(0, BigDecimal.valueOf(490000).compareTo(response.totalAmount()));
    }

    @Test
    void apply_fixedAmountVoucher_success() {
        User customer = new User();
        customer.setId(10L);
        customer.setEmail("customer@test.com");
        Voucher voucher = activeVoucher("FIX50", DiscountType.fixed_amount, BigDecimal.valueOf(50000));
        voucher.setId(2L);

        ApplyVoucherRequest request = new ApplyVoucherRequest(
                "FIX50",
                BigDecimal.valueOf(300000),
                BigDecimal.valueOf(30000)
        );

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));
        when(voucherRepository.findByCode("FIX50")).thenReturn(Optional.of(voucher));

        AppliedVoucherResponse response = voucherService.apply(request, "customer@test.com");

        assertEquals(0, BigDecimal.valueOf(50000).compareTo(response.discountAmount()));
        assertEquals(0, BigDecimal.valueOf(280000).compareTo(response.totalAmount()));
    }

    @Test
    void apply_invalidCode_throwsException() {
        User customer = new User();
        customer.setId(10L);

        ApplyVoucherRequest request = new ApplyVoucherRequest(
                "UNKNOWN",
                BigDecimal.valueOf(300000),
                BigDecimal.ZERO
        );

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));
        when(voucherRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> voucherService.apply(request, "customer@test.com"));
    }

    @Test
    void apply_expiredVoucher_throwsException() {
        User customer = new User();
        customer.setId(10L);
        Voucher voucher = activeVoucher("OLD10", DiscountType.percentage, BigDecimal.TEN);
        voucher.setEndDate(OffsetDateTime.now().minusDays(1));

        ApplyVoucherRequest request = new ApplyVoucherRequest(
                "OLD10",
                BigDecimal.valueOf(300000),
                BigDecimal.ZERO
        );

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));
        when(voucherRepository.findByCode("OLD10")).thenReturn(Optional.of(voucher));

        assertThrows(InvalidDataException.class, () -> voucherService.apply(request, "customer@test.com"));
    }

    @Test
    void apply_orderBelowMinimum_throwsException() {
        User customer = new User();
        customer.setId(10L);
        Voucher voucher = activeVoucher("MIN500", DiscountType.percentage, BigDecimal.TEN);
        voucher.setMinOrderAmount(BigDecimal.valueOf(500000));

        ApplyVoucherRequest request = new ApplyVoucherRequest(
                "MIN500",
                BigDecimal.valueOf(300000),
                BigDecimal.ZERO
        );

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));
        when(voucherRepository.findByCode("MIN500")).thenReturn(Optional.of(voucher));

        assertThrows(InvalidDataException.class, () -> voucherService.apply(request, "customer@test.com"));
    }

    @Test
    void deleteOrDeactivate_usedVoucher_deactivatesOnly() {
        Voucher voucher = activeVoucher("USED10", DiscountType.percentage, BigDecimal.TEN);
        voucher.setId(1L);
        voucher.setTimesUsed(3);

        when(voucherRepository.findById(1L)).thenReturn(Optional.of(voucher));

        voucherService.deleteOrDeactivate(1L);

        assertTrue(Boolean.FALSE.equals(voucher.getIsActive()));
        verify(voucherRepository).save(voucher);
        verify(voucherRepository, never()).delete(any());
    }

    @Test
    void deleteOrDeactivate_unusedVoucher_deletes() {
        Voucher voucher = activeVoucher("NEW10", DiscountType.percentage, BigDecimal.TEN);
        voucher.setId(1L);
        voucher.setTimesUsed(0);

        when(voucherRepository.findById(1L)).thenReturn(Optional.of(voucher));

        voucherService.deleteOrDeactivate(1L);

        verify(voucherRepository).delete(voucher);
        verify(voucherRepository, never()).save(any());
    }

    private Voucher activeVoucher(String code, DiscountType discountType, BigDecimal discountValue) {
        return Voucher.builder()
                .code(code)
                .discountType(discountType)
                .discountValue(discountValue)
                .minOrderAmount(BigDecimal.ZERO)
                .startDate(OffsetDateTime.now().minusDays(1))
                .endDate(OffsetDateTime.now().plusDays(7))
                .usageLimit(100)
                .timesUsed(0)
                .isActive(true)
                .build();
    }
}
