package vn.hcmute.edu.dp.nhom10.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ConfirmCheckoutRequestDTO;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PlaceOrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.security.AuthenticatedUserProvider;
import vn.hcmute.edu.dp.nhom10.backend.security.ClientIpResolver;
import vn.hcmute.edu.dp.nhom10.backend.service.PlaceOrderService;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/checkouts")
@RequiredArgsConstructor
@Tag(name = "Checkout", description = "Checkout confirmation")
@Slf4j(topic = "CHECKOUT-CONTROLLER")
public class CheckoutController {

    private final PlaceOrderService placeOrderService;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final ClientIpResolver clientIpResolver;

    @PostMapping("/confirm")
    public ApiResponse confirmCheckout(
            @Valid @RequestBody ConfirmCheckoutRequestDTO requestDTO,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        Long userId = authenticatedUserProvider.getCurrentUserId(authentication);
        String clientIp = clientIpResolver.resolve(httpRequest);
        log.info("Confirming checkout for authenticated user: {}", authentication.getName());
        PlaceOrderResponseDTO response = placeOrderService.confirmCheckout(requestDTO, userId, clientIp);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Confirm checkout successfully")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
