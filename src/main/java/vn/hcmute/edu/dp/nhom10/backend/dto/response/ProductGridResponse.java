package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductGridResponse implements Serializable {
    private Long id;
    private String name;
    private String slug;
    private String thumbnailUrl;
    private BigDecimal basePrice;
    private BigDecimal salePrice;
    private List<String> colors;
    private Boolean isActive;
    private String categoryName;
}
