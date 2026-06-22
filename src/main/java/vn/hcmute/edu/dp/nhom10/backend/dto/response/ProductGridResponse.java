package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductGridResponse {
    private Long id;
    private String name;
    private String slug;
    private String thumbnailUrl;
    private BigDecimal basePrice;
    private BigDecimal salePrice;
    private List<String> colors;
}
