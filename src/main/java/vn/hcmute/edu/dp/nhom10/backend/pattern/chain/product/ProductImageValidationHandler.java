package vn.hcmute.edu.dp.nhom10.backend.pattern.chain.product;

import org.springframework.stereotype.Component;

@Component
public class ProductImageValidationHandler extends ProductValidationHandler {

    @Override
    protected void validate(ProductValidationContext context) {
        if (context.getImages() == null || context.getImages().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng upload ít nhất 1 hình ảnh");
        }
    }
}
