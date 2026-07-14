package vn.hcmute.edu.dp.nhom10.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;
    private List<Rule> rules = new ArrayList<>();

    @Getter
    @Setter
    public static class Rule {
        private String key;
        private List<String> pathPatterns = new ArrayList<>();
        private List<String> methods = new ArrayList<>();
        private int limit;
        private long windowSeconds;
        private String message;
    }
}
