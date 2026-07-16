package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.CheckoutData;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.ResolvedProductPrice;
import vn.hcmute.edu.dp.nhom10.backend.entity.Address;
import vn.hcmute.edu.dp.nhom10.backend.entity.CartItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.enums.PriceSource;
import vn.hcmute.edu.dp.nhom10.backend.repository.AddressRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.CartItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.CheckoutDataServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutDataServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private FlashSalePricingService flashSalePricingService;

    @InjectMocks
    private CheckoutDataServiceImpl checkoutDataService;

    @BeforeEach
    void setUpPricing() {
        lenient().when(flashSalePricingService.resolve(any(Product.class), any()))
                .thenAnswer(invocation -> {
                    Product product = invocation.getArgument(0);
                    if (product.getSalePrice() != null) {
                        return new ResolvedProductPrice(product.getSalePrice(), PriceSource.PRODUCT_SALE, null);
                    }
                    return new ResolvedProductPrice(product.getBasePrice(), PriceSource.REGULAR, null);
                });
    }

    @Test
    void getCheckoutData_addressNotFound_throwsException() {
        when(addressRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> checkoutDataService.getCheckoutData(10L, 1L));

        verify(cartItemRepository, never()).findCheckoutItemsByUserId(10L);
    }

    @Test
    void getCheckoutData_addressNotOwnedByUser_throwsException() {
        when(addressRepository.findByIdAndUserId(1L, 20L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> checkoutDataService.getCheckoutData(20L, 1L));

        verify(cartItemRepository, never()).findCheckoutItemsByUserId(20L);
    }

    @Test
    void getCheckoutData_emptyCart_throwsException() {
        when(addressRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(address(1L, 10L)));
        when(cartItemRepository.findCheckoutItemsByUserId(10L)).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> checkoutDataService.getCheckoutData(10L, 1L));
    }

    @Test
    void getCheckoutData_productInactive_throwsException() {
        Product product = product(1L, "T-Shirt", "100000.00", null, false);
        ProductVariant variant = variant(2L, "10000.00", true);
        CartItem cartItem = cartItem(3L, product, variant, 1);
        mockCheckoutCart(List.of(cartItem));

        assertThrows(IllegalArgumentException.class, () -> checkoutDataService.getCheckoutData(10L, 1L));
    }

    @Test
    void getCheckoutData_productVariantInactive_throwsException() {
        Product product = product(1L, "T-Shirt", "100000.00", null, true);
        ProductVariant variant = variant(2L, "10000.00", false);
        CartItem cartItem = cartItem(3L, product, variant, 1);
        mockCheckoutCart(List.of(cartItem));

        assertThrows(IllegalArgumentException.class, () -> checkoutDataService.getCheckoutData(10L, 1L));
    }

    @Test
    void getCheckoutData_usesSalePriceWhenPresent() {
        Product product = product(1L, "T-Shirt", "100000.00", "80000.00", true);
        ProductVariant variant = variant(2L, "10000.00", true);
        CartItem cartItem = cartItem(3L, product, variant, 1);
        mockCheckoutCart(List.of(cartItem));

        CheckoutData result = checkoutDataService.getCheckoutData(10L, 1L);

        assertEquals(new BigDecimal("90000.00"), result.items().get(0).unitPrice());
        assertEquals(new BigDecimal("90000.00"), result.subtotal());
    }

    @Test
    void getCheckoutData_usesBasePriceWhenSalePriceMissing() {
        Product product = product(1L, "T-Shirt", "100000.00", null, true);
        ProductVariant variant = variant(2L, "10000.00", true);
        CartItem cartItem = cartItem(3L, product, variant, 1);
        mockCheckoutCart(List.of(cartItem));

        CheckoutData result = checkoutDataService.getCheckoutData(10L, 1L);

        assertEquals(new BigDecimal("110000.00"), result.items().get(0).unitPrice());
    }

    @Test
    void getCheckoutData_addsAdditionalPrice() {
        Product product = product(1L, "T-Shirt", "100000.00", null, true);
        ProductVariant variant = variant(2L, "25000.00", true);
        CartItem cartItem = cartItem(3L, product, variant, 1);
        mockCheckoutCart(List.of(cartItem));

        CheckoutData result = checkoutDataService.getCheckoutData(10L, 1L);

        assertEquals(new BigDecimal("125000.00"), result.items().get(0).unitPrice());
    }

    @Test
    void getCheckoutData_nullAdditionalPriceAsZero() {
        Product product = product(1L, "T-Shirt", "100000.00", null, true);
        ProductVariant variant = variant(2L, null, true);
        CartItem cartItem = cartItem(3L, product, variant, 1);
        mockCheckoutCart(List.of(cartItem));

        CheckoutData result = checkoutDataService.getCheckoutData(10L, 1L);

        assertEquals(new BigDecimal("100000.00"), result.items().get(0).unitPrice());
    }

    @Test
    void getCheckoutData_calculatesSubtotalForMultipleItems() {
        Product firstProduct = product(1L, "T-Shirt", "100000.00", "80000.00", true);
        ProductVariant firstVariant = variant(2L, "10000.00", true);
        Product secondProduct = product(4L, "Jeans", "200000.00", null, true);
        ProductVariant secondVariant = variant(5L, "0.00", true);
        CartItem firstItem = cartItem(3L, firstProduct, firstVariant, 2);
        CartItem secondItem = cartItem(6L, secondProduct, secondVariant, 1);
        mockCheckoutCart(List.of(firstItem, secondItem));

        CheckoutData result = checkoutDataService.getCheckoutData(10L, 1L);

        assertEquals(new BigDecimal("180000.00"), result.items().get(0).subtotal());
        assertEquals(new BigDecimal("200000.00"), result.items().get(1).subtotal());
        assertEquals(new BigDecimal("380000.00"), result.subtotal());
    }

    @Test
    void getCheckoutData_subtotalBelowFreeShippingThreshold_usesStandardShippingFee() {
        Product product = product(1L, "T-Shirt", "470000.00", null, true);
        ProductVariant variant = variant(2L, "0.00", true);
        CartItem cartItem = cartItem(3L, product, variant, 1);
        mockCheckoutCart(List.of(cartItem));

        CheckoutData result = checkoutDataService.getCheckoutData(10L, 1L);

        assertEquals(new BigDecimal("470000.00"), result.subtotal());
        assertEquals(new BigDecimal("30000.00"), result.shippingFee());
    }

    @Test
    void getCheckoutData_subtotalEqualFreeShippingThreshold_usesStandardShippingFee() {
        Product product = product(1L, "T-Shirt", "500000.00", null, true);
        ProductVariant variant = variant(2L, "0.00", true);
        CartItem cartItem = cartItem(3L, product, variant, 1);
        mockCheckoutCart(List.of(cartItem));

        CheckoutData result = checkoutDataService.getCheckoutData(10L, 1L);

        assertEquals(new BigDecimal("500000.00"), result.subtotal());
        assertEquals(new BigDecimal("30000.00"), result.shippingFee());
    }

    @Test
    void getCheckoutData_subtotalGreaterThanFreeShippingThreshold_usesFreeShipping() {
        Product product = product(1L, "T-Shirt", "500001.00", null, true);
        ProductVariant variant = variant(2L, "0.00", true);
        CartItem cartItem = cartItem(3L, product, variant, 1);
        mockCheckoutCart(List.of(cartItem));

        CheckoutData result = checkoutDataService.getCheckoutData(10L, 1L);

        assertEquals(new BigDecimal("500001.00"), result.subtotal());
        assertEquals(BigDecimal.ZERO, result.shippingFee());
    }

    @Test
    void getCheckoutData_buildsAddressSnapshot() {
        Product product = product(1L, "T-Shirt", "100000.00", null, true);
        ProductVariant variant = variant(2L, "0.00", true);
        CartItem cartItem = cartItem(3L, product, variant, 1);
        mockCheckoutCart(List.of(cartItem));

        CheckoutData result = checkoutDataService.getCheckoutData(10L, 1L);

        assertEquals("Nguyen Van A", result.addressSnapshot().recipientName());
        assertEquals("0900000000", result.addressSnapshot().phone());
        assertEquals("Ho Chi Minh", result.addressSnapshot().province());
        assertEquals("District 1", result.addressSnapshot().district());
        assertEquals("Ben Nghe", result.addressSnapshot().ward());
        assertEquals("1 Le Loi", result.addressSnapshot().streetAddress());
    }

    @Test
    void getCheckoutData_usesOnlyDatabasePrice() {
        Product product = product(1L, "T-Shirt", "100000.00", "80000.00", true);
        ProductVariant variant = variant(2L, "5000.00", true);
        CartItem cartItem = cartItem(3L, product, variant, 1);
        mockCheckoutCart(List.of(cartItem));

        CheckoutData result = checkoutDataService.getCheckoutData(10L, 1L);

        assertEquals(new BigDecimal("85000.00"), result.items().get(0).unitPrice());
        verify(cartItemRepository).findCheckoutItemsByUserId(10L);
    }

    @Test
    void getCheckoutData_flashSale_snapshotsPriceSourceItemAndVariantPrice() {
        Product product = product(1L, "T-Shirt", "100000.00", "80000.00", true);
        ProductVariant variant = variant(2L, "5000.00", true);
        CartItem cartItem = cartItem(3L, product, variant, 2);
        mockCheckoutCart(List.of(cartItem));
        when(flashSalePricingService.resolve(eq(product), any()))
                .thenReturn(new ResolvedProductPrice(new BigDecimal("60000.00"), PriceSource.FLASH_SALE, 9L));

        CheckoutData result = checkoutDataService.getCheckoutData(10L, 1L);

        assertEquals(new BigDecimal("65000.00"), result.items().get(0).unitPrice());
        assertEquals(new BigDecimal("130000.00"), result.items().get(0).subtotal());
        assertEquals(PriceSource.FLASH_SALE, result.items().get(0).priceSource());
        assertEquals(9L, result.items().get(0).flashSaleItemId());
        assertEquals(new BigDecimal("130000.00"), result.subtotal());
    }

    private void mockCheckoutCart(List<CartItem> cartItems) {
        when(addressRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(address(1L, 10L)));
        when(cartItemRepository.findCheckoutItemsByUserId(10L)).thenReturn(cartItems);
    }

    private Address address(Long id, Long userId) {
        return Address.builder()
                .id(id)
                .user(User.builder().id(userId).build())
                .recipientName("Nguyen Van A")
                .phone("0900000000")
                .province("Ho Chi Minh")
                .district("District 1")
                .ward("Ben Nghe")
                .streetAddress("1 Le Loi")
                .build();
    }

    private CartItem cartItem(Long id, Product product, ProductVariant variant, Integer quantity) {
        variant.setProduct(product);
        return CartItem.builder()
                .id(id)
                .productVariant(variant)
                .quantity(quantity)
                .build();
    }

    private Product product(Long id, String name, String basePrice, String salePrice, boolean active) {
        return Product.builder()
                .id(id)
                .name(name)
                .basePrice(new BigDecimal(basePrice))
                .salePrice(salePrice == null ? null : new BigDecimal(salePrice))
                .isActive(active)
                .build();
    }

    private ProductVariant variant(Long id, String additionalPrice, boolean active) {
        return ProductVariant.builder()
                .id(id)
                .sku("SKU-" + id)
                .size("M")
                .color("Black")
                .additionalPrice(additionalPrice == null ? null : new BigDecimal(additionalPrice))
                .isActive(active)
                .build();
    }
}
