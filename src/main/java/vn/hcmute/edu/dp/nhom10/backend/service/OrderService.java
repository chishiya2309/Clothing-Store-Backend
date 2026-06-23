package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.response.OrderDetailResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.OrderHistoryItemResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.OrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;

public interface OrderService {

    OrderResponseDTO createCodOrder(
            String checkoutCode,
            Long userId
    );

    PageResponse<OrderHistoryItemResponse> getOrderHistory(
            String email,
            OrderStatus status,
            int page,
            int size
    );

    OrderDetailResponse getOrderDetail(String orderCode, String email);

    void cancelOrder(String orderCode, String email);
}
