package vn.hcmute.edu.dp.nhom10.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.OrderDetailResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.OrderSummaryResponse;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.service.OrderService;

import java.io.Serializable;
import java.security.Principal;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/customer/orders")
@RequiredArgsConstructor
@Tag(name = "Customer Orders", description = "Xem lịch sử đơn hàng của chính mình và tự hủy đơn khi trạng thái cho phép")
@Slf4j(topic = "ORDER-CONTROLLER")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ApiResponse getOrderHistory(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        log.info("Fetching order history for user: {}, status: {}, page: {}, size: {}", 
                principal.getName(), status, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderSummaryResponse> history = orderService.getOrderHistory(principal.getName(), status, pageable);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Lấy lịch sử đơn hàng thành công")
                .data((Serializable) history)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @GetMapping("/{orderId}")
    public ApiResponse getOrderDetail(@PathVariable Long orderId, Principal principal) {
        log.info("Fetching order detail for user: {}, orderId: {}", principal.getName(), orderId);
        OrderDetailResponse detail = orderService.getOrderDetail(principal.getName(), orderId);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Lấy chi tiết đơn hàng thành công")
                .data(detail)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    @PatchMapping("/{orderId}/cancel")
    public ApiResponse cancelOrder(@PathVariable Long orderId, Principal principal) {
        log.info("Cancelling order for user: {}, orderId: {}", principal.getName(), orderId);
        orderService.cancelOrder(principal.getName(), orderId);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Hủy đơn hàng thành công")
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
