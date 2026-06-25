package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffDeleteReviewRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffReplyReviewRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffReviewResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Review;
import vn.hcmute.edu.dp.nhom10.backend.entity.ReviewImage;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.review.ProfanityScanner;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.review.ReviewContentScanner;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.review.SpamScanner;
import vn.hcmute.edu.dp.nhom10.backend.pattern.command.review.ApproveReviewCommand;
import vn.hcmute.edu.dp.nhom10.backend.pattern.command.review.DeleteReviewCommand;
import vn.hcmute.edu.dp.nhom10.backend.pattern.command.review.ReplyReviewCommand;
import vn.hcmute.edu.dp.nhom10.backend.pattern.command.review.ReviewCommandExecutor;
import vn.hcmute.edu.dp.nhom10.backend.pattern.policy.review.ReviewModerationPolicy;
import vn.hcmute.edu.dp.nhom10.backend.repository.ReviewRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.StaffReviewService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "STAFF-REVIEW-SERVICE")
public class StaffReviewServiceImpl implements StaffReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewCommandExecutor commandExecutor;
    private final ApplicationEventPublisher eventPublisher;
    private final ReviewModerationPolicy moderationPolicy;
    private final SpamScanner spamScanner;
    private final ProfanityScanner profanityScanner;
    private final ReviewContentScanner contentScannerChain;

    @Autowired
    public StaffReviewServiceImpl(
            ReviewRepository reviewRepository,
            ReviewCommandExecutor commandExecutor,
            ApplicationEventPublisher eventPublisher,
            ReviewModerationPolicy moderationPolicy,
            SpamScanner spamScanner,
            ProfanityScanner profanityScanner) {
        this.reviewRepository = reviewRepository;
        this.commandExecutor = commandExecutor;
        this.eventPublisher = eventPublisher;
        this.moderationPolicy = moderationPolicy;
        this.spamScanner = spamScanner;
        this.profanityScanner = profanityScanner;

        // Wire Chain of Responsibility
        this.spamScanner.setNextScanner(this.profanityScanner);
        this.contentScannerChain = this.spamScanner;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StaffReviewResponse> getReviewsByTab(String tab, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewPage;

        if (tab == null || tab.isBlank() || tab.equalsIgnoreCase("PENDING")) {
            reviewPage = reviewRepository.findByIsApprovedFalseAndIsActiveTrue(pageable);
        } else if (tab.equalsIgnoreCase("APPROVED")) {
            reviewPage = reviewRepository.findByIsApprovedTrueAndIsActiveTrue(pageable);
        } else if (tab.equalsIgnoreCase("DELETED")) {
            reviewPage = reviewRepository.findByIsActiveFalse(pageable);
        } else {
            throw new InvalidDataException("Tab không hợp lệ. Chỉ chấp nhận PENDING, APPROVED, DELETED.");
        }

        List<StaffReviewResponse> content = reviewPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalPages(),
                reviewPage.getTotalElements(),
                content
        );
    }

    @Override
    @Transactional
    public StaffReviewResponse approveReview(Long id, String username) {
        ApproveReviewCommand command = new ApproveReviewCommand(id, reviewRepository, eventPublisher);
        commandExecutor.execute(command, username);
        
        Review updatedReview = reviewRepository.findById(id)
                .orElseThrow(() -> new InvalidDataException("Đánh giá không tồn tại sau khi duyệt."));
        return mapToResponse(updatedReview);
    }

    @Override
    @Transactional
    public StaffReviewResponse replyToReview(Long id, StaffReplyReviewRequest request, String username) {
        moderationPolicy.validateReplyText(request.getReplyText());

        ReplyReviewCommand command = new ReplyReviewCommand(id, request.getReplyText(), reviewRepository, eventPublisher);
        commandExecutor.execute(command, username);

        Review updatedReview = reviewRepository.findById(id)
                .orElseThrow(() -> new InvalidDataException("Đánh giá không tồn tại sau khi phản hồi."));
        return mapToResponse(updatedReview);
    }

    @Override
    @Transactional
    public StaffReviewResponse deleteReview(Long id, StaffDeleteReviewRequest request, String username) {
        moderationPolicy.validateDeleteReason(request.getReason());

        DeleteReviewCommand command = new DeleteReviewCommand(id, request.getReason(), reviewRepository, eventPublisher);
        commandExecutor.execute(command, username);

        Review updatedReview = reviewRepository.findById(id)
                .orElseThrow(() -> new InvalidDataException("Đánh giá không tồn tại sau khi xóa."));
        return mapToResponse(updatedReview);
    }

    private StaffReviewResponse mapToResponse(Review review) {
        List<String> imageUrls = review.getImages() != null ?
                review.getImages().stream().map(ReviewImage::getImageUrl).collect(Collectors.toList()) :
                List.of();

        String productSku = (review.getProduct() != null && review.getProduct().getVariants() != null && !review.getProduct().getVariants().isEmpty()) ?
                review.getProduct().getVariants().get(0).getSku() : null;

        StaffReviewResponse response = StaffReviewResponse.builder()
                .id(review.getId())
                .reviewerName(review.getUser() != null ? review.getUser().getFullName() : null)
                .reviewerEmail(review.getUser() != null ? review.getUser().getEmail() : null)
                .productName(review.getProduct() != null ? review.getProduct().getName() : null)
                .productSku(productSku)
                .rating(review.getRating())
                .content(review.getContent())
                .imageUrls(imageUrls)
                .adminReply(review.getAdminReply())
                .repliedAt(review.getRepliedAt())
                .isApproved(review.getIsApproved())
                .isActive(review.getIsActive())
                .deleteReason(review.getDeleteReason())
                .createdAt(review.getCreatedAt())
                .isFlagged(false)
                .build();

        if (Boolean.TRUE.equals(response.getIsActive()) && !Boolean.TRUE.equals(response.getIsApproved())) {
            if (contentScannerChain != null) {
                contentScannerChain.scan(review, response);
            }
        }

        return response;
    }
}
