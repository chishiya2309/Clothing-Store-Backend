package vn.hcmute.edu.dp.nhom10.backend.pattern.observer.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.Review;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ReviewRepository;

import java.math.BigDecimal;

/**
 * ProductRatingUpdater
 * 
 * Mẫu thiết kế: Observer Pattern
 * - Lắng nghe sự kiện ReviewStatusChangedEvent phát ra khi có hành động kiểm duyệt đánh giá.
 * - Giảm sự phụ thuộc chặt chẽ giữa việc kiểm duyệt và tác vụ cập nhật điểm đánh giá trung bình của sản phẩm.
 */
@Component
@RequiredArgsConstructor
@Slf4j(topic = "PRODUCT-RATING-UPDATER")
public class ProductRatingUpdater {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    @EventListener
    @Transactional
    public void handleReviewStatusChanged(ReviewStatusChangedEvent event) {
        Review review = event.getReview();
        Product product = review.getProduct();
        
        log.info("Recalculating average rating for product ID: {} due to review status change on review ID: {}", 
                product.getId(), review.getId());
        
        Double avgRating = reviewRepository.calculateAverageRating(product.getId());
        product.setAverageRating(BigDecimal.valueOf(avgRating));
        productRepository.save(product);
        
        log.info("Updated product ID: {} average rating to: {}", product.getId(), avgRating);
    }
}
