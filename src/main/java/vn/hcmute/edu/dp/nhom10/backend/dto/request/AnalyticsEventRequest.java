package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record AnalyticsEventRequest(
        String experimentKey,
        String variantKey,
        Long experimentId,
        Long variantId,
        Long userId,
        String visitorId,
        String sessionId,
        @NotBlank String eventName,
        String pagePath,
        String gaClientId,
        Map<String, Object> metadata
) {
}
