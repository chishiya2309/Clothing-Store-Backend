package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.moderation;

import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class VietnameseProfanityStrategy implements ContentModerationStrategy {

    private static final List<String> PROFANITIES = Arrays.asList(
            "đm", "dkm", "vl", "vcl", "đéo", "chó", "ngu", "bitch", "mẹ mày", "đụ", "cặc", "lồn"
    );

    @Override
    public boolean containsProfanity(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String lowerText = text.toLowerCase();
        for (String word : PROFANITIES) {
            if (lowerText.contains(word)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getMatchedWords(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String lowerText = text.toLowerCase();
        return PROFANITIES.stream()
                .filter(lowerText::contains)
                .collect(Collectors.joining(", "));
    }
}
