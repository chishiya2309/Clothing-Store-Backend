package vn.hcmute.edu.dp.nhom10.backend.pattern.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import vn.hcmute.edu.dp.nhom10.backend.entity.ActivityLog;
import vn.hcmute.edu.dp.nhom10.backend.entity.Review;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.pattern.command.review.*;
import vn.hcmute.edu.dp.nhom10.backend.pattern.observer.review.ReviewStatusChangedEvent;
import vn.hcmute.edu.dp.nhom10.backend.repository.ActivityLogRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ReviewRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewCommandTest {

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReviewCommandExecutor commandExecutor;

    @Test
    public void testCommandExecutorLogging() {
        String email = "staff@store.com";
        User user = User.builder().id(2L).email(email).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        ReviewCommand<String> mockCommand = mock(ReviewCommand.class);
        when(mockCommand.execute()).thenReturn("Ok");
        when(mockCommand.getDescription()).thenReturn("Action description");

        String result = commandExecutor.execute(mockCommand, email);

        assertEquals("Ok", result);
        verify(mockCommand, times(1)).execute();
        verify(activityLogRepository, times(1)).save(any(ActivityLog.class));
    }

    @Test
    public void testApproveReviewCommand() {
        Review review = Review.builder().id(10L).isApproved(false).isActive(true).build();
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));

        ApproveReviewCommand command = new ApproveReviewCommand(10L, reviewRepository, eventPublisher);
        command.execute();

        assertTrue(review.getIsApproved());
        verify(reviewRepository, times(1)).save(review);
        verify(eventPublisher, times(1)).publishEvent(any(ReviewStatusChangedEvent.class));
    }

    @Test
    public void testDeleteReviewCommand() {
        Review review = Review.builder().id(11L).isApproved(true).isActive(true).build();
        when(reviewRepository.findById(11L)).thenReturn(Optional.of(review));

        DeleteReviewCommand command = new DeleteReviewCommand(11L, "Lý do xóa review", reviewRepository, eventPublisher);
        command.execute();

        assertFalse(review.getIsActive());
        assertFalse(review.getIsApproved());
        assertEquals("Lý do xóa review", review.getDeleteReason());
        verify(reviewRepository, times(1)).save(review);
        verify(eventPublisher, times(1)).publishEvent(any(ReviewStatusChangedEvent.class));
    }

    @Test
    public void testReplyReviewCommand() {
        Review review = Review.builder().id(12L).isApproved(true).isActive(true).build();
        when(reviewRepository.findById(12L)).thenReturn(Optional.of(review));

        ReplyReviewCommand command = new ReplyReviewCommand(12L, "Cảm ơn bạn!", reviewRepository, eventPublisher);
        command.execute();

        assertEquals("Cảm ơn bạn!", review.getAdminReply());
        assertNotNull(review.getRepliedAt());
        verify(reviewRepository, times(1)).save(review);
        verify(eventPublisher, never()).publishEvent(any(ReviewStatusChangedEvent.class));
    }
}
