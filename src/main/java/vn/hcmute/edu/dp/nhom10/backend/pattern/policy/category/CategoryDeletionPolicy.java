package vn.hcmute.edu.dp.nhom10.backend.pattern.policy.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.entity.Category;
import vn.hcmute.edu.dp.nhom10.backend.pattern.visitor.category.CategoryProductCountVisitor;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;

@Component
@RequiredArgsConstructor
public class CategoryDeletionPolicy {

    public void checkCanDelete(Category category) {
        if (category == null) return;
        
        CategoryProductCountVisitor visitor = new CategoryProductCountVisitor();
        long productCount = visitor.visit(category);
        if (productCount > 0) {
            throw new InvalidDataException("Không thể xóa danh mục vì vẫn còn " + productCount + " sản phẩm liên kết trực tiếp hoặc gián tiếp.");
        }
    }
}
