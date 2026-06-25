package vn.hcmute.edu.dp.nhom10.backend.pattern.chain.review;

import vn.hcmute.edu.dp.nhom10.backend.entity.Review;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffReviewResponse;

/**
 * ReviewContentScanner
 * 
 * Mẫu thiết kế: Chain of Responsibility Pattern
 * - Định nghĩa lớp xử lý cơ sở để tự động rà soát nội dung đánh giá.
 * - Các bộ quét cụ thể (Spam, Profanity) sẽ lần lượt kiểm tra và gắn cờ các đánh giá không hợp lệ.
 */
public abstract class ReviewContentScanner {
    protected ReviewContentScanner nextScanner;

    public void setNextScanner(ReviewContentScanner nextScanner) {
        this.nextScanner = nextScanner;
    }

    public abstract void scan(Review review, StaffReviewResponse response);

    protected void next(Review review, StaffReviewResponse response) {
        if (nextScanner != null) {
            nextScanner.scan(review, response);
        }
    }
}
