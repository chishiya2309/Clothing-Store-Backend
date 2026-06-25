package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StaffReplyReviewRequest {
    @NotBlank(message = "Nội dung phản hồi không được để trống")
    private String replyText;
}
