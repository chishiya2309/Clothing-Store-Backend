package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import java.io.Serializable;

public record ExperimentAssignmentResponse(
        Long experimentId,
        String experimentKey,
        String experimentName,
        Long variantId,
        String variantKey,
        String variantName,
        String payload,
        String visitorId
) implements Serializable {
}
