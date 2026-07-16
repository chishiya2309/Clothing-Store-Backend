package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

public record ExperimentResponse(
        Long id,
        String key,
        String name,
        String description,
        String status,
        String targetPage,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        List<ExperimentVariantResponse> variants,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) implements Serializable {
}
