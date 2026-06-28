package vn.hcmute.edu.dp.nhom10.backend.pattern.factory.product;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Predicate;

@Component
public class ProductIdentityFactory {

    public String createSlug(String productName, Predicate<String> slugExists) {
        String baseSlug = normalizeToken(productName, "-");
        return uniqueValue(baseSlug, slugExists);
    }

    public String createSku(String productSlug, String size, String color, Predicate<String> skuExists) {
        String baseSku = (normalizeToken(productSlug, "-") + "-"
                + normalizeToken(size, "-") + "-"
                + normalizeToken(color, "-"))
                .toUpperCase(Locale.ROOT);
        
        if (baseSku.length() > 40) {
            baseSku = baseSku.substring(0, 40);
            if (baseSku.endsWith("-")) {
                baseSku = baseSku.substring(0, 39);
            }
        }
        
        return uniqueValue(baseSku, skuExists);
    }

    private String uniqueValue(String baseValue, Predicate<String> exists) {
        String candidate = baseValue;
        int suffix = 2;
        while (exists.test(candidate)) {
            candidate = baseValue + "-" + suffix++;
        }
        return candidate;
    }

    private String normalizeToken(String value, String separator) {
        if (value == null || value.isBlank()) {
            return "item";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", separator)
                .replaceAll("^" + separator + "+|" + separator + "+$", "");
        return normalized.isBlank() ? "item" : normalized;
    }
}
