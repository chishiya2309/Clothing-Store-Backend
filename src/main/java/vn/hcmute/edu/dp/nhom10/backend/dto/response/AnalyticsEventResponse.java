package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import java.io.Serializable;
import java.time.OffsetDateTime;

public record AnalyticsEventResponse(
        Long id,
        Long experimentId,
        String experimentKey,
        Long variantId,
        String variantKey,
        String eventName,
        OffsetDateTime createdAt
) implements Serializable {
}
