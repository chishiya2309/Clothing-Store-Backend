package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.CreateReviewRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.EligibleOrderResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductReviewSummary;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ReviewResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.*;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.*;
import vn.hcmute.edu.dp.nhom10.backend.service.ReviewService;
import vn.hcmute.edu.dp.nhom10.backend.service.S3Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public void createReview(CreateReviewRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Validate: Don hang phai cua dung user
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("This order does not belong to the authenticated user");
        }

        // Validate: Trang thai don hang phai la completed
        if (order.getStatus() != OrderStatus.completed) {
            throw new InvalidDataException("Đơn hàng phải ở trạng thái Hoàn thành mới có thể đánh giá");
        }

        // Validate: Da review truoc do chua
        if (reviewRepository.existsByUserIdAndProductIdAndOrderId(user.getId(), product.getId(), order.getId())) {
            throw new InvalidDataException("Bạn đã đánh giá sản phẩm này cho đơn hàng hiện tại rồi");
        }

        // Validate: Nguoi dung da thuc su mua san pham nay chua
        if (!reviewRepository.hasPurchasedProduct(user.getId(), product.getId(), OrderStatus.completed)) {
            throw new InvalidDataException("Bạn chỉ có thể đánh giá sản phẩm đã mua");
        }

        // Tao review moi
        Review review = Review.builder()
                .user(user)
                .product(product)
                .order(order)
                .rating(request.rating())
                .content(request.content())
                .isApproved(false) // Mac dinh cho staff duyet
                .build();

        // Luu tru hinh anh review neu co
        if (request.imageUrls() != null && !request.imageUrls().isEmpty()) {
            List<ReviewImage> images = new ArrayList<>();
            for (int i = 0; i < request.imageUrls().size(); i++) {
                images.add(ReviewImage.builder()
                        .review(review)
                        .imageUrl(request.imageUrls().get(i))
                        .displayOrder(i)
                        .build());
            }
            review.setImages(images);
        }

        reviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductReviewSummary getProductReviews(Long productId, Short rating, Boolean withImages, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewPage;

        if (withImages != null && withImages) {
            reviewPage = reviewRepository.findApprovedWithImagesByProductId(productId, pageable);
        } else if (rating != null) {
            reviewPage = reviewRepository.findApprovedByProductIdAndRating(productId, rating, pageable);
        } else {
            reviewPage = reviewRepository.findApprovedByProductId(productId, pageable);
        }

        // Lay tat ca order items cho cac review nay de map variantInfo
        List<ReviewResponse> responseList = reviewPage.getContent().stream()
                .map(review -> {
                    List<OrderItem> orderItems = orderItemRepository.findByOrderIdAndProductVariantProductId(
                            review.getOrder().getId(), 
                            review.getProduct().getId()
                    );
                    String variantInfo = orderItems.stream()
                            .map(OrderItem::getVariantInfo)
                            .collect(Collectors.joining(", "));

                    List<String> imageUrls = review.getImages().stream()
                            .map(ReviewImage::getImageUrl)
                            .collect(Collectors.toList());

                    return ReviewResponse.builder()
                            .id(review.getId())
                            .reviewerName(review.getUser().getFullName())
                            .rating(review.getRating())
                            .content(review.getContent())
                            .variantInfo(variantInfo)
                            .adminReply(review.getAdminReply())
                            .repliedAt(review.getRepliedAt())
                            .createdAt(review.getCreatedAt())
                            .imageUrls(imageUrls)
                            .build();
                })
                .collect(Collectors.toList());

        PageResponse<ReviewResponse> pageResponse = new PageResponse<>(
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalPages(),
                reviewPage.getTotalElements(),
                responseList
        );

        // Tinh rating phan bo
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }
        List<Object[]> distributionData = reviewRepository.countRatingDistribution(productId);
        for (Object[] row : distributionData) {
            Short r = (Short) row[0];
            Long count = (Long) row[1];
            distribution.put(r.intValue(), count);
        }

        // Tinh rating trung binh
        Double avgRating = reviewRepository.calculateAverageRating(productId);
        long totalReviews = reviewRepository.countByProductIdAndIsApprovedTrue(productId);

        return ProductReviewSummary.builder()
                .averageRating(avgRating)
                .totalReviews(totalReviews)
                .ratingDistribution(distribution)
                .reviews(pageResponse)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canReview(Long productId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElse(null);
        if (user == null) {
            return false;
        }

        List<Order> orders = reviewRepository.findEligibleOrdersForReview(user.getId(), productId, OrderStatus.completed);
        return !orders.isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EligibleOrderResponse> getEligibleOrdersForReview(Long productId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Order> orders = reviewRepository.findEligibleOrdersForReview(user.getId(), productId, OrderStatus.completed);

        return orders.stream()
                .map(o -> EligibleOrderResponse.builder()
                        .id(o.getId())
                        .orderCode(o.getOrderCode())
                        .createdAt(o.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getPendingReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> pendingPage = reviewRepository.findByIsApprovedFalse(pageable);

        List<ReviewResponse> responseList = pendingPage.getContent().stream()
                .map(review -> {
                    List<OrderItem> orderItems = orderItemRepository.findByOrderIdAndProductVariantProductId(
                            review.getOrder().getId(), 
                            review.getProduct().getId()
                    );
                    String variantInfo = orderItems.stream()
                            .map(OrderItem::getVariantInfo)
                            .collect(Collectors.joining(", "));

                    List<String> imageUrls = review.getImages().stream()
                            .map(ReviewImage::getImageUrl)
                            .collect(Collectors.toList());

                    return ReviewResponse.builder()
                            .id(review.getId())
                            .reviewerName(review.getUser().getFullName())
                            .rating(review.getRating())
                            .content(review.getContent())
                            .variantInfo(variantInfo)
                            .adminReply(review.getAdminReply())
                            .repliedAt(review.getRepliedAt())
                            .createdAt(review.getCreatedAt())
                            .imageUrls(imageUrls)
                            .build();
                })
                .collect(Collectors.toList());

        return new PageResponse<>(
                pendingPage.getNumber(),
                pendingPage.getSize(),
                pendingPage.getTotalPages(),
                pendingPage.getTotalElements(),
                responseList
        );
    }

    @Override
    @Transactional
    public void approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        review.setIsApproved(true);
        reviewRepository.save(review);

        // Tinh toan lai rating trung binh cho Product
        Double newAvgRating = reviewRepository.calculateAverageRating(review.getProduct().getId());
        Product product = review.getProduct();
        product.setAverageRating(BigDecimal.valueOf(newAvgRating));
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void replyToReview(Long reviewId, String replyText) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        review.setAdminReply(replyText);
        review.setRepliedAt(OffsetDateTime.now());
        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        Product product = review.getProduct();
        reviewRepository.delete(review);

        // Tinh toan lai rating trung binh cho Product sau khi xoa
        Double newAvgRating = reviewRepository.calculateAverageRating(product.getId());
        product.setAverageRating(BigDecimal.valueOf(newAvgRating));
        productRepository.save(product);
    }

    @Override
    public String uploadReviewImage(MultipartFile file) {
        return s3Service.uploadFile("reviews", file);
    }
}
