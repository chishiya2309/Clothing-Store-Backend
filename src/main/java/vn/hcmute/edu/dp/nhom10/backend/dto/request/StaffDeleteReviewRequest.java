package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StaffDeleteReviewRequest {
    @NotBlank(message = "Lý do xóa không được để trống")
    private String reason;
}
