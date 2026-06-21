package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.user;

import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.entity.MembershipTier;
import java.util.List;

/**
 * Chiến lược nâng hạng thành viên tiêu chuẩn (mặc định).
 * Triển khai của Strategy Pattern, so khớp trực tiếp điểm số tích lũy hiện tại
 *          của khách hàng với ngưỡng điểm tối thiểu (minPoints) của từng hạng.
 */
public class StandardUpgradeStrategy implements TierUpgradeStrategy {

    @Override
    public MembershipTier determineEligibleTier(User user, List<MembershipTier> allTiers) {
        if (allTiers == null || allTiers.isEmpty()) {
            return user.getMembershipTier();
        }

        MembershipTier eligibleTier = null;
        int currentPoints = user.getLoyaltyPoints() != null ? user.getLoyaltyPoints() : 0;

        for (MembershipTier tier : allTiers) {
            if (currentPoints >= tier.getMinPoints()) {
                if (eligibleTier == null || tier.getMinPoints() > eligibleTier.getMinPoints()) {
                    eligibleTier = tier;
                }
            }
        }
        return eligibleTier != null ? eligibleTier : user.getMembershipTier();
    }
}
