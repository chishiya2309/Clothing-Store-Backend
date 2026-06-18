package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.AddressSnapshot;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.CheckoutData;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.CheckoutItemSnapshot;
import vn.hcmute.edu.dp.nhom10.backend.entity.Address;
import vn.hcmute.edu.dp.nhom10.backend.entity.CartItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.Product;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.AddressRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.CartItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.CheckoutDataService;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckoutDataServiceImpl implements CheckoutDataService {

    private final AddressRepository addressRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    @Transactional(readOnly = true)
    public CheckoutData getCheckoutData(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));

        List<CartItem> cartItems = cartItemRepository.findCheckoutItemsByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        List<CheckoutItemSnapshot> items = cartItems.stream()
                .map(this::toItemSnapshot)
                .toList();

        BigDecimal subtotal = items.stream()
                .map(CheckoutItemSnapshot::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CheckoutData(
                userId,
                addressId,
                toAddressSnapshot(address),
                items,
                subtotal,
                BigDecimal.ZERO
        );
    }

    private CheckoutItemSnapshot toItemSnapshot(CartItem cartItem) {
        Integer quantity = cartItem.getQuantity();
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Cart item quantity is invalid");
        }

        ProductVariant variant = cartItem.getProductVariant();
        if (variant == null) {
            throw new ResourceNotFoundException("Product variant not found for cart item: " + cartItem.getId());
        }

        Product product = variant.getProduct();
        if (product == null) {
            throw new ResourceNotFoundException("Product not found for variant: " + variant.getId());
        }

        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new IllegalArgumentException("Product is inactive: " + product.getName());
        }

        if (!Boolean.TRUE.equals(variant.getIsActive())) {
            throw new IllegalArgumentException("Product variant is inactive: " + variant.getId());
        }

        BigDecimal productPrice = product.getSalePrice() != null ? product.getSalePrice() : product.getBasePrice();
        if (productPrice == null) {
            throw new IllegalArgumentException("Product price is missing: " + product.getId());
        }

        BigDecimal additionalPrice = variant.getAdditionalPrice() != null ? variant.getAdditionalPrice() : BigDecimal.ZERO;
        BigDecimal unitPrice = productPrice.add(additionalPrice);
        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price must not be negative");
        }

        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        return new CheckoutItemSnapshot(
                cartItem.getId(),
                variant.getId(),
                product.getName(),
                buildVariantInfo(variant),
                quantity,
                unitPrice,
                subtotal
        );
    }

    private AddressSnapshot toAddressSnapshot(Address address) {
        return new AddressSnapshot(
                address.getRecipientName(),
                address.getPhone(),
                address.getProvince(),
                address.getDistrict(),
                address.getWard(),
                address.getStreetAddress()
        );
    }

    private String buildVariantInfo(ProductVariant variant) {
        return String.format("Size: %s, Color: %s", variant.getSize(), variant.getColor());
    }
}
