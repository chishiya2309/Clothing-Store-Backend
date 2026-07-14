package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.ResolvedProductPrice;
import vn.hcmute.edu.dp.nhom10.backend.entity.FlashSaleItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.enums.PriceSource;
import vn.hcmute.edu.dp.nhom10.backend.repository.FlashSaleItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.FlashSalePricingService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class FlashSalePricingServiceImpl implements FlashSalePricingService {
    private final FlashSaleItemRepository flashSaleItemRepository;

    @Override
    @Transactional(readOnly = true)
    public ResolvedProductPrice resolve(Product product, OffsetDateTime now) {
        if (product == null || product.getId() == null) {
            throw new IllegalArgumentException("Product is required for pricing");
        }
        if (now == null) {
            throw new IllegalArgumentException("Pricing time is required");
        }

        var activeItems = flashSaleItemRepository.findActiveForProductAt(
                product.getId(), now, PageRequest.of(0, 1));
        if (!activeItems.isEmpty()) {
            FlashSaleItem item = activeItems.get(0);
            int quota = value(item.getQuota());
            int reserved = value(item.getReservedQuantity());
            int sold = value(item.getSoldQuantity());
            if (quota - reserved - sold > 0) {
                return result(item.getFlashSalePrice(), PriceSource.FLASH_SALE, item.getId(), product.getId());
            }
        }
        if (product.getSalePrice() != null) {
            return result(product.getSalePrice(), PriceSource.PRODUCT_SALE, null, product.getId());
        }
        return result(product.getBasePrice(), PriceSource.REGULAR, null, product.getId());
    }

    private int value(Integer quantity) {
        return quantity == null ? 0 : quantity;
    }

    private ResolvedProductPrice result(BigDecimal price, PriceSource source, Long itemId, Long productId) {
        if (price == null) {
            throw new IllegalArgumentException("Product price is missing: " + productId);
        }
        if (price.signum() < 0) {
            throw new IllegalArgumentException("Product price must not be negative: " + productId);
        }
        return new ResolvedProductPrice(price, source, itemId);
    }
}
