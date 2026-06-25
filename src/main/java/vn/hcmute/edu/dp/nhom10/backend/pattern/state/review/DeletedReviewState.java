package vn.hcmute.edu.dp.nhom10.backend.pattern.state.review;

import vn.hcmute.edu.dp.nhom10.backend.entity.Review;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;

public class DeletedReviewState implements ReviewState {

    @Override
    public void approve(Review review) {
        review.setIsApproved(true);
        review.setIsActive(true);
        review.setDeleteReason(null);
    }

    @Override
    public void delete(Review review, String reason) {
        throw new InvalidDataException("Đánh giá này đã bị xóa rồi.");
    }

    @Override
    public void reply(Review review, String replyText) {
        throw new InvalidDataException("Không thể phản hồi đánh giá đã bị xóa.");
    }
}
