package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StaffCollectionProductsRequest {
    @NotEmpty(message = "Danh sách sản phẩm không được trống")
    private List<Long> productIds;
}
