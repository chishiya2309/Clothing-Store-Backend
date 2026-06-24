package vn.hcmute.edu.dp.nhom10.backend.pattern.policy.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.repository.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProductDeletionPolicyTest {

    private OrderItemRepository orderItemRepository;
    private CartItemRepository cartItemRepository;
    private WishlistRepository wishlistRepository;
    private ReviewRepository reviewRepository;
    private CheckoutSessionItemRepository checkoutSessionItemRepository;
    private InventoryReservationRepository inventoryReservationRepository;
    private ProductDeletionPolicy policy;

    @BeforeEach
    void setUp() {
        orderItemRepository = mock(OrderItemRepository.class);
        cartItemRepository = mock(CartItemRepository.class);
        wishlistRepository = mock(WishlistRepository.class);
        reviewRepository = mock(ReviewRepository.class);
        checkoutSessionItemRepository = mock(CheckoutSessionItemRepository.class);
        inventoryReservationRepository = mock(InventoryReservationRepository.class);

        policy = new ProductDeletionPolicy(
                orderItemRepository,
                cartItemRepository,
                wishlistRepository,
                reviewRepository,
                checkoutSessionItemRepository,
                inventoryReservationRepository
        );
    }

    @Test
    void decide_productWithOrderItems_returnsSoftDelete() {
        when(orderItemRepository.existsByProductVariantProductId(1L)).thenReturn(true);
        assertEquals(ProductDeletionDecision.SOFT_DELETE, policy.decide(1L));
    }

    @Test
    void decide_productWithCartItems_returnsSoftDelete() {
        when(cartItemRepository.existsByProductVariantProductId(1L)).thenReturn(true);
        assertEquals(ProductDeletionDecision.SOFT_DELETE, policy.decide(1L));
    }

    @Test
    void decide_productWithWishlist_returnsSoftDelete() {
        when(wishlistRepository.existsByProductId(1L)).thenReturn(true);
        assertEquals(ProductDeletionDecision.SOFT_DELETE, policy.decide(1L));
    }

    @Test
    void decide_productWithReviews_returnsSoftDelete() {
        when(reviewRepository.existsByProductId(1L)).thenReturn(true);
        assertEquals(ProductDeletionDecision.SOFT_DELETE, policy.decide(1L));
    }

    @Test
    void decide_productWithCheckoutSessionItems_returnsSoftDelete() {
        when(checkoutSessionItemRepository.existsByProductVariantProductId(1L)).thenReturn(true);
        assertEquals(ProductDeletionDecision.SOFT_DELETE, policy.decide(1L));
    }

    @Test
    void decide_productWithInventoryReservations_returnsSoftDelete() {
        when(inventoryReservationRepository.existsByProductVariantProductId(1L)).thenReturn(true);
        assertEquals(ProductDeletionDecision.SOFT_DELETE, policy.decide(1L));
    }

    @Test
    void decide_noReferencesExist_returnsHardDelete() {
        when(orderItemRepository.existsByProductVariantProductId(1L)).thenReturn(false);
        when(cartItemRepository.existsByProductVariantProductId(1L)).thenReturn(false);
        when(wishlistRepository.existsByProductId(1L)).thenReturn(false);
        when(reviewRepository.existsByProductId(1L)).thenReturn(false);
        when(checkoutSessionItemRepository.existsByProductVariantProductId(1L)).thenReturn(false);
        when(inventoryReservationRepository.existsByProductVariantProductId(1L)).thenReturn(false);

        assertEquals(ProductDeletionDecision.HARD_DELETE, policy.decide(1L));
    }
}
