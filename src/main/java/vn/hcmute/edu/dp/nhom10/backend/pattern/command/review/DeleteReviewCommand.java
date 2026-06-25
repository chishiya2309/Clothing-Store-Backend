package vn.hcmute.edu.dp.nhom10.backend.pattern.command.review;

import org.springframework.context.ApplicationEventPublisher;
import vn.hcmute.edu.dp.nhom10.backend.entity.Review;
import vn.hcmute.edu.dp.nhom10.backend.pattern.observer.review.ReviewStatusChangedEvent;
import vn.hcmute.edu.dp.nhom10.backend.pattern.state.review.ReviewState;
import vn.hcmute.edu.dp.nhom10.backend.pattern.state.review.ReviewStateContext;
import vn.hcmute.edu.dp.nhom10.backend.repository.ReviewRepository;

public class DeleteReviewCommand extends BaseReviewModerationCommand<Void> {

    private final String reason;

    public DeleteReviewCommand(Long reviewId, String reason, ReviewRepository reviewRepository, ApplicationEventPublisher eventPublisher) {
        super(reviewId, reviewRepository, eventPublisher);
        this.reason = reason;
    }

    @Override
    protected Void doExecute(Review review) {
        ReviewState state = ReviewStateContext.getReviewState(review);
        state.delete(review, reason);
        return null;
    }

    @Override
    public String getDescription() {
        return "Xóa đánh giá ID: " + reviewId + " với lý do: " + reason;
    }

    @Override
    protected void publishStatusChangedEvent(Review review) {
        eventPublisher.publishEvent(new ReviewStatusChangedEvent(this, review));
    }
}
