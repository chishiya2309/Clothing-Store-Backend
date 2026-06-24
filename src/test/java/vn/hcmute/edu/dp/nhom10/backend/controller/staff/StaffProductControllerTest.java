package vn.hcmute.edu.dp.nhom10.backend.controller.staff;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.*;
import vn.hcmute.edu.dp.nhom10.backend.enums.ImageType;
import vn.hcmute.edu.dp.nhom10.backend.enums.StaffProductStatus;
import vn.hcmute.edu.dp.nhom10.backend.exception.GlobalExceptionHandling;
import vn.hcmute.edu.dp.nhom10.backend.service.StaffProductService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StaffProductControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private StaffProductService staffProductService;

    @InjectMocks
    private StaffProductController staffProductController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(staffProductController)
                .setControllerAdvice(new GlobalExceptionHandling())
                .build();
    }

    @Test
    void getProducts_shouldReturnPageResponse() throws Exception {
        StaffProductListItemResponse item = StaffProductListItemResponse.builder()
                .id(1L)
                .name("T-Shirt")
                .slug("t-shirt")
                .categoryId(2L)
                .categoryName("Shirts")
                .basePrice(BigDecimal.TEN)
                .isActive(true)
                .status(StaffProductStatus.ACTIVE)
                .totalStock(100)
                .build();

        PageResponse<StaffProductListItemResponse> pageResponse = PageResponse.<StaffProductListItemResponse>builder()
                .pageNumber(0)
                .pageSize(20)
                .totalElements(1)
                .totalPages(1)
                .content(List.of(item))
                .build();

        when(staffProductService.getProducts(any(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/staff/products")
                        .param("keyword", "T-Shirt")
                        .param("categoryId", "2")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Lấy danh sách sản phẩm thành công"))
                .andExpect(jsonPath("$.data.content[0].name").value("T-Shirt"));
    }

    @Test
    void getProductDetail_shouldReturnDetail() throws Exception {
        StaffProductDetailResponse detail = StaffProductDetailResponse.builder()
                .id(1L)
                .name("T-Shirt")
                .slug("t-shirt")
                .basePrice(BigDecimal.TEN)
                .categoryName("Shirts")
                .images(Collections.emptyList())
                .variants(Collections.emptyList())
                .build();

        when(staffProductService.getProductDetail(1L)).thenReturn(detail);

        mockMvc.perform(get("/api/staff/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.name").value("T-Shirt"));
    }

    @Test
    void createProduct_shouldReturnCreated() throws Exception {
        StaffProductImageRequest image = new StaffProductImageRequest(null, "http://img", ImageType.main, 1, "alt");
        StaffProductVariantRequest variant = new StaffProductVariantRequest(null, "M", "Red", 10, BigDecimal.ZERO, true);
        StaffCreateProductRequest request = new StaffCreateProductRequest(
                "T-Shirt", "Desc", "Cotton", "instructions", 1L,
                BigDecimal.TEN, BigDecimal.ONE, true, List.of(image), List.of(variant)
        );

        StaffProductDetailResponse detail = StaffProductDetailResponse.builder()
                .id(10L)
                .name("T-Shirt")
                .build();

        when(staffProductService.createProduct(any())).thenReturn(detail);

        mockMvc.perform(post("/api/staff/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Thêm sản phẩm thành công"));
    }

    @Test
    void updateProduct_shouldReturnOk() throws Exception {
        StaffProductImageRequest image = new StaffProductImageRequest(null, "http://img", ImageType.main, 1, "alt");
        StaffProductVariantRequest variant = new StaffProductVariantRequest(null, "M", "Red", 10, BigDecimal.ZERO, true);
        StaffUpdateProductRequest request = new StaffUpdateProductRequest(
                "T-Shirt Updated", "Desc", "Cotton", "instructions", 1L,
                BigDecimal.TEN, BigDecimal.ONE, true, true, List.of(image), List.of(variant)
        );

        StaffProductDetailResponse detail = StaffProductDetailResponse.builder()
                .id(1L)
                .name("T-Shirt Updated")
                .build();

        when(staffProductService.updateProduct(eq(1L), any())).thenReturn(detail);

        mockMvc.perform(put("/api/staff/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật sản phẩm thành công"));
    }

    @Test
    void updateVisibility_shouldReturnOk() throws Exception {
        StaffUpdateProductVisibilityRequest request = new StaffUpdateProductVisibilityRequest(true);
        StaffProductDetailResponse detail = StaffProductDetailResponse.builder()
                .id(1L)
                .isActive(true)
                .build();

        when(staffProductService.updateVisibility(eq(1L), any())).thenReturn(detail);

        mockMvc.perform(patch("/api/staff/products/1/visibility")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật trạng thái hiển thị sản phẩm thành công"));
    }

    @Test
    void deleteProduct_shouldReturnOk() throws Exception {
        doNothing().when(staffProductService).deleteProduct(1L);

        mockMvc.perform(delete("/api/staff/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xóa sản phẩm thành công"));

        verify(staffProductService).deleteProduct(1L);
    }

    @Test
    void updateStock_shouldReturnOk() throws Exception {
        StaffUpdateStockRequest request = new StaffUpdateStockRequest(15);
        StaffStockUpdateResponse response = StaffStockUpdateResponse.builder()
                .productId(1L)
                .variantId(2L)
                .newStockQuantity(15)
                .build();

        when(staffProductService.updateStock(eq(1L), eq(2L), any())).thenReturn(response);

        mockMvc.perform(patch("/api/staff/products/1/variants/2/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật tồn kho thành công"));
    }
}
