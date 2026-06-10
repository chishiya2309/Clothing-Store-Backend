package vn.hcmute.edu.dp.nhom10.backend.pattern.state.voucher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class VoucherStateResolverTest {

    private VoucherStateResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new VoucherStateResolver(
                new ActiveVoucherState(),
                new InactiveVoucherState(),
                new UpcomingVoucherState(),
                new ExpiredVoucherState(),
                new ExhaustedVoucherState()
        );
    }

    @Test
    void resolve_inactiveVoucher() {
        Voucher voucher = baseVoucher();
        voucher.setIsActive(false);

        assertInstanceOf(InactiveVoucherState.class, resolver.resolve(voucher, OffsetDateTime.now()));
    }

    @Test
    void resolve_upcomingVoucher() {
        Voucher voucher = baseVoucher();
        OffsetDateTime now = OffsetDateTime.now();
        voucher.setStartDate(now.plusDays(1));
        voucher.setEndDate(now.plusDays(2));

        assertInstanceOf(UpcomingVoucherState.class, resolver.resolve(voucher, now));
    }

    @Test
    void resolve_expiredVoucher() {
        Voucher voucher = baseVoucher();
        OffsetDateTime now = OffsetDateTime.now();
        voucher.setStartDate(now.minusDays(2));
        voucher.setEndDate(now.minusDays(1));

        assertInstanceOf(ExpiredVoucherState.class, resolver.resolve(voucher, now));
    }

    @Test
    void resolve_exhaustedVoucher() {
        Voucher voucher = baseVoucher();
        voucher.setTimesUsed(10);
        voucher.setUsageLimit(10);

        assertInstanceOf(ExhaustedVoucherState.class, resolver.resolve(voucher, OffsetDateTime.now()));
    }

    @Test
    void resolve_activeVoucher() {
        Voucher voucher = baseVoucher();

        assertInstanceOf(ActiveVoucherState.class, resolver.resolve(voucher, OffsetDateTime.now()));
    }

    private Voucher baseVoucher() {
        return Voucher.builder()
                .code("SALE10")
                .discountType(DiscountType.percentage)
                .discountValue(BigDecimal.TEN)
                .startDate(OffsetDateTime.now().minusDays(1))
                .endDate(OffsetDateTime.now().plusDays(1))
                .usageLimit(10)
                .timesUsed(0)
                .isActive(true)
                .build();
    }
}
