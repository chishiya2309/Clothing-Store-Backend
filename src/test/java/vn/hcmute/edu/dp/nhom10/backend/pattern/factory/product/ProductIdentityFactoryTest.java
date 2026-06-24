package vn.hcmute.edu.dp.nhom10.backend.pattern.factory.product;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductIdentityFactoryTest {

    private final ProductIdentityFactory factory = new ProductIdentityFactory();

    @Test
    void createSlug_normalizesVietnameseName() {
        String slug = factory.createSlug("Áo Thun Đen Nam", value -> false);

        assertEquals("ao-thun-den-nam", slug);
    }

    @Test
    void createSlug_existingSlug_addsNumericSuffix() {
        Set<String> existing = Set.of("ao-polo", "ao-polo-2");

        String slug = factory.createSlug("Áo Polo", existing::contains);

        assertEquals("ao-polo-3", slug);
    }

    @Test
    void createSku_generatesUppercaseUniqueSku() {
        Set<String> existing = Set.of("ao-polo-m-den");

        String sku = factory.createSku("ao-polo", "m", "đen", existing::contains);

        assertEquals("AO-POLO-M-DEN", sku);
    }
}
