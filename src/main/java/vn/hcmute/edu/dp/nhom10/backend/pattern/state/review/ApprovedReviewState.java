package vn.hcmute.edu.dp.nhom10.backend.pattern.state.review;

import vn.hcmute.edu.dp.nhom10.backend.entity.Review;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;

import java.time.OffsetDateTime;

public class ApprovedReviewState implements ReviewState {

    @Override
    public void approve(Review review) {
        throw new InvalidDataException("Đánh giá này đã được duyệt rồi.");
    }

    @Override
    public void delete(Review review, String reason) {
        review.setIsActive(false);
        review.setIsApproved(false);
        review.setDeleteReason(reason);
    }

    @Override
    public void reply(Review review, String replyText) {
        review.setAdminReply(replyText);
        review.setRepliedAt(OffsetDateTime.now());
    }
}
