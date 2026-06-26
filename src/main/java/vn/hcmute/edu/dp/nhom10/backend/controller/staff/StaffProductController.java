package vn.hcmute.edu.dp.nhom10.backend.controller.staff;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import vn.hcmute.edu.dp.nhom10.backend.service.S3Service;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCreateProductRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffProductSearchCriteria;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffUpdateProductRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffUpdateProductVisibilityRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffUpdateStockRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.enums.StaffProductStatus;
import vn.hcmute.edu.dp.nhom10.backend.service.StaffProductService;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/staff/products")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class StaffProductController {

    private final StaffProductService staffProductService;
    private final S3Service s3Service;

    @GetMapping
    public ApiResponse getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) StaffProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        StaffProductSearchCriteria criteria = new StaffProductSearchCriteria(keyword, categoryId, status);
        return buildResponse(
                HttpStatus.OK,
                "Lấy danh sách sản phẩm thành công",
                staffProductService.getProducts(criteria, page, size, sortBy, sortDir)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse getProductDetail(@PathVariable Long id) {
        return buildResponse(
                HttpStatus.OK,
                "Lấy chi tiết sản phẩm thành công",
                staffProductService.getProductDetail(id)
        );
    }

    @PostMapping
    public ApiResponse createProduct(@Valid @RequestBody StaffCreateProductRequest request) {
        return buildResponse(
                HttpStatus.CREATED,
                "Thêm sản phẩm thành công",
                staffProductService.createProduct(request)
        );
    }

    @PutMapping("/{id}")
    public ApiResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody StaffUpdateProductRequest request
    ) {
        return buildResponse(
                HttpStatus.OK,
                "Cập nhật sản phẩm thành công",
                staffProductService.updateProduct(id, request)
        );
    }

    @PatchMapping("/{id}/visibility")
    public ApiResponse updateVisibility(
            @PathVariable Long id,
            @Valid @RequestBody StaffUpdateProductVisibilityRequest request
    ) {
        return buildResponse(
                HttpStatus.OK,
                "Cập nhật trạng thái hiển thị sản phẩm thành công",
                staffProductService.updateVisibility(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse deleteProduct(@PathVariable Long id) {
        staffProductService.deleteProduct(id);
        return buildResponse(HttpStatus.OK, "Xóa sản phẩm thành công", null);
    }

    @PatchMapping("/{productId}/variants/{variantId}/stock")
    public ApiResponse updateStock(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @Valid @RequestBody StaffUpdateStockRequest request
    ) {
        return buildResponse(
                HttpStatus.OK,
                "Cập nhật tồn kho thành công",
                staffProductService.updateStock(productId, variantId, request)
        );
    }

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse uploadImage(@RequestParam("file") MultipartFile file) {
        String url = s3Service.uploadFile("products", file);
        return buildResponse(HttpStatus.OK, "Upload hình ảnh thành công", url);
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
