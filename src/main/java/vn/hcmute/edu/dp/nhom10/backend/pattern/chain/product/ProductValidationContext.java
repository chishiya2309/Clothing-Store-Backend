package vn.hcmute.edu.dp.nhom10.backend.pattern.chain.product;

import lombok.Builder;
import lombok.Getter;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffProductImageRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffProductVariantRequest;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class ProductValidationContext {
    private final Long productId; // Null for creation, non-null for update
    private final String name;
    private final Long categoryId;
    private final BigDecimal basePrice;
    private final BigDecimal salePrice;
    private final List<StaffProductImageRequest> images;
    private final List<StaffProductVariantRequest> variants;
}
