package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerResponse implements Serializable {
    private Long id;
    private String title;
    private String imageUrl;
    private String linkUrl;
    private Integer displayOrder;
    private Boolean isActive;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private OffsetDateTime createdAt;
}
