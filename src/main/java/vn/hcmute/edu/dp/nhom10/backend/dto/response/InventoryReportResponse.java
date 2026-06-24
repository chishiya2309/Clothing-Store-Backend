package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import vn.hcmute.edu.dp.nhom10.backend.enums.InventoryReportStatus;

import java.io.Serializable;

public record InventoryReportResponse(
        String productCode,
        String productName,
        String variantInfo,
        Integer stockQuantity,
        InventoryReportStatus status,
        String statusLabel,
        Long categoryId,
        String categoryName,
        Long variantId,
        String sku
) implements Serializable {
}
