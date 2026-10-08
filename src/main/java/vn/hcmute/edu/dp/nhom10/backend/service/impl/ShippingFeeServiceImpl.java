package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.hcmute.edu.dp.nhom10.backend.config.shipping.ShippingProperties;
import vn.hcmute.edu.dp.nhom10.backend.dto.shipping.GeoCoordinate;
import vn.hcmute.edu.dp.nhom10.backend.entity.Address;
import vn.hcmute.edu.dp.nhom10.backend.service.OpenRouteService;
import vn.hcmute.edu.dp.nhom10.backend.service.ShippingFeeService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingFeeServiceImpl implements ShippingFeeService {

    private static final int MONEY_SCALE = 2;

    private final ShippingProperties shippingProperties;
    private final OpenRouteService openRouteService;

    @Override
    public BigDecimal calculate(Address address) {
        String fullAddress = buildFullAddress(address);
        GeoCoordinate shopCoordinate = new GeoCoordinate(
                shippingProperties.getShopLatitude(),
                shippingProperties.getShopLongitude()
        );

        Optional<BigDecimal> distanceKm = openRouteService.geocode(fullAddress)
                .flatMap(customerCoordinate -> openRouteService.drivingDistanceKm(shopCoordinate, customerCoordinate));

        if (distanceKm.isEmpty()) {
            log.warn("Using max shipping fee because distance could not be calculated for address: {}", fullAddress);
            return money(shippingProperties.getMaxFee());
        }

        BigDecimal shippingFee = calculateFee(distanceKm.get());
        log.info("Calculated shipping fee for address '{}' with driving distance {} km: {}",
                fullAddress, distanceKm.get(), shippingFee);
        return shippingFee;
    }

    BigDecimal calculateFee(BigDecimal distanceKm) {
        if (distanceKm == null || distanceKm.signum() < 0) {
            return money(shippingProperties.getMaxFee());
        }

        BigDecimal baseDistanceKm = shippingProperties.getBaseDistanceKm();
        BigDecimal baseFee = shippingProperties.getBaseFee();
        if (distanceKm.compareTo(baseDistanceKm) <= 0) {
            return money(baseFee);
        }

        BigDecimal maxDistanceKm = shippingProperties.getMaxDistanceKm();
        if (distanceKm.compareTo(maxDistanceKm) >= 0) {
            return money(shippingProperties.getMaxFee());
        }

        BigDecimal extraKm = distanceKm.subtract(baseDistanceKm)
                .setScale(0, RoundingMode.CEILING);
        BigDecimal calculatedFee = baseFee.add(extraKm.multiply(shippingProperties.getExtraFeePerKm()));
        return money(calculatedFee.min(shippingProperties.getMaxFee()));
    }

    private String buildFullAddress(Address address) {
        if (address == null) {
            return "";
        }

        return Stream.of(
                        address.getStreetAddress(),
                        address.getWard(),
                        address.getDistrict(),
                        address.getProvince(),
                        "Vietnam"
                )
                .filter(value -> value != null && !value.isBlank())
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private BigDecimal money(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
