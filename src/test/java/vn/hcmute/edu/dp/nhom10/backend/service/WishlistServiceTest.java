package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductGridResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Category;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.entity.Wishlist;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.WishlistRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.WishlistServiceImpl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private User testUser;
    private Product testProduct;
    private Wishlist testWishlist;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setBasePrice(new BigDecimal("100000"));
        
        testWishlist = new Wishlist();
        testWishlist.setId(1L);
        testWishlist.setUser(testUser);
        testWishlist.setProduct(testProduct);
    }

    @Test
    void getUserWishlist_WhenUserExists_ShouldReturnPageResponse() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        Page<Wishlist> wishlistPage = new PageImpl<>(Collections.singletonList(testWishlist));
        when(wishlistRepository.findByUserId(eq(testUser.getId()), any(PageRequest.class))).thenReturn(wishlistPage);

        // Act
        PageResponse<ProductGridResponse> response = wishlistService.getUserWishlist(testUser.getEmail(), 0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(testProduct.getName(), response.getContent().get(0).getName());
    }

    @Test
    void toggleWishlist_WhenWishlistExists_ShouldDeleteIt() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(wishlistRepository.findByUserIdAndProductId(testUser.getId(), testProduct.getId()))
                .thenReturn(Optional.of(testWishlist));

        // Act
        wishlistService.toggleWishlist(testUser.getEmail(), testProduct.getId());

        // Assert
        verify(wishlistRepository).delete(testWishlist);
        verify(wishlistRepository, never()).save(any());
    }

    @Test
    void toggleWishlist_WhenWishlistDoesNotExist_ShouldCreateIt() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(wishlistRepository.findByUserIdAndProductId(testUser.getId(), testProduct.getId()))
                .thenReturn(Optional.empty());
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));

        // Act
        wishlistService.toggleWishlist(testUser.getEmail(), testProduct.getId());

        // Assert
        verify(wishlistRepository).save(any(Wishlist.class));
        verify(wishlistRepository, never()).delete(any());
    }

    @Test
    void checkWishlist_WhenUserExists_ShouldReturnBoolean() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(wishlistRepository.existsByUserIdAndProductId(testUser.getId(), testProduct.getId())).thenReturn(true);

        // Act
        boolean result = wishlistService.checkWishlist(testUser.getEmail(), testProduct.getId());

        // Assert
        assertTrue(result);
    }
}
