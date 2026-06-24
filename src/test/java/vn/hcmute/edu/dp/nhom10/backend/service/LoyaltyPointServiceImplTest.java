package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.entity.MembershipTier;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.repository.MembershipTierRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.LoyaltyPointServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoyaltyPointServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MembershipTierRepository membershipTierRepository;

    @InjectMocks
    private LoyaltyPointServiceImpl loyaltyPointService;

    @Test
    void awardForCompletedOrder_locksCustomerAddsFloorPointsAndUpgradesTier() {
        MembershipTier bronze = tier(1L, "Dong", 0);
        MembershipTier silver = tier(2L, "Bac", 500);
        User customer = User.builder()
                .id(10L)
                .loyaltyPoints(400)
                .membershipTier(bronze)
                .build();
        Order order = Order.builder()
                .id(1L)
                .user(customer)
                .totalAmount(new BigDecimal("150500.00"))
                .build();
        when(userRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(customer));
        when(membershipTierRepository.findAllByOrderByMinPointsAsc()).thenReturn(List.of(bronze, silver));

        LoyaltyPointAwardResult result = loyaltyPointService.awardForCompletedOrder(order);

        assertEquals(150, result.awardedPoints());
        assertEquals(400, result.previousPoints());
        assertEquals(550, result.resultingPoints());
        assertEquals("Dong", result.previousMembershipTier());
        assertEquals("Bac", result.resultingMembershipTier());
        assertTrue(result.membershipTierChanged());
        assertEquals(550, customer.getLoyaltyPoints());
        assertEquals(silver, customer.getMembershipTier());
        verify(userRepository).findByIdForUpdate(10L);
    }

    @Test
    void awardForCompletedOrder_doesNotDowngradeTier() {
        MembershipTier bronze = tier(1L, "Dong", 0);
        MembershipTier silver = tier(2L, "Bac", 500);
        User customer = User.builder()
                .id(10L)
                .loyaltyPoints(500)
                .membershipTier(silver)
                .build();
        Order order = Order.builder()
                .id(1L)
                .user(customer)
                .totalAmount(new BigDecimal("1000.00"))
                .build();
        when(userRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(customer));
        when(membershipTierRepository.findAllByOrderByMinPointsAsc()).thenReturn(List.of(bronze, silver));

        LoyaltyPointAwardResult result = loyaltyPointService.awardForCompletedOrder(order);

        assertEquals(1, result.awardedPoints());
        assertEquals(501, customer.getLoyaltyPoints());
        assertEquals(silver, customer.getMembershipTier());
        assertFalse(result.membershipTierChanged());
    }

    @Test
    void awardForCompletedOrder_missingCustomer_throws() {
        Order order = Order.builder()
                .id(1L)
                .user(User.builder().id(10L).build())
                .totalAmount(new BigDecimal("1000.00"))
                .build();
        when(userRepository.findByIdForUpdate(10L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> loyaltyPointService.awardForCompletedOrder(order));
    }

    private MembershipTier tier(Long id, String name, int minPoints) {
        return MembershipTier.builder()
                .id(id)
                .name(name)
                .slug(name.toLowerCase())
                .minPoints(minPoints)
                .build();
    }
}
