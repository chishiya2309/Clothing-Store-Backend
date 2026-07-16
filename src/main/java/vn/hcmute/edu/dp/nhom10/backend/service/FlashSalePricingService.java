package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.ResolvedProductPrice;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;

import java.time.OffsetDateTime;

public interface FlashSalePricingService {
    ResolvedProductPrice resolve(Product product, OffsetDateTime now);
}
