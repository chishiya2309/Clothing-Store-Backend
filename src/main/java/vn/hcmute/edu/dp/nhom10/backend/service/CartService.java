package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.request.AddToCartRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.CartSyncRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.CartItemResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart(String email);
    CartItemResponse addToCart(String email, AddToCartRequest request);
    CartItemResponse updateQuantity(String email, Long itemId, Integer quantity);
    void removeItem(String email, Long itemId);
    void clearCart(String email);
    CartResponse syncCart(String email, CartSyncRequest request);
    CartResponse getGuestCart(CartSyncRequest request);
}
