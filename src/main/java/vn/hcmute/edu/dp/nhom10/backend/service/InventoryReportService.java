package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.response.InventoryReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.enums.InventoryReportStatus;

public interface InventoryReportService {
    PageResponse<InventoryReportResponse> getInventoryReport(
            InventoryReportStatus status,
            Long categoryId,
            String keyword,
            int page,
            int size,
            String sortBy
    );
}
