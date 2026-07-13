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
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "API công khai cho sản phẩm (không cần đăng nhập)")
@Slf4j(topic = "PRODUCT-CONTROLLER")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm sản phẩm full-text + Lọc + Sắp xếp")
    public ApiResponse searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "relevance") String sortBy,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) List<String> colors,
            @RequestParam(required = false) List<String> sizes,
            @RequestParam(required = false) String brand) {

        log.info("Full-text search query: '{}', sortBy: {}, page: {}, size: {}", q, sortBy, page, size);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Search products successfully")
                .data(productService.searchProductsFullText(q, sortBy, page, size, categorySlug, minPrice, maxPrice, colors, sizes, brand))
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Gợi ý tìm kiếm autocomplete")
    public ApiResponse getSuggestions(@RequestParam String q) {
        log.info("Autocomplete query: '{}'", q);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Fetch suggestions successfully")
                .data(productService.getAutocompleteSuggestionsList(q))
                .timestamp(OffsetDateTime.now())
                .build();
    }

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

    @GetMapping("/new-arrivals")
    @Operation(summary = "Lấy danh sách 8 sản phẩm mới nhất")
    public ApiResponse getNewArrivals() {
        log.info("Fetching new arrivals");
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Fetch new arrivals successfully")
                .data(productService.getNewArrivals())
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
