package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.VoucherQuote;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.VoucherQuoteRequest;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.enums.DiscountType;
import vn.hcmute.edu.dp.nhom10.backend.enums.VoucherSlot;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.state.voucher.VoucherState;
import vn.hcmute.edu.dp.nhom10.backend.pattern.state.voucher.VoucherStateResolver;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher.VoucherApplyContext;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher.VoucherApplyResult;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher.VoucherDiscountStrategy;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.voucher.VoucherDiscountStrategyResolver;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.VoucherQuoteServiceImpl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherQuoteServiceImplTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VoucherStateResolver voucherStateResolver;

    @Mock
    private VoucherDiscountStrategyResolver voucherDiscountStrategyResolver;

    @Mock
    private VoucherState voucherState;

    @Mock
    private VoucherDiscountStrategy voucherDiscountStrategy;

    private VoucherQuoteService voucherQuoteService;

    @BeforeEach
    void setUp() {
        voucherQuoteService = new VoucherQuoteServiceImpl(
                voucherRepository,
                userRepository,
                voucherStateResolver,
                voucherDiscountStrategyResolver
        );
    }

    @Test
    void quote_productVoucherWithProductSlot_success() {
        Voucher voucher = activeVoucher("SALE10", DiscountType.percentage, BigDecimal.TEN);
        mockQuoteDependencies(voucher);
        when(voucherDiscountStrategy.apply(any(Voucher.class), any(VoucherApplyContext.class)))
                .thenReturn(VoucherApplyResult.builder()
                        .discountAmount(BigDecimal.valueOf(30000))
                        .shippingDiscountAmount(BigDecimal.ZERO)
                        .finalTotalAmount(BigDecimal.valueOf(300000))
                        .message("Voucher applied successfully")
                        .build());

        VoucherQuote quote = voucherQuoteService.quote(new VoucherQuoteRequest(
                " SALE10 ",
                VoucherSlot.product,
                10L,
                BigDecimal.valueOf(300000),
                BigDecimal.valueOf(30000)
        ));

        assertEquals(1L, quote.voucherId());
        assertEquals("SALE10", quote.code());
        assertEquals(DiscountType.percentage, quote.discountType());
        assertEquals(VoucherSlot.product, quote.slot());
        assertEquals(0, BigDecimal.valueOf(30000).compareTo(quote.voucherDiscountAmount()));
    }

    @Test
    void quote_shippingVoucherWithShippingSlot_success() {
        Voucher voucher = activeVoucher("SHIP20", DiscountType.shipping_fixed_amount, BigDecimal.valueOf(20000));
        mockQuoteDependencies(voucher);
        when(voucherDiscountStrategy.apply(any(Voucher.class), any(VoucherApplyContext.class)))
                .thenReturn(VoucherApplyResult.builder()
                        .discountAmount(BigDecimal.ZERO)
                        .shippingDiscountAmount(BigDecimal.valueOf(20000))
                        .finalTotalAmount(BigDecimal.valueOf(310000))
                        .message("Voucher applied successfully")
                        .build());

        VoucherQuote quote = voucherQuoteService.quote(new VoucherQuoteRequest(
                "SHIP20",
                VoucherSlot.shipping,
                10L,
                BigDecimal.valueOf(300000),
                BigDecimal.valueOf(30000)
        ));

        assertEquals(DiscountType.shipping_fixed_amount, quote.discountType());
        assertEquals(VoucherSlot.shipping, quote.slot());
        assertEquals(0, BigDecimal.valueOf(20000).compareTo(quote.shippingDiscountAmount()));
    }

    @Test
    void quote_shippingVoucherWithProductSlot_throwsException() {
        Voucher voucher = activeVoucher("SHIP20", DiscountType.shipping_fixed_amount, BigDecimal.valueOf(20000));
        when(userRepository.findById(10L)).thenReturn(Optional.of(User.builder().id(10L).build()));
        when(voucherRepository.findByCode("SHIP20")).thenReturn(Optional.of(voucher));
        when(voucherStateResolver.resolve(any(Voucher.class), any(OffsetDateTime.class))).thenReturn(voucherState);

        assertThrows(InvalidDataException.class, () -> voucherQuoteService.quote(new VoucherQuoteRequest(
                "SHIP20",
                VoucherSlot.product,
                10L,
                BigDecimal.valueOf(300000),
                BigDecimal.valueOf(30000)
        )));

        verify(voucherDiscountStrategyResolver, never()).resolve(any(DiscountType.class));
    }

    private void mockQuoteDependencies(Voucher voucher) {
        when(userRepository.findById(10L)).thenReturn(Optional.of(User.builder().id(10L).build()));
        when(voucherRepository.findByCode(voucher.getCode())).thenReturn(Optional.of(voucher));
        when(voucherStateResolver.resolve(any(Voucher.class), any(OffsetDateTime.class))).thenReturn(voucherState);
        when(voucherDiscountStrategyResolver.resolve(voucher.getDiscountType())).thenReturn(voucherDiscountStrategy);
    }

    private Voucher activeVoucher(String code, DiscountType discountType, BigDecimal discountValue) {
        return Voucher.builder()
                .id(1L)
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
