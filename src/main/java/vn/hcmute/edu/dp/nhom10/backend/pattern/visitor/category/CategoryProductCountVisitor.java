package vn.hcmute.edu.dp.nhom10.backend.pattern.visitor.category;

import vn.hcmute.edu.dp.nhom10.backend.entity.Category;

public class CategoryProductCountVisitor implements CategoryVisitor<Long> {
    @Override
    public Long visit(Category category) {
        if (category == null) return 0L;
        long count = category.getProducts() != null ? category.getProducts().size() : 0L;
        if (category.getChildren() != null) {
            for (Category child : category.getChildren()) {
                count += visit(child);
            }
        }
        return count;
    }
}
