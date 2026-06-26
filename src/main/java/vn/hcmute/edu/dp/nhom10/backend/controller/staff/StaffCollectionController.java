package vn.hcmute.edu.dp.nhom10.backend.controller.staff;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCollectionProductsRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCollectionRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.StaffCollectionService;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/staff/collections")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class StaffCollectionController {

    private final StaffCollectionService staffCollectionService;

    @GetMapping
    public ApiResponse getCollections(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword
    ) {
        return buildResponse(HttpStatus.OK, "Lấy danh sách bộ sưu tập thành công", staffCollectionService.getCollections(page, size, keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse getCollectionDetail(@PathVariable Long id) {
        return buildResponse(HttpStatus.OK, "Lấy chi tiết bộ sưu tập thành công", staffCollectionService.getCollectionDetail(id));
    }

    @PostMapping
    public ApiResponse createCollection(
            @Valid @RequestBody StaffCollectionRequest request,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        return buildResponse(
                HttpStatus.CREATED,
                "Tạo bộ sưu tập thành công",
                staffCollectionService.createCollection(request, username)
        );
    }

    @PutMapping("/{id}")
    public ApiResponse updateCollection(
            @PathVariable Long id,
            @Valid @RequestBody StaffCollectionRequest request,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        return buildResponse(
                HttpStatus.OK,
                "Cập nhật bộ sưu tập thành công",
                staffCollectionService.updateCollection(id, request, username)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse deleteCollection(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        staffCollectionService.deleteCollection(id, username);
        return buildResponse(HttpStatus.OK, "Xóa bộ sưu tập thành công", null);
    }

    @PostMapping("/{id}/products")
    public ApiResponse addProducts(
            @PathVariable Long id,
            @Valid @RequestBody StaffCollectionProductsRequest request,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        return buildResponse(
                HttpStatus.OK,
                "Thêm sản phẩm vào bộ sưu tập thành công",
                staffCollectionService.addProductsToCollection(id, request, username)
        );
    }

    @DeleteMapping("/{id}/products")
    public ApiResponse removeProducts(
            @PathVariable Long id,
            @Valid @RequestBody StaffCollectionProductsRequest request,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        return buildResponse(
                HttpStatus.OK,
                "Xóa sản phẩm khỏi bộ sưu tập thành công",
                staffCollectionService.removeProductsFromCollection(id, request, username)
        );
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
