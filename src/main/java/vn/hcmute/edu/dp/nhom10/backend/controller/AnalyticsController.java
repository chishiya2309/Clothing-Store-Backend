package vn.hcmute.edu.dp.nhom10.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.AnalyticsEventRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.AnalyticsEventResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.ExperimentService;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "GA4-style event ingestion API")
@Slf4j(topic = "ANALYTICS-CONTROLLER")
public class AnalyticsController {

    private final ExperimentService experimentService;

    @PostMapping("/events")
    public ApiResponse recordEvent(@Valid @RequestBody AnalyticsEventRequest request) {
        AnalyticsEventResponse event = experimentService.recordEvent(request);
        return ApiResponse.builder()
                .status(HttpStatus.CREATED.value())
                .message("Record analytics event successful")
                .data(event)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
