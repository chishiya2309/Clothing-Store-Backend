package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.recommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductGridResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductImage;
import vn.hcmute.edu.dp.nhom10.backend.enums.ImageType;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Chiến lược Fallback: Gợi ý sản phẩm phổ biến nhất cùng danh mục.
 * Sử dụng khi không đủ data cho ML clustering hoặc co-purchase.
 * Sắp xếp theo totalSold (lượt bán) giảm dần.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PopularityRecommendationStrategy implements RecommendationStrategy {

    private final ProductRepository productRepository;

    @Override
    public List<ProductGridResponse> recommend(Long productId, int limit) {
        log.debug("PopularityStrategy: Finding popular products for product ID {} (limit={})", productId, limit);

        Product sourceProduct = productRepository.findById(productId).orElse(null);
        if (sourceProduct == null) {
            return List.of();
        }

        Long categoryId = sourceProduct.getCategory().getId();
        List<Product> popular = productRepository.findPopularByCategoryExcluding(
                categoryId, productId, PageRequest.of(0, limit));

        return popular.stream()
                .map(this::toGridResponse)
                .collect(Collectors.toList());
    }

    @Override
    public String getStrategyName() {
        return "POPULARITY";
    }

    private ProductGridResponse toGridResponse(Product product) {
        String thumbnail = product.getImages().stream()
                .filter(img -> img.getImageType() == ImageType.main)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(product.getImages().isEmpty() ? null : product.getImages().get(0).getImageUrl());

        List<String> colors = product.getVariants().stream()
                .filter(v -> v.getIsActive() && v.getStockQuantity() > 0)
                .map(v -> v.getColor())
                .distinct()
                .collect(Collectors.toList());

        return ProductGridResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .thumbnailUrl(thumbnail)
                .basePrice(product.getBasePrice())
                .salePrice(product.getSalePrice())
                .colors(colors)
                .build();
    }
}
