package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.dto.pricing.ProductPricingResult;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.CartSyncItem;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.CartSyncRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.CartResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.CartItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.enums.PriceSource;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.CartItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.CartServiceImpl;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplWhiteBoxTest {
    private static final String EMAIL = "customer@example.com";
    private static final Long USER_ID = 1L;
    private static final BigDecimal UNIT_PRICE = BigDecimal.valueOf(100_000);

    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductVariantRepository productVariantRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductPricingService productPricingService;
    @InjectMocks private CartServiceImpl cartService;

    private final User user = User.builder().id(USER_ID).email(EMAIL).build();
    private final List<CartItem> persistedCart = new ArrayList<>();

    @Test
    void wbC01_nullItems_returnsEmptyCart() {
        givenUserAndCart();

        CartResponse response = cartService.syncCart(EMAIL, new CartSyncRequest(null));

        assertEquals(List.of(), response.getItems());
        assertEquals(BigDecimal.ZERO, response.getTotalAmount());
        verifyNoInteractions(productVariantRepository);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void wbC02_emptyItems_skipsLoop() {
        givenUserAndCart();

        CartResponse response = cartService.syncCart(EMAIL, request());

        assertEquals(List.of(), response.getItems());
        verifyNoInteractions(productVariantRepository);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void wbC03_unknownVariant_continuesWithoutSaving() {
        givenUserAndCart();
        when(productVariantRepository.findByProductIdAndSizeIgnoreCaseAndColorIgnoreCaseAndIsActiveTrue(
                10L, "M", "Black")).thenReturn(Optional.empty());

        CartResponse response = cartService.syncCart(EMAIL, request(item(10L, 2)));

        assertEquals(List.of(), response.getItems());
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void wbC04_existingItem_withoutCap_increasesQuantity() {
        givenUserAndCart();
        ProductVariant variant = variant(10L, 5);
        givenVariant(variant);
        CartItem existing = cartItem(501L, variant, 2);
        persistedCart.add(existing);
        when(cartItemRepository.findByUserIdAndProductVariantId(USER_ID, 10L)).thenReturn(Optional.of(existing));
        givenSave();
        givenPrice();

        CartResponse response = cartService.syncCart(EMAIL, request(item(10L, 1)));

        assertEquals(3, existing.getQuantity());
        assertCart(response, 1, BigDecimal.valueOf(300_000));
        verify(cartItemRepository).save(existing);
    }

    @Test
    void wbC05_newItem_withoutCap_isSaved() {
        givenUserAndCart();
        ProductVariant variant = variant(10L, 5);
        givenVariant(variant);
        when(cartItemRepository.findByUserIdAndProductVariantId(USER_ID, 10L)).thenReturn(Optional.empty());
        givenSave();
        givenPrice();

        CartResponse response = cartService.syncCart(EMAIL, request(item(10L, 2)));

        assertCart(response, 1, BigDecimal.valueOf(200_000));
        assertEquals(2, persistedCart.get(0).getQuantity());
        assertEquals(USER_ID, persistedCart.get(0).getUser().getId());
    }

    @Test
    void wbC06_newItem_aboveStock_isCappedAndSaved() {
        givenUserAndCart();
        ProductVariant variant = variant(10L, 5);
        givenVariant(variant);
        when(cartItemRepository.findByUserIdAndProductVariantId(USER_ID, 10L)).thenReturn(Optional.empty());
        givenSave();
        givenPrice();

        CartResponse response = cartService.syncCart(EMAIL, request(item(10L, 8)));

        assertCart(response, 1, BigDecimal.valueOf(500_000));
        assertEquals(5, persistedCart.get(0).getQuantity());
    }

    @Test
    void wbC07_zeroStock_skipsNewItemAfterCap() {
        givenUserAndCart();
        givenVariant(variant(10L, 0));
        when(cartItemRepository.findByUserIdAndProductVariantId(USER_ID, 10L)).thenReturn(Optional.empty());

        CartResponse response = cartService.syncCart(EMAIL, request(item(10L, 1)));

        assertEquals(List.of(), response.getItems());
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void wbC08_twoItems_traversesLoopBackEdge() {
        givenUserAndCart();
        givenVariant(variant(10L, 5));
        givenVariant(variant(20L, 5));
        when(cartItemRepository.findByUserIdAndProductVariantId(eq(USER_ID), any(Long.class)))
                .thenReturn(Optional.empty());
        givenSave();
        givenPrice();

        CartResponse response = cartService.syncCart(EMAIL, request(item(10L, 1), item(20L, 2)));

        assertCart(response, 2, BigDecimal.valueOf(300_000));
        assertEquals(List.of(10L, 20L), persistedCart.stream()
                .map(cartItem -> cartItem.getProductVariant().getId()).toList());
    }

    @Test
    void wbC09_existingItemAtZeroStock_isNotUpdated() {
        givenUserAndCart();
        ProductVariant variant = variant(10L, 0);
        givenVariant(variant);
        CartItem existing = cartItem(501L, variant, 2);
        persistedCart.add(existing);
        when(cartItemRepository.findByUserIdAndProductVariantId(USER_ID, 10L)).thenReturn(Optional.of(existing));
        givenPrice();

        CartResponse response = cartService.syncCart(EMAIL, request(item(10L, 1)));

        assertEquals(2, existing.getQuantity());
        assertCart(response, 1, BigDecimal.valueOf(200_000));
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void wbC10_unknownUser_failsBeforeLoop() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.syncCart(EMAIL, request(item(10L, 1))));

        verifyNoInteractions(productVariantRepository, cartItemRepository);
    }

    private void givenUserAndCart() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(cartItemRepository.findAllByUserId(USER_ID)).thenAnswer(invocation -> List.copyOf(persistedCart));
    }

    private void givenVariant(ProductVariant variant) {
        when(productVariantRepository.findByProductIdAndSizeIgnoreCaseAndColorIgnoreCaseAndIsActiveTrue(
                variant.getProduct().getId(), "M", "Black")).thenReturn(Optional.of(variant));
    }

    private void givenSave() {
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(501L + persistedCart.size());
            }
            if (!persistedCart.contains(saved)) {
                persistedCart.add(saved);
            }
            return saved;
        });
    }

    private void givenPrice() {
        when(productPricingService.resolve(any(Product.class), any(ProductVariant.class), any(OffsetDateTime.class)))
                .thenReturn(new ProductPricingResult(UNIT_PRICE, BigDecimal.ZERO, UNIT_PRICE, PriceSource.REGULAR, null));
    }

    private ProductVariant variant(Long productId, int stockQuantity) {
        Product product = Product.builder().id(productId).name("Product " + productId)
                .images(List.of()).build();
        return ProductVariant.builder().id(productId).product(product).size("M").color("Black")
                .sku("SKU-" + productId).stockQuantity(stockQuantity).isActive(true).build();
    }

    private CartItem cartItem(Long id, ProductVariant variant, int quantity) {
        return CartItem.builder().id(id).user(user).productVariant(variant).quantity(quantity).build();
    }

    private CartSyncItem item(Long productId, int quantity) {
        return new CartSyncItem(productId, " M ", " Black ", quantity);
    }

    private CartSyncRequest request(CartSyncItem... items) {
        return new CartSyncRequest(List.of(items));
    }

    private void assertCart(CartResponse response, int expectedItems, BigDecimal expectedTotal) {
        assertEquals(expectedItems, response.getItems().size());
        assertEquals(expectedTotal, response.getTotalAmount());
    }
}
