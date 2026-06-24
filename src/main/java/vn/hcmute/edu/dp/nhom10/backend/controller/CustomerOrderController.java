package vn.hcmute.edu.dp.nhom10.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.ApiResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.OrderDetailResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.OrderHistoryItemResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.service.OrderService;

import java.security.Principal;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/customer/orders")
@RequiredArgsConstructor
@Tag(name = "Customer Orders", description = "Lịch sử đơn hàng của khách hàng")
@Slf4j(topic = "ORDER-HISTORY-CONTROLLER")
public class CustomerOrderController {

        private final OrderService orderService;

        @GetMapping
        public ApiResponse getOrderHistory(
                        Principal principal,
                        @RequestParam(required = false) OrderStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                log.info("Fetching order history for user: {}, status: {}", principal.getName(), status);
                PageResponse<OrderHistoryItemResponse> result = orderService.getOrderHistory(
                                principal.getName(), status, page, size);

                return ApiResponse.builder()
                                .status(HttpStatus.OK.value())
                                .message("Get order history successfully!")
                                .data(result)
                                .timestamp(OffsetDateTime.now())
                                .build();
        }

        @GetMapping("/{orderCode}")
        public ApiResponse getOrderDetail(
                        Principal principal,
                        @PathVariable String orderCode) {
                log.info("Fetching order detail for user: {}, orderCode: {}", principal.getName(), orderCode);
                OrderDetailResponse result = orderService.getOrderDetail(orderCode, principal.getName());

                return ApiResponse.builder()
                                .status(HttpStatus.OK.value())
                                .message("Get order detail successfully!")
                                .data(result)
                                .timestamp(OffsetDateTime.now())
                                .build();
        }

        @PostMapping("/{orderCode}/cancel")
        public ApiResponse cancelOrder(
                        Principal principal,
                        @PathVariable String orderCode) {
                log.info("Cancelling order for user: {}, orderCode: {}", principal.getName(), orderCode);
                orderService.cancelOrder(orderCode, principal.getName());

                return ApiResponse.builder()
                                .status(HttpStatus.OK.value())
                                .message("Cancel order successfully!")
                                .timestamp(OffsetDateTime.now())
                                .build();
        }
}
