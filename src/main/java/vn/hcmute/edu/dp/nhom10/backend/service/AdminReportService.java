package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.response.RevenueReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.BestsellerReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.LoyaltyCustomerReportResponse;

import java.io.Writer;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Interface cung cấp các dịch vụ báo cáo thống kê dành cho quản trị.
 * Định nghĩa các nghiệp vụ thống kê doanh thu, sản phẩm bán chạy
 *          và kết xuất dữ liệu khách hàng thân thiết phục vụ Dashboard và lưu file.
 */
public interface AdminReportService {
    List<RevenueReportResponse> getRevenueReport(OffsetDateTime startDate, OffsetDateTime endDate);
    void exportRevenueReport(Writer writer, OffsetDateTime startDate, OffsetDateTime endDate);

    List<BestsellerReportResponse> getBestsellerReport(OffsetDateTime startDate, OffsetDateTime endDate);
    void exportBestsellerReport(Writer writer, OffsetDateTime startDate, OffsetDateTime endDate);

    List<LoyaltyCustomerReportResponse> getLoyaltyCustomerReport(OffsetDateTime startDate, OffsetDateTime endDate);
    void exportLoyaltyCustomerReport(Writer writer, OffsetDateTime startDate, OffsetDateTime endDate);
}
