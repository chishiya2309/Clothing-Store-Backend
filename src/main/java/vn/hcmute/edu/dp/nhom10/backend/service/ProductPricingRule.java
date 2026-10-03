package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.ResolvedProductPrice;
import vn.hcmute.edu.dp.nhom10.backend.dto.pricing.ProductPricingContext;

public interface ProductPricingRule {
    boolean supports(ProductPricingContext context);

    ResolvedProductPrice resolve(ProductPricingContext context);
}
