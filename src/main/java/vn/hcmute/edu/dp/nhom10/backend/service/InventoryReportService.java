package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.response.InventoryReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ReportExportDescriptor;
import vn.hcmute.edu.dp.nhom10.backend.enums.InventoryReportStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReportExportFormat;

import java.io.IOException;
import java.io.OutputStream;

public interface InventoryReportService {
    PageResponse<InventoryReportResponse> getInventoryReport(
            InventoryReportStatus status,
            Long categoryId,
            String keyword,
            int page,
            int size,
            String sortBy
    );

    ReportExportDescriptor describeInventoryExport(ReportExportFormat format);

    void exportInventoryReport(
            OutputStream outputStream,
            InventoryReportStatus status,
            Long categoryId,
            String keyword,
            String sortBy,
            ReportExportFormat format
    ) throws IOException;
}
