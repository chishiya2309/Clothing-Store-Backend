package vn.hcmute.edu.dp.nhom10.backend.controller.staff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.InventoryReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.enums.InventoryReportStatus;
import vn.hcmute.edu.dp.nhom10.backend.exception.GlobalExceptionHandling;
import vn.hcmute.edu.dp.nhom10.backend.service.InventoryReportService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StaffInventoryReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InventoryReportService inventoryReportService;

    @InjectMocks
    private StaffInventoryReportController staffInventoryReportController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(staffInventoryReportController)
                .setControllerAdvice(new GlobalExceptionHandling())
                .build();
    }

    @Test
    void getInventoryReport_success_returnsApiResponseAndPassesParams() throws Exception {
        PageResponse<InventoryReportResponse> report = PageResponse.<InventoryReportResponse>builder()
                .pageNumber(0)
                .pageSize(20)
                .totalElements(1)
                .totalPages(1)
                .content(List.of(new InventoryReportResponse(
                        "SP001",
                        "Áo Polo Nam Cotton",
                        "L / Trắng",
                        5,
                        InventoryReportStatus.LOW_STOCK,
                        "Sắp hết",
                        3L,
                        "Áo Polo",
                        10L,
                        "SKU-10"
                )))
                .build();

        when(inventoryReportService.getInventoryReport(
                eq(InventoryReportStatus.LOW_STOCK),
                eq(3L),
                eq("polo"),
                eq(0),
                eq(20),
                eq("stockAsc")
        )).thenReturn(report);

        mockMvc.perform(get("/api/staff/reports/inventory")
                        .param("status", "LOW_STOCK")
                        .param("categoryId", "3")
                        .param("keyword", "polo")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sortBy", "stockAsc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Lấy báo cáo tồn kho thành công"))
                .andExpect(jsonPath("$.data.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(20))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.content[0].productCode").value("SP001"))
                .andExpect(jsonPath("$.data.content[0].productName").value("Áo Polo Nam Cotton"))
                .andExpect(jsonPath("$.data.content[0].variantInfo").value("L / Trắng"))
                .andExpect(jsonPath("$.data.content[0].stockQuantity").value(5))
                .andExpect(jsonPath("$.data.content[0].status").value("LOW_STOCK"))
                .andExpect(jsonPath("$.data.content[0].statusLabel").value("Sắp hết"));

        verify(inventoryReportService).getInventoryReport(
                InventoryReportStatus.LOW_STOCK,
                3L,
                "polo",
                0,
                20,
                "stockAsc"
        );
    }

    @Test
    void getInventoryReport_defaultsOptionalParams() throws Exception {
        PageResponse<InventoryReportResponse> report = PageResponse.<InventoryReportResponse>builder()
                .pageNumber(0)
                .pageSize(20)
                .totalElements(0)
                .totalPages(0)
                .content(List.of())
                .build();
        when(inventoryReportService.getInventoryReport(isNull(), isNull(), isNull(), eq(0), eq(20), eq("stockAsc")))
                .thenReturn(report);

        mockMvc.perform(get("/api/staff/reports/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(20))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isEmpty());

        verify(inventoryReportService).getInventoryReport(null, null, null, 0, 20, "stockAsc");
    }

    @Test
    void getInventoryReport_invalidStatus_returns400() throws Exception {
        mockMvc.perform(get("/api/staff/reports/inventory")
                        .param("status", "ABC"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getInventoryReport_invalidPageFromService_returns400() throws Exception {
        when(inventoryReportService.getInventoryReport(any(), any(), any(), eq(-1), anyInt(), anyString()))
                .thenThrow(new IllegalArgumentException("page must be greater than or equal to 0"));

        mockMvc.perform(get("/api/staff/reports/inventory")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getInventoryReport_invalidSizeFromService_returns400() throws Exception {
        when(inventoryReportService.getInventoryReport(any(), any(), any(), anyInt(), eq(101), anyString()))
                .thenThrow(new IllegalArgumentException("size must be between 1 and 100"));

        mockMvc.perform(get("/api/staff/reports/inventory")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getInventoryReport_invalidSortByFromService_returns400() throws Exception {
        when(inventoryReportService.getInventoryReport(any(), any(), any(), anyInt(), anyInt(), eq("stockQuantity")))
                .thenThrow(new IllegalArgumentException("Unsupported sortBy: stockQuantity"));

        mockMvc.perform(get("/api/staff/reports/inventory")
                        .param("sortBy", "stockQuantity"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getInventoryReport_passesAllParamsToService() throws Exception {
        PageResponse<InventoryReportResponse> report = PageResponse.<InventoryReportResponse>builder()
                .pageNumber(2)
                .pageSize(50)
                .totalElements(0)
                .totalPages(0)
                .content(List.of())
                .build();
        when(inventoryReportService.getInventoryReport(any(), any(), any(), anyInt(), anyInt(), anyString()))
                .thenReturn(report);

        mockMvc.perform(get("/api/staff/reports/inventory")
                        .param("status", "IN_STOCK")
                        .param("categoryId", "7")
                        .param("keyword", "ao")
                        .param("page", "2")
                        .param("size", "50")
                        .param("sortBy", "skuAsc"))
                .andExpect(status().isOk());

        ArgumentCaptor<InventoryReportStatus> statusCaptor = ArgumentCaptor.forClass(InventoryReportStatus.class);
        ArgumentCaptor<Long> categoryCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> keywordCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> pageCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> sizeCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> sortCaptor = ArgumentCaptor.forClass(String.class);

        verify(inventoryReportService).getInventoryReport(
                statusCaptor.capture(),
                categoryCaptor.capture(),
                keywordCaptor.capture(),
                pageCaptor.capture(),
                sizeCaptor.capture(),
                sortCaptor.capture()
        );

        assertEquals(InventoryReportStatus.IN_STOCK, statusCaptor.getValue());
        assertEquals(7L, categoryCaptor.getValue());
        assertEquals("ao", keywordCaptor.getValue());
        assertEquals(2, pageCaptor.getValue());
        assertEquals(50, sizeCaptor.getValue());
        assertEquals("skuAsc", sortCaptor.getValue());
    }

    @Test
    void controller_isRestrictedToStaffRole() {
        PreAuthorize preAuthorize = StaffInventoryReportController.class.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertEquals("hasRole('STAFF')", preAuthorize.value());
    }
}
