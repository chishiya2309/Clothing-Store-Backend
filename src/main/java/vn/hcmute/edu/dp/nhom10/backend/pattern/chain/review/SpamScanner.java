package vn.hcmute.edu.dp.nhom10.backend.pattern.chain.review;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffReviewResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Review;

import java.util.regex.Pattern;

@Component
public class SpamScanner extends ReviewContentScanner {

    private static final Pattern REPETITIVE_WORDS = Pattern.compile("(?U)\\b(\\w+)\\s+\\1\\s+\\1\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern REPETITIVE_CHARS = Pattern.compile("(.)\\1{4,}");

    @Override
    public void scan(Review review, StaffReviewResponse response) {
        String content = review.getContent();
        if (content != null && !content.isBlank()) {
            if (REPETITIVE_WORDS.matcher(content).find() || REPETITIVE_CHARS.matcher(content).find()) {
                response.setIsFlagged(true);
                appendFlagReason(response, "Spam: Phát hiện ký tự hoặc từ lặp đi lặp lại.");
            } else if (content.trim().length() < 3) {
                response.setIsFlagged(true);
                appendFlagReason(response, "Spam: Nội dung quá ngắn.");
            }
        }
        
        next(review, response);
    }

    private void appendFlagReason(StaffReviewResponse response, String reason) {
        if (response.getFlagReason() == null || response.getFlagReason().isBlank()) {
            response.setFlagReason(reason);
        } else {
            response.setFlagReason(response.getFlagReason() + " | " + reason);
        }
    }
}
