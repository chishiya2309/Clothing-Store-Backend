package vn.hcmute.edu.dp.nhom10.backend.pattern.state.review;

import vn.hcmute.edu.dp.nhom10.backend.entity.Review;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;

public class PendingReviewState implements ReviewState {

    @Override
    public void approve(Review review) {
        review.setIsApproved(true);
        review.setIsActive(true);
    }

    @Override
    public void delete(Review review, String reason) {
        review.setIsActive(false);
        review.setDeleteReason(reason);
    }

    @Override
    public void reply(Review review, String replyText) {
        throw new InvalidDataException("Không thể phản hồi đánh giá đang chờ duyệt.");
    }
}
