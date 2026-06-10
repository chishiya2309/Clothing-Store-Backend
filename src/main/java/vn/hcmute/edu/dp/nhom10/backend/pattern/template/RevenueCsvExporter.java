package vn.hcmute.edu.dp.nhom10.backend.pattern.template;

import vn.hcmute.edu.dp.nhom10.backend.dto.response.RevenueReportResponse;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;

/**
 * Lớp triển khai cụ thể để xuất báo cáo thống kê doanh thu ra CSV.
 * Triển khai các phương thức writeHeader và writeRow để map dữ liệu doanh số,
 *          số lượng đơn hàng, chiết khấu và doanh thu thực nhận vào định dạng CSV.
 */
public class RevenueCsvExporter extends CsvReportExporterTemplate<RevenueReportResponse> {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void writeHeader(Writer writer) throws IOException {
        writer.write("Ngày,Tổng số đơn hàng,Đơn hoàn thành,Đơn hủy,Doanh thu tổng (VNĐ),Chiết khấu giảm giá (VNĐ),Doanh thu thực (VNĐ)\n");
    }

    @Override
    protected void writeRow(Writer writer, RevenueReportResponse item, int index) throws IOException {
        writer.write(String.format("%s,%s,%s,%s,%s,%s,%s\n",
                escapeCsvField(item.date() != null ? dateFormat.format(item.date()) : ""),
                escapeCsvField(item.totalOrders()),
                escapeCsvField(item.completedOrders()),
                escapeCsvField(item.cancelledOrders()),
                escapeCsvField(item.totalRevenue()),
                escapeCsvField(item.totalDiscounts()),
                escapeCsvField(item.netRevenue())
        ));
    }
}
