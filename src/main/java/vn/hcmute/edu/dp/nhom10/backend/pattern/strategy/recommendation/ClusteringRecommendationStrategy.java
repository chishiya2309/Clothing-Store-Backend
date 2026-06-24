package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.recommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductGridResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.ProductClusterService;

import java.util.List;

/**
 * Chiến lược gợi ý dựa trên K-Means Clustering (Học không giám sát).
 * Sản phẩm được biểu diễn thành feature vector (giá, danh mục, lượt bán, đánh giá...),
 * sau đó phân cụm bằng K-Means. Sản phẩm cùng cụm được coi là "tương tự".
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClusteringRecommendationStrategy implements RecommendationStrategy {

    private final ProductClusterService productClusterService;

    @Override
    public List<ProductGridResponse> recommend(Long productId, int limit) {
        log.debug("ClusteringStrategy: Finding similar products for product ID {} (limit={})", productId, limit);
        return productClusterService.getProductsInSameCluster(productId, limit);
    }

    @Override
    public String getStrategyName() {
        return "CLUSTERING";
    }
}
