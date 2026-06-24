package vn.hcmute.edu.dp.nhom10.backend.pattern.state.product;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.enums.StaffProductStatus;

@Component
public class ProductStockStateResolver {

    public static final int LOW_STOCK_THRESHOLD = 10;

    public StaffProductStatus resolve(Product product) {
        if (!Boolean.TRUE.equals(product.getIsActive())) {
            return StaffProductStatus.INACTIVE;
        }
        int totalStock = totalStock(product);
        if (totalStock == 0) {
            return StaffProductStatus.OUT_OF_STOCK;
        }
        if (totalStock < LOW_STOCK_THRESHOLD) {
            return StaffProductStatus.LOW_STOCK;
        }
        return StaffProductStatus.ACTIVE;
    }

    public int totalStock(Product product) {
        if (product.getVariants() == null) {
            return 0;
        }
        return product.getVariants().stream()
                .filter(variant -> Boolean.TRUE.equals(variant.getIsActive()))
                .map(ProductVariant::getStockQuantity)
                .filter(stock -> stock != null && stock > 0)
                .mapToInt(Integer::intValue)
                .sum();
    }

    public boolean isLowStock(Integer stockQuantity) {
        return stockQuantity != null && stockQuantity < LOW_STOCK_THRESHOLD;
    }
}
