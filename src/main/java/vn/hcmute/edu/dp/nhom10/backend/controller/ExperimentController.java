package vn.hcmute.edu.dp.nhom10.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ExperimentAssignmentResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.ExperimentService;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/experiments")
@RequiredArgsConstructor
@Tag(name = "Experiment", description = "Public A/B test assignment API")
@Slf4j(topic = "EXPERIMENT-CONTROLLER")
public class ExperimentController {

    private final ExperimentService experimentService;

    @GetMapping("/{experimentKey}/assignment")
    public ApiResponse assignVariant(
            @PathVariable String experimentKey,
            @RequestParam(required = false) String visitorId,
            @RequestParam(required = false) Long userId) {
        ExperimentAssignmentResponse assignment = experimentService.assignVariant(experimentKey, visitorId, userId);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Assign experiment variant successful")
                .data(assignment)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
