package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StaffCategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Long parentId;
    private String parentName;
    private Integer displayOrder;
    private Boolean isActive;
    private Integer depth;
    private Long recursiveProductCount;
    private List<StaffCategoryResponse> children;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
