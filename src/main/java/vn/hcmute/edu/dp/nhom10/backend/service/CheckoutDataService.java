package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.CheckoutData;

public interface CheckoutDataService {
    CheckoutData getCheckoutData(Long userId, Long addressId);
}
