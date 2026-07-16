package vn.hcmute.edu.dp.nhom10.backend.dto.checkout;

import vn.hcmute.edu.dp.nhom10.backend.enums.PriceSource;

import java.math.BigDecimal;

public record ResolvedProductPrice(
        BigDecimal price,
        PriceSource priceSource,
        Long flashSaleItemId
) {
}
