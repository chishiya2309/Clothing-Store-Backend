package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import vn.hcmute.edu.dp.nhom10.backend.config.shipping.OpenRouteServiceProperties;
import vn.hcmute.edu.dp.nhom10.backend.dto.shipping.GeoCoordinate;
import vn.hcmute.edu.dp.nhom10.backend.service.OpenRouteService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class OpenRouteServiceImpl implements OpenRouteService {

    private static final int COORDINATE_SCALE = 7;
    private static final int DISTANCE_SCALE = 3;

    private final OpenRouteServiceProperties properties;
    private final RestClient restClient;

    public OpenRouteServiceImpl(OpenRouteServiceProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    @Override
    public Optional<GeoCoordinate> geocode(String address) {
        if (!properties.hasApiKey() || address == null || address.isBlank()) {
            return Optional.empty();
        }

        try {
            GeocodeResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/pelias/v1/search")
                            .queryParam("text", address)
                            .queryParam("size", 1)
                            .queryParam("boundary.country", "VNM")
                            .build())
                    .header("Authorization", properties.getApiKey())
                    .retrieve()
                    .body(GeocodeResponse.class);

            return firstCoordinate(response);
        } catch (Exception ex) {
            log.warn("Failed to geocode shipping address via OpenRouteService: {}", address, ex);
            return Optional.empty();
        }
    }

    @Override
    public Optional<BigDecimal> drivingDistanceKm(GeoCoordinate origin, GeoCoordinate destination) {
        if (!properties.hasApiKey() || origin == null || destination == null) {
            return Optional.empty();
        }

        Map<String, Object> request = Map.of(
                "coordinates", List.of(
                        List.of(origin.longitude(), origin.latitude()),
                        List.of(destination.longitude(), destination.latitude())
                ),
                "instructions", false
        );

        try {
            DirectionsResponse response = restClient.post()
                    .uri("/openrouteservice/v2/directions/driving-car/json")
                    .header("Authorization", properties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(DirectionsResponse.class);

            return firstDistanceKm(response);
        } catch (Exception ex) {
            log.warn("Failed to calculate driving distance via OpenRouteService", ex);
            return Optional.empty();
        }
    }

    private Optional<GeoCoordinate> firstCoordinate(GeocodeResponse response) {
        if (response == null || response.features() == null || response.features().isEmpty()) {
            return Optional.empty();
        }

        GeocodeFeature feature = response.features().get(0);
        if (feature == null || feature.geometry() == null || feature.geometry().coordinates() == null
                || feature.geometry().coordinates().size() < 2) {
            return Optional.empty();
        }

        List<BigDecimal> coordinates = feature.geometry().coordinates();
        BigDecimal longitude = normalizeCoordinate(coordinates.get(0));
        BigDecimal latitude = normalizeCoordinate(coordinates.get(1));
        return Optional.of(new GeoCoordinate(latitude, longitude));
    }

    private Optional<BigDecimal> firstDistanceKm(DirectionsResponse response) {
        if (response == null || response.routes() == null || response.routes().isEmpty()) {
            return Optional.empty();
        }

        DirectionsRoute route = response.routes().get(0);
        if (route == null || route.summary() == null || route.summary().distance() == null) {
            return Optional.empty();
        }

        return Optional.of(route.summary().distance()
                .divide(BigDecimal.valueOf(1000), DISTANCE_SCALE, RoundingMode.HALF_UP));
    }

    private BigDecimal normalizeCoordinate(BigDecimal coordinate) {
        return coordinate.setScale(COORDINATE_SCALE, RoundingMode.HALF_UP);
    }

    private record GeocodeResponse(List<GeocodeFeature> features) {
    }

    private record GeocodeFeature(GeocodeGeometry geometry) {
    }

    private record GeocodeGeometry(List<BigDecimal> coordinates) {
    }

    private record DirectionsResponse(List<DirectionsRoute> routes) {
    }

    private record DirectionsRoute(DirectionsSummary summary) {
    }

    private record DirectionsSummary(BigDecimal distance) {
    }
}
