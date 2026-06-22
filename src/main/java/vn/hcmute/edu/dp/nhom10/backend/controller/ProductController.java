package vn.hcmute.edu.dp.nhom10.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.ProductService;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "API công khai cho sản phẩm (không cần đăng nhập)")
@Slf4j(topic = "PRODUCT-CONTROLLER")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{slug}")
    @Operation(summary = "Xem chi tiết sản phẩm theo slug")
    public ApiResponse getProductDetail(@PathVariable String slug) {
        log.info("Fetching product detail for slug: {}", slug);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Fetch product detail successfully")
                .data(productService.getProductBySlug(slug))
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
