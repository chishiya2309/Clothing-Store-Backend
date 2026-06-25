package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.RevenueReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.BestsellerReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.LoyaltyCustomerReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.AdminReportService;
import vn.hcmute.edu.dp.nhom10.backend.pattern.template.report.RevenueCsvExporter;
import vn.hcmute.edu.dp.nhom10.backend.pattern.template.report.BestsellerCsvExporter;
import vn.hcmute.edu.dp.nhom10.backend.pattern.template.report.LoyaltyCustomerCsvExporter;

import java.io.Writer;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Lớp triển khai các dịch vụ báo cáo thống kê cho Admin.
 * Kết hợp OrderRepository để truy xuất dữ liệu gộp nhóm và điều phối các
 *          lớp Exporter cụ thể (áp dụng Template Method Pattern) để xuất file CSV.
 * Áp dụng Mapper Pattern: convert interface Projection (native query result)
 *          sang domain DTO Record trước khi trả về controller/exporter.
 */
@Slf4j(topic = "ADMIN-REPORT-SERVICE")
@Service
@RequiredArgsConstructor
public class AdminReportServiceImpl implements AdminReportService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RevenueReportResponse> getRevenueReport(OffsetDateTime startDate, OffsetDateTime endDate) {
        log.info("Fetching revenue report between {} and {}", startDate, endDate);
        return orderRepository.findRevenueReport(startDate, endDate)
                .stream()
                .map(p -> new RevenueReportResponse(
                        p.getDate(),
                        p.getTotalOrders(),
                        p.getCompletedOrders(),
                        p.getCancelledOrders(),
                        p.getTotalRevenue(),
                        p.getTotalDiscounts(),
                        p.getNetRevenue()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public void exportRevenueReport(Writer writer, OffsetDateTime startDate, OffsetDateTime endDate) {
        log.info("Exporting revenue CSV report between {} and {}", startDate, endDate);
        try {
            List<RevenueReportResponse> data = getRevenueReport(startDate, endDate);
            new RevenueCsvExporter().export(writer, data);
        } catch (Exception e) {
            log.error("Failed to export revenue CSV report", e);
            throw new RuntimeException("Lỗi trong quá trình xuất dữ liệu CSV doanh thu", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BestsellerReportResponse> getBestsellerReport(OffsetDateTime startDate, OffsetDateTime endDate) {
        log.info("Fetching bestseller report between {} and {}", startDate, endDate);
        return orderRepository.findBestsellingProducts(startDate, endDate)
                .stream()
                .map(p -> new BestsellerReportResponse(
                        p.getProductId(),
                        p.getProductName(),
                        p.getCategoryName(),
                        p.getTotalQuantitySold(),
                        p.getTotalRevenue()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public void exportBestsellerReport(Writer writer, OffsetDateTime startDate, OffsetDateTime endDate) {
        log.info("Exporting bestseller CSV report between {} and {}", startDate, endDate);
        try {
            List<BestsellerReportResponse> data = getBestsellerReport(startDate, endDate);
            new BestsellerCsvExporter().export(writer, data);
        } catch (Exception e) {
            log.error("Failed to export bestseller CSV report", e);
            throw new RuntimeException("Lỗi trong quá trình xuất dữ liệu CSV sản phẩm bán chạy", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoyaltyCustomerReportResponse> getLoyaltyCustomerReport(OffsetDateTime startDate, OffsetDateTime endDate) {
        log.info("Fetching loyalty customer report between {} and {}", startDate, endDate);
        return orderRepository.findLoyaltyCustomers(startDate, endDate)
                .stream()
                .map(p -> new LoyaltyCustomerReportResponse(
                        p.getUserId(),
                        p.getFullName(),
                        p.getEmail(),
                        p.getMembershipTier(),
                        p.getTotalOrders(),
                        p.getTotalSpent(),
                        p.getLoyaltyPoints()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public void exportLoyaltyCustomerReport(Writer writer, OffsetDateTime startDate, OffsetDateTime endDate) {
        log.info("Exporting loyalty customer CSV report between {} and {}", startDate, endDate);
        try {
            List<LoyaltyCustomerReportResponse> data = getLoyaltyCustomerReport(startDate, endDate);
            new LoyaltyCustomerCsvExporter().export(writer, data);
        } catch (Exception e) {
            log.error("Failed to export loyalty customer CSV report", e);
            throw new RuntimeException("Lỗi trong quá trình xuất dữ liệu CSV khách hàng thân thiết", e);
        }
    }
}
