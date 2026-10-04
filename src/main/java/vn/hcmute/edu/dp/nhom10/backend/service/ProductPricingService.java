package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.pricing.ProductPricingResult;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;

import java.time.OffsetDateTime;

public interface ProductPricingService {
    ProductPricingResult resolve(Product product, ProductVariant variant, OffsetDateTime pricingTime);
}
