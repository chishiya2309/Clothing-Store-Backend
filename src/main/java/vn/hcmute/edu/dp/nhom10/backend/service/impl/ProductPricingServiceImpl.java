package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.ResolvedProductPrice;
import vn.hcmute.edu.dp.nhom10.backend.dto.pricing.ProductPricingContext;
import vn.hcmute.edu.dp.nhom10.backend.dto.pricing.ProductPricingResult;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.service.ProductPricingRule;
import vn.hcmute.edu.dp.nhom10.backend.service.ProductPricingService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductPricingServiceImpl implements ProductPricingService {

    private final List<ProductPricingRule> pricingRules;

    @Override
    public ProductPricingResult resolve(Product product, ProductVariant variant, OffsetDateTime pricingTime) {
        validate(product, variant, pricingTime);
        ProductPricingContext context = new ProductPricingContext(product, variant, pricingTime);
        ResolvedProductPrice productPrice = pricingRules.stream()
                .filter(rule -> rule.supports(context))
                .findFirst()
                .map(rule -> rule.resolve(context))
                .orElseThrow(() -> new InvalidDataException("No product pricing rule is available"));

        BigDecimal variantAdditionalPrice = variant.getAdditionalPrice() != null
                ? variant.getAdditionalPrice()
                : BigDecimal.ZERO;
        if (variantAdditionalPrice.signum() < 0) {
            throw new InvalidDataException("Variant additional price must not be negative: " + variant.getId());
        }

        BigDecimal unitPrice = productPrice.price().add(variantAdditionalPrice);
        if (unitPrice.signum() < 0) {
            throw new InvalidDataException("Unit price must not be negative");
        }

        return new ProductPricingResult(
                productPrice.price(),
                variantAdditionalPrice,
                unitPrice,
                productPrice.priceSource(),
                productPrice.flashSaleItemId()
        );
    }

    private void validate(Product product, ProductVariant variant, OffsetDateTime pricingTime) {
        if (product == null || product.getId() == null) {
            throw new IllegalArgumentException("Product is required for pricing");
        }
        if (variant == null || variant.getId() == null) {
            throw new IllegalArgumentException("Product variant is required for pricing");
        }
        if (pricingTime == null) {
            throw new IllegalArgumentException("Pricing time is required");
        }
    }
}
