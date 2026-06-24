package vn.hcmute.edu.dp.nhom10.backend.controller.staff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCancelOrderRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCompleteOrderRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffOrderDetailResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffOrderListItemResponse;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderCompletionSource;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentStatus;
import vn.hcmute.edu.dp.nhom10.backend.exception.GlobalExceptionHandling;
import vn.hcmute.edu.dp.nhom10.backend.exception.OrderStateConflictException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.security.AuthenticatedUserProvider;
import vn.hcmute.edu.dp.nhom10.backend.service.StaffOrderService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StaffOrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StaffOrderService staffOrderService;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

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

    @Test
    void confirmOrder_withoutRequestBody_returnsDetailResponse() throws Exception {
        Authentication authentication = authentication();
        when(authenticatedUserProvider.getCurrentUserId(authentication)).thenReturn(5L);
        when(staffOrderService.confirmOrder("ORD-1", 5L)).thenReturn(StaffOrderDetailResponse.builder()
                .orderCode("ORD-1")
                .status(OrderStatus.processing)
                .items(List.of())
                .timeline(List.of())
                .build());

        mockMvc.perform(patch("/api/staff/orders/ORD-1/confirm")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Confirm order successful"))
                .andExpect(jsonPath("$.data.orderCode").value("ORD-1"))
                .andExpect(jsonPath("$.data.status").value("processing"));

        verify(authenticatedUserProvider).getCurrentUserId(authentication);
        verify(staffOrderService).confirmOrder("ORD-1", 5L);
    }

    @Test
    void shipOrder_withoutRequestBody_returnsDetailResponse() throws Exception {
        Authentication authentication = authentication();
        when(authenticatedUserProvider.getCurrentUserId(authentication)).thenReturn(5L);
        when(staffOrderService.shipOrder("ORD-1", 5L)).thenReturn(StaffOrderDetailResponse.builder()
                .orderCode("ORD-1")
                .status(OrderStatus.shipping)
                .items(List.of())
                .timeline(List.of())
                .build());

        mockMvc.perform(patch("/api/staff/orders/ORD-1/ship")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Ship order successful"))
                .andExpect(jsonPath("$.data.orderCode").value("ORD-1"))
                .andExpect(jsonPath("$.data.status").value("shipping"));

        verify(authenticatedUserProvider).getCurrentUserId(authentication);
        verify(staffOrderService).shipOrder("ORD-1", 5L);
    }

    @Test
    void confirmOrder_unknownOrder_returnsNotFound() throws Exception {
        Authentication authentication = authentication();
        when(authenticatedUserProvider.getCurrentUserId(authentication)).thenReturn(5L);
        when(staffOrderService.confirmOrder("ORD-404", 5L))
                .thenThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(patch("/api/staff/orders/ORD-404/confirm")
                        .principal(authentication))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shipOrder_conflict_returnsConflict() throws Exception {
        Authentication authentication = authentication();
        when(authenticatedUserProvider.getCurrentUserId(authentication)).thenReturn(5L);
        when(staffOrderService.shipOrder("ORD-1", 5L))
                .thenThrow(new OrderStateConflictException("Không thể chuyển từ trạng thái pending sang shipping"));

        mockMvc.perform(patch("/api/staff/orders/ORD-1/ship")
                        .principal(authentication))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Không thể chuyển từ trạng thái pending sang shipping"));
    }

    @Test
    void completeOrder_withValidRequest_returnsDetailResponseAndPassesRequest() throws Exception {
        Authentication authentication = authentication();
        when(authenticatedUserProvider.getCurrentUserId(authentication)).thenReturn(5L);
        when(staffOrderService.completeOrder(eq("ORD-1"), eq(5L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(StaffOrderDetailResponse.builder()
                        .orderCode("ORD-1")
                        .status(OrderStatus.completed)
                        .items(List.of())
                        .timeline(List.of())
                        .build());

        mockMvc.perform(patch("/api/staff/orders/ORD-1/complete")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "confirmationSource": "shipping_partner",
                                  "note": "GHN confirmed"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Complete order successful"))
                .andExpect(jsonPath("$.data.orderCode").value("ORD-1"))
                .andExpect(jsonPath("$.data.status").value("completed"));

        ArgumentCaptor<StaffCompleteOrderRequest> requestCaptor =
                ArgumentCaptor.forClass(StaffCompleteOrderRequest.class);
        verify(authenticatedUserProvider).getCurrentUserId(authentication);
        verify(staffOrderService).completeOrder(eq("ORD-1"), eq(5L), requestCaptor.capture());
        assertEquals(OrderCompletionSource.shipping_partner, requestCaptor.getValue().confirmationSource());
        assertEquals("GHN confirmed", requestCaptor.getValue().note());
    }

    @Test
    void completeOrder_conflict_returnsConflict() throws Exception {
        Authentication authentication = authentication();
        when(authenticatedUserProvider.getCurrentUserId(authentication)).thenReturn(5L);
        when(staffOrderService.completeOrder(eq("ORD-1"), eq(5L), org.mockito.ArgumentMatchers.any()))
                .thenThrow(new OrderStateConflictException("KhÃ´ng thá»ƒ chuyá»ƒn tá»« tráº¡ng thÃ¡i pending sang completed"));

        mockMvc.perform(patch("/api/staff/orders/ORD-1/complete")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "confirmationSource": "shipping_partner",
                                  "note": "GHN confirmed"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void completeOrder_unknownOrder_returnsNotFound() throws Exception {
        Authentication authentication = authentication();
        when(authenticatedUserProvider.getCurrentUserId(authentication)).thenReturn(5L);
        when(staffOrderService.completeOrder(eq("ORD-404"), eq(5L), org.mockito.ArgumentMatchers.any()))
                .thenThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(patch("/api/staff/orders/ORD-404/complete")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "confirmationSource": "shipping_partner",
                                  "note": "GHN confirmed"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void completeOrder_nullConfirmationSource_returnsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/staff/orders/ORD-1/complete")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "confirmationSource": null,
                                  "note": "GHN confirmed"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void completeOrder_invalidConfirmationSource_returnsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/staff/orders/ORD-1/complete")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "confirmationSource": "unknown",
                                  "note": "GHN confirmed"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completeOrder_nullNote_returnsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/staff/orders/ORD-1/complete")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "confirmationSource": "shipping_partner",
                                  "note": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void completeOrder_blankNote_returnsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/staff/orders/ORD-1/complete")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "confirmationSource": "shipping_partner",
                                  "note": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void completeOrder_noteTooLong_returnsBadRequest() throws Exception {
        String longNote = "a".repeat(501);

        mockMvc.perform(patch("/api/staff/orders/ORD-1/complete")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "confirmationSource": "shipping_partner",
                                  "note": "%s"
                                }
                                """.formatted(longNote)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void allCompletionSources_parseSuccessfully() throws Exception {
        Authentication authentication = authentication();
        when(authenticatedUserProvider.getCurrentUserId(authentication)).thenReturn(5L);
        when(staffOrderService.completeOrder(eq("ORD-1"), eq(5L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(StaffOrderDetailResponse.builder()
                        .orderCode("ORD-1")
                        .status(OrderStatus.completed)
                        .items(List.of())
                        .timeline(List.of())
                        .build());

        for (OrderCompletionSource source : OrderCompletionSource.values()) {
            mockMvc.perform(patch("/api/staff/orders/ORD-1/complete")
                            .principal(authentication)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "confirmationSource": "%s",
                                      "note": "Confirmed"
                                    }
                                    """.formatted(source.name())))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void cancelOrder_withValidRequest_returnsDetailResponseAndPassesRequest() throws Exception {
        Authentication authentication = authentication();
        when(authenticatedUserProvider.getCurrentUserId(authentication)).thenReturn(5L);
        when(staffOrderService.cancelOrder(eq("ORD-1"), eq(5L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(StaffOrderDetailResponse.builder()
                        .orderCode("ORD-1")
                        .status(OrderStatus.cancelled)
                        .items(List.of())
                        .timeline(List.of())
                        .build());

        mockMvc.perform(patch("/api/staff/orders/ORD-1/cancel")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "Customer requested cancellation"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Cancel order successful"))
                .andExpect(jsonPath("$.data.orderCode").value("ORD-1"))
                .andExpect(jsonPath("$.data.status").value("cancelled"));

        ArgumentCaptor<StaffCancelOrderRequest> requestCaptor =
                ArgumentCaptor.forClass(StaffCancelOrderRequest.class);
        verify(authenticatedUserProvider).getCurrentUserId(authentication);
        verify(staffOrderService).cancelOrder(eq("ORD-1"), eq(5L), requestCaptor.capture());
        assertEquals("Customer requested cancellation", requestCaptor.getValue().reason());
    }

    @Test
    void cancelOrder_conflict_returnsConflict() throws Exception {
        Authentication authentication = authentication();
        when(authenticatedUserProvider.getCurrentUserId(authentication)).thenReturn(5L);
        when(staffOrderService.cancelOrder(eq("ORD-1"), eq(5L), org.mockito.ArgumentMatchers.any()))
                .thenThrow(new OrderStateConflictException("Cannot cancel shipped order"));

        mockMvc.perform(patch("/api/staff/orders/ORD-1/cancel")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "Customer requested cancellation"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void cancelOrder_blankReason_returnsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/staff/orders/ORD-1/cancel")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void cancelOrder_reasonTooLong_returnsBadRequest() throws Exception {
        String longReason = "a".repeat(501);

        mockMvc.perform(patch("/api/staff/orders/ORD-1/cancel")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "%s"
                                }
                                """.formatted(longReason)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void patchStatusEndpoint_doesNotExist() throws Exception {
        mockMvc.perform(patch("/api/staff/orders/ORD-1/status")
                        .principal(authentication()))
                .andExpect(status().isNotFound());
    }

    private Authentication authentication() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("staff@test.com", null);
        authentication.setAuthenticated(true);
        return authentication;
    }
}
