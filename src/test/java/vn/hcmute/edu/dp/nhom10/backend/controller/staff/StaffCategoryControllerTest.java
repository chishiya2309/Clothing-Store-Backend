package vn.hcmute.edu.dp.nhom10.backend.controller.staff;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCategoryRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffCategoryResponse;
import vn.hcmute.edu.dp.nhom10.backend.exception.GlobalExceptionHandling;
import vn.hcmute.edu.dp.nhom10.backend.service.StaffCategoryService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StaffCategoryControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private StaffCategoryService staffCategoryService;

    @InjectMocks
    private StaffCategoryController staffCategoryController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(staffCategoryController)
                .setControllerAdvice(new GlobalExceptionHandling())
                .build();
    }

    @Test
    void getHierarchy_shouldReturnHierarchy() throws Exception {
        StaffCategoryResponse root = StaffCategoryResponse.builder()
                .id(1L)
                .name("Root Category")
                .depth(1)
                .children(Collections.emptyList())
                .build();

        when(staffCategoryService.getCategoryHierarchy()).thenReturn(List.of(root));

        mockMvc.perform(get("/api/staff/categories/hierarchy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].name").value("Root Category"));
    }

    @Test
    void createCategory_shouldReturnCreated() throws Exception {
        StaffCategoryRequest request = StaffCategoryRequest.builder()
                .name("New Category")
                .isActive(true)
                .build();

        StaffCategoryResponse response = StaffCategoryResponse.builder()
                .id(10L)
                .name("New Category")
                .build();

        // Stub leniently because stand-alone security might not pass principal
        when(staffCategoryService.createCategory(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/staff/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // standalone wraps in ApiResponse with status 201
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Tạo danh mục thành công"));
    }

    @Test
    void updateCategory_shouldReturnOk() throws Exception {
        StaffCategoryRequest request = StaffCategoryRequest.builder()
                .name("Updated Category")
                .isActive(true)
                .build();

        StaffCategoryResponse response = StaffCategoryResponse.builder()
                .id(1L)
                .name("Updated Category")
                .build();

        when(staffCategoryService.updateCategory(eq(1L), any(), any())).thenReturn(response);

        mockMvc.perform(put("/api/staff/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Cập nhật danh mục thành công"));
    }

    @Test
    void deleteCategory_shouldReturnOk() throws Exception {
        doNothing().when(staffCategoryService).deleteCategory(eq(1L), any());

        mockMvc.perform(delete("/api/staff/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Xóa danh mục thành công"));
    }
}
