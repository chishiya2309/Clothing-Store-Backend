package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipInfoResponse implements Serializable {
    private Integer loyaltyPoints;
    private String currentTierName;
    private BigDecimal currentTierDiscount;
    private String currentTierDescription;
    private String nextTierName;
    private Integer pointsNeededForNextTier;
    private List<MembershipTierDto> allTiers;
}
