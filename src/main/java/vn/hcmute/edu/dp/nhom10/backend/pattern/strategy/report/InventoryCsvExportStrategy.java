package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.report;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.InventoryReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReportExportFormat;
import vn.hcmute.edu.dp.nhom10.backend.pattern.template.report.InventoryCsvExporter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InventoryCsvExportStrategy implements ReportExportStrategy<InventoryReportResponse> {
    private static final String CONTENT_TYPE = "text/csv";
    private static final String FILE_NAME = "inventory_report.csv";

    private final InventoryCsvExporter inventoryCsvExporter;

    @Override
    public ReportExportFormat supportFormat() {
        return ReportExportFormat.CSV;
    }

    @Override
    public String contentType() {
        return CONTENT_TYPE;
    }

    @Override
    public String fileName() {
        return FILE_NAME;
    }

    @Override
    public void export(OutputStream outputStream, List<InventoryReportResponse> data) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        inventoryCsvExporter.export(writer, data);
    }
}
