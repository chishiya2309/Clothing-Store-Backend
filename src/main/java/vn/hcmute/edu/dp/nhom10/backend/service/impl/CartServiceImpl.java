package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.AddToCartRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.CartSyncItem;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.CartSyncRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.CartItemResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.CartResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.CartItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductImage;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.enums.ImageType;
import vn.hcmute.edu.dp.nhom10.backend.exception.InsufficientStockException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.CartItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.CartService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        List<CartItem> cartItems = cartItemRepository.findAllByUserId(user.getId());

        List<CartItemResponse> items = cartItems.stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .items(items)
                .totalAmount(totalAmount)
                .build();
    }

    @Override
    @Transactional
    public CartItemResponse addToCart(String email, AddToCartRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String trimmedSize = request.size().trim();
        String trimmedColor = request.color().trim();
        ProductVariant variant = productVariantRepository.findByProductIdAndSizeIgnoreCaseAndColorIgnoreCaseAndIsActiveTrue(
                request.productId(), trimmedSize, trimmedColor)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Không tìm thấy biến thể sản phẩm hoạt động với ID sản phẩm: %d, Size: %s, Màu: %s", 
                                request.productId(), trimmedSize, trimmedColor)));

        // QĐ10: Kiểm tra tồn kho trước khi thêm
        if (variant.getStockQuantity() < request.quantity()) {
            throw new InsufficientStockException("Sản phẩm không đủ số lượng tồn kho. Tồn kho hiện tại: " + variant.getStockQuantity());
        }

        CartItem cartItem = cartItemRepository.findByUserIdAndProductVariantId(user.getId(), variant.getId())
                .orElse(null);

        if (cartItem != null) {
            int newQuantity = cartItem.getQuantity() + request.quantity();
            if (variant.getStockQuantity() < newQuantity) {
                throw new InsufficientStockException("Tổng số lượng trong giỏ hàng vượt quá tồn kho. Tồn kho hiện tại: " + variant.getStockQuantity());
            }
            cartItem.setQuantity(newQuantity);
        } else {
            cartItem = CartItem.builder()
                    .user(user)
                    .productVariant(variant)
                    .quantity(request.quantity())
                    .build();
        }

        CartItem savedItem = cartItemRepository.save(cartItem);
        return mapToCartItemResponse(savedItem);
    }

    @Override
    @Transactional
    public CartItemResponse updateQuantity(String email, Long itemId, Integer quantity) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + itemId));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("This cart item does not belong to the authenticated user");
        }

        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be greater than or equal to 0");
        }

        if (quantity == 0) {
            cartItemRepository.delete(cartItem);
            return null;
        }

        ProductVariant variant = cartItem.getProductVariant();
        if (!variant.getIsActive()) {
            throw new IllegalArgumentException("Product variant is inactive");
        }

        if (variant.getStockQuantity() < quantity) {
            throw new InsufficientStockException("Sản phẩm không đủ số lượng tồn kho. Tồn kho hiện tại: " + variant.getStockQuantity());
        }

        cartItem.setQuantity(quantity);
        CartItem savedItem = cartItemRepository.save(cartItem);
        return mapToCartItemResponse(savedItem);
    }

    @Override
    @Transactional
    public void removeItem(String email, Long itemId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + itemId));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("This cart item does not belong to the authenticated user");
        }

        cartItemRepository.delete(cartItem);
    }

    @Override
    @Transactional
    public void clearCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        cartItemRepository.deleteAllByUserId(user.getId());
    }

    private CartItemResponse mapToCartItemResponse(CartItem cartItem) {
        ProductVariant variant = cartItem.getProductVariant();
        BigDecimal basePrice = variant.getProduct().getSalePrice() != null 
                ? variant.getProduct().getSalePrice() 
                : variant.getProduct().getBasePrice();
        BigDecimal unitPrice = basePrice.add(variant.getAdditionalPrice());
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        String imageUrl = variant.getProduct().getImages().stream()
                .filter(img -> img.getImageType() == ImageType.main)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElseGet(() -> variant.getProduct().getImages().stream()
                        .filter(img -> img.getImageType() == ImageType.thumbnail)
                        .map(ProductImage::getImageUrl)
                        .findFirst()
                        .orElseGet(() -> variant.getProduct().getImages().stream()
                                .map(ProductImage::getImageUrl)
                                .findFirst()
                                .orElse(null)));

        return CartItemResponse.builder()
                .id(cartItem.getId())
                .productVariantId(variant.getId())
                .productId(variant.getProduct().getId())
                .productName(variant.getProduct().getName())
                .size(variant.getSize())
                .color(variant.getColor())
                .sku(variant.getSku())
                .imageUrl(imageUrl)
                .unitPrice(unitPrice)
                .quantity(cartItem.getQuantity())
                .subtotal(subtotal)
                .build();
    }

    @Override
    @Transactional
    public CartResponse syncCart(String email, CartSyncRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (request.items() != null) {
            for (CartSyncItem item : request.items()) {
                String trimmedSize = item.size().trim();
                String trimmedColor = item.color().trim();

                // 2. Bỏ qua sản phẩm không tìm thấy hoặc inactive
                var variantOpt = productVariantRepository.findByProductIdAndSizeIgnoreCaseAndColorIgnoreCaseAndIsActiveTrue(
                        item.productId(), trimmedSize, trimmedColor);
                if (variantOpt.isEmpty()) {
                    continue;
                }

                ProductVariant variant = variantOpt.get();

                // Tìm sản phẩm trong giỏ hàng hiện tại của khách hàng
                CartItem cartItem = cartItemRepository.findByUserIdAndProductVariantId(user.getId(), variant.getId())
                        .orElse(null);

                int targetQuantity;
                if (cartItem != null) {
                    targetQuantity = cartItem.getQuantity() + item.quantity();
                } else {
                    targetQuantity = item.quantity();
                }

                // 1. Tự động giới hạn (cap) bằng số lượng tồn kho tối đa
                if (targetQuantity > variant.getStockQuantity()) {
                    targetQuantity = variant.getStockQuantity();
                }

                // Nếu tồn kho tối đa là 0, bỏ qua không thêm/cập nhật
                if (targetQuantity <= 0) {
                    continue;
                }

                if (cartItem != null) {
                    cartItem.setQuantity(targetQuantity);
                    cartItemRepository.save(cartItem);
                } else {
                    CartItem newCartItem = CartItem.builder()
                            .user(user)
                            .productVariant(variant)
                            .quantity(targetQuantity)
                            .build();
                    cartItemRepository.save(newCartItem);
                }
            }
        }

        return getCart(email);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getGuestCart(CartSyncRequest request) {
        if (request == null || request.items() == null) {
            return CartResponse.builder()
                    .items(List.of())
                    .totalAmount(BigDecimal.ZERO)
                    .build();
        }

        List<CartItemResponse> items = request.items().stream()
                .map(item -> {
                    String trimmedSize = item.size().trim();
                    String trimmedColor = item.color().trim();

                    var variantOpt = productVariantRepository.findByProductIdAndSizeIgnoreCaseAndColorIgnoreCaseAndIsActiveTrue(
                            item.productId(), trimmedSize, trimmedColor);
                    if (variantOpt.isEmpty()) {
                        return null;
                    }

                    ProductVariant variant = variantOpt.get();

                    int qty = item.quantity();
                    if (qty > variant.getStockQuantity()) {
                        qty = variant.getStockQuantity();
                    }
                    if (qty <= 0) {
                        return null;
                    }

                    BigDecimal basePrice = variant.getProduct().getSalePrice() != null 
                            ? variant.getProduct().getSalePrice() 
                            : variant.getProduct().getBasePrice();
                    BigDecimal unitPrice = basePrice.add(variant.getAdditionalPrice());
                    BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(qty));

                    String imageUrl = variant.getProduct().getImages().stream()
                            .filter(img -> img.getImageType() == ImageType.main)
                            .map(ProductImage::getImageUrl)
                            .findFirst()
                            .orElseGet(() -> variant.getProduct().getImages().stream()
                                    .filter(img -> img.getImageType() == ImageType.thumbnail)
                                    .map(ProductImage::getImageUrl)
                                    .findFirst()
                                    .orElseGet(() -> variant.getProduct().getImages().stream()
                                            .map(ProductImage::getImageUrl)
                                            .findFirst()
                                            .orElse(null)));

                    return CartItemResponse.builder()
                            .id(null)
                            .productVariantId(variant.getId())
                            .productId(variant.getProduct().getId())
                            .productName(variant.getProduct().getName())
                            .size(variant.getSize())
                            .color(variant.getColor())
                            .sku(variant.getSku())
                            .imageUrl(imageUrl)
                            .unitPrice(unitPrice)
                            .quantity(qty)
                            .subtotal(subtotal)
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .items(items)
                .totalAmount(totalAmount)
                .build();
    }
}
