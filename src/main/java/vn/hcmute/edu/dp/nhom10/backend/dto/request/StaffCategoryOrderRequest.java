package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StaffCategoryOrderRequest {
    @NotNull(message = "ID danh mục không được để trống")
    private Long id;

    @NotNull(message = "Thứ tự hiển thị không được để trống")
    private Integer displayOrder;
}
