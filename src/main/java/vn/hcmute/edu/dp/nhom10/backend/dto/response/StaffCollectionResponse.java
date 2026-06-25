package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.*;
import java.time.OffsetDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StaffCollectionResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String bannerUrl;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private Boolean isActive;
    private String statusState; // ACTIVE, INACTIVE, SCHEDULED, EXPIRED
    private Integer productCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
