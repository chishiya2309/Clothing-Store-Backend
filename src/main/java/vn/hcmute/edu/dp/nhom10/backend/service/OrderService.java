package vn.hcmute.edu.dp.nhom10.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.OrderDetailResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.OrderSummaryResponse;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;

/**
 * Service phía Customer: Chỉ truy cập đơn hàng của chính người dùng đang đăng nhập.
 * Không có quyền xem hoặc thao tác đơn hàng của người khác.
 */
public interface OrderService {
    Page<OrderSummaryResponse> getOrderHistory(String email, OrderStatus status, Pageable pageable);
    OrderDetailResponse getOrderDetail(String email, Long orderId);
    void cancelOrder(String email, Long orderId);
}
