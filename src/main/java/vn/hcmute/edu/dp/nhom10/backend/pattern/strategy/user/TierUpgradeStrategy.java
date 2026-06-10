package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.user;

import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.entity.MembershipTier;
import java.util.List;

/**
 * Interface quy định chiến lược tính toán nâng hạng thành viên khách hàng.
 * Định nghĩa hợp đồng cho Strategy Pattern, cho phép chuyển đổi linh hoạt chính sách
 *      định hạng thành viên (Standard vs Promo) tùy thuộc cấu hình hệ thống.
 */
public interface TierUpgradeStrategy {
    MembershipTier determineEligibleTier(User user, List<MembershipTier> allTiers);
}
