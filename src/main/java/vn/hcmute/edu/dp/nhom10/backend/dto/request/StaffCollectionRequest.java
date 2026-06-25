package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.OffsetDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StaffCollectionRequest {
    @NotBlank(message = "Tên bộ sưu tập không được để trống")
    @Size(max = 255, message = "Tên bộ sưu tập tối đa 255 ký tự")
    private String name;

    private String slug;

    private String description;

    private String bannerUrl;

    private OffsetDateTime startDate;

    private OffsetDateTime endDate;

    private Boolean isActive;
}
