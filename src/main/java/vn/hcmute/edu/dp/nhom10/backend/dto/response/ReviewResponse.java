package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class ReviewResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String reviewerName;
    private Short rating;
    private String content;
    private String variantInfo;
    private String adminReply;
    private OffsetDateTime repliedAt;
    private OffsetDateTime createdAt;
    private List<String> imageUrls;
}
