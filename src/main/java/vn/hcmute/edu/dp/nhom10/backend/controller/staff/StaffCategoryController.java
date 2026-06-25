package vn.hcmute.edu.dp.nhom10.backend.controller.staff;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCategoryRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.StaffCategoryService;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/staff/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
public class StaffCategoryController {

    private final StaffCategoryService staffCategoryService;

    @GetMapping("/hierarchy")
    public ApiResponse getHierarchy() {
        return buildResponse(HttpStatus.OK, "Lấy sơ đồ cây danh mục thành công", staffCategoryService.getCategoryHierarchy());
    }

    @PostMapping
    public ApiResponse createCategory(
            @Valid @RequestBody StaffCategoryRequest request,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        return buildResponse(
                HttpStatus.CREATED,
                "Tạo danh mục thành công",
                staffCategoryService.createCategory(request, username)
        );
    }

    @PutMapping("/{id}")
    public ApiResponse updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody StaffCategoryRequest request,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        return buildResponse(
                HttpStatus.OK,
                "Cập nhật danh mục thành công",
                staffCategoryService.updateCategory(id, request, username)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse deleteCategory(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        staffCategoryService.deleteCategory(id, username);
        return buildResponse(HttpStatus.OK, "Xóa danh mục thành công", null);
    }

    private ApiResponse buildResponse(HttpStatus status, String message, Object data) {
        return ApiResponse.builder()
                .status(status.value())
                .message(message)
                .data(data)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
