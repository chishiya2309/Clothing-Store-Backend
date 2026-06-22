package vn.hcmute.edu.dp.nhom10.backend.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import smile.clustering.KMeans;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductGridResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductImage;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.enums.ImageType;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * UC-29: Service quản lý phân cụm sản phẩm bằng K-Means (Smile ML).
 *
 * Pipeline:
 * 1. Lấy tất cả Product active từ DB
 * 2. Xây dựng Feature Vector cho mỗi SP (giá, danh mục, lượt bán, rating, sale)
 * 3. Chuẩn hóa (normalize) các feature về [0, 1]
 * 4. Chạy K-Means clustering (Smile library)
 * 5. Cache kết quả: productId → clusterId
 * 6. Khi query: tìm cụm của SP → trả về các SP cùng cụm
 *
 * Tự động re-train mỗi ngày 1 lần (cron) hoặc khi khởi động ứng dụng.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductClusterService {

    private final ProductRepository productRepository;

    // Cache: productId -> clusterLabel
    private final Map<Long, Integer> clusterAssignments = new ConcurrentHashMap<>();
    // Cache: clusterLabel -> list of productIds
    private final Map<Integer, List<Long>> clusterMembers = new ConcurrentHashMap<>();
    // Cache: productId -> ProductGridResponse (để build response nhanh và tránh LazyInitializationException)
    private final Map<Long, ProductGridResponse> productCache = new ConcurrentHashMap<>();

    private static final int MIN_PRODUCTS_FOR_CLUSTERING = 6;
    private static final int MIN_K = 3;
    private static final int MAX_K = 15;

    /**
     * Train K-Means khi ứng dụng khởi động.
     */
    @PostConstruct
    public void init() {
        try {
            trainClusters();
        } catch (Exception e) {
            log.warn("Failed to train clusters on startup (will retry on schedule): {}", e.getMessage());
        }
    }

    /**
     * Tự động re-train mỗi ngày lúc 3:00 AM.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledRetrain() {
        log.info("Scheduled cluster retraining started...");
        trainClusters();
    }

    /**
     * Train K-Means clustering trên toàn bộ sản phẩm active.
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public synchronized void trainClusters() {
        log.info("=== PRODUCT CLUSTERING PIPELINE START ===");

        List<Product> products = productRepository.findAll().stream()
                .filter(Product::getIsActive)
                .toList();

        log.info("Step 1: Loaded {} active products", products.size());

        if (products.size() < MIN_PRODUCTS_FOR_CLUSTERING) {
            log.warn("Not enough products for clustering (need >= {}). Skipping.", MIN_PRODUCTS_FOR_CLUSTERING);
            // Fallback: mỗi SP tự thành 1 cụm
            for (Product product : products) {
                clusterAssignments.put(product.getId(), 0);
            }
            clusterMembers.clear();
            clusterMembers.put(0, products.stream().map(Product::getId).collect(Collectors.toList()));
            products.forEach(p -> productCache.put(p.getId(), toGridResponse(p)));
            return;
        }

        // Step 2: Thu thập tất cả categoryIds duy nhất để one-hot encode
        List<Long> allCategoryIds = products.stream()
                .map(p -> p.getCategory().getId())
                .distinct()
                .sorted()
                .toList();
        log.info("Step 2: Found {} unique categories", allCategoryIds.size());

        // Step 3: Tìm giá trị max để chuẩn hóa
        double maxPrice = products.stream()
                .mapToDouble(p -> p.getBasePrice().doubleValue())
                .max().orElse(1.0);
        double maxSold = products.stream()
                .mapToInt(Product::getTotalSold)
                .max().orElse(1);

        // Step 4: Xây dựng feature matrix
        // Features: [one-hot-category..., normalizedPrice, normalizedSold, normalizedRating, hasSale, priceRange]
        int categoryCount = allCategoryIds.size();
        int featureCount = categoryCount + 4; // +4: price, sold, rating, hasSale

        double[][] featureMatrix = new double[products.size()][featureCount];

        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            double[] features = featureMatrix[i];

            // One-hot encode category
            int catIndex = allCategoryIds.indexOf(p.getCategory().getId());
            if (catIndex >= 0) {
                features[catIndex] = 1.0;
            }

            // Normalized price [0, 1]
            features[categoryCount] = p.getBasePrice().doubleValue() / maxPrice;

            // Normalized totalSold [0, 1]
            features[categoryCount + 1] = (double) p.getTotalSold() / maxSold;

            // Normalized averageRating [0, 1]
            features[categoryCount + 2] = p.getAverageRating() != null
                    ? p.getAverageRating().doubleValue() / 5.0
                    : 0.0;

            // Has sale (binary)
            features[categoryCount + 3] = p.getSalePrice() != null ? 1.0 : 0.0;
        }

        log.info("Step 3: Built feature matrix [{}x{}]", products.size(), featureCount);

        // Step 5: Xác định K tối ưu: K = sqrt(n/2), clamp [MIN_K, MAX_K]
        int k = (int) Math.round(Math.sqrt(products.size() / 2.0));
        k = Math.max(MIN_K, Math.min(MAX_K, k));
        // K phải < số sản phẩm
        k = Math.min(k, products.size() - 1);
        log.info("Step 4: Selected K={} for {} products", k, products.size());

        // Step 6: Chạy K-Means (Smile library)
        KMeans model = KMeans.fit(featureMatrix, k);

        // Step 7: Lưu kết quả clustering vào cache
        clusterAssignments.clear();
        clusterMembers.clear();
        productCache.clear();

        int[] labels = model.y; // cluster labels cho mỗi data point

        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            int label = labels[i];
            clusterAssignments.put(p.getId(), label);
            clusterMembers.computeIfAbsent(label, x -> new ArrayList<>()).add(p.getId());
            productCache.put(p.getId(), toGridResponse(p));
        }

        // Log kết quả
        log.info("=== CLUSTERING RESULTS ===");
        for (Map.Entry<Integer, List<Long>> entry : clusterMembers.entrySet()) {
            List<String> names = entry.getValue().stream()
                    .map(id -> productCache.get(id).getName())
                    .collect(Collectors.toList());
            log.info("Cluster {}: {} products → {}", entry.getKey(), names.size(), names);
        }
        log.info("=== PRODUCT CLUSTERING PIPELINE COMPLETE ===");
    }

    /**
     * Lấy các sản phẩm cùng cụm (cluster) với sản phẩm cho trước.
     *
     * @param productId ID sản phẩm nguồn
     * @param limit     Số lượng tối đa
     * @return Danh sách sản phẩm gợi ý (loại trừ sản phẩm nguồn)
     */
    public List<ProductGridResponse> getProductsInSameCluster(Long productId, int limit) {
        Integer clusterLabel = clusterAssignments.get(productId);
        if (clusterLabel == null) {
            log.debug("Product {} not found in any cluster", productId);
            return List.of();
        }

        List<Long> memberIds = clusterMembers.getOrDefault(clusterLabel, List.of());

        return memberIds.stream()
                .filter(id -> !id.equals(productId))
                .map(productCache::get)
                .filter(Objects::nonNull)
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Lấy cluster label của một sản phẩm.
     */
    public Integer getClusterForProduct(Long productId) {
        return clusterAssignments.get(productId);
    }

    /**
     * Kiểm tra xem đã train chưa.
     */
    public boolean isTrained() {
        return !clusterAssignments.isEmpty();
    }

    private ProductGridResponse toGridResponse(Product product) {
        String thumbnail = product.getImages().stream()
                .filter(img -> img.getImageType() == ImageType.main)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(product.getImages().isEmpty() ? null : product.getImages().get(0).getImageUrl());

        List<String> colors = product.getVariants().stream()
                .filter(v -> v.getIsActive() && v.getStockQuantity() > 0)
                .map(ProductVariant::getColor)
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
