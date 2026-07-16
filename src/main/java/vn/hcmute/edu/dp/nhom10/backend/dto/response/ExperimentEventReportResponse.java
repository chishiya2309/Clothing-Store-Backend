package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import java.io.Serializable;

public record ExperimentEventReportResponse(
        Long variantId,
        String variantKey,
        String variantName,
        String eventName,
        Long eventCount,
        Long uniqueVisitors
) implements Serializable {
}
