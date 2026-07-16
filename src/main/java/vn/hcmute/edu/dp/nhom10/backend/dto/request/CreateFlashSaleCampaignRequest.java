package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateFlashSaleCampaignRequest(
        @NotBlank(message = "Campaign name must not be blank")
        @Size(max = 150, message = "Campaign name must not exceed 150 characters")
        String name,

        String description,

        @NotNull(message = "Start time is required")
        OffsetDateTime startAt,

        @NotNull(message = "End time is required")
        OffsetDateTime endAt,

        Boolean isActive
) {
}
