package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.config.shipping.ShippingProperties;
import vn.hcmute.edu.dp.nhom10.backend.dto.shipping.GeoCoordinate;
import vn.hcmute.edu.dp.nhom10.backend.entity.Address;
import vn.hcmute.edu.dp.nhom10.backend.service.OpenRouteService;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShippingFeeServiceImplTest {

    private ShippingProperties shippingProperties;
    private FakeOpenRouteService openRouteService;
    private ShippingFeeServiceImpl shippingFeeService;

    @BeforeEach
    void setUp() {
        shippingProperties = new ShippingProperties();
        openRouteService = new FakeOpenRouteService();
        shippingFeeService = new ShippingFeeServiceImpl(shippingProperties, openRouteService);
    }

    @Test
    void calculateFee_distanceWithinBaseDistance_returnsBaseFee() {
        assertEquals(new BigDecimal("25000.00"), shippingFeeService.calculateFee(new BigDecimal("10.00")));
    }

    @Test
    void calculateFee_distanceAboveBaseDistance_roundsExtraKmUp() {
        assertEquals(new BigDecimal("31000.00"), shippingFeeService.calculateFee(new BigDecimal("11.20")));
    }

    @Test
    void calculateFee_distanceFarAway_capsAtMaxFee() {
        assertEquals(new BigDecimal("50000.00"), shippingFeeService.calculateFee(new BigDecimal("25.00")));
    }

    @Test
    void calculate_whenRouteDistanceIsUnavailable_returnsMaxFee() {
        openRouteService.coordinate = Optional.empty();

        BigDecimal result = shippingFeeService.calculate(address());

        assertEquals(new BigDecimal("50000.00"), result);
    }

    @Test
    void calculate_whenDistanceIsAvailable_returnsDistanceBasedFee() {
        openRouteService.coordinate = Optional.of(
                new GeoCoordinate(new BigDecimal("10.7800000"), new BigDecimal("106.7000000"))
        );
        openRouteService.distanceKm = Optional.of(new BigDecimal("12.40"));

        BigDecimal result = shippingFeeService.calculate(address());

        assertEquals(new BigDecimal("34000.00"), result);
    }

    private Address address() {
        return Address.builder()
                .streetAddress("1 Le Loi")
                .ward("Ben Nghe")
                .district("District 1")
                .province("Ho Chi Minh")
                .build();
    }

    private static class FakeOpenRouteService implements OpenRouteService {
        private Optional<GeoCoordinate> coordinate = Optional.empty();
        private Optional<BigDecimal> distanceKm = Optional.empty();

        @Override
        public Optional<GeoCoordinate> geocode(String address) {
            return coordinate;
        }

        @Override
        public Optional<BigDecimal> drivingDistanceKm(GeoCoordinate origin, GeoCoordinate destination) {
            return distanceKm;
        }
    }
}
