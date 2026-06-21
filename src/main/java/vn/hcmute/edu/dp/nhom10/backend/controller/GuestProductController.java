package vn.hcmute.edu.dp.nhom10.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ProductSearchCriteria;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.ProductService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/guest/products")
@RequiredArgsConstructor
@Tag(name = "Public Product", description = "Các API tìm kiếm, lọc và sắp xếp sản phẩm")
public class GuestProductController {

    private final ProductService productService;

    @GetMapping("/search")
    @Operation(summary = "UC-11/12/13: Tìm kiếm + Lọc + Sắp xếp sản phẩm")
    public ApiResponse searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) List<String> colors,
            @RequestParam(required = false) List<String> sizes,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "latest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .keyword(keyword)
                .categorySlug(categorySlug)
                .colors(colors)
                .sizes(sizes)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .sortBy(sortBy)
                .build();

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Search products successfully")
                .data(productService.searchProducts(criteria, page, size))
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping("/autocomplete")
    @Operation(summary = "UC-11: Gợi ý autocomplete khi gõ từ khóa")
    public ApiResponse autocomplete(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "5") int limit) {

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Autocomplete suggestions")
                .data(new ArrayList<>(productService.getAutocompleteSuggestions(keyword, limit)))
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
