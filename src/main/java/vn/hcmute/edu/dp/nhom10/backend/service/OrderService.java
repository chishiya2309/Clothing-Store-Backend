package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.response.OrderResponseDTO;

public interface OrderService {

    OrderResponseDTO createCodOrder(
            String checkoutCode,
            Long userId
    );
}
