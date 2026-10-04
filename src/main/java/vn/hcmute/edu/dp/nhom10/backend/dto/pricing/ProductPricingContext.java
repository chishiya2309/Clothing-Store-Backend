package vn.hcmute.edu.dp.nhom10.backend.dto.pricing;

import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;

import java.time.OffsetDateTime;

public record ProductPricingContext(
        Product product,
        ProductVariant variant,
        OffsetDateTime pricingTime
) {
}
