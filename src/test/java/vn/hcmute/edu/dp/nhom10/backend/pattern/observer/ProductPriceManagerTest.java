package vn.hcmute.edu.dp.nhom10.backend.pattern.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductPriceManagerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductPriceObserver observer1;

    @Mock
    private ProductPriceObserver observer2;

    private ProductPriceManager productPriceManager;

    @BeforeEach
    void setUp() {
        productPriceManager = new ProductPriceManager(productRepository, new ArrayList<>());
    }

    @Test
    void addObserver_ShouldAddToList() {
        productPriceManager.addObserver(observer1);
        productPriceManager.notifyObservers(new Product(), BigDecimal.ZERO, BigDecimal.TEN);
        
        verify(observer1, times(1)).update(any(), any(), any());
    }

    @Test
    void removeObserver_ShouldRemoveFromList() {
        productPriceManager.addObserver(observer1);
        productPriceManager.removeObserver(observer1);
        productPriceManager.notifyObservers(new Product(), BigDecimal.ZERO, BigDecimal.TEN);
        
        verify(observer1, never()).update(any(), any(), any());
    }

    @Test
    void setSalePrice_WhenProductExists_ShouldUpdatePriceAndNotifyObservers() {
        // Arrange
        Long productId = 1L;
        BigDecimal oldPrice = new BigDecimal("100000");
        BigDecimal newPrice = new BigDecimal("80000");
        
        Product product = new Product();
        product.setId(productId);
        product.setSalePrice(oldPrice);
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        
        productPriceManager.addObserver(observer1);
        productPriceManager.addObserver(observer2);

        // Act
        Product updatedProduct = productPriceManager.setSalePrice(productId, newPrice);

        // Assert
        assertEquals(newPrice, updatedProduct.getSalePrice());
        verify(productRepository).save(product);
        verify(observer1).update(product, oldPrice, newPrice);
        verify(observer2).update(product, oldPrice, newPrice);
    }

    @Test
    void setSalePrice_WhenProductNotFound_ShouldThrowException() {
        // Arrange
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            productPriceManager.setSalePrice(productId, new BigDecimal("80000"))
        );
        
        verify(productRepository, never()).save(any());
    }
}
