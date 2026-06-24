package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import vn.hcmute.edu.dp.nhom10.backend.enums.ImageType;

public record StaffProductImageRequest(
        Long id,

        @NotBlank
        String imageUrl,

        @NotNull
        ImageType imageType,

        @Min(0)
        Integer displayOrder,

        String altText
) {
}
