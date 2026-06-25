package vn.hcmute.edu.dp.nhom10.backend.pattern.chain.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffReviewResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Review;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.moderation.ContentModerationStrategy;

@Component
@RequiredArgsConstructor
public class ProfanityScanner extends ReviewContentScanner {

    private final ContentModerationStrategy moderationStrategy;

    @Override
    public void scan(Review review, StaffReviewResponse response) {
        String content = review.getContent();
        if (content != null && !content.isBlank()) {
            if (moderationStrategy.containsProfanity(content)) {
                response.setIsFlagged(true);
                String matched = moderationStrategy.getMatchedWords(content);
                appendFlagReason(response, "Từ ngữ nhạy cảm: " + matched);
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
