package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;

@Builder
public record StaffProductImageResponse(
        Long id,
        String imageUrl,
        String imageType,
        Integer displayOrder,
        String altText
) {
}
