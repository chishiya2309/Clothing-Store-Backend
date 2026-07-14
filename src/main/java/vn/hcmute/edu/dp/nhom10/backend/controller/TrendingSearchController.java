package vn.hcmute.edu.dp.nhom10.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.TrendingSearchResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.TrendingSearchService;

import java.util.List;

@RestController
@RequestMapping("/api/trending-search")
@RequiredArgsConstructor
@Tag(name = "Trending Search", description = "API thống kê từ khóa tìm kiếm phổ biến trong memory")
public class TrendingSearchController {

    private final TrendingSearchService trendingSearchService;

    @GetMapping
    @Operation(summary = "Lấy top 10 từ khóa tìm kiếm phổ biến nhất")
    public List<TrendingSearchResponse> getTrendingSearches() {
        return trendingSearchService.getTopTrendingSearches(10);
    }
}