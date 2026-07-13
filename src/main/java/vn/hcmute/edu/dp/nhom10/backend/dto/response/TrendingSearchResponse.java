package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
public class TrendingSearchResponse implements Serializable {
    private final String keyword;
    private final long count;
}