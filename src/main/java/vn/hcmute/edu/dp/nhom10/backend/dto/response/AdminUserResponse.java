package vn.hcmute.edu.dp.nhom10.backend.dto.response;


import vn.hcmute.edu.dp.nhom10.backend.enums.GenderType;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record AdminUserResponse(
        Long id,
        String email,
        String fullName,
        String phone,
        GenderType gender,
        LocalDate dateOfBirth,
        String avatarUrl,
        UserRole role,
        Integer loyaltyPoints,
        Long membershipTierId,
        String membershipTierName,
        String authProvider,
        Boolean emailVerified,
        Boolean isActive,
        OffsetDateTime lastLoginAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
}
