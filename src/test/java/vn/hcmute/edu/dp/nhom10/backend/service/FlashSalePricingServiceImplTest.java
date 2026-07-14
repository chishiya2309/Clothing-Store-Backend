package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.ResolvedProductPrice;
import vn.hcmute.edu.dp.nhom10.backend.entity.FlashSaleItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.enums.PriceSource;
import vn.hcmute.edu.dp.nhom10.backend.repository.FlashSaleItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.FlashSalePricingServiceImpl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlashSalePricingServiceImplTest {
    @Mock
    private FlashSaleItemRepository repository;

    @Test
    void resolve_activeFlashSale_hasHighestPriority() {
        Product product = product("100000", "80000");
        FlashSaleItem item = FlashSaleItem.builder().id(9L).flashSalePrice(new BigDecimal("60000")).build();
        OffsetDateTime now = OffsetDateTime.parse("2026-07-14T20:00:00+07:00");
        when(repository.findActiveForProductAt(eq(1L), eq(now), any(Pageable.class)))
                .thenReturn(List.of(item));

        ResolvedProductPrice result = new FlashSalePricingServiceImpl(repository).resolve(product, now);

        assertEquals(new BigDecimal("60000"), result.price());
        assertEquals(PriceSource.FLASH_SALE, result.priceSource());
        assertEquals(9L, result.flashSaleItemId());
    }

    @Test
    void resolve_noFlashSale_usesProductSalePrice() {
        Product product = product("100000", "80000");
        when(repository.findActiveForProductAt(eq(1L), any(), any(Pageable.class))).thenReturn(List.of());

        ResolvedProductPrice result = new FlashSalePricingServiceImpl(repository)
                .resolve(product, OffsetDateTime.now());

        assertEquals(new BigDecimal("80000"), result.price());
        assertEquals(PriceSource.PRODUCT_SALE, result.priceSource());
    }

    @Test
    void resolve_noDiscount_usesRegularPrice() {
        Product product = product("100000", null);
        when(repository.findActiveForProductAt(eq(1L), any(), any(Pageable.class))).thenReturn(List.of());

        ResolvedProductPrice result = new FlashSalePricingServiceImpl(repository)
                .resolve(product, OffsetDateTime.now());

        assertEquals(new BigDecimal("100000"), result.price());
        assertEquals(PriceSource.REGULAR, result.priceSource());
    }

    private Product product(String basePrice, String salePrice) {
        return Product.builder()
                .id(1L)
                .basePrice(new BigDecimal(basePrice))
                .salePrice(salePrice == null ? null : new BigDecimal(salePrice))
                .build();
    }
}
