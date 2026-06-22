package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffOrderDetailResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffOrderListItemResponse;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;

import java.time.LocalDate;

public interface StaffOrderService {
    PageResponse<StaffOrderListItemResponse> getOrders(
            OrderStatus status,
            LocalDate fromDate,
            LocalDate toDate,
            String keyword,
            int page,
            int size,
            String sortBy,
            String sortDir
    );

    StaffOrderDetailResponse getOrderDetail(String orderCode);
}
