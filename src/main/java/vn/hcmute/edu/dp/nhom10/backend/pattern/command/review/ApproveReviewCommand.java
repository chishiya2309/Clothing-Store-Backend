package vn.hcmute.edu.dp.nhom10.backend.pattern.command.review;

import org.springframework.context.ApplicationEventPublisher;
import vn.hcmute.edu.dp.nhom10.backend.entity.Review;
import vn.hcmute.edu.dp.nhom10.backend.pattern.observer.review.ReviewStatusChangedEvent;
import vn.hcmute.edu.dp.nhom10.backend.pattern.state.review.ReviewState;
import vn.hcmute.edu.dp.nhom10.backend.pattern.state.review.ReviewStateContext;
import vn.hcmute.edu.dp.nhom10.backend.repository.ReviewRepository;

public class ApproveReviewCommand extends BaseReviewModerationCommand<Void> {

    public ApproveReviewCommand(Long reviewId, ReviewRepository reviewRepository, ApplicationEventPublisher eventPublisher) {
        super(reviewId, reviewRepository, eventPublisher);
    }

    @Override
    protected Void doExecute(Review review) {
        ReviewState state = ReviewStateContext.getReviewState(review);
        state.approve(review);
        return null;
    }

    @Override
    public String getDescription() {
        return "Duyệt đánh giá ID: " + reviewId;
    }

    @Override
    protected void publishStatusChangedEvent(Review review) {
        eventPublisher.publishEvent(new ReviewStatusChangedEvent(this, review));
    }
}
