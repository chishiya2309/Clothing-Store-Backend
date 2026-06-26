package vn.hcmute.edu.dp.nhom10.backend.controller.staff;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffDeleteReviewRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffReplyReviewRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffReviewResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.StaffReviewService;

import java.security.Principal;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/staff/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
@Slf4j(topic = "STAFF-REVIEW-CONTROLLER")
public class StaffReviewController {

    private final StaffReviewService staffReviewService;

    @GetMapping
    public ApiResponse getReviews(
            @RequestParam(required = false, defaultValue = "PENDING") String tab,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Staff fetching reviews for tab: {}, page: {}, size: {}", tab, page, size);
        PageResponse<StaffReviewResponse> response = staffReviewService.getReviewsByTab(tab, page, size);
        return buildResponse(HttpStatus.OK, "Lấy danh sách đánh giá thành công", response);
    }

    @PutMapping("/{id}/approve")
    public ApiResponse approveReview(
            @PathVariable Long id,
            Principal principal
    ) {
        String username = principal != null ? principal.getName() : "system";
        log.info("Staff {} is approving review ID: {}", username, id);
        StaffReviewResponse response = staffReviewService.approveReview(id, username);
        return buildResponse(HttpStatus.OK, "Duyệt đánh giá thành công", response);
    }

    @PutMapping("/{id}/reply")
    public ApiResponse replyToReview(
            @PathVariable Long id,
            @Valid @RequestBody StaffReplyReviewRequest request,
            Principal principal
    ) {
        String username = principal != null ? principal.getName() : "system";
        log.info("Staff {} is replying to review ID: {}", username, id);
        StaffReviewResponse response = staffReviewService.replyToReview(id, request, username);
        return buildResponse(HttpStatus.OK, "Phản hồi đánh giá thành công", response);
    }

    @PutMapping("/{id}/delete")
    public ApiResponse deleteReview(
            @PathVariable Long id,
            @Valid @RequestBody StaffDeleteReviewRequest request,
            Principal principal
    ) {
        String username = principal != null ? principal.getName() : "system";
        log.info("Staff {} is soft-deleting review ID: {} with reason: {}", username, id, request.getReason());
        StaffReviewResponse response = staffReviewService.deleteReview(id, request, username);
        return buildResponse(HttpStatus.OK, "Xóa đánh giá thành công", response);
    }

    private ApiResponse buildResponse(HttpStatus status, String message, Object data) {
        return ApiResponse.builder()
                .status(status.value())
                .message(message)
                .data(data)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
