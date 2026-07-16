package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.AnalyticsEventRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ExperimentRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ExperimentVariantRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ExperimentAssignmentResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ExperimentResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.AnalyticsEvent;
import vn.hcmute.edu.dp.nhom10.backend.entity.Experiment;
import vn.hcmute.edu.dp.nhom10.backend.entity.ExperimentVariant;
import vn.hcmute.edu.dp.nhom10.backend.repository.AnalyticsEventRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ExperimentRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ExperimentVariantRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.ExperimentServiceImpl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExperimentServiceImplTest {

    @Mock
    private ExperimentRepository experimentRepository;

    @Mock
    private ExperimentVariantRepository variantRepository;

    @Mock
    private AnalyticsEventRepository analyticsEventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExperimentServiceImpl experimentService;

    @Test
    void createExperiment_mapsVariantsAndDefaultsStatus() {
        ExperimentRequest request = new ExperimentRequest(
                "product-page-ab",
                "Product Page A/B",
                "Test CTA copy",
                null,
                "/products/*",
                null,
                null,
                List.of(
                        new ExperimentVariantRequest(null, "control", "Control", 50, true, true, "{}"),
                        new ExperimentVariantRequest(null, "b", "Variant B", 50, false, true, "{\"cta\":\"Buy now\"}")
                )
        );

        when(experimentRepository.existsByKey("product-page-ab")).thenReturn(false);
        when(experimentRepository.save(any(Experiment.class))).thenAnswer(invocation -> {
            Experiment experiment = invocation.getArgument(0);
            experiment.setId(10L);
            experiment.getVariants().get(0).setId(100L);
            experiment.getVariants().get(1).setId(101L);
            return experiment;
        });

        ExperimentResponse response = experimentService.createExperiment(request);

        assertEquals("draft", response.status());
        assertEquals(2, response.variants().size());
        assertEquals("control", response.variants().get(0).key());
        verify(experimentRepository).save(any(Experiment.class));
    }

    @Test
    void assignVariant_returnsStableVariantForSameVisitor() {
        Experiment experiment = runningExperiment();
        ExperimentVariant control = variant(100L, experiment, "control", 50);
        ExperimentVariant variantB = variant(101L, experiment, "b", 50);

        when(experimentRepository.findByKey("product-page-ab")).thenReturn(Optional.of(experiment));
        when(variantRepository.findByExperimentIdAndIsActiveTrueOrderByIdAsc(10L))
                .thenReturn(List.of(control, variantB));

        ExperimentAssignmentResponse first = experimentService.assignVariant("product-page-ab", "visitor-1", null);
        ExperimentAssignmentResponse second = experimentService.assignVariant("product-page-ab", "visitor-1", null);

        assertEquals(first.variantKey(), second.variantKey());
        assertEquals("visitor-1", first.visitorId());
    }

    @Test
    void recordEvent_persistsExperimentVariantAndMetadata() {
        Experiment experiment = runningExperiment();
        ExperimentVariant variant = variant(101L, experiment, "b", 50);
        AnalyticsEventRequest request = new AnalyticsEventRequest(
                "product-page-ab",
                "b",
                null,
                null,
                null,
                "visitor-1",
                "session-1",
                "add_to_cart",
                "/products/polo",
                "GA1.1.123",
                Map.of("currency", "VND")
        );

        when(experimentRepository.findByKey("product-page-ab")).thenReturn(Optional.of(experiment));
        when(variantRepository.findByExperimentIdAndKey(10L, "b")).thenReturn(Optional.of(variant));
        when(analyticsEventRepository.save(any(AnalyticsEvent.class))).thenAnswer(invocation -> {
            AnalyticsEvent event = invocation.getArgument(0);
            event.setId(500L);
            return event;
        });

        experimentService.recordEvent(request);

        ArgumentCaptor<AnalyticsEvent> captor = ArgumentCaptor.forClass(AnalyticsEvent.class);
        verify(analyticsEventRepository).save(captor.capture());
        assertEquals(experiment, captor.getValue().getExperiment());
        assertEquals(variant, captor.getValue().getVariant());
        assertEquals("add_to_cart", captor.getValue().getEventName());
        assertEquals("VND", captor.getValue().getMetadata().get("currency"));
    }

    private Experiment runningExperiment() {
        return Experiment.builder()
                .id(10L)
                .key("product-page-ab")
                .name("Product Page A/B")
                .status("running")
                .startsAt(OffsetDateTime.now().minusDays(1))
                .endsAt(OffsetDateTime.now().plusDays(1))
                .build();
    }

    private ExperimentVariant variant(Long id, Experiment experiment, String key, int weight) {
        return ExperimentVariant.builder()
                .id(id)
                .experiment(experiment)
                .key(key)
                .name(key)
                .trafficWeight(weight)
                .isActive(true)
                .isControl("control".equals(key))
                .build();
    }
}
