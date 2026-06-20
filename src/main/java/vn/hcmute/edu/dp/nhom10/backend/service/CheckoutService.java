package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.ReservedCheckoutResult;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ConfirmCheckoutRequestDTO;

public interface CheckoutService {

    ReservedCheckoutResult prepareCheckout(
            ConfirmCheckoutRequestDTO requestDTO,
            Long userId
    );
}
