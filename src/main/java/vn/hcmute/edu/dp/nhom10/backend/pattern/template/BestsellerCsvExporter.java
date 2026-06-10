package vn.hcmute.edu.dp.nhom10.backend.pattern.template;

import vn.hcmute.edu.dp.nhom10.backend.dto.response.BestsellerReportResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * Lớp triển khai cụ thể để xuất báo cáo sản phẩm bán chạy ra CSV.
 * Định nghĩa cấu trúc cột đúng chuẩn BM2 (Hạng, Mã SP, Tên SP, Danh mục, Số lượng, Doanh thu)
 *          trong đó cột "Hạng" tự động map theo index lặp của Template Method.
 */
public class BestsellerCsvExporter extends CsvReportExporterTemplate<BestsellerReportResponse> {

    @Override
    protected void writeHeader(Writer writer) throws IOException {
        writer.write("Hạng,Mã SP,Tên sản phẩm,Danh mục,Số lượng bán,Doanh thu (VNĐ)\n");
    }

    @Override
    protected void writeRow(Writer writer, BestsellerReportResponse item, int index) throws IOException {
        writer.write(String.format("%s,%s,%s,%s,%s,%s\n",
                escapeCsvField(index), // Hạng (Rank) maps to index
                escapeCsvField(item.productId()),
                escapeCsvField(item.productName()),
                escapeCsvField(item.categoryName()),
                escapeCsvField(item.totalQuantitySold()),
                escapeCsvField(item.totalRevenue())
        ));
    }
}
