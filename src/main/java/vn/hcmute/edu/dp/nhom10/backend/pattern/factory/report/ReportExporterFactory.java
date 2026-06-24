package vn.hcmute.edu.dp.nhom10.backend.pattern.factory.report;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.InventoryReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReportExportFormat;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.report.ReportExportStrategy;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ReportExporterFactory {
    private final Map<ReportExportFormat, ReportExportStrategy<InventoryReportResponse>> inventoryReportExporters;

    public ReportExporterFactory(List<ReportExportStrategy<InventoryReportResponse>> inventoryReportExporters) {
        this.inventoryReportExporters = inventoryReportExporters.stream()
                .collect(Collectors.toUnmodifiableMap(
                        ReportExportStrategy::supportFormat,
                        Function.identity()
                ));
    }

    public ReportExportStrategy<InventoryReportResponse> getInventoryReportExporter(ReportExportFormat format) {
        ReportExportFormat resolvedFormat = format == null ? ReportExportFormat.CSV : format;
        ReportExportStrategy<InventoryReportResponse> exporter = inventoryReportExporters.get(resolvedFormat);

        if (exporter == null) {
            throw new IllegalArgumentException("Unsupported export format: " + resolvedFormat);
        }

        return exporter;
    }
}
