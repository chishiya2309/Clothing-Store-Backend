package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReportDtoTest {

    @Test
    void testRevenueReportResponse_compactConstructor_defaultsNulls() {
        // Construct with nulls
        LocalDate date = LocalDate.now();
        RevenueReportResponse response = new RevenueReportResponse(
                date, null, null, null, null, null, null
        );

        // Assert defaults
        assertEquals(date, response.date());
        assertEquals(0L, response.totalOrders());
        assertEquals(0L, response.completedOrders());
        assertEquals(0L, response.cancelledOrders());
        assertEquals(BigDecimal.ZERO, response.totalRevenue());
        assertEquals(BigDecimal.ZERO, response.totalDiscounts());
        assertEquals(BigDecimal.ZERO, response.netRevenue());
    }

    @Test
    void testBestsellerReportResponse_compactConstructor_defaultsNulls() {
        // Construct with nulls
        BestsellerReportResponse response = new BestsellerReportResponse(
                1L, "Test Product", null, null, null
        );

        // Assert defaults
        assertEquals(1L, response.productId());
        assertEquals("Test Product", response.productName());
        assertEquals("Không rõ", response.categoryName());
        assertEquals(0L, response.totalQuantitySold());
        assertEquals(BigDecimal.ZERO, response.totalRevenue());
    }

    @Test
    void testLoyaltyCustomerReportResponse_compactConstructor_defaultsNulls() {
        // Construct with nulls
        LoyaltyCustomerReportResponse response = new LoyaltyCustomerReportResponse(
                1L, "Customer A", "a@test.com", null, null, null, null
        );

        // Assert defaults
        assertEquals(1L, response.userId());
        assertEquals("Customer A", response.fullName());
        assertEquals("a@test.com", response.email());
        assertEquals("Thành viên", response.membershipTier());
        assertEquals(0L, response.totalOrders());
        assertEquals(BigDecimal.ZERO, response.totalSpent());
        assertEquals(0, response.loyaltyPoints());
    }
}
