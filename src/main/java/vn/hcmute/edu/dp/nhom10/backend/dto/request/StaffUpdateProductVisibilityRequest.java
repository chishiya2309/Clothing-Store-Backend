package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record StaffUpdateProductVisibilityRequest(
        @NotNull
        Boolean isActive
) {
}
