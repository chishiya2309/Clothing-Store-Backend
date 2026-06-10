package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;

public record UpdateUserRoleRequest(
        @NotNull UserRole role
        ) {
}
