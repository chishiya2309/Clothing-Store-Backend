package vn.hcmute.edu.dp.nhom10.backend.pattern.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ProductSearchCriteria;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.entity.CollectionProduct;
import vn.hcmute.edu.dp.nhom10.backend.entity.Collection;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Specification Pattern: xây dựng các điều kiện truy vấn động cho Product.
 * <p>
 * Mỗi phương thức static trả về một Specification đơn lẻ (single responsibility).
 * Các Specification được kết hợp bằng {@code Specification.where().and()} trong
 * {@link #fromCriteria(ProductSearchCriteria, List)}.
 * </p>
 */
public final class ProductSpecification {

    private ProductSpecification() {
        // Utility class
    }

    /**
     * Builder method: tổng hợp tất cả criteria thành một Specification duy nhất.
     *
     * @param criteria    Các tiêu chí tìm kiếm/lọc
     * @param categoryIds Danh sách category IDs đã được resolve (bao gồm descendants)
     * @return Specification tổng hợp
     */
    public static Specification<Product> fromCriteria(ProductSearchCriteria criteria, List<Long> categoryIds) {
        Specification<Product> spec = Specification.where(isActive());

        if (categoryIds != null && !categoryIds.isEmpty()) {
            spec = spec.and(inCategories(categoryIds));
        }

        if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
            spec = spec.and(nameContains(criteria.getKeyword().trim()));
        }

        if (criteria.getCollectionSlug() != null && !criteria.getCollectionSlug().isBlank()) {
            spec = spec.and(inCollection(criteria.getCollectionSlug().trim()));
        }

        if (criteria.getColors() != null && !criteria.getColors().isEmpty()) {
            spec = spec.and(hasColors(criteria.getColors()));
        }

        if (criteria.getSizes() != null && !criteria.getSizes().isEmpty()) {
            spec = spec.and(hasSizes(criteria.getSizes()));
        }

        if (criteria.getMinPrice() != null) {
            spec = spec.and(priceGreaterThanOrEqual(criteria.getMinPrice()));
        }

        if (criteria.getMaxPrice() != null) {
            spec = spec.and(priceLessThanOrEqual(criteria.getMaxPrice()));
        }

        return spec;
    }

    /** Lọc theo Collection Slug */
    public static Specification<Product> inCollection(String collectionSlug) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<CollectionProduct> cpRoot = subquery.from(CollectionProduct.class);
            Join<CollectionProduct, Collection> collectionJoin = cpRoot.join("collection");
            subquery.select(cpRoot.get("product").get("id"))
                    .where(
                            cb.equal(collectionJoin.get("slug"), collectionSlug),
                            cb.isTrue(collectionJoin.get("isActive"))
                    );
            return cb.in(root.get("id")).value(subquery);
        };
    }

    /** Chỉ lấy sản phẩm đang hoạt động */
    public static Specification<Product> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }

    /** Lọc theo danh sách category IDs */
    public static Specification<Product> inCategories(List<Long> categoryIds) {
        return (root, query, cb) -> root.get("category").get("id").in(categoryIds);
    }

    /** Tìm kiếm theo tên sản phẩm (LIKE, case-insensitive) */
    public static Specification<Product> nameContains(String keyword) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
    }

    /** Lọc theo màu sắc (JOIN variants, DISTINCT) */
    public static Specification<Product> hasColors(List<String> colors) {
        return (root, query, cb) -> {
            Join<Product, ProductVariant> variantJoin = root.join("variants", JoinType.INNER);
            query.distinct(true);

            List<String> lowerColors = colors.stream()
                    .map(String::toLowerCase)
                    .toList();
            return cb.lower(variantJoin.get("color")).in(lowerColors);
        };
    }

    /** Lọc theo kích cỡ (JOIN variants, DISTINCT) */
    public static Specification<Product> hasSizes(List<String> sizes) {
        return (root, query, cb) -> {
            Join<Product, ProductVariant> variantJoin = root.join("variants", JoinType.INNER);
            query.distinct(true);

            List<String> upperSizes = sizes.stream()
                    .map(String::toUpperCase)
                    .toList();
            return variantJoin.get("size").in(upperSizes);
        };
    }

    /** Lọc giá tối thiểu (dùng salePrice nếu có, fallback basePrice) */
    public static Specification<Product> priceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(
                cb.coalesce(root.get("salePrice"), root.get("basePrice")),
                minPrice
        );
    }

    /** Lọc giá tối đa (dùng salePrice nếu có, fallback basePrice) */
    public static Specification<Product> priceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(
                cb.coalesce(root.get("salePrice"), root.get("basePrice")),
                maxPrice
        );
    }

    /** Chuẩn hóa bỏ dấu tiếng Việt */
    public static String removeAccents(String src) {
        if (src == null) return null;
        String nfdNormalizedString = java.text.Normalizer.normalize(src, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }

    /** Tìm kiếm full-text không dấu, không phân biệt hoa thường trên: name, description, brand, category name */
    public static Specification<Product> fullTextSearch(String query) {
        return (root, criteriaQuery, cb) -> {
            if (query == null || query.isBlank()) {
                return cb.conjunction();
            }

            String normalized = removeAccents(query.toLowerCase().trim());
            String pattern = "%" + normalized + "%";

            // Join Category
            Join<Object, Object> categoryJoin = root.join("category", JoinType.LEFT);

            // Gọi unaccent trong PostgreSQL
            Expression<String> unaccentName = cb.function("unaccent", String.class, root.get("name"));
            Expression<String> unaccentDesc = cb.function("unaccent", String.class, cb.coalesce(root.get("description"), ""));
            Expression<String> unaccentBrand = cb.function("unaccent", String.class, cb.coalesce(root.get("brand"), ""));
            Expression<String> unaccentCatName = cb.function("unaccent", String.class, categoryJoin.get("name"));

            return cb.or(
                cb.like(cb.lower(unaccentName), pattern),
                cb.like(cb.lower(unaccentDesc), pattern),
                cb.like(cb.lower(unaccentBrand), pattern),
                cb.like(cb.lower(unaccentCatName), pattern)
            );
        };
    }

    /** Lọc theo danh sách thương hiệu */
    public static Specification<Product> hasBrands(List<String> brands) {
        return (root, query, cb) -> {
            if (brands == null || brands.isEmpty()) {
                return cb.conjunction();
            }
            Expression<String> unaccentBrand = cb.function("unaccent", String.class, cb.coalesce(root.get("brand"), ""));
            List<String> normalizedBrands = brands.stream()
                    .map(b -> removeAccents(b.toLowerCase().trim()))
                    .toList();
            return cb.lower(unaccentBrand).in(normalizedBrands);
        };
    }

    /** Tạo Specification tổng hợp cho tìm kiếm và lọc */
    public static Specification<Product> fromFullTextCriteria(
            String q, List<Long> categoryIds, BigDecimal minPrice, BigDecimal maxPrice,
            List<String> colors, List<String> sizes, List<String> brands) {
        
        Specification<Product> spec = Specification.where(isActive());

        if (q != null && !q.isBlank()) {
            spec = spec.and(fullTextSearch(q));
        }

        if (categoryIds != null && !categoryIds.isEmpty()) {
            spec = spec.and(inCategories(categoryIds));
        }

        if (brands != null && !brands.isEmpty()) {
            spec = spec.and(hasBrands(brands));
        }

        if (colors != null && !colors.isEmpty()) {
            spec = spec.and(hasColors(colors));
        }

        if (sizes != null && !sizes.isEmpty()) {
            spec = spec.and(hasSizes(sizes));
        }

        if (minPrice != null) {
            spec = spec.and(priceGreaterThanOrEqual(minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and(priceLessThanOrEqual(maxPrice));
        }

        return spec;
    }
}
