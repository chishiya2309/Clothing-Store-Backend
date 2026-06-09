package vn.hcmute.edu.dp.nhom10.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.CartSyncRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.CartResponse;
import vn.hcmute.edu.dp.nhom10.backend.service.CartService;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/guest/cart")
@RequiredArgsConstructor
@Tag(name = "Guest Cart", description = "Xử lý giỏ hàng của khách vãng lai")
@Slf4j(topic = "GUEST-CART-CONTROLLER")
public class GuestCartController {

    private final CartService cartService;

    @PostMapping
    public ApiResponse getGuestCart(@Valid @RequestBody CartSyncRequest request) {
        log.info("Fetching guest cart details for item count: {}", 
                request.items() != null ? request.items().size() : 0);
        CartResponse cartResponse = cartService.getGuestCart(request);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Fetch guest cart details successfully")
                .data(cartResponse)
                .timestamp(OffsetDateTime.now())
                .build();
     }

    @GetMapping
    public ApiResponse getEmptyGuestCart() {
        log.info("Fetching empty guest cart");
        CartResponse cartResponse = CartResponse.builder()
                .items(java.util.List.of())
                .totalAmount(java.math.BigDecimal.ZERO)
                .build();
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Fetch guest cart details successfully")
                .data(cartResponse)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
