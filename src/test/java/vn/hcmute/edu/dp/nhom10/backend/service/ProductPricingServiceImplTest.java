package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.ResolvedProductPrice;
import vn.hcmute.edu.dp.nhom10.backend.dto.pricing.ProductPricingContext;
import vn.hcmute.edu.dp.nhom10.backend.dto.pricing.ProductPricingResult;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.enums.PriceSource;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.ProductPricingServiceImpl;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.RegularProductPricingRule;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductPricingServiceImplTest {

    @Test
    void resolve_regularRule_usesSalePriceAndVariantAdditionalPrice() {
        Product product = product("100000.00", "80000.00");
        ProductVariant variant = variant("15000.00");

        ProductPricingResult result = service().resolve(product, variant, OffsetDateTime.now());

        assertEquals(new BigDecimal("80000.00"), result.productPrice());
        assertEquals(new BigDecimal("15000.00"), result.variantAdditionalPrice());
        assertEquals(new BigDecimal("95000.00"), result.unitPrice());
        assertEquals(PriceSource.PRODUCT_SALE, result.priceSource());
    }

    @Test
    void resolve_regularRule_usesBasePriceWhenSalePriceMissing() {
        Product product = product("100000.00", null);
        ProductVariant variant = variant("0.00");

        ProductPricingResult result = service().resolve(product, variant, OffsetDateTime.now());

        assertEquals(new BigDecimal("100000.00"), result.productPrice());
        assertEquals(new BigDecimal("100000.00"), result.unitPrice());
        assertEquals(PriceSource.REGULAR, result.priceSource());
    }

    @Test
    void resolve_firstMatchingRuleCanProvideFuturePriceSources() {
        Product product = product("100000.00", null);
        ProductVariant variant = variant("5000.00");
        ProductPricingRule promotionalRule = new ProductPricingRule() {
            @Override
            public boolean supports(ProductPricingContext context) {
                return true;
            }

            @Override
            public ResolvedProductPrice resolve(ProductPricingContext context) {
                return new ResolvedProductPrice(new BigDecimal("60000.00"), PriceSource.FLASH_SALE, 9L);
            }
        };
        ProductPricingServiceImpl pricingService = new ProductPricingServiceImpl(
                List.of(promotionalRule, new RegularProductPricingRule())
        );

        ProductPricingResult result = pricingService.resolve(product, variant, OffsetDateTime.now());

        assertEquals(new BigDecimal("65000.00"), result.unitPrice());
        assertEquals(PriceSource.FLASH_SALE, result.priceSource());
        assertEquals(9L, result.flashSaleItemId());
    }

    @Test
    void resolve_negativeVariantAdditionalPrice_throwsException() {
        Product product = product("100000.00", null);
        ProductVariant variant = variant("-1.00");

        assertThrows(RuntimeException.class, () -> service().resolve(product, variant, OffsetDateTime.now()));
    }

    private ProductPricingServiceImpl service() {
        return new ProductPricingServiceImpl(List.of(new RegularProductPricingRule()));
    }

    private Product product(String basePrice, String salePrice) {
        return Product.builder()
                .id(1L)
                .basePrice(new BigDecimal(basePrice))
                .salePrice(salePrice == null ? null : new BigDecimal(salePrice))
                .build();
    }

    private ProductVariant variant(String additionalPrice) {
        return ProductVariant.builder()
                .id(2L)
                .additionalPrice(additionalPrice == null ? null : new BigDecimal(additionalPrice))
                .build();
    }
}
