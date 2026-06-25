package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StaffCollectionDetailResponse {
    private StaffCollectionResponse collection;
    private List<ProductGridResponse> products;
}
