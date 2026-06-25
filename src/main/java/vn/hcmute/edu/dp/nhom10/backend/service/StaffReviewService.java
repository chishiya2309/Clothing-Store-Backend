package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffDeleteReviewRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffReplyReviewRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffReviewResponse;

public interface StaffReviewService {
    PageResponse<StaffReviewResponse> getReviewsByTab(String tab, int page, int size);
    StaffReviewResponse approveReview(Long id, String username);
    StaffReviewResponse replyToReview(Long id, StaffReplyReviewRequest request, String username);
    StaffReviewResponse deleteReview(Long id, StaffDeleteReviewRequest request, String username);
}
