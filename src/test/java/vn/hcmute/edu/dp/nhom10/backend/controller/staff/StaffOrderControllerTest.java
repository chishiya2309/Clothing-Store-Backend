package vn.hcmute.edu.dp.nhom10.backend.controller.staff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffOrderDetailResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffOrderListItemResponse;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentStatus;
import vn.hcmute.edu.dp.nhom10.backend.exception.GlobalExceptionHandling;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.service.StaffOrderService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StaffOrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StaffOrderService staffOrderService;

    @InjectMocks
    private StaffOrderController staffOrderController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(staffOrderController)
                .setControllerAdvice(new GlobalExceptionHandling())
                .build();
    }

    @Test
    void getOrders_returnsPageResponse() throws Exception {
        PageResponse<StaffOrderListItemResponse> response = PageResponse.<StaffOrderListItemResponse>builder()
                .pageNumber(0)
                .pageSize(10)
                .totalPages(1)
                .totalElements(1)
                .content(List.of(StaffOrderListItemResponse.builder()
                        .orderCode("ORD-1")
                        .customerName("Alice")
                        .customerEmail("alice@test.com")
                        .customerPhone("0909000001")
                        .createdAt(OffsetDateTime.parse("2026-01-10T09:00:00+07:00"))
                        .totalAmount(new BigDecimal("250000.00"))
                        .status(OrderStatus.pending)
                        .paymentMethod(PaymentMethod.cod)
                        .paymentStatus(PaymentStatus.completed)
                        .build()))
                .build();
        when(staffOrderService.getOrders(
                eq(OrderStatus.pending),
                eq(LocalDate.of(2026, 1, 1)),
                eq(LocalDate.of(2026, 1, 31)),
                eq("Alice"),
                eq(0),
                eq(10),
                eq("createdAt"),
                eq("desc")
        )).thenReturn(response);

        mockMvc.perform(get("/api/staff/orders")
                        .param("status", "pending")
                        .param("fromDate", "2026-01-01")
                        .param("toDate", "2026-01-31")
                        .param("keyword", "Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Fetch staff order list successful"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].orderCode").value("ORD-1"))
                .andExpect(jsonPath("$.data.content[0].paymentMethod").value("cod"));

        verify(staffOrderService).getOrders(
                OrderStatus.pending,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                "Alice",
                0,
                10,
                "createdAt",
                "desc"
        );
    }

    @Test
    void getOrders_serviceValidationFailure_returnsBadRequest() throws Exception {
        when(staffOrderService.getOrders(null, null, null, null, 0, 101, "createdAt", "desc"))
                .thenThrow(new IllegalArgumentException("Size must not exceed 100"));

        mockMvc.perform(get("/api/staff/orders")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getOrderDetail_returnsDetailResponse() throws Exception {
        when(staffOrderService.getOrderDetail("ORD-1")).thenReturn(StaffOrderDetailResponse.builder()
                .orderCode("ORD-1")
                .status(OrderStatus.pending)
                .customerName("Alice")
                .totalAmount(new BigDecimal("250000.00"))
                .items(List.of())
                .timeline(List.of())
                .build());

        mockMvc.perform(get("/api/staff/orders/ORD-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Fetch staff order detail successful"))
                .andExpect(jsonPath("$.data.orderCode").value("ORD-1"))
                .andExpect(jsonPath("$.data.status").value("pending"));
    }

    @Test
    void getOrderDetail_unknownOrder_returnsNotFound() throws Exception {
        when(staffOrderService.getOrderDetail("ORD-404"))
                .thenThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(get("/api/staff/orders/ORD-404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
