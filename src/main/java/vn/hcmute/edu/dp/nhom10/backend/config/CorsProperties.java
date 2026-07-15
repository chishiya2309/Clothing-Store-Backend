package vn.hcmute.edu.dp.nhom10.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    private boolean allowCredentials = true;
    private List<String> allowedOriginPatterns = new ArrayList<>();
    private List<String> exposedHeaders = new ArrayList<>();
}
