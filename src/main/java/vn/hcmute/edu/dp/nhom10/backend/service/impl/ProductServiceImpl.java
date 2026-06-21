package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductDetailResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductImageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductVariantResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductImage;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.ProductService;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

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
}
