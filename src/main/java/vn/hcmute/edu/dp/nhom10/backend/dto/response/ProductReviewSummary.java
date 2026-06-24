package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Getter
@Builder
public class ProductReviewSummary implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Double averageRating;
    private Long totalReviews;
    private Map<Integer, Long> ratingDistribution; // e.g. {5: 105, 4: 15, ...}
    private PageResponse<ReviewResponse> reviews;
}
