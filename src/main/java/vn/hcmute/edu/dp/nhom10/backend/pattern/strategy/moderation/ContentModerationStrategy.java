package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.moderation;

/**
 * ContentModerationStrategy
 * 
 * Mẫu thiết kế: Strategy Pattern
 * - Tách biệt thuật toán lọc từ ngữ tục tĩu ra khỏi bộ quét nội dung.
 * - Cho phép thay đổi linh hoạt quy tắc lọc (ví dụ: tiếng Việt, tiếng Anh) mà không cần viết lại mã nguồn chính.
 */
public interface ContentModerationStrategy {
    boolean containsProfanity(String text);
    String getMatchedWords(String text);
}
