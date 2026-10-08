package vn.hcmute.edu.dp.nhom10.backend.dto.pricing;

import vn.hcmute.edu.dp.nhom10.backend.enums.PriceSource;

import java.math.BigDecimal;

public record ProductPricingResult(
        BigDecimal productPrice,
        BigDecimal variantAdditionalPrice,
        BigDecimal unitPrice,
        PriceSource priceSource,
        Long flashSaleItemId
) {
}
