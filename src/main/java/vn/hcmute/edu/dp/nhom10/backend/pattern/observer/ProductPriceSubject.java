package vn.hcmute.edu.dp.nhom10.backend.pattern.observer;

import vn.hcmute.edu.dp.nhom10.backend.entity.Product;

import java.math.BigDecimal;

public interface ProductPriceSubject {
    void addObserver(ProductPriceObserver observer);
    void removeObserver(ProductPriceObserver observer);
    void notifyObservers(Product product, BigDecimal oldPrice, BigDecimal newPrice);
}
