package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.projection.ExperimentEventReportProjection;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.AnalyticsEventRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ExperimentRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ExperimentVariantRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.*;
import vn.hcmute.edu.dp.nhom10.backend.entity.AnalyticsEvent;
import vn.hcmute.edu.dp.nhom10.backend.entity.Experiment;
import vn.hcmute.edu.dp.nhom10.backend.entity.ExperimentVariant;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.AnalyticsEventRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ExperimentRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ExperimentVariantRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.ExperimentService;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j(topic = "EXPERIMENT-SERVICE")
@Service
@RequiredArgsConstructor
public class ExperimentServiceImpl implements ExperimentService {

    private static final List<String> ALLOWED_STATUSES = List.of("draft", "running", "paused", "completed");

    private final ExperimentRepository experimentRepository;
    private final ExperimentVariantRepository variantRepository;
    private final AnalyticsEventRepository analyticsEventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ExperimentResponse createExperiment(ExperimentRequest request) {
        if (experimentRepository.existsByKey(request.key())) {
            throw new InvalidDataException("Experiment key already exists");
        }

        Experiment experiment = Experiment.builder()
                .key(request.key())
                .name(request.name())
                .description(request.description())
                .status(normalizeStatus(request.status()))
                .targetPage(request.targetPage())
                .startsAt(request.startsAt())
                .endsAt(request.endsAt())
                .build();

        applyVariants(experiment, request.variants());
        validateExperiment(experiment);
        return toResponse(experimentRepository.save(experiment));
    }

