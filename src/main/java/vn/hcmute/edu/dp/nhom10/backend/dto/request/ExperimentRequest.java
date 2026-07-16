package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.OffsetDateTime;
import java.util.List;

public record ExperimentRequest(
        @NotBlank String key,
        @NotBlank String name,
        String description,
        String status,
        String targetPage,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        @NotEmpty List<@Valid ExperimentVariantRequest> variants
) {
}
