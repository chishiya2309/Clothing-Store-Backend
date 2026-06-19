package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.request.ConfirmCheckoutRequestDTO;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PlaceOrderResponseDTO;

public interface PlaceOrderService {

    PlaceOrderResponseDTO confirmCheckout(
            ConfirmCheckoutRequestDTO requestDTO,
            Long userId,
            String clientIp
    );
}
