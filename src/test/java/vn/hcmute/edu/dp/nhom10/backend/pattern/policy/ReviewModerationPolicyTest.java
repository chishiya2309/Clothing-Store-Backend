package vn.hcmute.edu.dp.nhom10.backend.pattern.policy;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.policy.review.ReviewModerationPolicy;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.moderation.VietnameseProfanityStrategy;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewModerationPolicyTest {

    private final ReviewModerationPolicy policy = new ReviewModerationPolicy(new VietnameseProfanityStrategy());

    @Test
    public void testValidateDeleteReasonSuccess() {
        assertDoesNotThrow(() -> policy.validateDeleteReason("Spam quảng cáo sai sự thật"));
        assertDoesNotThrow(() -> policy.validateDeleteReason("Từ ngữ thô tục vô căn cứ"));
    }

    @Test
    public void testValidateDeleteReasonFailure() {
        assertThrows(InvalidDataException.class, () -> policy.validateDeleteReason(null));
        assertThrows(InvalidDataException.class, () -> policy.validateDeleteReason(""));
        assertThrows(InvalidDataException.class, () -> policy.validateDeleteReason("   "));
        assertThrows(InvalidDataException.class, () -> policy.validateDeleteReason("Spam"));
    }

    @Test
    public void testValidateReplyTextSuccess() {
        assertDoesNotThrow(() -> policy.validateReplyText("Cảm ơn quý khách đã phản hồi, chúng tôi sẽ cải thiện sản phẩm!"));
        assertDoesNotThrow(() -> policy.validateReplyText("Chào bạn, rất tiếc vì trải nghiệm không tốt của bạn."));
    }

    @Test
    public void testValidateReplyTextFailure() {
        assertThrows(InvalidDataException.class, () -> policy.validateReplyText(null));
        assertThrows(InvalidDataException.class, () -> policy.validateReplyText(""));

        String longText = "a".repeat(501);
        assertThrows(InvalidDataException.class, () -> policy.validateReplyText(longText));

        assertThrows(InvalidDataException.class, () -> policy.validateReplyText("Chào bạn ngu ngốc nha!"));
        assertThrows(InvalidDataException.class, () -> policy.validateReplyText("Phản hồi vcl"));
    }
}
