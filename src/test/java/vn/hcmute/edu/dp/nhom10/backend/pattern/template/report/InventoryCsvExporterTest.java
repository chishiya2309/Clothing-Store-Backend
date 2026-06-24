package vn.hcmute.edu.dp.nhom10.backend.pattern.template.report;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.InventoryReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.enums.InventoryReportStatus;

import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryCsvExporterTest {

    @Test
    void export_writesBomHeaderAndRowsInBm3Order() throws Exception {
        InventoryCsvExporter exporter = new InventoryCsvExporter();
        StringWriter writer = new StringWriter();

        exporter.export(writer, List.of(new InventoryReportResponse(
                "SP001",
                "Áo Polo Nam Cotton",
                "L / Trắng",
                5,
                InventoryReportStatus.LOW_STOCK,
                "Sắp hết",
                3L,
                "Áo Polo",
                10L,
                "SKU-10"
        )));

        String result = writer.toString();

        assertTrue(result.startsWith("\uFEFF"));
        assertTrue(result.contains("Mã SP,Tên sản phẩm,Biến thể (Size/Màu),Tồn kho,Trạng thái"));
        assertTrue(result.contains("SP001,Áo Polo Nam Cotton,L / Trắng,5,Sắp hết"));
    }

    @Test
    void export_escapesCommaQuoteAndNewLine() throws Exception {
        InventoryCsvExporter exporter = new InventoryCsvExporter();
        StringWriter writer = new StringWriter();

        exporter.export(writer, List.of(new InventoryReportResponse(
                "SP002",
                "Áo \"Polo\", Cotton",
                "M\nĐen",
                12,
                InventoryReportStatus.IN_STOCK,
                "Còn hàng",
                3L,
                "Áo Polo",
                11L,
                "SKU-11"
        )));

        String result = writer.toString();

        assertTrue(result.contains("\"Áo \"\"Polo\"\", Cotton\""));
        assertTrue(result.contains("\"M\nĐen\""));
    }

    @Test
    void export_emptyList_writesOnlyBomAndHeader() throws Exception {
        InventoryCsvExporter exporter = new InventoryCsvExporter();
        StringWriter writer = new StringWriter();

        exporter.export(writer, List.of());

        String result = writer.toString();

        assertTrue(result.startsWith("\uFEFF"));
        assertTrue(result.contains("Mã SP,Tên sản phẩm,Biến thể (Size/Màu),Tồn kho,Trạng thái"));
    }
}
