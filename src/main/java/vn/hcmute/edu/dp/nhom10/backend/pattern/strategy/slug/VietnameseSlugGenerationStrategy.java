package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.slug;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class VietnameseSlugGenerationStrategy implements SlugGenerationStrategy {
    @Override
    public String generate(String input) {
        if (input == null || input.isBlank()) return "";
        
        // Remove accents
        String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String plain = pattern.matcher(temp).replaceAll("")
                .toLowerCase()
                .replace("đ", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
        
        // Trim leading and trailing hyphens
        if (plain.startsWith("-")) {
            plain = plain.substring(1);
        }
        if (plain.endsWith("-")) {
            plain = plain.substring(0, plain.length() - 1);
        }
        return plain.trim();
    }
}
