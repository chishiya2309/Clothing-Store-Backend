package vn.hcmute.edu.dp.nhom10.backend.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpdateUserRoleRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.AdminUserResponse;
import vn.hcmute.edu.dp.nhom10.backend.enums.GenderType;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;
import vn.hcmute.edu.dp.nhom10.backend.exception.GlobalExceptionHandling;
import vn.hcmute.edu.dp.nhom10.backend.service.AdminUserService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AdminUserService adminUserService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .setControllerAdvice(new GlobalExceptionHandling())
                .setValidator(validator)
                .build();
    }

    @Test
    void getUsers_success() throws Exception {
        AdminUserResponse userResponse = new AdminUserResponse(
                1L, "user@test.com", "Test User", "0123456789", GenderType.male,
                null, null, UserRole.customer, 0, null, null,
                null, true, true, null, null, null
        );
        Page<AdminUserResponse> userPage = new PageImpl<>(Collections.singletonList(userResponse), PageRequest.of(0, 10), 1);

        when(adminUserService.getUsers(anyInt(), anyInt(), any(), any(), any())).thenReturn(userPage);

        mockMvc.perform(get("/api/admin/users")
                .param("page", "0")
                .param("size", "10")
                .param("keyword", "Test")
                .param("role", "customer")
                .param("isActive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Fetch user list successful"))
                .andExpect(jsonPath("$.data.content[0].email").value("user@test.com"))
                .andExpect(jsonPath("$.data.content[0].fullName").value("Test User"))
                .andExpect(jsonPath("$.data.content[0].role").value("customer"));
    }

    @Test
    void updateUserStatus_success() throws Exception {
        AdminUserResponse updatedUser = new AdminUserResponse(
                1L, "user@test.com", "Test User", "0123456789", GenderType.male,
                null, null, UserRole.customer, 0, null, null,
                null, true, false, null, null, null
        );

        when(adminUserService.updateUserStatus(1L, false)).thenReturn(updatedUser);

        mockMvc.perform(patch("/api/admin/users/{id}/status", 1L)
                .param("isActive", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Update user status successful"))
                .andExpect(jsonPath("$.data.email").value("user@test.com"))
                .andExpect(jsonPath("$.data.isActive").value(false));
    }

    @Test
    void updateUserRole_success() throws Exception {
        AdminUserResponse updatedUser = new AdminUserResponse(
                1L, "user@test.com", "Test User", "0123456789", GenderType.male,
                null, null, UserRole.staff, 0, null, null,
                null, true, true, null, null, null
        );

        UpdateUserRoleRequest request = new UpdateUserRoleRequest(UserRole.staff);

        when(adminUserService.updateUserRole(1L, UserRole.staff)).thenReturn(updatedUser);

        mockMvc.perform(patch("/api/admin/users/{id}/role", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Update user role successful"))
                .andExpect(jsonPath("$.data.email").value("user@test.com"))
                .andExpect(jsonPath("$.data.role").value("staff"));
    }

    @Test
    void updateUserRole_invalidRequest_returns400() throws Exception {
        UpdateUserRoleRequest request = new UpdateUserRoleRequest(null);

        mockMvc.perform(patch("/api/admin/users/{id}/role", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
