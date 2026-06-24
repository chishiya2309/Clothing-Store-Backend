package vn.hcmute.edu.dp.nhom10.backend.pattern.factory.report;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.InventoryReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReportExportFormat;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.report.InventoryCsvExportStrategy;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.report.ReportExportStrategy;
import vn.hcmute.edu.dp.nhom10.backend.pattern.template.report.InventoryCsvExporter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReportExporterFactoryTest {

    @Test
    void getInventoryReportExporter_csv_returnsInventoryCsvStrategy() {
        InventoryCsvExportStrategy strategy = new InventoryCsvExportStrategy(new InventoryCsvExporter());
        ReportExporterFactory factory = new ReportExporterFactory(List.of(strategy));

        ReportExportStrategy<InventoryReportResponse> result =
                factory.getInventoryReportExporter(ReportExportFormat.CSV);

        assertSame(strategy, result);
    }

    @Test
    void getInventoryReportExporter_nullFormat_defaultsToCsv() {
        InventoryCsvExportStrategy strategy = new InventoryCsvExportStrategy(new InventoryCsvExporter());
        ReportExporterFactory factory = new ReportExporterFactory(List.of(strategy));

        ReportExportStrategy<InventoryReportResponse> result = factory.getInventoryReportExporter(null);

        assertSame(strategy, result);
    }

    @Test
    void getInventoryReportExporter_missingStrategy_throws() {
        ReportExporterFactory factory = new ReportExporterFactory(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> factory.getInventoryReportExporter(ReportExportFormat.CSV));
    }

    @Test
    void constructor_duplicateFormat_throws() {
        ReportExportStrategy<InventoryReportResponse> first = new StubStrategy();
        ReportExportStrategy<InventoryReportResponse> second = new StubStrategy();

        assertThrows(IllegalStateException.class,
                () -> new ReportExporterFactory(List.of(first, second)));
    }

    private static class StubStrategy implements ReportExportStrategy<InventoryReportResponse> {
        @Override
        public ReportExportFormat supportFormat() {
            return ReportExportFormat.CSV;
        }

        @Override
        public String contentType() {
            return "text/csv";
        }

        @Override
        public String fileName() {
            return "inventory_report.csv";
        }

        @Override
        public void export(OutputStream outputStream, List<InventoryReportResponse> data) throws IOException {
        }
    }
}
