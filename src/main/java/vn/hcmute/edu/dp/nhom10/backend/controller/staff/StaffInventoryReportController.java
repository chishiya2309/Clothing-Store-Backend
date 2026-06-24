package vn.hcmute.edu.dp.nhom10.backend.controller.staff;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.InventoryReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ReportExportDescriptor;
import vn.hcmute.edu.dp.nhom10.backend.enums.InventoryReportStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReportExportFormat;
import vn.hcmute.edu.dp.nhom10.backend.service.InventoryReportService;

import java.io.IOException;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/staff/reports/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
public class StaffInventoryReportController {

    private final InventoryReportService inventoryReportService;

    @GetMapping
    public ApiResponse getInventoryReport(
            @RequestParam(required = false) InventoryReportStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "stockAsc") String sortBy
    ) {
        PageResponse<InventoryReportResponse> report = inventoryReportService.getInventoryReport(
                status,
                categoryId,
                keyword,
                page,
                size,
                sortBy
        );

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Lấy báo cáo tồn kho thành công")
                .data(report)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping("/export")
    public void exportInventoryReport(
            @RequestParam(required = false) InventoryReportStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "stockAsc") String sortBy,
            @RequestParam(defaultValue = "CSV") ReportExportFormat format,
            HttpServletResponse response
    ) throws IOException {
        ReportExportDescriptor descriptor = inventoryReportService.describeInventoryExport(format);

        response.setContentType(descriptor.contentType());
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + descriptor.fileName());

        inventoryReportService.exportInventoryReport(
                response.getOutputStream(),
                status,
                categoryId,
                keyword,
                sortBy,
                format
        );
    }
}
