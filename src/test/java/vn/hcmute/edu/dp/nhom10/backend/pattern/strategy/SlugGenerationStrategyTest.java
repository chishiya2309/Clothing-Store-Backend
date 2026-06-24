package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.slug.DefaultSlugGenerationStrategy;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.slug.SlugGenerationStrategy;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.slug.VietnameseSlugGenerationStrategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SlugGenerationStrategyTest {

    @Test
    public void testVietnameseSlugGeneration() {
        SlugGenerationStrategy strategy = new VietnameseSlugGenerationStrategy();

        assertEquals("ao-so-mi-nam-cotton", strategy.generate("Áo sơ mi nam Cotton"));
        assertEquals("ao-polo-the-thao-nam", strategy.generate("Áo polo thể thao nam  "));
        assertEquals("dong-phuc-gia-dinh", strategy.generate("Đồng Phục Gia Đình!"));
        assertEquals("", strategy.generate(null));
        assertEquals("", strategy.generate("   "));
    }

    @Test
    public void testDefaultSlugGeneration() {
        SlugGenerationStrategy strategy = new DefaultSlugGenerationStrategy();

        assertEquals("men-t-shirt-cotton", strategy.generate("Men T-shirt Cotton"));
        assertEquals("collection-2026", strategy.generate("Collection 2026!!!"));
        assertEquals("", strategy.generate(null));
    }
}
