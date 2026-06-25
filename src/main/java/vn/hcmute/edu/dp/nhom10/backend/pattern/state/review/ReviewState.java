package vn.hcmute.edu.dp.nhom10.backend.pattern.state.review;

import vn.hcmute.edu.dp.nhom10.backend.entity.Review;

/**
 * ReviewState
 * 
 * Mẫu thiết kế: State Pattern
 * - Đóng gói các hành vi cụ thể theo trạng thái (PENDING, APPROVED, DELETED) và các quy tắc chuyển đổi trạng thái.
 * - Tránh việc kiểm tra if-else lồng nhau bằng cách ủy thác các thay đổi trạng thái cho các lớp con cụ thể.
 */
public interface ReviewState {
    void approve(Review review);
    void delete(Review review, String reason);
    void reply(Review review, String replyText);
}
