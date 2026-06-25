package vn.hcmute.edu.dp.nhom10.backend.pattern.observer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.Review;
import vn.hcmute.edu.dp.nhom10.backend.pattern.observer.review.ProductRatingUpdater;
import vn.hcmute.edu.dp.nhom10.backend.pattern.observer.review.ReviewStatusChangedEvent;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ReviewRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewRatingObserverTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ProductRatingUpdater ratingUpdater;

    @Test
    public void testHandleReviewStatusChanged() {
        Product product = Product.builder().id(20L).averageRating(BigDecimal.ZERO).build();
        Review review = Review.builder().id(100L).product(product).build();
        ReviewStatusChangedEvent event = new ReviewStatusChangedEvent(this, review);

        when(reviewRepository.calculateAverageRating(20L)).thenReturn(4.5);

        ratingUpdater.handleReviewStatusChanged(event);

        verify(reviewRepository, times(1)).calculateAverageRating(20L);
        
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(1)).save(productCaptor.capture());
        
        Product savedProduct = productCaptor.getValue();
        assertEquals(20L, savedProduct.getId());
        assertEquals(BigDecimal.valueOf(4.5), savedProduct.getAverageRating());
    }
}
