package vn.hcmute.edu.dp.nhom10.backend.pattern.policy.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.repository.CartItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.InventoryReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ReviewRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.WishlistRepository;

@Component
@RequiredArgsConstructor
public class ProductDeletionPolicy {

    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final WishlistRepository wishlistRepository;
    private final ReviewRepository reviewRepository;
    private final CheckoutSessionItemRepository checkoutSessionItemRepository;
    private final InventoryReservationRepository inventoryReservationRepository;

    public ProductDeletionDecision decide(Long productId) {
        if (orderItemRepository.existsByProductVariantProductId(productId) ||
            cartItemRepository.existsByProductVariantProductId(productId) ||
            wishlistRepository.existsByProductId(productId) ||
            reviewRepository.existsByProductId(productId) ||
            checkoutSessionItemRepository.existsByProductVariantProductId(productId) ||
            inventoryReservationRepository.existsByProductVariantProductId(productId)) {
            return ProductDeletionDecision.SOFT_DELETE;
        }
        return ProductDeletionDecision.HARD_DELETE;
    }
}
