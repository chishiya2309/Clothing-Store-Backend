package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.entity.MembershipTier;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.user.StandardUpgradeStrategy;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.user.TierUpgradeStrategy;
import vn.hcmute.edu.dp.nhom10.backend.repository.MembershipTierRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.LoyaltyPointAwardResult;
import vn.hcmute.edu.dp.nhom10.backend.service.LoyaltyPointService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoyaltyPointServiceImpl implements LoyaltyPointService {

    private static final BigDecimal VND_PER_POINT = BigDecimal.valueOf(1000);

    private final UserRepository userRepository;
    private final MembershipTierRepository membershipTierRepository;
    private final TierUpgradeStrategy tierUpgradeStrategy = new StandardUpgradeStrategy();

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public LoyaltyPointAwardResult awardForCompletedOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order is required");
        }
        if (order.getUser() == null || order.getUser().getId() == null) {
            throw new IllegalStateException("Order customer is required before awarding loyalty points");
        }
        if (order.getTotalAmount() == null || order.getTotalAmount().signum() < 0) {
            throw new IllegalStateException("Order total amount is required before awarding loyalty points");
        }

        User customer = userRepository.findByIdForUpdate(order.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("Order customer not found: " + order.getUser().getId()));

        Integer previousPoints = customer.getLoyaltyPoints() == null ? 0 : customer.getLoyaltyPoints();
        MembershipTier previousTier = customer.getMembershipTier();
        int awardedPoints = calculateAwardedPoints(order.getTotalAmount());
        int resultingPoints = previousPoints + awardedPoints;
        customer.setLoyaltyPoints(resultingPoints);

        MembershipTier resultingTier = determineResultingTier(customer, previousTier);
        boolean tierChanged = hasTierChanged(previousTier, resultingTier);
        if (tierChanged) {
            customer.setMembershipTier(resultingTier);
        }

        return new LoyaltyPointAwardResult(
                awardedPoints,
                previousPoints,
                resultingPoints,
                tierName(previousTier),
                tierName(tierChanged ? resultingTier : previousTier),
                tierChanged
        );
    }

    private int calculateAwardedPoints(BigDecimal totalAmount) {
        return totalAmount.divide(VND_PER_POINT, 0, RoundingMode.DOWN).intValue();
    }

    private MembershipTier determineResultingTier(User customer, MembershipTier previousTier) {
        List<MembershipTier> tiers = membershipTierRepository.findAllByOrderByMinPointsAsc();
        MembershipTier eligibleTier = tierUpgradeStrategy.determineEligibleTier(customer, tiers);
        if (eligibleTier == null) {
            return previousTier;
        }
        if (previousTier == null) {
            return eligibleTier;
        }
        Integer previousMinPoints = previousTier.getMinPoints() == null ? 0 : previousTier.getMinPoints();
        Integer eligibleMinPoints = eligibleTier.getMinPoints() == null ? 0 : eligibleTier.getMinPoints();
        return eligibleMinPoints > previousMinPoints ? eligibleTier : previousTier;
    }

    private boolean hasTierChanged(MembershipTier previousTier, MembershipTier resultingTier) {
        if (previousTier == null) {
            return resultingTier != null;
        }
        if (resultingTier == null) {
            return false;
        }
        return !previousTier.getId().equals(resultingTier.getId());
    }

    private String tierName(MembershipTier tier) {
        return tier == null ? null : tier.getName();
    }
}
