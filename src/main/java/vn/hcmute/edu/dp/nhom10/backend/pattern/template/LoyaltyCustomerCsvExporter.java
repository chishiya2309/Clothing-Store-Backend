package vn.hcmute.edu.dp.nhom10.backend.pattern.template;

import vn.hcmute.edu.dp.nhom10.backend.dto.response.LoyaltyCustomerReportResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * Lớp triển khai cụ thể để xuất báo cáo khách hàng thân thiết ra CSV.
 * Định nghĩa cấu trúc cột đúng chuẩn BM5 gồm thông tin cá nhân, chi tiêu tích lũy,
 *          số lượng đơn hàng và điểm tích lũy thành viên hiện tại của từng khách hàng.
 */
public class LoyaltyCustomerCsvExporter extends CsvReportExporterTemplate<LoyaltyCustomerReportResponse> {

    @Override
    protected void writeHeader(Writer writer) throws IOException {
        writer.write("Mã KH,Họ tên,Email,Hạng,Tổng số đơn hàng,Tổng chi tiêu (VNĐ),Điểm tích lũy hiện tại\n");
    }

    @Override
    protected void writeRow(Writer writer, LoyaltyCustomerReportResponse item, int index) throws IOException {
        writer.write(String.format("%s,%s,%s,%s,%s,%s,%s\n",
                escapeCsvField(item.userId()),
                escapeCsvField(item.fullName()),
                escapeCsvField(item.email()),
                escapeCsvField(item.membershipTier()),
                escapeCsvField(item.totalOrders()),
                escapeCsvField(item.totalSpent()),
                escapeCsvField(item.loyaltyPoints())
        ));
    }
}
