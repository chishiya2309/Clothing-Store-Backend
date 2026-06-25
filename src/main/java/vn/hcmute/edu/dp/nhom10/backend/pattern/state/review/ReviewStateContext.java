package vn.hcmute.edu.dp.nhom10.backend.pattern.state.review;

import vn.hcmute.edu.dp.nhom10.backend.entity.Review;

public class ReviewStateContext {

    public static ReviewState getReviewState(Review review) {
        if (!Boolean.TRUE.equals(review.getIsActive())) {
            return new DeletedReviewState();
        }
        if (Boolean.TRUE.equals(review.getIsApproved())) {
            return new ApprovedReviewState();
        }
        return new PendingReviewState();
    }
}
