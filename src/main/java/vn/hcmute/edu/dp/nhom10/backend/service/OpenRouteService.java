package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.shipping.GeoCoordinate;

import java.math.BigDecimal;
import java.util.Optional;

public interface OpenRouteService {
    Optional<GeoCoordinate> geocode(String address);

    Optional<BigDecimal> drivingDistanceKm(GeoCoordinate origin, GeoCoordinate destination);
}
