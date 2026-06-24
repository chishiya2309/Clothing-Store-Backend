package vn.hcmute.edu.dp.nhom10.backend.pattern.observer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.Wishlist;
import vn.hcmute.edu.dp.nhom10.backend.repository.WishlistRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.EmailService;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WishlistNotificationObserver implements ProductPriceObserver {

    private final WishlistRepository wishlistRepository;
    private final EmailService emailService;

    @Override
    public void update(Product product, BigDecimal oldPrice, BigDecimal newPrice) {
        // Chỉ gửi thông báo khi có giá sale mới và thấp hơn giá cũ (hoặc trước đây chưa
        // có giá sale)
        boolean isSaleApplied = newPrice != null && (oldPrice == null || newPrice.compareTo(oldPrice) < 0);

        if (isSaleApplied) {
            log.info("Product ID {} went on sale (Price: {} -> {}). Notifying wishlist users.",
                    product.getId(), oldPrice, newPrice);

            List<Wishlist> wishlists = wishlistRepository.findByProductId(product.getId());

            // Force load images before async execution to avoid LazyInitializationException
            if (product.getImages() != null) {
                product.getImages().size();
            }

            for (Wishlist wishlist : wishlists) {
                try {
                    emailService.sendProductSaleEmail(
                            wishlist.getUser().getEmail(),
                            wishlist.getUser().getFullName(),
                            product);
                } catch (Exception e) {
                    log.error("Failed to send sale notification to user ID {}", wishlist.getUser().getId(), e);
                }
            }
        }
    }
}
