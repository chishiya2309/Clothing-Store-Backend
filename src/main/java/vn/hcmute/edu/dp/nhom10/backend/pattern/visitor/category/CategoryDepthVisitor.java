package vn.hcmute.edu.dp.nhom10.backend.pattern.visitor.category;

import vn.hcmute.edu.dp.nhom10.backend.entity.Category;

public class CategoryDepthVisitor implements CategoryVisitor<Integer> {
    @Override
    public Integer visit(Category category) {
        if (category == null) return 0;
        int maxChildDepth = 0;
        if (category.getChildren() != null) {
            for (Category child : category.getChildren()) {
                maxChildDepth = Math.max(maxChildDepth, visit(child));
            }
        }
        return 1 + maxChildDepth;
    }
}
