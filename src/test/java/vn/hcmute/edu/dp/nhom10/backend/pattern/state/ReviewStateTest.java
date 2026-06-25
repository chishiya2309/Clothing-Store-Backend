package vn.hcmute.edu.dp.nhom10.backend.pattern.state;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.entity.Review;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.state.review.*;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewStateTest {

    @Test
    public void testPendingReviewStateTransitions() {
        Review review = Review.builder()
                .isApproved(false)
                .isActive(true)
                .build();

        ReviewState state = ReviewStateContext.getReviewState(review);
        assertTrue(state instanceof PendingReviewState);

        state.approve(review);
        assertTrue(review.getIsApproved());
        assertTrue(review.getIsActive());

        review.setIsApproved(false);
        state = ReviewStateContext.getReviewState(review);
        state.delete(review, "Spam");
        assertFalse(review.getIsActive());
        assertEquals("Spam", review.getDeleteReason());

        review.setIsApproved(false);
        review.setIsActive(true);
        Review finalReview = review;
        assertThrows(InvalidDataException.class, () -> {
            ReviewStateContext.getReviewState(finalReview).reply(finalReview, "Thank you!");
        });
    }

    @Test
    public void testApprovedReviewStateTransitions() {
        Review review = Review.builder()
                .isApproved(true)
                .isActive(true)
                .build();

        ReviewState state = ReviewStateContext.getReviewState(review);
        assertTrue(state instanceof ApprovedReviewState);

        Review finalReview = review;
        assertThrows(InvalidDataException.class, () -> {
            state.approve(finalReview);
        });

        state.reply(review, "Cảm ơn bạn!");
        assertEquals("Cảm ơn bạn!", review.getAdminReply());
        assertNotNull(review.getRepliedAt());

        state.delete(review, "Vi phạm tiêu chuẩn");
        assertFalse(review.getIsActive());
        assertFalse(review.getIsApproved());
        assertEquals("Vi phạm tiêu chuẩn", review.getDeleteReason());
    }

    @Test
    public void testDeletedReviewStateTransitions() {
        Review review = Review.builder()
                .isApproved(false)
                .isActive(false)
                .deleteReason("Spam")
                .build();

        ReviewState state = ReviewStateContext.getReviewState(review);
        assertTrue(state instanceof DeletedReviewState);

        Review finalReview = review;
        assertThrows(InvalidDataException.class, () -> {
            state.delete(finalReview, "Another reason");
        });

        assertThrows(InvalidDataException.class, () -> {
            state.reply(finalReview, "Sorry");
        });

        state.approve(review);
        assertTrue(review.getIsApproved());
        assertTrue(review.getIsActive());
        assertNull(review.getDeleteReason());
    }
}
