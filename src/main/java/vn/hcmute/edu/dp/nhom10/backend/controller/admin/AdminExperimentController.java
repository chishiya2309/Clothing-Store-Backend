package vn.hcmute.edu.dp.nhom10.backend.controller.admin;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ExperimentRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ExperimentReportResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ExperimentResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.ExperimentService;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/experiments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Experiment", description = "A/B testing experiment management")
@Slf4j(topic = "ADMIN-EXPERIMENT-CONTROLLER")
public class AdminExperimentController {

    private final ExperimentService experimentService;

    @GetMapping
    public ApiResponse getExperiments() {
        List<ExperimentResponse> experiments = experimentService.getExperiments();
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Fetch experiments successful")
                .data((Serializable) experiments)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PostMapping
    public ApiResponse createExperiment(@Valid @RequestBody ExperimentRequest request) {
        log.info("Admin creating experiment: {}", request.key());
        ExperimentResponse experiment = experimentService.createExperiment(request);
        return ApiResponse.builder()
                .status(HttpStatus.CREATED.value())
                .message("Create experiment successful")
                .data(experiment)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse updateExperiment(
            @PathVariable Long id,
            @Valid @RequestBody ExperimentRequest request) {
        log.info("Admin updating experiment id: {}", id);
        ExperimentResponse experiment = experimentService.updateExperiment(id, request);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Update experiment successful")
                .data(experiment)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping("/{id}/report")
    public ApiResponse getExperimentReport(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {
        ExperimentReportResponse report = experimentService.getReport(id, startDate, endDate);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Fetch experiment report successful")
                .data(report)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
