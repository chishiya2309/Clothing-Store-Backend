package vn.hcmute.edu.dp.nhom10.backend.pattern.chain.product;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffProductVariantRequest;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class ProductVariantValidationHandler extends ProductValidationHandler {

    @Override
    protected void validate(ProductValidationContext context) {
        if (context.getVariants() == null || context.getVariants().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng thêm ít nhất 1 biến thể (size + màu)");
        }

        Set<String> variantKeys = new HashSet<>();
        for (StaffProductVariantRequest variant : context.getVariants()) {
            String key = normalizeVariantKey(variant.size(), variant.color());
            if (!variantKeys.add(key)) {
                throw new InvalidDataException("Biến thể size + màu bị trùng trong request");
            }
        }
    }

    private String normalizeVariantKey(String size, String color) {
        return (size == null ? "" : size.trim().toUpperCase(Locale.ROOT))
                + "|"
                + (color == null ? "" : color.trim().toLowerCase(Locale.ROOT));
    }
}
