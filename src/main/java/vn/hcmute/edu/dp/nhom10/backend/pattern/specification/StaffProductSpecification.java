package vn.hcmute.edu.dp.nhom10.backend.pattern.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffProductSearchCriteria;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.enums.StaffProductStatus;

public final class StaffProductSpecification {

    private StaffProductSpecification() {
    }

    public static Specification<Product> fromCriteria(StaffProductSearchCriteria criteria) {
        Specification<Product> spec = alwaysTrue();

        if (criteria == null) {
            return spec;
        }

        if (criteria.keyword() != null && !criteria.keyword().isBlank()) {
            spec = spec.and(keywordContains(criteria.keyword().trim()));
        }
        if (criteria.categoryId() != null) {
            spec = spec.and(belongsToCategory(criteria.categoryId()));
        }
        if (criteria.status() != null) {
            spec = spec.and(hasStatus(criteria.status()));
        }

        return spec;
    }

    public static Specification<Product> alwaysTrue() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<Product> keywordContains(String keyword) {
        return (root, query, cb) -> {
            String like = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("slug")), like)
            );
        };
    }

    public static Specification<Product> belongsToCategory(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> hasStatus(StaffProductStatus status) {
        return switch (status) {
            case ACTIVE -> isActiveWithStockAtLeast(10);
            case INACTIVE -> (root, query, cb) -> cb.isFalse(root.get("isActive"));
            case OUT_OF_STOCK -> isOutOfStock();
            case LOW_STOCK -> isActiveWithStockBetween(1, 9);
        };
    }

    private static Specification<Product> isActiveWithStockAtLeast(int minStock) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Product, ProductVariant> variantJoin = root.join("variants", JoinType.INNER);
            return cb.and(
                    cb.isTrue(root.get("isActive")),
                    cb.isTrue(variantJoin.get("isActive")),
                    cb.greaterThanOrEqualTo(variantJoin.get("stockQuantity"), minStock)
            );
        };
    }

    private static Specification<Product> isActiveWithStockBetween(int minStock, int maxStock) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Product, ProductVariant> variantJoin = root.join("variants", JoinType.INNER);
            return cb.and(
                    cb.isTrue(root.get("isActive")),
                    cb.isTrue(variantJoin.get("isActive")),
                    cb.between(variantJoin.get("stockQuantity"), minStock, maxStock)
            );
        };
    }

    private static Specification<Product> isOutOfStock() {
        return (root, query, cb) -> {
            Subquery<Long> activeStockVariant = query.subquery(Long.class);
            var variant = activeStockVariant.from(ProductVariant.class);
            activeStockVariant.select(variant.get("id"));
            Predicate sameProduct = cb.equal(variant.get("product").get("id"), root.get("id"));
            Predicate activeVariant = cb.isTrue(variant.get("isActive"));
            Predicate hasStock = cb.greaterThan(variant.get("stockQuantity"), 0);
            activeStockVariant.where(sameProduct, activeVariant, hasStock);

            return cb.and(
                    cb.isTrue(root.get("isActive")),
                    cb.not(cb.exists(activeStockVariant))
            );
        };
    }
}
