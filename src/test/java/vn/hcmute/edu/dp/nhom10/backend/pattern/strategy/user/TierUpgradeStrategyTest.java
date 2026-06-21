package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.entity.MembershipTier;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TierUpgradeStrategyTest {

    private List<MembershipTier> mockTiers;
    private MembershipTier bronzeTier;
    private MembershipTier silverTier;
    private MembershipTier goldTier;

    @BeforeEach
    void setUp() {
        bronzeTier = MembershipTier.builder()
                .id(1L)
                .name("Bronze")
                .slug("bronze")
                .minPoints(100)
                .discountPercent(BigDecimal.valueOf(2.0))
                .build();

        silverTier = MembershipTier.builder()
                .id(2L)
                .name("Silver")
                .slug("silver")
                .minPoints(500)
                .discountPercent(BigDecimal.valueOf(5.0))
                .build();

        goldTier = MembershipTier.builder()
                .id(3L)
                .name("Gold")
                .slug("gold")
                .minPoints(1000)
                .discountPercent(BigDecimal.valueOf(10.0))
                .build();

        // Tiers in ascending or any order
        mockTiers = List.of(bronzeTier, silverTier, goldTier);
    }

    @Test
    void standardStrategy_returnsCorrectTiersBasedOnPoints() {
        TierUpgradeStrategy strategy = new StandardUpgradeStrategy();

        // 1. User with 0 points (no current tier)
        User user0 = User.builder().loyaltyPoints(0).membershipTier(null).build();
        assertNull(strategy.determineEligibleTier(user0, mockTiers));

        // 2. User with 150 points -> Bronze (min 100)
        User userBronze = User.builder().loyaltyPoints(150).membershipTier(null).build();
        assertEquals(bronzeTier, strategy.determineEligibleTier(userBronze, mockTiers));

        // 3. User with 500 points -> Silver (min 500)
        User userSilver = User.builder().loyaltyPoints(500).membershipTier(bronzeTier).build();
        assertEquals(silverTier, strategy.determineEligibleTier(userSilver, mockTiers));

        // 4. User with 1200 points -> Gold (min 1000)
        User userGold = User.builder().loyaltyPoints(1200).membershipTier(silverTier).build();
        assertEquals(goldTier, strategy.determineEligibleTier(userGold, mockTiers));
    }

    @Test
    void standardStrategy_handlesNullOrEmptyTiersGracefully() {
        TierUpgradeStrategy strategy = new StandardUpgradeStrategy();
        User user = User.builder().loyaltyPoints(500).membershipTier(bronzeTier).build();

        // Empty tiers list should return current user tier
        assertEquals(bronzeTier, strategy.determineEligibleTier(user, List.of()));
        assertEquals(bronzeTier, strategy.determineEligibleTier(user, null));
    }

    @Test
    void promoStrategy_appliesTwentyPercentDiscountOnMinPoints() {
        TierUpgradeStrategy strategy = new PromoUpgradeStrategy();

        // Standard Bronze min = 100 -> Promo min = 80
        // Standard Silver min = 500 -> Promo min = 400
        // Standard Gold min = 1000 -> Promo min = 800

        // 1. User with 50 points -> under promo threshold of 80 -> returns null
        User userUnder = User.builder().loyaltyPoints(50).membershipTier(null).build();
        assertNull(strategy.determineEligibleTier(userUnder, mockTiers));

        // 2. User with 85 points -> meets Bronze promo threshold (80)
        User userBronze = User.builder().loyaltyPoints(85).membershipTier(null).build();
        assertEquals(bronzeTier, strategy.determineEligibleTier(userBronze, mockTiers));

        // 3. User with 420 points -> meets Silver promo threshold (400)
        User userSilver = User.builder().loyaltyPoints(420).membershipTier(bronzeTier).build();
        assertEquals(silverTier, strategy.determineEligibleTier(userSilver, mockTiers));

        // 4. User with 800 points -> meets Gold promo threshold (800)
        User userGold = User.builder().loyaltyPoints(800).membershipTier(silverTier).build();
        assertEquals(goldTier, strategy.determineEligibleTier(userGold, mockTiers));
    }

    @Test
    void promoStrategy_handlesNullOrEmptyTiersGracefully() {
        TierUpgradeStrategy strategy = new PromoUpgradeStrategy();
        User user = User.builder().loyaltyPoints(500).membershipTier(silverTier).build();

        assertEquals(silverTier, strategy.determineEligibleTier(user, List.of()));
        assertEquals(silverTier, strategy.determineEligibleTier(user, null));
    }
}
