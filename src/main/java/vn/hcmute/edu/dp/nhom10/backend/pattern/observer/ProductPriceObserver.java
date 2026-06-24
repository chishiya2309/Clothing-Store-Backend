package vn.hcmute.edu.dp.nhom10.backend.pattern.observer;

import vn.hcmute.edu.dp.nhom10.backend.entity.Product;

import java.math.BigDecimal;

public interface ProductPriceObserver {
    void update(Product product, BigDecimal oldPrice, BigDecimal newPrice);
}
