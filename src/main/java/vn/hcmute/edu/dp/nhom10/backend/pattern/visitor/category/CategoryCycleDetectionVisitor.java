package vn.hcmute.edu.dp.nhom10.backend.pattern.visitor.category;

import vn.hcmute.edu.dp.nhom10.backend.entity.Category;

public class CategoryCycleDetectionVisitor implements CategoryVisitor<Boolean> {
    private final Long candidateParentId;

    public CategoryCycleDetectionVisitor(Long candidateParentId) {
        this.candidateParentId = candidateParentId;
    }

    @Override
    public Boolean visit(Category category) {
        if (category == null) return false;
        if (category.getId().equals(candidateParentId)) return true;
        if (category.getChildren() != null) {
            for (Category child : category.getChildren()) {
                if (visit(child)) return true;
            }
        }
        return false;
    }
}
