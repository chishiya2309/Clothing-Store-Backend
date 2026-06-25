package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffDeleteReviewRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffReplyReviewRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffReviewResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.Review;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.review.ProfanityScanner;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.review.SpamScanner;
import vn.hcmute.edu.dp.nhom10.backend.pattern.command.review.ReviewCommand;
import vn.hcmute.edu.dp.nhom10.backend.pattern.command.review.ReviewCommandExecutor;
import vn.hcmute.edu.dp.nhom10.backend.pattern.policy.review.ReviewModerationPolicy;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.moderation.VietnameseProfanityStrategy;
import vn.hcmute.edu.dp.nhom10.backend.repository.ReviewRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.StaffReviewServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StaffReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewCommandExecutor commandExecutor;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ReviewModerationPolicy moderationPolicy;
    private SpamScanner spamScanner;
    private ProfanityScanner profanityScanner;
    private StaffReviewServiceImpl staffReviewService;

    @BeforeEach
    public void setup() {
        moderationPolicy = new ReviewModerationPolicy(new VietnameseProfanityStrategy());
        spamScanner = new SpamScanner();
        profanityScanner = new ProfanityScanner(new VietnameseProfanityStrategy());
        staffReviewService = new StaffReviewServiceImpl(
                reviewRepository,
                commandExecutor,
                eventPublisher,
                moderationPolicy,
                spamScanner,
                profanityScanner
        );
    }

    @Test
    public void testGetReviewsByTabPending() {
        User user = User.builder().fullName("Nguyễn Văn A").email("a@gmail.com").build();
        Product product = Product.builder().name("Áo thun").build();
        Review review = Review.builder().id(1L).user(user).product(product).rating((short) 5).content("Rất tốt").isApproved(false).isActive(true).build();
        Page<Review> page = new PageImpl<>(List.of(review));

        when(reviewRepository.findByIsApprovedFalseAndIsActiveTrue(any(Pageable.class))).thenReturn(page);

        PageResponse<StaffReviewResponse> result = staffReviewService.getReviewsByTab("PENDING", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Rất tốt", result.getContent().get(0).getContent());
        assertFalse(result.getContent().get(0).getIsApproved());
        assertTrue(result.getContent().get(0).getIsActive());
    }

    @Test
    public void testGetReviewsByTabApproved() {
        User user = User.builder().fullName("Nguyễn Văn A").email("a@gmail.com").build();
        Product product = Product.builder().name("Áo thun").build();
        Review review = Review.builder().id(1L).user(user).product(product).rating((short) 5).content("Rất tốt").isApproved(true).isActive(true).build();
        Page<Review> page = new PageImpl<>(List.of(review));

        when(reviewRepository.findByIsApprovedTrueAndIsActiveTrue(any(Pageable.class))).thenReturn(page);

        PageResponse<StaffReviewResponse> result = staffReviewService.getReviewsByTab("APPROVED", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).getIsApproved());
    }

    @Test
    public void testGetReviewsByTabDeleted() {
        User user = User.builder().fullName("Nguyễn Văn A").email("a@gmail.com").build();
        Product product = Product.builder().name("Áo thun").build();
        Review review = Review.builder().id(1L).user(user).product(product).rating((short) 1).content("Tệ").isApproved(false).isActive(false).deleteReason("Spam").build();
        Page<Review> page = new PageImpl<>(List.of(review));

        when(reviewRepository.findByIsActiveFalse(any(Pageable.class))).thenReturn(page);

        PageResponse<StaffReviewResponse> result = staffReviewService.getReviewsByTab("DELETED", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertFalse(result.getContent().get(0).getIsActive());
        assertEquals("Spam", result.getContent().get(0).getDeleteReason());
    }

    @Test
    public void testApproveReview() {
        User user = User.builder().fullName("Nguyễn Văn A").email("a@gmail.com").build();
        Review review = Review.builder().id(1L).user(user).isApproved(true).isActive(true).build();
        
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        StaffReviewResponse result = staffReviewService.approveReview(1L, "staff@gmail.com");

        assertNotNull(result);
        assertTrue(result.getIsApproved());
        verify(commandExecutor, times(1)).execute(any(ReviewCommand.class), eq("staff@gmail.com"));
    }

    @Test
    public void testReplyToReviewWithProfanity() {
        StaffReplyReviewRequest request = new StaffReplyReviewRequest("Đồ ngu, cảm ơn bạn.");
        
        assertThrows(InvalidDataException.class, () -> {
            staffReviewService.replyToReview(1L, request, "staff@gmail.com");
        });
        
        verify(commandExecutor, never()).execute(any(ReviewCommand.class), anyString());
    }

    @Test
    public void testDeleteReviewWithEmptyReason() {
        StaffDeleteReviewRequest request = new StaffDeleteReviewRequest("");

        assertThrows(InvalidDataException.class, () -> {
            staffReviewService.deleteReview(1L, request, "staff@gmail.com");
        });

        verify(commandExecutor, never()).execute(any(ReviewCommand.class), anyString());
    }
}
