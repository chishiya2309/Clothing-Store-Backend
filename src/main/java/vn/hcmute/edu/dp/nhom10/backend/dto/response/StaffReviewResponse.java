package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StaffReviewResponse {
    private Long id;
    private String reviewerName;
    private String reviewerEmail;
    private String productName;
    private String productSku;
    private Short rating;
    private String content;
    private List<String> imageUrls;
    private String adminReply;
    private OffsetDateTime repliedAt;
    private Boolean isApproved;
    private Boolean isActive;
    private Boolean isFlagged;
    private String flagReason;
    private String deleteReason;
    private OffsetDateTime createdAt;
}
