package vn.hcmute.edu.dp.nhom10.backend.pattern.chain.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductRepository;

@Component
@RequiredArgsConstructor
public class ProductNameUniqueValidationHandler extends ProductValidationHandler {

    private final ProductRepository productRepository;

    @Override
    protected void validate(ProductValidationContext context) {
        if (context.getName() == null || context.getName().isBlank() || context.getCategoryId() == null) {
            return;
        }

        String name = context.getName().trim();
        Long categoryId = context.getCategoryId();
        Long productId = context.getProductId();

        if (productId == null) {
            if (productRepository.existsByCategoryIdAndNameIgnoreCase(categoryId, name)) {
                throw new InvalidDataException("Tên sản phẩm đã tồn tại trong danh mục này");
            }
        } else {
            if (productRepository.existsByCategoryIdAndNameIgnoreCaseAndIdNot(categoryId, name, productId)) {
                throw new InvalidDataException("Tên sản phẩm đã tồn tại trong danh mục này");
            }
        }
    }
}
