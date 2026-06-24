package vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.report;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.InventoryReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.enums.InventoryReportStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReportExportFormat;
import vn.hcmute.edu.dp.nhom10.backend.pattern.template.report.InventoryCsvExporter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryCsvExportStrategyTest {

    @Test
    void metadata_returnsCsvDescriptorValues() {
        InventoryCsvExportStrategy strategy = new InventoryCsvExportStrategy(new InventoryCsvExporter());

        assertEquals(ReportExportFormat.CSV, strategy.supportFormat());
        assertEquals("text/csv", strategy.contentType());
        assertEquals("inventory_report.csv", strategy.fileName());
    }

    @Test
    void export_writesUtf8CsvAndDoesNotCloseOutputStream() throws Exception {
        InventoryCsvExportStrategy strategy = new InventoryCsvExportStrategy(new InventoryCsvExporter());
        NonClosingOutputStream outputStream = new NonClosingOutputStream();

        strategy.export(outputStream, List.of(new InventoryReportResponse(
                "SP001",
                "Áo Polo",
                "L / Trắng",
                5,
                InventoryReportStatus.LOW_STOCK,
                "Sắp hết",
                1L,
                "Áo Polo",
                10L,
                "SKU-10"
        )));

        String result = outputStream.toString(StandardCharsets.UTF_8);

        assertTrue(result.startsWith("\uFEFF"));
        assertTrue(result.contains("Áo Polo"));
        assertTrue(result.contains("Sắp hết"));
        assertFalse(outputStream.isClosed());
    }

    private static class NonClosingOutputStream extends ByteArrayOutputStream {
        private boolean closed;

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }

        boolean isClosed() {
            return closed;
        }
    }
}
