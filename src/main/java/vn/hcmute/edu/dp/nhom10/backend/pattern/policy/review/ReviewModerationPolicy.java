package vn.hcmute.edu.dp.nhom10.backend.pattern.policy.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.moderation.ContentModerationStrategy;

/**
 * ReviewModerationPolicy
 * 
 * Mẫu thiết kế: Policy Pattern
 * - Đóng gói các quy tắc và chính sách kiểm duyệt của hệ thống cửa hàng.
 * - Kiểm tra tính hợp lệ của các lý do xóa và các phản hồi từ nhân viên.
 */
@Component
@RequiredArgsConstructor
public class ReviewModerationPolicy {

    private final ContentModerationStrategy moderationStrategy;

    public void validateDeleteReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new InvalidDataException("Lý do xóa không được trống.");
        }
        if (reason.trim().length() < 5) {
            throw new InvalidDataException("Lý do xóa phải có ít nhất 5 ký tự để đảm bảo tính minh bạch.");
        }
    }

    public void validateReplyText(String replyText) {
        if (replyText == null || replyText.isBlank()) {
            throw new InvalidDataException("Nội dung phản hồi không được trống.");
        }
        if (replyText.length() > 500) {
            throw new InvalidDataException("Nội dung phản hồi không được vượt quá 500 ký tự.");
        }
        if (moderationStrategy.containsProfanity(replyText)) {
            throw new InvalidDataException("Nội dung phản hồi của nhân viên không được chứa từ ngữ nhạy cảm.");
        }
    }
}
