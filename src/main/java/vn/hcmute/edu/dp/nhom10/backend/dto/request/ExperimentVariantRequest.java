package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExperimentVariantRequest(
        Long id,
        @NotBlank String key,
        @NotBlank String name,
        @NotNull @Min(0) @Max(100) Integer trafficWeight,
        Boolean isControl,
        Boolean isActive,
        String payload
) {
}
