package vn.hcmute.edu.dp.nhom10.backend.controller.admin;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpdateUserRoleRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.AdminUserResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;
import vn.hcmute.edu.dp.nhom10.backend.service.AdminUserService;

import java.io.Serializable;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Proxy Pattern (Security AOP)
@Tag(name = "Admin User", description = "Quản trị người dùng")
@Slf4j(topic = "ADMIN-CONTROLLER")
public class AdminController {

    private final AdminUserService adminUserService;

    @GetMapping("/users")
    public ApiResponse getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean isActive) {

        log.info("Admin fetching user list with pagination. Page: {}, Size: {}, Keyword: {}, Role: {}, IsActive: {}",
                page, size, keyword, role, isActive);

        Page<AdminUserResponse> userPage = adminUserService.getUsers(page, size, keyword, role, isActive);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Fetch user list successful")
                .data((Serializable) userPage)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PatchMapping("/users/{id}/status")
    public ApiResponse updateUserStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {

        log.info("Admin changing status of user id: {} to isActive: {}", id, isActive);
        AdminUserResponse response = adminUserService.updateUserStatus(id, isActive);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Update user status successful")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PatchMapping("/users/{id}/role")
    public ApiResponse updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request) {

        log.info("Admin changing role of user id: {} to role: {}", id, request.role());
        AdminUserResponse response = adminUserService.updateUserRole(id, request.role());

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Update user role successful")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}

