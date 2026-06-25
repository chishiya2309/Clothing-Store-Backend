package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.RevenueReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.BestsellerReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.LoyaltyCustomerReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.AdminReportServiceImpl;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private AdminReportServiceImpl reportService;

    @Test
    void getRevenueReport_returnsDataSuccessfully() {
        OffsetDateTime start = OffsetDateTime.now().minusDays(7);
        OffsetDateTime end = OffsetDateTime.now();
        List<RevenueReportResponse> mockData = List.of(
                new RevenueReportResponse(new Date(), 10L, 8L, 2L, BigDecimal.valueOf(1000), BigDecimal.valueOf(100), BigDecimal.valueOf(900))
        );

        when(orderRepository.findRevenueReport(start, end, OrderStatus.completed, OrderStatus.cancelled)).thenReturn(mockData);

        List<RevenueReportResponse> result = reportService.getRevenueReport(start, end);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).totalOrders());
        assertEquals(BigDecimal.valueOf(900), result.get(0).netRevenue());
        verify(orderRepository, times(1)).findRevenueReport(start, end, OrderStatus.completed, OrderStatus.cancelled);
    }

    @Test
    void exportRevenueReport_writesCsvSuccessfully() throws Exception {
        OffsetDateTime start = OffsetDateTime.now().minusDays(7);
        OffsetDateTime end = OffsetDateTime.now();
        List<RevenueReportResponse> mockData = List.of(
                new RevenueReportResponse(new Date(), 5L, 4L, 1L, BigDecimal.valueOf(500), BigDecimal.valueOf(50), BigDecimal.valueOf(450))
        );

        when(orderRepository.findRevenueReport(start, end, OrderStatus.completed, OrderStatus.cancelled)).thenReturn(mockData);

        StringWriter writer = new StringWriter();
        reportService.exportRevenueReport(writer, start, end);

        String csvOutput = writer.toString();
        assertNotNull(csvOutput);
        assertTrue(csvOutput.startsWith("\uFEFF")); // Check UTF-8 BOM
        assertTrue(csvOutput.contains("Ngày,Tổng số đơn hàng,Đơn hoàn thành,Đơn hủy,Doanh thu tổng (VNĐ)"));
        assertTrue(csvOutput.contains("5,4,1"));
    }

    @Test
    void getBestsellerReport_returnsDataSuccessfully() {
        OffsetDateTime start = OffsetDateTime.now().minusDays(7);
        OffsetDateTime end = OffsetDateTime.now();
        List<BestsellerReportResponse> mockData = List.of(
                new BestsellerReportResponse(1L, "Áo Thun Polo", "Nam > Polo", 50L, BigDecimal.valueOf(5000))
        );

        when(orderRepository.findBestsellingProducts(start, end, OrderStatus.completed)).thenReturn(mockData);

        List<BestsellerReportResponse> result = reportService.getBestsellerReport(start, end);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Áo Thun Polo", result.get(0).productName());
        verify(orderRepository, times(1)).findBestsellingProducts(start, end, OrderStatus.completed);
    }

    @Test
    void exportBestsellerReport_writesCsvSuccessfully() throws Exception {
        OffsetDateTime start = OffsetDateTime.now().minusDays(7);
        OffsetDateTime end = OffsetDateTime.now();
        List<BestsellerReportResponse> mockData = List.of(
                new BestsellerReportResponse(1L, "Áo Thun Polo", "Nam > Polo", 50L, BigDecimal.valueOf(5000))
        );

        when(orderRepository.findBestsellingProducts(start, end, OrderStatus.completed)).thenReturn(mockData);

        StringWriter writer = new StringWriter();
        reportService.exportBestsellerReport(writer, start, end);

        String csvOutput = writer.toString();
        assertNotNull(csvOutput);
        assertTrue(csvOutput.startsWith("\uFEFF"));
        assertTrue(csvOutput.contains("Hạng,Mã SP,Tên sản phẩm,Danh mục,Số lượng bán,Doanh thu (VNĐ)"));
        assertTrue(csvOutput.contains("1,1,Áo Thun Polo,Nam > Polo,50,5000"));
    }

    @Test
    void getLoyaltyCustomerReport_returnsDataSuccessfully() {
        OffsetDateTime start = OffsetDateTime.now().minusDays(7);
        OffsetDateTime end = OffsetDateTime.now();
        List<LoyaltyCustomerReportResponse> mockData = List.of(
                new LoyaltyCustomerReportResponse(100L, "Nguyễn Văn A", "a@gmail.com", "Vàng", 5L, BigDecimal.valueOf(5000), 500)
        );

        when(orderRepository.findLoyaltyCustomers(start, end, OrderStatus.completed)).thenReturn(mockData);

        List<LoyaltyCustomerReportResponse> result = reportService.getLoyaltyCustomerReport(start, end);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Nguyễn Văn A", result.get(0).fullName());
        verify(orderRepository, times(1)).findLoyaltyCustomers(start, end, OrderStatus.completed);
    }

    @Test
    void exportLoyaltyCustomerReport_writesCsvSuccessfully() throws Exception {
        OffsetDateTime start = OffsetDateTime.now().minusDays(7);
        OffsetDateTime end = OffsetDateTime.now();
        List<LoyaltyCustomerReportResponse> mockData = List.of(
                new LoyaltyCustomerReportResponse(100L, "Nguyễn Văn A", "a@gmail.com", "Vàng", 5L, BigDecimal.valueOf(5000), 500)
        );

        when(orderRepository.findLoyaltyCustomers(start, end, OrderStatus.completed)).thenReturn(mockData);

        StringWriter writer = new StringWriter();
        reportService.exportLoyaltyCustomerReport(writer, start, end);

        String csvOutput = writer.toString();
        assertNotNull(csvOutput);
        assertTrue(csvOutput.startsWith("\uFEFF"));
        assertTrue(csvOutput.contains("Mã KH,Họ tên,Email,Hạng,Tổng số đơn hàng,Tổng chi tiêu (VNĐ),Điểm tích lũy hiện tại"));
        assertTrue(csvOutput.contains("100,Nguyễn Văn A,a@gmail.com,Vàng,5,5000,500"));
    }
}
