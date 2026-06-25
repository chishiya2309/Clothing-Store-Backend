package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.moderation.ContentModerationStrategy;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.moderation.VietnameseProfanityStrategy;

import static org.junit.jupiter.api.Assertions.*;

public class ContentModerationStrategyTest {

    private final ContentModerationStrategy strategy = new VietnameseProfanityStrategy();

    @Test
    public void testCleanText() {
        String text = "Sản phẩm này rất tốt, tôi cực kỳ thích!";
        assertFalse(strategy.containsProfanity(text));
        assertEquals("", strategy.getMatchedWords(text));
    }

    @Test
    public void testProfaneText() {
        String text1 = "Đồ ngu, bán hàng lừa đảo vcl";
        assertTrue(strategy.containsProfanity(text1));
        assertTrue(strategy.getMatchedWords(text1).contains("ngu"));
        assertTrue(strategy.getMatchedWords(text1).contains("vcl"));

        String text2 = "Shop này làm ăn như đm vậy";
        assertTrue(strategy.containsProfanity(text2));
        assertEquals("đm", strategy.getMatchedWords(text2));
    }

    @Test
    public void testNullOrEmptyText() {
        assertFalse(strategy.containsProfanity(null));
        assertFalse(strategy.containsProfanity(""));
        assertFalse(strategy.containsProfanity("   "));
    }
}
