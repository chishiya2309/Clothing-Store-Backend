package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.Builder;
import vn.hcmute.edu.dp.nhom10.backend.enums.FlashSaleStatus;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record FlashSaleCampaignResponse(
        Long id,
        String name,
        String description,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        Boolean isActive,
        FlashSaleStatus status,
        List<FlashSaleItemResponse> items,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
