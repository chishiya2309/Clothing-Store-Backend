package vn.hcmute.edu.dp.nhom10.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.CreateReviewRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductReviewSummary;
import vn.hcmute.edu.dp.nhom10.backend.service.ReviewService;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.OffsetDateTime;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "API quản lý đánh giá sản phẩm dành cho khách hàng")
@Slf4j(topic = "REVIEW-CONTROLLER")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/api/products/{productId}/reviews")
    @Operation(summary = "Xem danh sách đánh giá của sản phẩm (public)")
    public ApiResponse getProductReviews(
            @PathVariable Long productId,
            @RequestParam(required = false) Short rating,
            @RequestParam(required = false) Boolean withImages,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        log.info("Fetching reviews for product: {} with filters - rating: {}, withImages: {}, page: {}, size: {}",
                productId, rating, withImages, page, size);

        ProductReviewSummary summary = reviewService.getProductReviews(productId, rating, withImages, page, size);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Fetch product reviews successfully")
                .data(summary)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PostMapping("/api/customer/reviews")
    @Operation(summary = "Gửi đánh giá sản phẩm mới (cần đăng nhập)")
    public ApiResponse createReview(
            Principal principal,
            @Validated @RequestBody CreateReviewRequest request) {

        log.info("User {} is creating a review for product {}", principal.getName(), request.productId());
        reviewService.createReview(request, principal.getName());

        return ApiResponse.builder()
                .status(HttpStatus.CREATED.value())
                .message("Đánh giá sản phẩm đã được gửi và đang chờ duyệt")
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping("/api/customer/reviews/can-review")
    @Operation(summary = "Kiểm tra khách hàng có thể đánh giá sản phẩm hay không (cần đăng nhập)")
    public ApiResponse canReview(
            Principal principal,
            @RequestParam Long productId) {

        log.info("Checking review eligibility for user: {} and product: {}", principal.getName(), productId);
        boolean canReview = reviewService.canReview(productId, principal.getName());

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Check review eligibility successfully")
                .data(canReview)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping("/api/customer/reviews/eligible-orders")
    @Operation(summary = "Lấy danh sách đơn hàng đã mua đủ điều kiện để đánh giá (cần đăng nhập)")
    public ApiResponse getEligibleOrders(
            Principal principal,
            @RequestParam Long productId) {

        log.info("Fetching eligible orders for review - user: {}, product: {}", principal.getName(), productId);
        java.util.List<vn.hcmute.edu.dp.nhom10.backend.dto.response.EligibleOrderResponse> orders =
                reviewService.getEligibleOrdersForReview(productId, principal.getName());

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Fetched eligible orders successfully")
                .data(new java.util.ArrayList<>(orders))
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PostMapping(value = "/api/customer/reviews/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload hình ảnh cho đánh giá (cần đăng nhập)")
    public ApiResponse uploadReviewImage(
            @RequestParam("file") MultipartFile file) {

        log.info("Uploading review image");
        String url = reviewService.uploadReviewImage(file);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Upload hình ảnh thành công")
                .data(url)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
