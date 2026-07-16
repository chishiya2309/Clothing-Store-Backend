package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

public record ExperimentReportResponse(
        Long experimentId,
        String experimentKey,
        String experimentName,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        List<ExperimentEventReportResponse> events
) implements Serializable {
}
