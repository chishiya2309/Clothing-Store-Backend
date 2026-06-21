package vn.hcmute.edu.dp.nhom10.backend.controller.admin;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.RevenueReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.BestsellerReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.LoyaltyCustomerReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.AdminReportService;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * REST Controller cung cấp các endpoint API thống kê và xuất báo cáo dành riêng cho Admin.
 * Áp dụng Proxy Pattern (Security AOP Proxy) ở mức lớp để chỉ cho phép tài khoản có vai trò ADMIN
 *          truy cập vào toàn bộ các chức năng thống kê doanh thu (BM1), sản phẩm bán chạy (BM2) và khách hàng thân thiết (BM5).
 */
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Security Proxy Pattern ở mức Class
@Tag(name = "Admin Report", description = "Báo cáo và thống kê dành cho Admin")
@Slf4j(topic = "ADMIN-REPORT-CONTROLLER")
public class AdminReportController {

    private final AdminReportService reportService;

    // --- BM1: THỐNG KÊ DOANH THU ---
    @GetMapping("/revenue")
    public ApiResponse getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {

        log.info("API request to fetch revenue report from {} to {}", startDate, endDate);
        List<RevenueReportResponse> report = reportService.getRevenueReport(startDate, endDate);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Lấy dữ liệu thống kê doanh thu thành công")
                .data((Serializable) report)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping("/revenue/export")
    public void exportRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            HttpServletResponse response) throws Exception {

        log.info("API request to export revenue report from {} to {}", startDate, endDate);
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=revenue_report.csv");

        reportService.exportRevenueReport(response.getWriter(), startDate, endDate);
    }

    // --- BM2: THỐNG KÊ SẢN PHẨM BÁN CHẠY ---
    @GetMapping("/bestsellers")
    public ApiResponse getBestsellerReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {

        log.info("API request to fetch bestseller report from {} to {}", startDate, endDate);
        List<BestsellerReportResponse> report = reportService.getBestsellerReport(startDate, endDate);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Lấy dữ liệu thống kê sản phẩm bán chạy thành công")
                .data((Serializable) report)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping("/bestsellers/export")
    public void exportBestsellerReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            HttpServletResponse response) throws Exception {

        log.info("API request to export bestseller report from {} to {}", startDate, endDate);
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bestselling_products.csv");

        reportService.exportBestsellerReport(response.getWriter(), startDate, endDate);
    }

    // --- BM5: BÁO CÁO KHÁCH HÀNG THÂN THIẾT ---
    @GetMapping("/loyalty")
    public ApiResponse getLoyaltyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {

        log.info("API request to fetch loyalty report from {} to {}", startDate, endDate);
        List<LoyaltyCustomerReportResponse> report = reportService.getLoyaltyCustomerReport(startDate, endDate);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Lấy báo cáo khách hàng thân thiết thành công")
                .data((Serializable) report)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping("/loyalty/export")
    public void exportLoyaltyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            HttpServletResponse response) throws Exception {

        log.info("API request to export loyalty report from {} to {}", startDate, endDate);
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=loyalty_customers.csv");

        reportService.exportLoyaltyCustomerReport(response.getWriter(), startDate, endDate);
    }
}
