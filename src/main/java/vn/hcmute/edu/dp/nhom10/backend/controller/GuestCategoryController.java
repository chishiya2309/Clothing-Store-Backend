package vn.hcmute.edu.dp.nhom10.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.CategoryService;
import vn.hcmute.edu.dp.nhom10.backend.service.ProductService;

import java.time.OffsetDateTime;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/guest/categories")
@RequiredArgsConstructor
@Tag(name = "Public Category", description = "Các API danh mục và sản phẩm cho khách vãng lai")
public class GuestCategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Lấy cây danh mục (Cached by Redis)")
    public ApiResponse getCategories() {
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Get categories successfully")
                .data(new ArrayList<>(categoryService.getCategoryHierarchy()))
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping("/{slug}/products")
    @Operation(summary = "Lấy danh sách sản phẩm theo danh mục (phân trang)")
    public ApiResponse getProductsByCategory(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Get products by category successfully")
                .data(productService.getProductsByCategorySlug(slug, page, size))
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
