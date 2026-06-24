package vn.hcmute.edu.dp.nhom10.backend.pattern.chain.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.CategoryRepository;

@Component
@RequiredArgsConstructor
public class CategoryExistsValidationHandler extends ProductValidationHandler {

    private final CategoryRepository categoryRepository;

    @Override
    protected void validate(ProductValidationContext context) {
        Long categoryId = context.getCategoryId();
        if (categoryId != null) {
            categoryRepository.findById(categoryId)
                    .filter(category -> Boolean.TRUE.equals(category.getIsActive()))
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy danh mục đang hoạt động với id: " + categoryId));
        }
    }
}
