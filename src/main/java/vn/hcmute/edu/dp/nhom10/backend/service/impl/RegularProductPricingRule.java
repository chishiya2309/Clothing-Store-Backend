package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.ResolvedProductPrice;
import vn.hcmute.edu.dp.nhom10.backend.dto.pricing.ProductPricingContext;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.enums.PriceSource;
import vn.hcmute.edu.dp.nhom10.backend.service.ProductPricingRule;

import java.math.BigDecimal;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class RegularProductPricingRule implements ProductPricingRule {

    @Override
    public boolean supports(ProductPricingContext context) {
        return true;
    }

    @Override
    public ResolvedProductPrice resolve(ProductPricingContext context) {
        if (context == null || context.product() == null || context.product().getId() == null) {
            throw new IllegalArgumentException("Product is required for pricing");
        }
        Product product = context.product();
        if (product.getSalePrice() != null) {
            return result(product.getSalePrice(), PriceSource.PRODUCT_SALE, product.getId());
        }
        return result(product.getBasePrice(), PriceSource.REGULAR, product.getId());
    }

    private ResolvedProductPrice result(BigDecimal price, PriceSource source, Long productId) {
        if (price == null) {
            throw new IllegalArgumentException("Product price is missing: " + productId);
        }
        if (price.signum() < 0) {
            throw new IllegalArgumentException("Product price must not be negative: " + productId);
        }
        return new ResolvedProductPrice(price, source, null);
    }
}
