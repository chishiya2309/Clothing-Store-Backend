package vn.hcmute.edu.dp.nhom10.backend.controller.staff;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCancelOrderRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCompleteOrderRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffOrderDetailResponse;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.security.AuthenticatedUserProvider;
import vn.hcmute.edu.dp.nhom10.backend.service.StaffOrderService;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/staff/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class StaffOrderController {

    private final StaffOrderService staffOrderService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @GetMapping
    public ApiResponse getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Fetch staff order list successful")
                .data(staffOrderService.getOrders(status, fromDate, toDate, keyword, page, size, sortBy, sortDir))
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping("/{orderCode}")
    public ApiResponse getOrderDetail(@PathVariable String orderCode) {
        StaffOrderDetailResponse response = staffOrderService.getOrderDetail(orderCode);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Fetch staff order detail successful")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PatchMapping("/{orderCode}/confirm")
    public ApiResponse confirmOrder(@PathVariable String orderCode, Authentication authentication) {
        Long staffUserId = authenticatedUserProvider.getCurrentUserId(authentication);
        StaffOrderDetailResponse response = staffOrderService.confirmOrder(orderCode, staffUserId);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Confirm order successful")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PatchMapping("/{orderCode}/ship")
    public ApiResponse shipOrder(@PathVariable String orderCode, Authentication authentication) {
        Long staffUserId = authenticatedUserProvider.getCurrentUserId(authentication);
        StaffOrderDetailResponse response = staffOrderService.shipOrder(orderCode, staffUserId);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Ship order successful")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PatchMapping("/{orderCode}/complete")
    public ApiResponse completeOrder(
            @PathVariable String orderCode,
            @Valid @RequestBody StaffCompleteOrderRequest request,
            Authentication authentication
    ) {
        Long staffUserId = authenticatedUserProvider.getCurrentUserId(authentication);
        StaffOrderDetailResponse response = staffOrderService.completeOrder(orderCode, staffUserId, request);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Complete order successful")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PatchMapping("/{orderCode}/cancel")
    public ApiResponse cancelOrder(
            @PathVariable String orderCode,
            @Valid @RequestBody StaffCancelOrderRequest request,
            Authentication authentication
    ) {
        Long staffUserId = authenticatedUserProvider.getCurrentUserId(authentication);
        StaffOrderDetailResponse response = staffOrderService.cancelOrder(orderCode, staffUserId, request);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Cancel order successful")
                .data(response)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
