package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;

public interface PaymentGatewayAdapter {

    PaymentMethod supportMethod();

    GatewayPaymentCreationResult createPayment(
            GatewayPaymentCreationCommand command
    );
}
