package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.recommendation;

import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductGridResponse;

import java.util.List;

/**
 * Strategy Pattern Interface cho hệ thống gợi ý sản phẩm (UC-29).
 * Mỗi chiến lược triển khai một thuật toán gợi ý khác nhau:
 * - ClusteringRecommendationStrategy: K-Means ML Clustering
 * - CoPurchaseRecommendationStrategy: "Người dùng cũng mua"
 * - PopularityRecommendationStrategy: Sản phẩm phổ biến (Fallback)
 */
public interface RecommendationStrategy {

    /**
     * Gợi ý sản phẩm tương tự dựa trên một sản phẩm cụ thể.
     *
     * @param productId ID sản phẩm nguồn
     * @param limit     Số lượng gợi ý tối đa
     * @return Danh sách sản phẩm gợi ý
     */
    List<ProductGridResponse> recommend(Long productId, int limit);

    /**
     * Tên định danh chiến lược (CLUSTERING, CO_PURCHASE, POPULARITY).
     */
    String getStrategyName();
}
