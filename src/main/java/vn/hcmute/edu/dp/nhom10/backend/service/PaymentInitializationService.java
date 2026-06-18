package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.payment.OnlinePaymentInitializationResult;

public interface PaymentInitializationService {

    OnlinePaymentInitializationResult initializeOnlinePayment(
            String checkoutCode,
            Long userId
    );
}
