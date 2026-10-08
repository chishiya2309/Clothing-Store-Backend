package vn.hcmute.edu.dp.nhom10.backend.config.shipping;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "shipping")
public class ShippingProperties {

    private BigDecimal shopLatitude = new BigDecimal("10.762622");
    private BigDecimal shopLongitude = new BigDecimal("106.660172");
    private BigDecimal baseFee = new BigDecimal("25000.00");
    private BigDecimal baseDistanceKm = new BigDecimal("10.00");
    private BigDecimal maxDistanceKm = new BigDecimal("30.00");
    private BigDecimal extraFeePerKm = new BigDecimal("3000.00");
    private BigDecimal maxFee = new BigDecimal("50000.00");

    public BigDecimal getShopLatitude() {
        return shopLatitude;
    }

    public void setShopLatitude(BigDecimal shopLatitude) {
        this.shopLatitude = shopLatitude;
    }

    public BigDecimal getShopLongitude() {
        return shopLongitude;
    }

    public void setShopLongitude(BigDecimal shopLongitude) {
        this.shopLongitude = shopLongitude;
    }

    public BigDecimal getBaseFee() {
        return baseFee;
    }

    public void setBaseFee(BigDecimal baseFee) {
        this.baseFee = baseFee;
    }

    public BigDecimal getBaseDistanceKm() {
        return baseDistanceKm;
    }

    public void setBaseDistanceKm(BigDecimal baseDistanceKm) {
        this.baseDistanceKm = baseDistanceKm;
    }

    public BigDecimal getMaxDistanceKm() {
        return maxDistanceKm;
    }

    public void setMaxDistanceKm(BigDecimal maxDistanceKm) {
        this.maxDistanceKm = maxDistanceKm;
    }

    public BigDecimal getExtraFeePerKm() {
        return extraFeePerKm;
    }

    public void setExtraFeePerKm(BigDecimal extraFeePerKm) {
        this.extraFeePerKm = extraFeePerKm;
    }

    public BigDecimal getMaxFee() {
        return maxFee;
    }

    public void setMaxFee(BigDecimal maxFee) {
        this.maxFee = maxFee;
    }
}
