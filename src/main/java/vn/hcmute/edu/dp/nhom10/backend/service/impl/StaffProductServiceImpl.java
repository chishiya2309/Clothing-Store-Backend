package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCreateProductRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffProductImageRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffProductSearchCriteria;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffProductVariantRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffUpdateProductRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffUpdateProductVisibilityRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffUpdateStockRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffProductDetailResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffProductImageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffProductListItemResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffProductVariantResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffStockUpdateResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Category;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductImage;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.enums.ImageType;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.factory.product.ProductIdentityFactory;
import vn.hcmute.edu.dp.nhom10.backend.pattern.observer.ProductPriceManager;
import vn.hcmute.edu.dp.nhom10.backend.pattern.policy.product.ProductDeletionDecision;
import vn.hcmute.edu.dp.nhom10.backend.pattern.policy.product.ProductDeletionPolicy;
import vn.hcmute.edu.dp.nhom10.backend.pattern.specification.StaffProductSpecification;
import vn.hcmute.edu.dp.nhom10.backend.pattern.state.product.ProductStockStateResolver;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.product.ProductValidationContext;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.product.ProductPriceValidationHandler;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.product.ProductImageValidationHandler;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.product.ProductVariantValidationHandler;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.product.CategoryExistsValidationHandler;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.product.ProductNameUniqueValidationHandler;
import vn.hcmute.edu.dp.nhom10.backend.repository.CategoryRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.StaffProductService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffProductServiceImpl implements StaffProductService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt", "updatedAt", "name", "basePrice", "totalSold"
    );

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryRepository categoryRepository;
    private final ProductIdentityFactory productIdentityFactory;
    private final ProductStockStateResolver stockStateResolver;
    private final ProductDeletionPolicy productDeletionPolicy;
    private final ProductPriceManager productPriceManager;
    private final CacheManager cacheManager;

    private final ProductPriceValidationHandler productPriceValidationHandler;
    private final ProductImageValidationHandler productImageValidationHandler;
    private final ProductVariantValidationHandler productVariantValidationHandler;
    private final CategoryExistsValidationHandler categoryExistsValidationHandler;
    private final ProductNameUniqueValidationHandler productNameUniqueValidationHandler;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StaffProductListItemResponse> getProducts(
            StaffProductSearchCriteria criteria,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Specification<Product> spec = StaffProductSpecification.fromCriteria(criteria);
        PageRequest pageRequest = PageRequest.of(page, size, resolveSort(sortBy, sortDir));
        Page<Product> productPage = productRepository.findAll(spec, pageRequest);

        List<StaffProductListItemResponse> content = productPage.getContent().stream()
                .map(this::mapToListItemResponse)
                .toList();

        return PageResponse.<StaffProductListItemResponse>builder()
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .content(content)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StaffProductDetailResponse getProductDetail(Long productId) {
        Product product = findProduct(productId);
        return mapToDetailResponse(product);
    }

    @Override
    @Transactional
    public StaffProductDetailResponse createProduct(StaffCreateProductRequest request) {
        ProductValidationContext validationContext = ProductValidationContext.builder()
                .name(request.name())
                .categoryId(request.categoryId())
                .basePrice(request.basePrice())
                .salePrice(request.salePrice())
                .images(request.images())
                .variants(request.variants())
                .build();

        executeValidationChain(validationContext);

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục đang hoạt động với id: " + request.categoryId()));

        Product product = Product.builder()
                .name(request.name().trim())
                .slug(productIdentityFactory.createSlug(request.name(), productRepository::existsBySlug))
                .description(request.description())
                .material(request.material())
                .careInstructions(request.careInstructions())
                .category(category)
                .basePrice(request.basePrice())
                .salePrice(request.salePrice())
                .isActive(true)
                .isFeatured(Boolean.TRUE.equals(request.isFeatured()))
                .build();

        applyImages(product, request.images());
        applyVariants(product, request.variants());

        Product saved = productRepository.save(product);
        evictProductCaches();
        if (saved.getSalePrice() != null) {
            productPriceManager.notifyObservers(saved, null, saved.getSalePrice());
        }
        return mapToDetailResponse(saved);
    }

    @Override
    @Transactional
    public StaffProductDetailResponse updateProduct(Long productId, StaffUpdateProductRequest request) {
        ProductValidationContext validationContext = ProductValidationContext.builder()
                .productId(productId)
                .name(request.name())
                .categoryId(request.categoryId())
                .basePrice(request.basePrice())
                .salePrice(request.salePrice())
                .images(request.images())
                .variants(request.variants())
                .build();

        executeValidationChain(validationContext);

        Product product = findProduct(productId);
        BigDecimal oldSalePrice = product.getSalePrice();
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục đang hoạt động với id: " + request.categoryId()));

        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setMaterial(request.material());
        product.setCareInstructions(request.careInstructions());
        product.setCategory(category);
        product.setBasePrice(request.basePrice());
        product.setSalePrice(request.salePrice());
        product.setIsActive(request.isActive() == null || request.isActive());
        product.setIsFeatured(Boolean.TRUE.equals(request.isFeatured()));
        if (Boolean.TRUE.equals(product.getIsActive())) {
            product.setDeletedAt(null);
        }

        applyImages(product, request.images());
        reconcileVariants(product, request.variants());

        Product saved = productRepository.save(product);
        evictProductCaches();
        if (isPriceDrop(oldSalePrice, saved.getSalePrice())) {
            productPriceManager.notifyObservers(saved, oldSalePrice, saved.getSalePrice());
        }
        return mapToDetailResponse(saved);
    }

    @Override
    @Transactional
    public StaffProductDetailResponse updateVisibility(Long productId, StaffUpdateProductVisibilityRequest request) {
        Product product = productRepository.findByIdIgnoringSoftDelete(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với id: " + productId));
        product.setIsActive(request.isActive());
        // DO NOT set deletedAt - visibility toggle only changes isActive flag
        // Soft delete (deletedAt) is only set when user clicks Delete button

        Product saved = productRepository.save(product);
        evictProductCaches();
        return mapToDetailResponse(saved);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = findProduct(productId);
        ProductDeletionDecision decision = productDeletionPolicy.decide(productId);

        if (decision == ProductDeletionDecision.HARD_DELETE) {
            productRepository.delete(product);
        } else {
            product.setIsActive(false);
            product.setDeletedAt(OffsetDateTime.now());
            productRepository.save(product);
        }
        evictProductCaches();
    }

    @Override
    @Transactional
    public StaffStockUpdateResponse updateStock(Long productId, Long variantId, StaffUpdateStockRequest request) {
        ProductVariant variant = productVariantRepository.findByIdAndProductIdForUpdate(variantId, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy biến thể sản phẩm với id: " + variantId));

        Integer oldStock = variant.getStockQuantity();
        variant.setStockQuantity(request.stockQuantity());
        ProductVariant saved = productVariantRepository.save(variant);

        evictProductCaches();

        boolean lowStock = stockStateResolver.isLowStock(saved.getStockQuantity());
        return StaffStockUpdateResponse.builder()
                .productId(productId)
                .variantId(saved.getId())
                .sku(saved.getSku())
                .oldStockQuantity(oldStock)
                .newStockQuantity(saved.getStockQuantity())
                .lowStock(lowStock)
                .warningMessage(lowStock ? "Sản phẩm sắp hết hàng" : null)
                .build();
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với id: " + productId));
    }

    private void executeValidationChain(ProductValidationContext context) {
        productPriceValidationHandler
                .setNext(productImageValidationHandler)
                .setNext(productVariantValidationHandler)
                .setNext(categoryExistsValidationHandler)
                .setNext(productNameUniqueValidationHandler);

        productPriceValidationHandler.handle(context);
    }

    private void applyImages(Product product, List<StaffProductImageRequest> imageRequests) {
        product.getImages().clear();
        for (StaffProductImageRequest imageRequest : imageRequests) {
            ProductImage image = ProductImage.builder()
                    .product(product)
                    .imageUrl(imageRequest.imageUrl())
                    .imageType(imageRequest.imageType() == null ? ImageType.gallery : imageRequest.imageType())
                    .displayOrder(imageRequest.displayOrder() == null ? 0 : imageRequest.displayOrder())
                    .altText(imageRequest.altText())
                    .build();
            product.getImages().add(image);
        }
    }

    private void applyVariants(Product product, List<StaffProductVariantRequest> variantRequests) {
        for (StaffProductVariantRequest variantRequest : variantRequests) {
            ProductVariant variant = ProductVariant.builder()
                    .product(product)
                    .sku(productIdentityFactory.createSku(
                            product.getSlug(),
                            variantRequest.size(),
                            variantRequest.color(),
                            productVariantRepository::existsBySkuIgnoreCase))
                    .size(variantRequest.size().trim().toUpperCase(Locale.ROOT))
                    .color(variantRequest.color().trim())
                    .stockQuantity(variantRequest.stockQuantity())
                    .additionalPrice(variantRequest.additionalPrice())
                    .isActive(variantRequest.isActive() == null || variantRequest.isActive())
                    .build();
            product.getVariants().add(variant);
        }
    }

    private void reconcileVariants(Product product, List<StaffProductVariantRequest> variantRequests) {
        Map<Long, ProductVariant> variantsById = product.getVariants().stream()
                .filter(variant -> variant.getId() != null)
                .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));

        Set<Long> requestedIds = new HashSet<>();
        for (StaffProductVariantRequest request : variantRequests) {
            ProductVariant variant = resolveVariantForUpdate(product, request, variantsById);
            applyVariantFields(product, variant, request);
            if (!product.getVariants().contains(variant)) {
                product.getVariants().add(variant);
            }
            if (variant.getId() != null) {
                requestedIds.add(variant.getId());
            }
        }

        product.getVariants().stream()
                .filter(variant -> variant.getId() != null)
                .filter(variant -> !requestedIds.contains(variant.getId()))
                .forEach(variant -> variant.setIsActive(false));
    }

    private ProductVariant resolveVariantForUpdate(
            Product product,
            StaffProductVariantRequest request,
            Map<Long, ProductVariant> variantsById
    ) {
        if (request.id() != null) {
            ProductVariant existing = variantsById.get(request.id());
            if (existing == null) {
                throw new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm với id: " + request.id());
            }
            return existing;
        }

        Optional<ProductVariant> sameVariant = product.getVariants().stream()
                .filter(variant -> normalizeVariantKey(variant.getSize(), variant.getColor())
                        .equals(normalizeVariantKey(request.size(), request.color())))
                .findFirst();

        return sameVariant.orElseGet(ProductVariant::new);
    }

    private void applyVariantFields(Product product, ProductVariant variant, StaffProductVariantRequest request) {
        variant.setProduct(product);
        if (variant.getSku() == null || variant.getSku().isBlank()) {
            variant.setSku(productIdentityFactory.createSku(
                    product.getSlug(),
                    request.size(),
                    request.color(),
                    productVariantRepository::existsBySkuIgnoreCase));
        }
        variant.setSize(request.size().trim().toUpperCase(Locale.ROOT));
        variant.setColor(request.color().trim());
        variant.setStockQuantity(request.stockQuantity());
        variant.setAdditionalPrice(request.additionalPrice());
        variant.setIsActive(request.isActive() == null || request.isActive());
    }

    private String normalizeVariantKey(String size, String color) {
        return (size == null ? "" : size.trim().toUpperCase(Locale.ROOT))
                + "|"
                + (color == null ? "" : color.trim().toLowerCase(Locale.ROOT));
    }

    private StaffProductListItemResponse mapToListItemResponse(Product product) {
        return StaffProductListItemResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .basePrice(product.getBasePrice())
                .salePrice(product.getSalePrice())
                .isActive(product.getIsActive())
                .isFeatured(product.getIsFeatured())
                .status(stockStateResolver.resolve(product))
                .totalStock(stockStateResolver.totalStock(product))
                .variantCount(product.getVariants() == null ? 0 : product.getVariants().size())
                .thumbnailUrl(resolveThumbnailUrl(product))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private StaffProductDetailResponse mapToDetailResponse(Product product) {
        List<StaffProductImageResponse> images = product.getImages().stream()
                .sorted(Comparator.comparing(ProductImage::getDisplayOrder, Comparator.nullsLast(Integer::compareTo)))
                .map(this::mapImage)
                .toList();

        List<StaffProductVariantResponse> variants = product.getVariants().stream()
                .sorted(Comparator.comparing(ProductVariant::getId, Comparator.nullsLast(Long::compareTo)))
                .map(this::mapVariant)
                .toList();

        int totalStock = stockStateResolver.totalStock(product);
        return StaffProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .material(product.getMaterial())
                .careInstructions(product.getCareInstructions())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .basePrice(product.getBasePrice())
                .salePrice(product.getSalePrice())
                .isActive(product.getIsActive())
                .isFeatured(product.getIsFeatured())
                .status(stockStateResolver.resolve(product))
                .totalStock(totalStock)
                .stockWarning(totalStock < ProductStockStateResolver.LOW_STOCK_THRESHOLD)
                .images(images)
                .variants(variants)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private StaffProductImageResponse mapImage(ProductImage image) {
        return StaffProductImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .imageType(image.getImageType().name())
                .displayOrder(image.getDisplayOrder())
                .altText(image.getAltText())
                .build();
    }

    private StaffProductVariantResponse mapVariant(ProductVariant variant) {
        return StaffProductVariantResponse.builder()
                .id(variant.getId())
                .sku(variant.getSku())
                .size(variant.getSize())
                .color(variant.getColor())
                .stockQuantity(variant.getStockQuantity())
                .additionalPrice(variant.getAdditionalPrice())
                .isActive(variant.getIsActive())
                .lowStock(stockStateResolver.isLowStock(variant.getStockQuantity()))
                .build();
    }

    private String resolveThumbnailUrl(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }
        return product.getImages().stream()
                .filter(image -> image.getImageType() == ImageType.thumbnail)
                .findFirst()
                .or(() -> product.getImages().stream()
                        .filter(image -> image.getImageType() == ImageType.main)
                        .findFirst())
                .orElse(product.getImages().get(0))
                .getImageUrl();
    }

    private Sort resolveSort(String sortBy, String sortDir) {
        String resolvedSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, resolvedSortBy);
    }

    private boolean isPriceDrop(BigDecimal oldPrice, BigDecimal newPrice) {
        return newPrice != null && (oldPrice == null || newPrice.compareTo(oldPrice) < 0);
    }

    private void evictProductCaches() {
        List.of("newArrivals", "bestSellers", "categories", "collections").stream()
                .map(cacheManager::getCache)
                .filter(Objects::nonNull)
                .forEach(Cache::clear);
    }
}
