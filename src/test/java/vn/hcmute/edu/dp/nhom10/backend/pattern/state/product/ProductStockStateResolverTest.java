package vn.hcmute.edu.dp.nhom10.backend.pattern.state.product;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.enums.StaffProductStatus;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductStockStateResolverTest {

    private final ProductStockStateResolver resolver = new ProductStockStateResolver();

    @Test
    void resolve_inactiveProduct_returnsInactive() {
        Product product = product(false, 99);

        assertEquals(StaffProductStatus.INACTIVE, resolver.resolve(product));
    }

    @Test
    void resolve_activeWithoutStock_returnsOutOfStock() {
        Product product = product(true, 0);

        assertEquals(StaffProductStatus.OUT_OF_STOCK, resolver.resolve(product));
    }

    @Test
    void resolve_activeLowStock_returnsLowStock() {
        Product product = product(true, 5);

        assertEquals(StaffProductStatus.LOW_STOCK, resolver.resolve(product));
        assertTrue(resolver.isLowStock(5));
    }

    @Test
    void resolve_activeEnoughStock_returnsActive() {
        Product product = product(true, 10);

        assertEquals(StaffProductStatus.ACTIVE, resolver.resolve(product));
    }

    private Product product(boolean active, int stock) {
        Product product = Product.builder()
                .isActive(active)
                .variants(new ArrayList<>())
                .build();
        product.getVariants().add(ProductVariant.builder()
                .isActive(true)
                .stockQuantity(stock)
                .build());
        return product;
    }
}
