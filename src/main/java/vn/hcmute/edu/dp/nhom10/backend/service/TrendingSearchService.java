package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.response.TrendingSearchResponse;

import java.util.List;

public interface TrendingSearchService {

    void recordSearch(String keyword);

    List<TrendingSearchResponse> getTopTrendingSearches(int limit);
}