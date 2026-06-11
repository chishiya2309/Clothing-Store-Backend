package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.user;

import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.entity.MembershipTier;
import java.util.List;

/**
 * Chiến lược nâng hạng thành viên mùa khuyến mại/sự kiện.
 * Triển khai của Strategy Pattern, giảm 20% ngưỡng điểm thăng hạng (minPoints)
 *          cần thiết để kích cầu mua sắm và thăng hạng nhanh hơn cho khách hàng.
 */
public class PromoUpgradeStrategy implements TierUpgradeStrategy {

    @Override
    public MembershipTier determineEligibleTier(User user, List<MembershipTier> allTiers) {
        if (allTiers == null || allTiers.isEmpty()) {
            return user.getMembershipTier();
        }

        MembershipTier eligibleTier = null;
        int currentPoints = user.getLoyaltyPoints() != null ? user.getLoyaltyPoints() : 0;

        for (MembershipTier tier : allTiers) {
            int promoMinPoints = (int) (tier.getMinPoints() * 0.8);
            if (currentPoints >= promoMinPoints) {
                if (eligibleTier == null || tier.getMinPoints() > eligibleTier.getMinPoints()) {
                    eligibleTier = tier;
                }
            }
        }
        return eligibleTier != null ? eligibleTier : user.getMembershipTier();
    }
}