    @Override
    @Transactional
    public ExperimentResponse updateExperiment(Long id, ExperimentRequest request) {
        Experiment experiment = getExperiment(id);
        if (!Objects.equals(experiment.getKey(), request.key()) && experimentRepository.existsByKey(request.key())) {
            throw new InvalidDataException("Experiment key already exists");
        }

        experiment.setKey(request.key());
        experiment.setName(request.name());
        experiment.setDescription(request.description());
        experiment.setStatus(normalizeStatus(request.status()));
        experiment.setTargetPage(request.targetPage());
        experiment.setStartsAt(request.startsAt());
        experiment.setEndsAt(request.endsAt());
        experiment.getVariants().clear();
        applyVariants(experiment, request.variants());
        validateExperiment(experiment);
        return toResponse(experimentRepository.save(experiment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExperimentResponse> getExperiments() {
        return experimentRepository.findAll().stream()
                .sorted(Comparator.comparing(Experiment::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExperimentAssignmentResponse assignVariant(String experimentKey, String visitorId, Long userId) {
        Experiment experiment = experimentRepository.findByKey(experimentKey)
                .orElseThrow(() -> new ResourceNotFoundException("Experiment not found"));

        if (!isExperimentRunning(experiment)) {
            throw new InvalidDataException("Experiment is not running");
        }

        List<ExperimentVariant> variants = variantRepository.findByExperimentIdAndIsActiveTrueOrderByIdAsc(experiment.getId());
        if (variants.isEmpty()) {
            throw new InvalidDataException("Experiment has no active variant");
        }

        String resolvedVisitorId = blankToNull(visitorId) == null ? UUID.randomUUID().toString() : visitorId.trim();
        String assignmentKey = assignmentKey(experimentKey, resolvedVisitorId, userId);
        ExperimentVariant variant = pickVariant(variants, assignmentKey);
        return new ExperimentAssignmentResponse(
                experiment.getId(),
                experiment.getKey(),
                experiment.getName(),
                variant.getId(),
                variant.getKey(),
                variant.getName(),
                variant.getPayload(),
                resolvedVisitorId
        );
    }

    @Override
    @Transactional
    public AnalyticsEventResponse recordEvent(AnalyticsEventRequest request) {
        Experiment experiment = resolveExperiment(request);
        ExperimentVariant variant = resolveVariant(request, experiment);
        User user = request.userId() == null
                ? null
                : userRepository.findById(request.userId()).orElse(null);

        AnalyticsEvent event = AnalyticsEvent.builder()
                .experiment(experiment)
                .variant(variant)
                .user(user)
                .visitorId(blankToNull(request.visitorId()))
                .sessionId(blankToNull(request.sessionId()))
                .eventName(request.eventName())
                .pagePath(blankToNull(request.pagePath()))
                .gaClientId(blankToNull(request.gaClientId()))
                .metadata(request.metadata())
                .build();

        AnalyticsEvent saved = analyticsEventRepository.save(event);
        return new AnalyticsEventResponse(
                saved.getId(),
                experiment == null ? null : experiment.getId(),
                experiment == null ? null : experiment.getKey(),
                variant == null ? null : variant.getId(),
                variant == null ? null : variant.getKey(),
                saved.getEventName(),
                saved.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ExperimentReportResponse getReport(Long experimentId, OffsetDateTime startDate, OffsetDateTime endDate) {
        Experiment experiment = getExperiment(experimentId);
        List<ExperimentEventReportProjection> projections =
                analyticsEventRepository.findExperimentEventReport(experimentId, startDate, endDate);

        List<ExperimentEventReportResponse> events = projections.stream()
                .map(p -> new ExperimentEventReportResponse(
                        p.getVariantId(),
                        p.getVariantKey(),
                        p.getVariantName(),
                        p.getEventName(),
                        p.getEventCount(),
                        p.getUniqueVisitors()
                ))
                .toList();

        return new ExperimentReportResponse(
                experiment.getId(),
                experiment.getKey(),
                experiment.getName(),
                startDate,
                endDate,
                events
        );
    }

    private Experiment getExperiment(Long id) {
        return experimentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Experiment not found"));
    }

    private void applyVariants(Experiment experiment, List<ExperimentVariantRequest> requests) {
        requests.forEach(request -> experiment.getVariants().add(ExperimentVariant.builder()
                .experiment(experiment)
                .key(request.key())
                .name(request.name())
                .trafficWeight(request.trafficWeight())
                .isControl(Boolean.TRUE.equals(request.isControl()))
                .isActive(request.isActive() == null || request.isActive())
                .payload(request.payload())
                .build()));
    }

    private void validateExperiment(Experiment experiment) {
        if (experiment.getEndsAt() != null
                && experiment.getStartsAt() != null
                && !experiment.getEndsAt().isAfter(experiment.getStartsAt())) {
            throw new InvalidDataException("Experiment end time must be after start time");
        }

        int activeWeight = experiment.getVariants().stream()
                .filter(variant -> Boolean.TRUE.equals(variant.getIsActive()))
                .mapToInt(ExperimentVariant::getTrafficWeight)
                .sum();
        if (activeWeight <= 0) {
            throw new InvalidDataException("Experiment must have at least one active variant with traffic weight");
        }
    }

    private String normalizeStatus(String status) {
        String normalized = blankToNull(status) == null ? "draft" : status.trim().toLowerCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new InvalidDataException("Experiment status is invalid");
        }
        return normalized;
    }

    private boolean isExperimentRunning(Experiment experiment) {
        OffsetDateTime now = OffsetDateTime.now();
        return "running".equals(experiment.getStatus())
                && (experiment.getStartsAt() == null || !experiment.getStartsAt().isAfter(now))
                && (experiment.getEndsAt() == null || experiment.getEndsAt().isAfter(now));
    }

    private String assignmentKey(String experimentKey, String visitorId, Long userId) {
        if (userId != null) {
            return experimentKey + ":user:" + userId;
        }
        if (blankToNull(visitorId) != null) {
            return experimentKey + ":visitor:" + visitorId.trim();
        }
        return experimentKey + ":anonymous:" + UUID.randomUUID();
    }

    private ExperimentVariant pickVariant(List<ExperimentVariant> variants, String assignmentKey) {
        int totalWeight = variants.stream().mapToInt(ExperimentVariant::getTrafficWeight).sum();
        int bucket = Math.floorMod(assignmentKey.hashCode(), totalWeight);
        int cumulative = 0;
        for (ExperimentVariant variant : variants) {
            cumulative += variant.getTrafficWeight();
            if (bucket < cumulative) {
                return variant;
            }
        }
        return variants.get(variants.size() - 1);
    }

    private Experiment resolveExperiment(AnalyticsEventRequest request) {
        if (request.experimentId() != null) {
            return experimentRepository.findById(request.experimentId()).orElse(null);
        }
        if (blankToNull(request.experimentKey()) != null) {
            return experimentRepository.findByKey(request.experimentKey()).orElse(null);
        }
        return null;
    }

    private ExperimentVariant resolveVariant(AnalyticsEventRequest request, Experiment experiment) {
        if (request.variantId() != null) {
            return variantRepository.findById(request.variantId()).orElse(null);
        }
        if (experiment != null && blankToNull(request.variantKey()) != null) {
            return variantRepository.findByExperimentIdAndKey(experiment.getId(), request.variantKey()).orElse(null);
        }
        return null;
    }

    private ExperimentResponse toResponse(Experiment experiment) {
        return new ExperimentResponse(
                experiment.getId(),
                experiment.getKey(),
                experiment.getName(),
                experiment.getDescription(),
                experiment.getStatus(),
                experiment.getTargetPage(),
                experiment.getStartsAt(),
                experiment.getEndsAt(),
                experiment.getVariants().stream().map(this::toResponse).toList(),
                experiment.getCreatedAt(),
                experiment.getUpdatedAt()
        );
    }

    private ExperimentVariantResponse toResponse(ExperimentVariant variant) {
        return new ExperimentVariantResponse(
                variant.getId(),
                variant.getKey(),
                variant.getName(),
                variant.getTrafficWeight(),
                variant.getIsControl(),
                variant.getIsActive(),
                variant.getPayload()
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
