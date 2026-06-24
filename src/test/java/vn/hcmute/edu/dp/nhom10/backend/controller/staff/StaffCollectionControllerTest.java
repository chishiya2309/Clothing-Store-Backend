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
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCollectionProductsRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCollectionRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.*;
import vn.hcmute.edu.dp.nhom10.backend.exception.GlobalExceptionHandling;
import vn.hcmute.edu.dp.nhom10.backend.service.StaffCollectionService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StaffCollectionControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private StaffCollectionService staffCollectionService;

    @InjectMocks
    private StaffCollectionController staffCollectionController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(staffCollectionController)
                .setControllerAdvice(new GlobalExceptionHandling())
                .build();
    }

    @Test
    void getCollections_shouldReturnPageResponse() throws Exception {
        StaffCollectionResponse response = StaffCollectionResponse.builder()
                .id(1L)
                .name("Summer")
                .statusState("ACTIVE")
                .build();

        PageResponse<StaffCollectionResponse> pageResponse = PageResponse.<StaffCollectionResponse>builder()
                .pageNumber(0)
                .pageSize(20)
                .totalElements(1)
                .totalPages(1)
                .content(List.of(response))
                .build();

        when(staffCollectionService.getCollections(anyInt(), anyInt(), any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/staff/collections")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content[0].name").value("Summer"));
    }

    @Test
    void getCollectionDetail_shouldReturnDetail() throws Exception {
        StaffCollectionResponse response = StaffCollectionResponse.builder()
                .id(1L)
                .name("Summer")
                .build();

        StaffCollectionDetailResponse detail = StaffCollectionDetailResponse.builder()
                .collection(response)
                .products(Collections.emptyList())
                .build();

        when(staffCollectionService.getCollectionDetail(1L)).thenReturn(detail);

        mockMvc.perform(get("/api/staff/collections/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.collection.name").value("Summer"));
    }

    @Test
    void createCollection_shouldReturnCreated() throws Exception {
        StaffCollectionRequest request = StaffCollectionRequest.builder()
                .name("Autumn")
                .isActive(true)
                .build();

        StaffCollectionResponse response = StaffCollectionResponse.builder()
                .id(10L)
                .name("Autumn")
                .build();

        when(staffCollectionService.createCollection(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/staff/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Tạo bộ sưu tập thành công"));
    }

    @Test
    void updateCollection_shouldReturnOk() throws Exception {
        StaffCollectionRequest request = StaffCollectionRequest.builder()
                .name("Autumn Updated")
                .isActive(true)
                .build();

        StaffCollectionResponse response = StaffCollectionResponse.builder()
                .id(1L)
                .name("Autumn Updated")
                .build();

        when(staffCollectionService.updateCollection(eq(1L), any(), any())).thenReturn(response);

        mockMvc.perform(put("/api/staff/collections/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Cập nhật bộ sưu tập thành công"));
    }

    @Test
    void deleteCollection_shouldReturnOk() throws Exception {
        doNothing().when(staffCollectionService).deleteCollection(eq(1L), any());

        mockMvc.perform(delete("/api/staff/collections/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Xóa bộ sưu tập thành công"));
    }

    @Test
    void addProducts_shouldReturnOk() throws Exception {
        StaffCollectionProductsRequest request = new StaffCollectionProductsRequest(List.of(100L));
        StaffCollectionDetailResponse detail = StaffCollectionDetailResponse.builder()
                .products(Collections.emptyList())
                .build();

        when(staffCollectionService.addProductsToCollection(eq(1L), any(), any())).thenReturn(detail);

        mockMvc.perform(post("/api/staff/collections/1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Thêm sản phẩm vào bộ sưu tập thành công"));
    }

    @Test
    void removeProducts_shouldReturnOk() throws Exception {
        StaffCollectionProductsRequest request = new StaffCollectionProductsRequest(List.of(100L));
        StaffCollectionDetailResponse detail = StaffCollectionDetailResponse.builder()
                .products(Collections.emptyList())
                .build();

        when(staffCollectionService.removeProductsFromCollection(eq(1L), any(), any())).thenReturn(detail);

        mockMvc.perform(delete("/api/staff/collections/1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Xóa sản phẩm khỏi bộ sưu tập thành công"));
    }
}
