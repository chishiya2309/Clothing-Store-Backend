package vn.hcmute.edu.dp.nhom10.backend.pattern.observer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductPriceManager implements ProductPriceSubject {

    private final List<ProductPriceObserver> observers = new ArrayList<>();
    private final ProductRepository productRepository;

    @Autowired
    public ProductPriceManager(ProductRepository productRepository, List<ProductPriceObserver> injectedObservers) {
        this.productRepository = productRepository;
        if (injectedObservers != null) {
            this.observers.addAll(injectedObservers);
        }
    }

    @Override
    public void addObserver(ProductPriceObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(ProductPriceObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Product product, BigDecimal oldPrice, BigDecimal newPrice) {
        for (ProductPriceObserver observer : observers) {
            observer.update(product, oldPrice, newPrice);
        }
    }

    @org.springframework.cache.annotation.CacheEvict(value = { "newArrivals", "bestSellers", "categories",
            "collections" }, allEntries = true)
    @Transactional
    public Product setSalePrice(Long productId, BigDecimal newSalePrice) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        BigDecimal oldPrice = product.getSalePrice();
        product.setSalePrice(newSalePrice);

        Product savedProduct = productRepository.save(product);

        // Kích hoạt notification pattern
        notifyObservers(savedProduct, oldPrice, newSalePrice);

        return savedProduct;
    }
}
