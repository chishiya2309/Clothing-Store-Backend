package vn.hcmute.edu.dp.nhom10.backend.pattern.chain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffReviewResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Review;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.review.ProfanityScanner;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.review.SpamScanner;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.moderation.VietnameseProfanityStrategy;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewContentScannerTest {

    private SpamScanner spamScanner;
    private ProfanityScanner profanityScanner;

    @BeforeEach
    public void setup() {
        spamScanner = new SpamScanner();
        profanityScanner = new ProfanityScanner(new VietnameseProfanityStrategy());
        spamScanner.setNextScanner(profanityScanner);
    }

    @Test
    public void testCleanReviewContent() {
        Review review = Review.builder().content("Sản phẩm rất đẹp, vải mát.").build();
        StaffReviewResponse response = StaffReviewResponse.builder().isFlagged(false).build();

        spamScanner.scan(review, response);

        assertFalse(response.getIsFlagged());
        assertNull(response.getFlagReason());
    }

    @Test
    public void testRepetitiveWordSpam() {
        Review review = Review.builder().content("Đẹp đẹp đẹp quá shop ơi!").build();
        StaffReviewResponse response = StaffReviewResponse.builder().isFlagged(false).build();

        spamScanner.scan(review, response);

        assertTrue(response.getIsFlagged());
        assertTrue(response.getFlagReason().contains("Spam"));
    }

    @Test
    public void testRepetitiveCharSpam() {
        Review review = Review.builder().content("aaaaaaaaaaaaaaaa").build();
        StaffReviewResponse response = StaffReviewResponse.builder().isFlagged(false).build();

        spamScanner.scan(review, response);

        assertTrue(response.getIsFlagged());
        assertTrue(response.getFlagReason().contains("Spam"));
    }

    @Test
    public void testShortReviewSpam() {
        Review review = Review.builder().content("Ok").build();
        StaffReviewResponse response = StaffReviewResponse.builder().isFlagged(false).build();

        spamScanner.scan(review, response);

        assertTrue(response.getIsFlagged());
        assertTrue(response.getFlagReason().contains("Spam"));
    }

    @Test
    public void testProfanityViolation() {
        Review review = Review.builder().content("Đồ ngu, bán hàng kiểu gì vậy đm").build();
        StaffReviewResponse response = StaffReviewResponse.builder().isFlagged(false).build();

        spamScanner.scan(review, response);

        assertTrue(response.getIsFlagged());
        assertTrue(response.getFlagReason().contains("Từ ngữ nhạy cảm"));
        assertTrue(response.getFlagReason().contains("ngu"));
        assertTrue(response.getFlagReason().contains("đm"));
    }

    @Test
    public void testSpamAndProfanityCombined() {
        Review review = Review.builder().content("Shop làm ăn như đm ooooooooooo").build();
        StaffReviewResponse response = StaffReviewResponse.builder().isFlagged(false).build();

        spamScanner.scan(review, response);

        assertTrue(response.getIsFlagged());
        assertTrue(response.getFlagReason().contains("Spam"));
        assertTrue(response.getFlagReason().contains("Từ ngữ nhạy cảm"));
    }
}
