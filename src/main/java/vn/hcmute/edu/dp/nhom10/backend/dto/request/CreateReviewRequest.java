package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateReviewRequest(
        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "Order ID is required")
        Long orderId,

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        Short rating,

        @NotBlank(message = "Content is required")
        @Size(min = 10, message = "Nội dung đánh giá cần tối thiểu 10 ký tự")
        String content,

        List<String> imageUrls
) {}
