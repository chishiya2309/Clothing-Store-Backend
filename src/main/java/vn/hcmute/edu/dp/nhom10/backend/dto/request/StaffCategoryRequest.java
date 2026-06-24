package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StaffCategoryRequest {
    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục tối đa 100 ký tự")
    private String name;

    private String slug;

    private Long parentId;

    private Integer displayOrder;

    private String description;

    private Boolean isActive;
}
