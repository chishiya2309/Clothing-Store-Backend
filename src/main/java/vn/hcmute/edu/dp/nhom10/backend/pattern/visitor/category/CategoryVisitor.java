package vn.hcmute.edu.dp.nhom10.backend.pattern.visitor.category;

import vn.hcmute.edu.dp.nhom10.backend.entity.Category;

public interface CategoryVisitor<R> {
    R visit(Category category);
}
