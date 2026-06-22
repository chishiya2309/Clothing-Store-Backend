package vn.hcmute.edu.dp.nhom10.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ProductGridResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.WishlistService;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Quản lý sản phẩm yêu thích của khách hàng")
@Slf4j(topic = "WISHLIST-CONTROLLER")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ApiResponse getWishlist(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
            
        log.info("Fetching wishlist for user: {}", principal.getName());
        PageResponse<ProductGridResponse> wishlistPage = wishlistService.getUserWishlist(principal.getName(), page, size);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Get wishlist successfully!")
                .data(wishlistPage)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PostMapping("/toggle")
    public ApiResponse toggleWishlist(
            Principal principal,
            @RequestParam Long productId) {
            
        log.info("Toggling wishlist for user: {}, product: {}", principal.getName(), productId);
        wishlistService.toggleWishlist(principal.getName(), productId);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Toggle wishlist successfully!")
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping("/check")
    public ApiResponse checkWishlist(
            Principal principal,
            @RequestParam Long productId) {
            
        boolean isWishlisted = wishlistService.checkWishlist(principal.getName(), productId);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Check wishlist successfully!")
                .data((java.io.Serializable) new java.util.HashMap<>(Map.of("isWishlisted", isWishlisted)))
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping("/ids")
    public ApiResponse getWishlistIds(Principal principal) {
        java.util.List<Long> ids = wishlistService.getWishlistProductIds(principal.getName());

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Get wishlist IDs successfully!")
                .data((java.io.Serializable) new java.util.ArrayList<>(ids))
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
