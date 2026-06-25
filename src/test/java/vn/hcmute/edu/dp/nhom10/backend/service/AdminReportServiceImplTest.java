package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.dto.projection.BestsellerReportProjection;
import vn.hcmute.edu.dp.nhom10.backend.dto.projection.LoyaltyCustomerReportProjection;
import vn.hcmute.edu.dp.nhom10.backend.dto.projection.RevenueReportProjection;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.BestsellerReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.LoyaltyCustomerReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.RevenueReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.AdminReportServiceImpl;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private AdminReportServiceImpl reportService;

    // ─── Revenue Report ───────────────────────────────────────────────────────

    @Test
    void getRevenueReport_returnsDataSuccessfully() {
        OffsetDateTime start = OffsetDateTime.now().minusDays(7);
        OffsetDateTime end   = OffsetDateTime.now();

        RevenueReportProjection proj = mockRevenueProjection(LocalDate.now(), 10L, 8L, 2L,
                BigDecimal.valueOf(1000), BigDecimal.valueOf(100), BigDecimal.valueOf(900));

        when(orderRepository.findRevenueReport(start, end)).thenReturn(List.of(proj));

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
        OffsetDateTime end   = OffsetDateTime.now();

        RevenueReportProjection proj = mockRevenueProjection(LocalDate.now(), 5L, 4L, 1L,
                BigDecimal.valueOf(500), BigDecimal.valueOf(50), BigDecimal.valueOf(450));

        when(orderRepository.findRevenueReport(start, end)).thenReturn(List.of(proj));

        StringWriter writer = new StringWriter();
        reportService.exportRevenueReport(writer, start, end);

        String csv = writer.toString();
        assertNotNull(csv);
        assertTrue(csv.startsWith("\uFEFF")); // UTF-8 BOM
        assertTrue(csv.contains("Ng\u00e0y,T\u1ed5ng s\u1ed1 \u0111\u01a1n h\u00e0ng,\u0110\u01a1n ho\u00e0n th\u00e0nh,\u0110\u01a1n h\u1ee7y,Doanh thu t\u1ed5ng (VN\u0110)"));
        assertTrue(csv.contains("5,4,1"));
    }

    // ─── Bestseller Report ────────────────────────────────────────────────────

    @Test
    void getBestsellerReport_returnsDataSuccessfully() {
        OffsetDateTime start = OffsetDateTime.now().minusDays(7);
        OffsetDateTime end   = OffsetDateTime.now();

        BestsellerReportProjection proj = mockBestsellerProjection(1L, "\u00c1o Thun Polo", "Nam > Polo", 50L, BigDecimal.valueOf(5000));

        when(orderRepository.findBestsellingProducts(start, end)).thenReturn(List.of(proj));

        List<BestsellerReportResponse> result = reportService.getBestsellerReport(start, end);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("\u00c1o Thun Polo", result.get(0).productName());
        verify(orderRepository, times(1)).findBestsellingProducts(start, end);
    }

    @Test
    void exportBestsellerReport_writesCsvSuccessfully() throws Exception {
        OffsetDateTime start = OffsetDateTime.now().minusDays(7);
        OffsetDateTime end   = OffsetDateTime.now();

        BestsellerReportProjection proj = mockBestsellerProjection(1L, "\u00c1o Thun Polo", "Nam > Polo", 50L, BigDecimal.valueOf(5000));

        when(orderRepository.findBestsellingProducts(start, end)).thenReturn(List.of(proj));

        StringWriter writer = new StringWriter();
        reportService.exportBestsellerReport(writer, start, end);

        String csv = writer.toString();
        assertNotNull(csv);
        assertTrue(csv.startsWith("\uFEFF"));
        assertTrue(csv.contains("H\u1ea1ng,M\u00e3 SP,T\u00ean s\u1ea3n ph\u1ea9m,Danh m\u1ee5c,S\u1ed1 l\u01b0\u1ee3ng b\u00e1n,Doanh thu (VN\u0110)"));
        assertTrue(csv.contains("1,1,\u00c1o Thun Polo,Nam > Polo,50,5000"));
    }

    // ─── Loyalty Customer Report ──────────────────────────────────────────────

    @Test
    void getLoyaltyCustomerReport_returnsDataSuccessfully() {
        OffsetDateTime start = OffsetDateTime.now().minusDays(7);
        OffsetDateTime end   = OffsetDateTime.now();

        LoyaltyCustomerReportProjection proj = mockLoyaltyProjection(100L, "Nguy\u1ec5n V\u0103n A", "a@gmail.com", "V\u00e0ng", 5L, BigDecimal.valueOf(5000), 500);

        when(orderRepository.findLoyaltyCustomers(start, end)).thenReturn(List.of(proj));

        List<LoyaltyCustomerReportResponse> result = reportService.getLoyaltyCustomerReport(start, end);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Nguy\u1ec5n V\u0103n A", result.get(0).fullName());
        verify(orderRepository, times(1)).findLoyaltyCustomers(start, end);
    }

    @Test
    void exportLoyaltyCustomerReport_writesCsvSuccessfully() throws Exception {
        OffsetDateTime start = OffsetDateTime.now().minusDays(7);
        OffsetDateTime end   = OffsetDateTime.now();

        LoyaltyCustomerReportProjection proj = mockLoyaltyProjection(100L, "Nguy\u1ec5n V\u0103n A", "a@gmail.com", "V\u00e0ng", 5L, BigDecimal.valueOf(5000), 500);

        when(orderRepository.findLoyaltyCustomers(start, end)).thenReturn(List.of(proj));

        StringWriter writer = new StringWriter();
        reportService.exportLoyaltyCustomerReport(writer, start, end);

        String csv = writer.toString();
        assertNotNull(csv);
        assertTrue(csv.startsWith("\uFEFF"));
        assertTrue(csv.contains("M\u00e3 KH,H\u1ecd t\u00ean,Email,H\u1ea1ng,T\u1ed5ng s\u1ed1 \u0111\u01a1n h\u00e0ng,T\u1ed5ng chi ti\u00eau (VN\u0110),\u0110i\u1ec3m t\u00edch l\u0169y hi\u1ec7n t\u1ea1i"));
        assertTrue(csv.contains("100,Nguy\u1ec5n V\u0103n A,a@gmail.com,V\u00e0ng,5,5000,500"));
    }

    // ─── Projection mock helpers ──────────────────────────────────────────────

    private RevenueReportProjection mockRevenueProjection(LocalDate date, Long totalOrders,
            Long completedOrders, Long cancelledOrders,
            BigDecimal totalRevenue, BigDecimal totalDiscounts, BigDecimal netRevenue) {
        RevenueReportProjection m = mock(RevenueReportProjection.class);
        when(m.getDate()).thenReturn(date);
        when(m.getTotalOrders()).thenReturn(totalOrders);
        when(m.getCompletedOrders()).thenReturn(completedOrders);
        when(m.getCancelledOrders()).thenReturn(cancelledOrders);
        when(m.getTotalRevenue()).thenReturn(totalRevenue);
        when(m.getTotalDiscounts()).thenReturn(totalDiscounts);
        when(m.getNetRevenue()).thenReturn(netRevenue);
        return m;
    }

    private BestsellerReportProjection mockBestsellerProjection(Long productId, String productName,
            String categoryName, Long totalQuantitySold, BigDecimal totalRevenue) {
        BestsellerReportProjection m = mock(BestsellerReportProjection.class);
        when(m.getProductId()).thenReturn(productId);
        when(m.getProductName()).thenReturn(productName);
        when(m.getCategoryName()).thenReturn(categoryName);
        when(m.getTotalQuantitySold()).thenReturn(totalQuantitySold);
        when(m.getTotalRevenue()).thenReturn(totalRevenue);
        return m;
    }

    private LoyaltyCustomerReportProjection mockLoyaltyProjection(Long userId, String fullName,
            String email, String membershipTier, Long totalOrders,
            BigDecimal totalSpent, Integer loyaltyPoints) {
        LoyaltyCustomerReportProjection m = mock(LoyaltyCustomerReportProjection.class);
        when(m.getUserId()).thenReturn(userId);
        when(m.getFullName()).thenReturn(fullName);
        when(m.getEmail()).thenReturn(email);
        when(m.getMembershipTier()).thenReturn(membershipTier);
        when(m.getTotalOrders()).thenReturn(totalOrders);
        when(m.getTotalSpent()).thenReturn(totalSpent);
        when(m.getLoyaltyPoints()).thenReturn(loyaltyPoints);
        return m;
    }
}
