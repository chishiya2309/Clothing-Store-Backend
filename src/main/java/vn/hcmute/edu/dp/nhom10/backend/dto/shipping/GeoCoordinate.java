package vn.hcmute.edu.dp.nhom10.backend.dto.shipping;

import java.math.BigDecimal;

public record GeoCoordinate(
        BigDecimal latitude,
        BigDecimal longitude
) {
}
