package vn.hcmute.edu.dp.nhom10.backend.controller.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.RevenueReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.BestsellerReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.LoyaltyCustomerReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.exception.GlobalExceptionHandling;
import vn.hcmute.edu.dp.nhom10.backend.service.AdminReportService;

import java.io.Writer;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminReportService reportService;

    @InjectMocks
    private AdminReportController adminReportController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminReportController)
                .setControllerAdvice(new GlobalExceptionHandling())
                .build();
    }

    @Test
    void getRevenueReport_success() throws Exception {
        OffsetDateTime start = OffsetDateTime.parse("2026-06-01T00:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2026-06-30T23:59:59Z");
        
        List<RevenueReportResponse> mockData = List.of(
                new RevenueReportResponse(LocalDate.now(), 100L, 90L, 10L, BigDecimal.valueOf(300000), BigDecimal.valueOf(20000), BigDecimal.valueOf(280000))
        );

        when(reportService.getRevenueReport(eq(start), eq(end))).thenReturn(mockData);

        mockMvc.perform(get("/api/admin/reports/revenue")
                        .param("startDate", "2026-06-01T00:00:00Z")
                        .param("endDate", "2026-06-30T23:59:59Z")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Lấy dữ liệu thống kê doanh thu thành công"))
                .andExpect(jsonPath("$.data[0].totalOrders").value(100))
                .andExpect(jsonPath("$.data[0].completedOrders").value(90))
                .andExpect(jsonPath("$.data[0].netRevenue").value(280000));

        verify(reportService, times(1)).getRevenueReport(eq(start), eq(end));
    }

    @Test
    void exportRevenueReport_success() throws Exception {
        OffsetDateTime start = OffsetDateTime.parse("2026-06-01T00:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2026-06-30T23:59:59Z");

        doAnswer(invocation -> {
            Writer writer = invocation.getArgument(0);
            writer.write("\uFEFFNgày,Tổng số đơn hàng\n2026-06-10,100\n");
            return null;
        }).when(reportService).exportRevenueReport(any(Writer.class), eq(start), eq(end));

        mockMvc.perform(get("/api/admin/reports/revenue/export")
                        .param("startDate", "2026-06-01T00:00:00Z")
                        .param("endDate", "2026-06-30T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(status().is(200));

        verify(reportService, times(1)).exportRevenueReport(any(Writer.class), eq(start), eq(end));
    }

    @Test
    void getBestsellerReport_success() throws Exception {
        OffsetDateTime start = OffsetDateTime.parse("2026-06-01T00:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2026-06-30T23:59:59Z");

        List<BestsellerReportResponse> mockData = List.of(
                new BestsellerReportResponse(1L, "Áo Polo Nam", "Nam > Polo", 50L, BigDecimal.valueOf(50000))
        );

        when(reportService.getBestsellerReport(eq(start), eq(end))).thenReturn(mockData);

        mockMvc.perform(get("/api/admin/reports/bestsellers")
                        .param("startDate", "2026-06-01T00:00:00Z")
                        .param("endDate", "2026-06-30T23:59:59Z")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Lấy dữ liệu thống kê sản phẩm bán chạy thành công"))
                .andExpect(jsonPath("$.data[0].productId").value(1))
                .andExpect(jsonPath("$.data[0].productName").value("Áo Polo Nam"))
                .andExpect(jsonPath("$.data[0].totalQuantitySold").value(50));

        verify(reportService, times(1)).getBestsellerReport(eq(start), eq(end));
    }

    @Test
    void exportBestsellerReport_success() throws Exception {
        OffsetDateTime start = OffsetDateTime.parse("2026-06-01T00:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2026-06-30T23:59:59Z");

        doAnswer(invocation -> {
            Writer writer = invocation.getArgument(0);
            writer.write("\uFEFFHạng,Mã SP,Tên sản phẩm\n1,1,Áo Polo Nam\n");
            return null;
        }).when(reportService).exportBestsellerReport(any(Writer.class), eq(start), eq(end));

        mockMvc.perform(get("/api/admin/reports/bestsellers/export")
                        .param("startDate", "2026-06-01T00:00:00Z")
                        .param("endDate", "2026-06-30T23:59:59Z"))
                .andExpect(status().isOk());

        verify(reportService, times(1)).exportBestsellerReport(any(Writer.class), eq(start), eq(end));
    }

    @Test
    void getLoyaltyReport_success() throws Exception {
        OffsetDateTime start = OffsetDateTime.parse("2026-06-01T00:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2026-06-30T23:59:59Z");

        List<LoyaltyCustomerReportResponse> mockData = List.of(
                new LoyaltyCustomerReportResponse(10L, "Nguyễn Văn A", "a@gmail.com", "Vàng", 5L, BigDecimal.valueOf(50000), 500)
        );

        when(reportService.getLoyaltyCustomerReport(eq(start), eq(end))).thenReturn(mockData);

        mockMvc.perform(get("/api/admin/reports/loyalty")
                        .param("startDate", "2026-06-01T00:00:00Z")
                        .param("endDate", "2026-06-30T23:59:59Z")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Lấy báo cáo khách hàng thân thiết thành công"))
                .andExpect(jsonPath("$.data[0].userId").value(10))
                .andExpect(jsonPath("$.data[0].fullName").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.data[0].membershipTier").value("Vàng"));

        verify(reportService, times(1)).getLoyaltyCustomerReport(eq(start), eq(end));
    }

    @Test
    void exportLoyaltyReport_success() throws Exception {
        OffsetDateTime start = OffsetDateTime.parse("2026-06-01T00:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2026-06-30T23:59:59Z");

        doAnswer(invocation -> {
            Writer writer = invocation.getArgument(0);
            writer.write("\uFEFFMã KH,Họ tên\n10,Nguyễn Văn A\n");
            return null;
        }).when(reportService).exportLoyaltyCustomerReport(any(Writer.class), eq(start), eq(end));

        mockMvc.perform(get("/api/admin/reports/loyalty/export")
                        .param("startDate", "2026-06-01T00:00:00Z")
                        .param("endDate", "2026-06-30T23:59:59Z"))
                .andExpect(status().isOk());

        verify(reportService, times(1)).exportLoyaltyCustomerReport(any(Writer.class), eq(start), eq(end));
    }
}
