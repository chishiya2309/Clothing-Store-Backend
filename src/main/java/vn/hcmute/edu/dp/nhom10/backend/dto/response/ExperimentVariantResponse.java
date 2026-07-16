package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import java.io.Serializable;

public record ExperimentVariantResponse(
        Long id,
        String key,
        String name,
        Integer trafficWeight,
        Boolean isControl,
        Boolean isActive,
        String payload
) implements Serializable {
}
