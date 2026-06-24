package vn.hcmute.edu.dp.nhom10.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductGridResponse;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.recommendation.RecommendationStrategy;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Orchestrator cho hệ thống gợi ý sản phẩm (UC-29).
 * Sử dụng Strategy Pattern để linh hoạt chọn hoặc kết hợp các thuật toán gợi ý:
 * 1. Clustering (K-Means)
 * 2. Co-Purchase ("Người dùng cũng mua")
 * 3. Popularity (Fallback)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    // Spring tự động inject tất cả các class implement RecommendationStrategy
    private final List<RecommendationStrategy> strategies;
    private final ProductClusterService productClusterService;

    /**
     * Gợi ý sản phẩm tương tự cho một sản phẩm cụ thể (Trang chi tiết sản phẩm).
     * Kết hợp (Merge) kết quả từ nhiều strategy để có danh sách đa dạng nhất.
     */
    public List<ProductGridResponse> getRecommendations(Long productId, int limit) {
        log.info("Generating recommendations for product ID {} with limit {}", productId, limit);

        // Đảm bảo K-Means đã được train
        if (!productClusterService.isTrained()) {
            log.warn("Clusters not trained yet, triggering training now...");
            productClusterService.trainClusters();
        }

        // Dùng LinkedHashSet để giữ nguyên thứ tự insert và loại bỏ trùng lặp (dựa vào id)
        Set<ProductGridResponse> resultSet = new LinkedHashSet<>();

        // Ưu tiên 1: Co-purchase (Hành vi người dùng thực tế)
        RecommendationStrategy coPurchaseStrategy = getStrategy("CO_PURCHASE");
        if (coPurchaseStrategy != null) {
            List<ProductGridResponse> coPurchased = coPurchaseStrategy.recommend(productId, limit);
            resultSet.addAll(coPurchased);
            log.debug("Added {} products from CO_PURCHASE strategy", coPurchased.size());
        }

        // Ưu tiên 2: Clustering (Đặc trưng sản phẩm)
        if (resultSet.size() < limit) {
            RecommendationStrategy clusteringStrategy = getStrategy("CLUSTERING");
            if (clusteringStrategy != null) {
                // Lấy nhiều hơn mức cần thiết một chút để bù trừ nếu bị trùng lặp
                List<ProductGridResponse> clustered = clusteringStrategy.recommend(productId, limit);
                resultSet.addAll(clustered);
                log.debug("Added products from CLUSTERING strategy. Total unique so far: {}", resultSet.size());
            }
        }

        // Fallback: Popularity (Nếu 2 cái trên vẫn chưa đủ limit)
        if (resultSet.size() < limit) {
            RecommendationStrategy popularityStrategy = getStrategy("POPULARITY");
            if (popularityStrategy != null) {
                List<ProductGridResponse> popular = popularityStrategy.recommend(productId, limit);
                resultSet.addAll(popular);
                log.debug("Added products from POPULARITY strategy. Total unique: {}", resultSet.size());
            }
        }

        // Chuyển Set về List và cắt đúng limit
        List<ProductGridResponse> finalResult = new ArrayList<>(resultSet);
        return finalResult.size() > limit ? finalResult.subList(0, limit) : finalResult;
    }

    private RecommendationStrategy getStrategy(String name) {
        return strategies.stream()
                .filter(s -> s.getStrategyName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
