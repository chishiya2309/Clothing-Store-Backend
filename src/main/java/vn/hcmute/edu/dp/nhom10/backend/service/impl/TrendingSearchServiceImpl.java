package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import org.springframework.stereotype.Service;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.TrendingSearchResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.TrendingSearchService;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

@Service
public class TrendingSearchServiceImpl implements TrendingSearchService {

    private final ConcurrentHashMap<String, TrendingKeywordStat> trendingKeywords = new ConcurrentHashMap<>();

    @Override
    public void recordSearch(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.isEmpty()) {
            return;
        }

        trendingKeywords.compute(normalizedKeyword, (key, existing) -> {
            if (existing == null) {
                return new TrendingKeywordStat(keyword.trim());
            }
            existing.increment();
            return existing;
        });
    }

    @Override
    public List<TrendingSearchResponse> getTopTrendingSearches(int limit) {
        int safeLimit = Math.max(limit, 0);

        return trendingKeywords.values().stream()
                .map(stat -> TrendingSearchResponse.builder()
                        .keyword(stat.keyword())
                        .count(stat.count())
                        .build())
                .sorted(Comparator.comparingLong(TrendingSearchResponse::getCount).reversed()
                        .thenComparing(TrendingSearchResponse::getKeyword, String.CASE_INSENSITIVE_ORDER))
                .limit(safeLimit)
                .toList();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        return keyword.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private static final class TrendingKeywordStat {
        private final String keyword;
        private final LongAdder counter = new LongAdder();

        private TrendingKeywordStat(String keyword) {
            this.keyword = keyword;
            this.counter.increment();
        }

        private void increment() {
            counter.increment();
        }

        private String keyword() {
            return keyword;
        }

        private long count() {
            return counter.sum();
        }
    }
}