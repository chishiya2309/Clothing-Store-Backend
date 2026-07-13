package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductDetailResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductImageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductVariantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ProductSearchCriteria;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductGridResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductSearchDto;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductSuggestionDto;
import vn.hcmute.edu.dp.nhom10.backend.entity.Category;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductImage;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.ProductService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import vn.hcmute.edu.dp.nhom10.backend.pattern.specification.ProductSpecification;
import vn.hcmute.edu.dp.nhom10.backend.repository.CategoryRepository;

import java.util.Collections;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

        private final ProductRepository productRepository;
        private final CategoryRepository categoryRepository;

        @PersistenceContext
        private EntityManager entityManager;

        @Override
        @Transactional(readOnly = true)
        public ProductDetailResponse getProductBySlug(String slug) {
                Product product = productRepository.findBySlugAndIsActiveTrue(slug)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Không tìm thấy sản phẩm với slug: " + slug));
                return mapToDetailResponse(product);
        }

        // ─── Mapper ──────────────────────────────────────────────────────────────────

        private ProductDetailResponse mapToDetailResponse(Product product) {
                List<ProductImageResponse> images = product.getImages().stream()
                                .sorted(Comparator.comparingInt(ProductImage::getDisplayOrder))
                                .map(img -> ProductImageResponse.builder()
                                                .imageUrl(img.getImageUrl())
                                                .imageType(img.getImageType().name())
                                                .displayOrder(img.getDisplayOrder())
                                                .altText(img.getAltText())
                                                .build())
                                .toList();

                List<ProductVariantResponse> variants = product.getVariants().stream()
                                .filter(ProductVariant::getIsActive)
                                .map(v -> ProductVariantResponse.builder()
                                                .id(v.getId())
                                                .sku(v.getSku())
                                                .size(v.getSize())
                                                .color(v.getColor())
                                                .stockQuantity(v.getStockQuantity())
                                                .additionalPrice(v.getAdditionalPrice())
                                                .build())
                                .toList();

                BigDecimal displayPrice = product.getSalePrice() != null
                                ? product.getSalePrice()
                                : product.getBasePrice();

                // originalPrice chỉ trả về khi đang có khuyến mãi
                BigDecimal originalPrice = product.getSalePrice() != null
                                ? product.getBasePrice()
                                : null;

                return ProductDetailResponse.builder()
                                .id(product.getId())
                                .name(product.getName())
                                .slug(product.getSlug())
                                .description(product.getDescription())
                                .material(product.getMaterial())
                                .careInstructions(product.getCareInstructions())
                                .price(displayPrice)
                                .originalPrice(originalPrice)
                                .averageRating(product.getAverageRating())
                                .totalSold(product.getTotalSold())
                                .categoryName(product.getCategory().getName())
                                .categorySlug(product.getCategory().getSlug())
                                .images(images)
                                .variants(variants)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public PageResponse<ProductGridResponse> getProductsByCategorySlug(String slug, int page, int size) {
                Category category = categoryRepository.findBySlugAndIsActiveTrue(slug)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Category not found with slug: " + slug));

                List<Long> categoryIds = categoryRepository.findAllDescendantIds(category.getId());

                PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
                Page<Product> productPage = productRepository.findByCategoryIdInAndIsActiveTrue(categoryIds,
                                pageRequest);

                List<ProductGridResponse> productGridResponses = productPage.getContent().stream()
                                .map(this::mapToGridResponse)
                                .collect(Collectors.toList());

                return PageResponse.<ProductGridResponse>builder()
                                .pageNumber(productPage.getNumber())
                                .pageSize(productPage.getSize())
                                .totalElements(productPage.getTotalElements())
                                .totalPages(productPage.getTotalPages())
                                .content(productGridResponses)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public PageResponse<ProductGridResponse> searchProducts(ProductSearchCriteria criteria, int page, int size) {
                // Resolve category IDs (including descendants) if slug is provided
                List<Long> categoryIds = resolveCategoryIds(criteria.getCategorySlug());

                // Build Specification from criteria (Specification Pattern)
                Specification<Product> spec = ProductSpecification.fromCriteria(criteria, categoryIds);

                // Build Sort from criteria
                Sort sort = resolveSort(criteria.getSortBy());

                PageRequest pageRequest = PageRequest.of(page, size, sort);
                Page<Product> productPage = productRepository.findAll(spec, pageRequest);

                List<ProductGridResponse> content = productPage.getContent().stream()
                                .map(this::mapToGridResponse)
                                .collect(Collectors.toList());

                return PageResponse.<ProductGridResponse>builder()
                                .pageNumber(productPage.getNumber())
                                .pageSize(productPage.getSize())
                                .totalElements(productPage.getTotalElements())
                                .totalPages(productPage.getTotalPages())
                                .content(content)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public List<ProductGridResponse> getAutocompleteSuggestions(String keyword, int limit) {
                if (keyword == null || keyword.isBlank()) {
                        return Collections.emptyList();
                }

                List<Product> products = productRepository.findTopByKeyword(
                                keyword.trim(), PageRequest.of(0, limit));

                return products.stream()
                                .map(this::mapToGridResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        @org.springframework.cache.annotation.Cacheable(value = "newArrivals", key = "'top8'")
        public List<ProductGridResponse> getNewArrivals() {
                List<Product> products = productRepository.findTop8ByIsActiveTrueOrderByCreatedAtDesc();
                return products.stream()
                                .map(this::mapToGridResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        @org.springframework.cache.annotation.Cacheable(value = "bestSellers", key = "#limit")
        public List<ProductGridResponse> getBestSellers(int limit) {
                PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "totalSold"));
                Page<Product> productPage = productRepository.findByIsActiveTrue(pageRequest);
                return productPage.getContent().stream()
                                .map(this::mapToGridResponse)
                                .collect(Collectors.toList());
        }


        // ─────────────────────────── Private helpers ───────────────────────────

        private List<Long> resolveCategoryIds(String categorySlug) {
                if (categorySlug == null || categorySlug.isBlank()) {
                        return null;
                }
                Category category = categoryRepository.findBySlugAndIsActiveTrue(categorySlug)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Category not found with slug: " + categorySlug));
                return categoryRepository.findAllDescendantIds(category.getId());
        }

        /**
         * Strategy Pattern (đơn giản): chuyển đổi chuỗi sortBy thành đối tượng Sort
         */
        private Sort resolveSort(String sortBy) {
                if (sortBy == null || sortBy.isBlank()) {
                        return Sort.by(Sort.Direction.DESC, "createdAt");
                }
                return switch (sortBy) {
                        case "price_asc" -> Sort.by(Sort.Direction.ASC, "basePrice");
                        case "price_desc" -> Sort.by(Sort.Direction.DESC, "basePrice");
                        case "best_selling" -> Sort.by(Sort.Direction.DESC, "totalSold");
                        default -> Sort.by(Sort.Direction.DESC, "createdAt"); // "latest" and fallback
                };
        }

        private ProductGridResponse mapToGridResponse(Product product) {
                // Extract thumbnail from images
                String thumbnail = null;
                if (product.getImages() != null && !product.getImages().isEmpty()) {
                        thumbnail = product.getImages().stream()
                                        .filter(img -> img
                                                        .getImageType() == vn.hcmute.edu.dp.nhom10.backend.enums.ImageType.thumbnail)
                                        .findFirst()
                                        .map(ProductImage::getImageUrl)
                                        .orElse(product.getImages().get(0).getImageUrl());
                }

                // Extract unique colors from variants
                List<String> colors = null;
                if (product.getVariants() != null) {
                        colors = product.getVariants().stream()
                                        .map(ProductVariant::getColor)
                                        .distinct()
                                        .collect(Collectors.toList());
                }

                return ProductGridResponse.builder()
                                .id(product.getId())
                                .name(product.getName())
                                .slug(product.getSlug())
                                .basePrice(product.getBasePrice())
                                .salePrice(product.getSalePrice())
                                .thumbnailUrl(thumbnail)
                                .colors(colors)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public PageResponse<ProductSearchDto> searchProductsFullText(
                String q, String sortBy, int page, int size,
                String categorySlug, BigDecimal minPrice, BigDecimal maxPrice,
                List<String> colors, List<String> sizes, String brand) {

                List<Long> categoryIds = resolveCategoryIds(categorySlug);

                CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                CriteriaQuery<Product> query = cb.createQuery(Product.class);
                Root<Product> root = query.from(Product.class);

                // Fetch Join category to avoid N+1 query
                root.fetch("category", JoinType.LEFT);

                Specification<Product> spec = ProductSpecification.fromFullTextCriteria(
                        q, categoryIds, minPrice, maxPrice, colors, sizes, brand);

                Predicate predicate = spec.toPredicate(root, query, cb);
                if (predicate != null) {
                        query.where(predicate);
                }

                // Sorting
                if ("relevance".equalsIgnoreCase(sortBy) && q != null && !q.isBlank()) {
                        String normalizedQ = ProductSpecification.removeAccents(q.toLowerCase().trim());
                        Expression<Double> sim = cb.function("similarity", Double.class,
                                cb.function("unaccent", String.class, root.get("name")),
                                cb.literal(normalizedQ)
                        );
                        query.orderBy(cb.desc(sim));
                } else if ("price_asc".equalsIgnoreCase(sortBy)) {
                        query.orderBy(cb.asc(cb.coalesce(root.get("salePrice"), root.get("basePrice"))));
                } else if ("price_desc".equalsIgnoreCase(sortBy)) {
                        query.orderBy(cb.desc(cb.coalesce(root.get("salePrice"), root.get("basePrice"))));
                } else if ("newest".equalsIgnoreCase(sortBy)) {
                        query.orderBy(cb.desc(root.get("createdAt")));
                } else {
                        // Default sorting (newest/latest)
                        query.orderBy(cb.desc(root.get("createdAt")));
                }

                TypedQuery<Product> typedQuery = entityManager.createQuery(query);
                typedQuery.setFirstResult(page * size);
                typedQuery.setMaxResults(size);
                List<Product> products = typedQuery.getResultList();

                // Count Query
                CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
                Root<Product> countRoot = countQuery.from(Product.class);
                Predicate countPredicate = spec.toPredicate(countRoot, countQuery, cb);
                if (countPredicate != null) {
                        countQuery.where(countPredicate);
                }
                countQuery.select(cb.count(countRoot));
                Long totalElements = entityManager.createQuery(countQuery).getSingleResult();

                List<ProductSearchDto> content = products.stream()
                        .map(this::mapToSearchDto)
                        .collect(Collectors.toList());

                int totalPages = (int) Math.ceil((double) totalElements / size);

                return PageResponse.<ProductSearchDto>builder()
                        .pageNumber(page)
                        .pageSize(size)
                        .totalElements(totalElements)
                        .totalPages(totalPages)
                        .content(content)
                        .build();
        }

        @Override
        @Transactional(readOnly = true)
        @org.springframework.cache.annotation.Cacheable(value = "autocompleteSuggestions", key = "#q.trim().toLowerCase()")
        public List<ProductSuggestionDto> getAutocompleteSuggestionsList(String q) {
                if (q == null || q.trim().length() < 2) {
                        return Collections.emptyList();
                }

                String normalizedQ = ProductSpecification.removeAccents(q.toLowerCase().trim());

                CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                CriteriaQuery<Product> query = cb.createQuery(Product.class);
                Root<Product> root = query.from(Product.class);

                Join<Product, Category> categoryJoin = root.join("category", JoinType.LEFT);

                Expression<String> unaccentName = cb.function("unaccent", String.class, root.get("name"));
                Expression<String> unaccentBrand = cb.function("unaccent", String.class, cb.coalesce(root.get("brand"), ""));
                Expression<String> unaccentCatName = cb.function("unaccent", String.class, categoryJoin.get("name"));

                String pattern = "%" + normalizedQ + "%";
                Predicate specPredicate = cb.and(
                        cb.isTrue(root.get("isActive")),
                        cb.or(
                                cb.like(cb.lower(unaccentName), pattern),
                                cb.like(cb.lower(unaccentBrand), pattern),
                                cb.like(cb.lower(unaccentCatName), pattern)
                        )
                );

                query.where(specPredicate);

                Expression<Double> sim = cb.function("similarity", Double.class,
                        cb.function("unaccent", String.class, root.get("name")),
                        cb.literal(normalizedQ)
                );
                query.orderBy(cb.desc(sim));

                TypedQuery<Product> typedQuery = entityManager.createQuery(query);
                typedQuery.setMaxResults(8);
                List<Product> products = typedQuery.getResultList();

                return products.stream()
                        .map(p -> new ProductSuggestionDto(p.getName(), p.getSlug()))
                        .collect(Collectors.toList());
        }

        private ProductSearchDto mapToSearchDto(Product product) {
                String thumbnail = null;
                if (product.getImages() != null && !product.getImages().isEmpty()) {
                        thumbnail = product.getImages().stream()
                                .filter(img -> img.getImageType() == vn.hcmute.edu.dp.nhom10.backend.enums.ImageType.thumbnail)
                                .findFirst()
                                .map(ProductImage::getImageUrl)
                                .orElseGet(() -> product.getImages().stream()
                                        .filter(img -> img.getImageType() == vn.hcmute.edu.dp.nhom10.backend.enums.ImageType.main)
                                        .findFirst()
                                        .map(ProductImage::getImageUrl)
                                        .orElse(product.getImages().get(0).getImageUrl())
                                );
                }

                return ProductSearchDto.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .slug(product.getSlug())
                        .image(thumbnail)
                        .category(product.getCategory() != null ? product.getCategory().getName() : null)
                        .basePrice(product.getBasePrice())
                        .salePrice(product.getSalePrice())
                        .rating(product.getAverageRating())
                        .soldQuantity(product.getTotalSold())
                        .brand(product.getBrand())
                        .build();
        }
}
