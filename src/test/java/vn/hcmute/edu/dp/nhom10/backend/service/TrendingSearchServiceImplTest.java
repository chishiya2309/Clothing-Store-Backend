package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.TrendingSearchResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.TrendingSearchServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrendingSearchServiceImplTest {

    @Test
    void getTopTrendingSearches_shouldReturnTopOrderedUniqueKeywords() {
        TrendingSearchService service = new TrendingSearchServiceImpl();

        service.recordSearch("Batman");
        service.recordSearch("spiderman");
        service.recordSearch("Batman");
        service.recordSearch("  spiderMan  ");
        service.recordSearch("Avengers");

        List<TrendingSearchResponse> trending = service.getTopTrendingSearches(10);

        assertEquals(3, trending.size());
        assertEquals("Batman", trending.get(0).getKeyword());
        assertEquals(2L, trending.get(0).getCount());
        assertEquals("spiderman", trending.get(1).getKeyword());
        assertEquals(2L, trending.get(1).getCount());
        assertEquals("Avengers", trending.get(2).getKeyword());
        assertEquals(1L, trending.get(2).getCount());
    }
}