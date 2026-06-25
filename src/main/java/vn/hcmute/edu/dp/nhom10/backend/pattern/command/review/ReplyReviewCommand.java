package vn.hcmute.edu.dp.nhom10.backend.pattern.command.review;

import org.springframework.context.ApplicationEventPublisher;
import vn.hcmute.edu.dp.nhom10.backend.entity.Review;
import vn.hcmute.edu.dp.nhom10.backend.pattern.state.review.ReviewState;
import vn.hcmute.edu.dp.nhom10.backend.pattern.state.review.ReviewStateContext;
import vn.hcmute.edu.dp.nhom10.backend.repository.ReviewRepository;

public class ReplyReviewCommand extends BaseReviewModerationCommand<Void> {

    private final String replyText;

    public ReplyReviewCommand(Long reviewId, String replyText, ReviewRepository reviewRepository, ApplicationEventPublisher eventPublisher) {
        super(reviewId, reviewRepository, eventPublisher);
        this.replyText = replyText;
    }

    @Override
    protected Void doExecute(Review review) {
        ReviewState state = ReviewStateContext.getReviewState(review);
        state.reply(review, replyText);
        return null;
    }

    @Override
    public String getDescription() {
        return "Phản hồi đánh giá ID: " + reviewId + " với nội dung: " + replyText;
    }
}
