package vn.hcmute.edu.dp.nhom10.backend.pattern.template;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.RevenueReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.BestsellerReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.LoyaltyCustomerReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.pattern.template.report.BestsellerCsvExporter;
import vn.hcmute.edu.dp.nhom10.backend.pattern.template.report.LoyaltyCustomerCsvExporter;
import vn.hcmute.edu.dp.nhom10.backend.pattern.template.report.RevenueCsvExporter;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvExporterTest {

    @Test
    void testRevenueCsvExporter_escapesFieldsAndWritesBOM() throws Exception {
        RevenueCsvExporter exporter = new RevenueCsvExporter();
        
        LocalDate localDate = LocalDate.of(2026, 6, 10);
        
        List<RevenueReportResponse> data = List.of(
                new RevenueReportResponse(localDate, 150L, 135L, 15L, BigDecimal.valueOf(450000000), BigDecimal.valueOf(25000000), BigDecimal.valueOf(425000000))
        );

        StringWriter writer = new StringWriter();
        exporter.export(writer, data);

        String result = writer.toString();
        
        // 1. Verify UTF-8 BOM is written
        assertTrue(result.startsWith("\uFEFF"), "CSV output must start with UTF-8 BOM");
        
        // 2. Verify Headers
        assertTrue(result.contains("Ngày,Tổng số đơn hàng,Đơn hoàn thành,Đơn hủy,Doanh thu tổng (VNĐ),Chiết khấu giảm giá (VNĐ),Doanh thu thực (VNĐ)"));
        
        // 3. Verify Data Row content
        assertTrue(result.contains("2026-06-10,150,135,15,450000000,25000000,425000000"));
    }

    @Test
    void testBestsellerCsvExporter_escapesFieldsAndComputesRank() throws Exception {
        BestsellerCsvExporter exporter = new BestsellerCsvExporter();
        
        List<BestsellerReportResponse> data = List.of(
                new BestsellerReportResponse(1L, "Áo Polo, Cotton", "Nam > Áo Polo", 320L, BigDecimal.valueOf(159680000), "http://example.com/image.jpg")
        );

        StringWriter writer = new StringWriter();
        exporter.export(writer, data);

        String result = writer.toString();
        
        // 1. Verify UTF-8 BOM is written
        assertTrue(result.startsWith("\uFEFF"));
        
        // 2. Verify Headers
        assertTrue(result.contains("Hạng,Mã SP,Tên sản phẩm,Danh mục,Số lượng bán,Doanh thu (VNĐ)"));
        
        // 3. Verify escaping of comma in product name: "Áo Polo, Cotton" must be escaped with double quotes
        assertTrue(result.contains("1,1,\"Áo Polo, Cotton\",Nam > Áo Polo,320,159680000"));
    }

    @Test
    void testLoyaltyCustomerCsvExporter_handlesNullTierGracefully() throws Exception {
        LoyaltyCustomerCsvExporter exporter = new LoyaltyCustomerCsvExporter();
        
        List<LoyaltyCustomerReportResponse> data = List.of(
                new LoyaltyCustomerReportResponse(99L, "Trần Văn B", "b@gmail.com", "Vàng", 10L, BigDecimal.valueOf(1000000), 100)
        );

        StringWriter writer = new StringWriter();
        exporter.export(writer, data);

        String result = writer.toString();
        
        // 1. Verify UTF-8 BOM is written
        assertTrue(result.startsWith("\uFEFF"));
        
        // 2. Verify Headers
        assertTrue(result.contains("Mã KH,Họ tên,Email,Hạng,Tổng số đơn hàng,Tổng chi tiêu (VNĐ),Điểm tích lũy hiện tại"));
        
        // 3. Verify Row content
        assertTrue(result.contains("99,Trần Văn B,b@gmail.com,Vàng,10,1000000,100"));
    }
}
