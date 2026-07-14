package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchDto implements Serializable {
    private Long id;
    private String name;
    private String slug;
    private String image;
    private String category;
    private BigDecimal basePrice;
    private BigDecimal salePrice;
    private BigDecimal rating;
    private Integer soldQuantity;
    private String brand;
}
