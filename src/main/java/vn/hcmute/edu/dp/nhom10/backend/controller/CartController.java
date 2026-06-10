package vn.hcmute.edu.dp.nhom10.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.AddToCartRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.CartSyncRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.CartItemResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.CartResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.CartService;

import java.security.Principal;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/customer/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Quản lý giỏ hàng của khách hàng")
@Slf4j(topic = "CART-CONTROLLER")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse getCart(Principal principal) {
        log.info("Fetching cart for user: {}", principal.getName());
        CartResponse cartResponse = cartService.getCart(principal.getName());
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Fetch cart successfully")
                .data(cartResponse)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse addToCart(@Valid @RequestBody AddToCartRequest request, Principal principal) {
        log.info("Adding item to cart for user: {}, product ID: {}, size: {}, color: {}, qty: {}", 
                principal.getName(), request.productId(), request.size(), request.color(), request.quantity());
        CartItemResponse cartItemResponse = cartService.addToCart(principal.getName(), request);
        return ApiResponse.builder()
                .status(HttpStatus.CREATED.value())
                .message("Add item to cart successfully")
                .data(cartItemResponse)
                .timestamp(OffsetDateTime.now())
                .build();
     }

    @PutMapping("/items/{itemId}")
    public ApiResponse updateQuantity(
            @PathVariable Long itemId,
            @RequestParam Integer quantity,
            Principal principal) {
        log.info("Updating quantity for cart item: {}, user: {}, new qty: {}", 
                itemId, principal.getName(), quantity);
        CartItemResponse cartItemResponse = cartService.updateQuantity(principal.getName(), itemId, quantity);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Update quantity successfully")
                .data(cartItemResponse)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse removeItem(@PathVariable Long itemId, Principal principal) {
        log.info("Removing cart item: {}, user: {}", itemId, principal.getName());
        cartService.removeItem(principal.getName(), itemId);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Remove item from cart successfully")
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @DeleteMapping
    public ApiResponse clearCart(Principal principal) {
        log.info("Clearing cart for user: {}", principal.getName());
        cartService.clearCart(principal.getName());
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Clear cart successfully")
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PostMapping("/sync")
    public ApiResponse syncCart(@Valid @RequestBody CartSyncRequest request, Principal principal) {
        log.info("Syncing cart for user: {}, item count: {}", principal.getName(), 
                request.items() != null ? request.items().size() : 0);
        CartResponse cartResponse = cartService.syncCart(principal.getName(), request);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Sync cart successfully")
                .data(cartResponse)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
