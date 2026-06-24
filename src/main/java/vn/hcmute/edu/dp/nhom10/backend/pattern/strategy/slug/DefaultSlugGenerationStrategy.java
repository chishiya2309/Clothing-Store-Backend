package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.slug;

public class DefaultSlugGenerationStrategy implements SlugGenerationStrategy {
    @Override
    public String generate(String input) {
        if (input == null || input.isBlank()) return "";
        String plain = input.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
        
        if (plain.startsWith("-")) {
            plain = plain.substring(1);
        }
        if (plain.endsWith("-")) {
            plain = plain.substring(0, plain.length() - 1);
        }
        return plain.trim();
    }
}
