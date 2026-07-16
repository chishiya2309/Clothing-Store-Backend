package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.request.AnalyticsEventRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ExperimentRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.AnalyticsEventResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ExperimentAssignmentResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ExperimentReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ExperimentResponse;

import java.time.OffsetDateTime;
import java.util.List;

public interface ExperimentService {

    ExperimentResponse createExperiment(ExperimentRequest request);

    ExperimentResponse updateExperiment(Long id, ExperimentRequest request);

    List<ExperimentResponse> getExperiments();

    ExperimentAssignmentResponse assignVariant(String experimentKey, String visitorId, Long userId);

    AnalyticsEventResponse recordEvent(AnalyticsEventRequest request);

    ExperimentReportResponse getReport(Long experimentId, OffsetDateTime startDate, OffsetDateTime endDate);
}
