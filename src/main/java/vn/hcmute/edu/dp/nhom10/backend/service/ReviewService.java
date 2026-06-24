package vn.hcmute.edu.dp.nhom10.backend.service;

import org.springframework.data.domain.Page;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.CreateReviewRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductReviewSummary;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ReviewResponse;

import vn.hcmute.edu.dp.nhom10.backend.dto.response.EligibleOrderResponse;
import java.util.List;

public interface ReviewService {

    // Gui danh gia cho san pham
    void createReview(CreateReviewRequest request, String userEmail);

    // Lay danh sach review cua san pham voi cac bo loc (rating, co hinh anh)
    ProductReviewSummary getProductReviews(Long productId, Short rating, Boolean withImages, int page, int size);

    // Kiem tra khach hang co the danh gia san pham hay khong
    boolean canReview(Long productId, String userEmail);

    // Lay danh sach don hang du dieu kien de danh gia
    List<EligibleOrderResponse> getEligibleOrdersForReview(Long productId, String userEmail);

    // Lay tat ca review cho staff duyet (phan trang)
    PageResponse<ReviewResponse> getPendingReviews(int page, int size);

    // Duyet review
    void approveReview(Long reviewId);

    // Staff tra loi review
    void replyToReview(Long reviewId, String replyText);

    // Xoa review
    void deleteReview(Long reviewId);

    // Upload hinh anh cho review
    String uploadReviewImage(org.springframework.web.multipart.MultipartFile file);
}
