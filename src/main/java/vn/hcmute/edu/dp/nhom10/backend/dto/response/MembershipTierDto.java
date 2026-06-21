package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipTierDto implements Serializable {
    private String name;
    private Integer minPoints;
    private BigDecimal discountPercent;
    private String description;
}
