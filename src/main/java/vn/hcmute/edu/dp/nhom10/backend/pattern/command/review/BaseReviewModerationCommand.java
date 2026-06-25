package vn.hcmute.edu.dp.nhom10.backend.pattern.command.review;

import org.springframework.context.ApplicationEventPublisher;
import vn.hcmute.edu.dp.nhom10.backend.entity.Review;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.ReviewRepository;

/**
 * BaseReviewModerationCommand
 * 
 * Mẫu thiết kế: Template Method Pattern & Command Pattern
 * - Command: Đóng gói các hành động kiểm duyệt đánh giá thành các đối tượng lệnh có thể thực thi.
 * - Template Method: Phương thức {@link #execute()} định nghĩa bộ khung của quy trình kiểm duyệt.
 *   Các lớp con phải triển khai {@link #doExecute(Review)} để thực hiện hành động cụ thể.
 */
public abstract class BaseReviewModerationCommand<T> implements ReviewCommand<T> {

    protected final Long reviewId;
    protected final ReviewRepository reviewRepository;
    protected final ApplicationEventPublisher eventPublisher;

    protected BaseReviewModerationCommand(Long reviewId, ReviewRepository reviewRepository, ApplicationEventPublisher eventPublisher) {
        this.reviewId = reviewId;
        this.reviewRepository = reviewRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public final T execute() {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tồn tại ID: " + reviewId));
        
        T result = doExecute(review);
        
        reviewRepository.save(review);
        
        publishStatusChangedEvent(review);
        
        return result;
    }

    protected abstract T doExecute(Review review);

    protected void publishStatusChangedEvent(Review review) {
        // Subclasses will implement this if they trigger rating updates
    }
}
