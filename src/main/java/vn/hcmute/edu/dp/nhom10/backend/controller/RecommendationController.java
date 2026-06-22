package vn.hcmute.edu.dp.nhom10.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.RecommendationService;

import java.io.Serializable;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/guest/recommendations")
@RequiredArgsConstructor
@Tag(name = "Public Recommendation", description = "Các API gợi ý sản phẩm (UC-29)")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/product/{productId}")
    @Operation(summary = "UC-29: Lấy danh sách sản phẩm tương tự cho trang chi tiết")
    public ApiResponse getProductRecommendations(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "8") int limit) {

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Recommendations fetched successfully")
                .data((Serializable) recommendationService.getRecommendations(productId, limit))
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
