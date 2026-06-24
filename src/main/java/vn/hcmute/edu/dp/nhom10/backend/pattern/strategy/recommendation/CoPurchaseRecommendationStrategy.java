package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.recommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductGridResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductImage;
import vn.hcmute.edu.dp.nhom10.backend.enums.ImageType;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Chiến lược gợi ý dựa trên hành vi mua hàng chung (Co-Purchase / Collaborative Filtering).
 * Phân tích: "Khách hàng đã mua sản phẩm A cũng thường mua sản phẩm B".
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CoPurchaseRecommendationStrategy implements RecommendationStrategy {

    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Override
    public List<ProductGridResponse> recommend(Long productId, int limit) {
        log.debug("CoPurchaseStrategy: Finding co-purchased products for product ID {} (limit={})", productId, limit);
        
        // Tìm các sản phẩm thường được mua cùng trong cùng đơn hàng
        List<Long> coPurchasedIds = orderItemRepository.findCoPurchasedProductIds(productId, limit);
        
        if (coPurchasedIds.isEmpty()) {
            return List.of();
        }

        List<Product> products = productRepository.findAllById(coPurchasedIds);
        return products.stream()
                .filter(p -> p.getIsActive() && !p.getId().equals(productId))
                .map(this::toGridResponse)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public String getStrategyName() {
        return "CO_PURCHASE";
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
