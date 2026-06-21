package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;

public interface PaymentGatewayAdapter {

    PaymentMethod supportMethod();

    boolean isAvailable();

    default String unavailableReason() {
        PaymentMethod method = supportMethod();
        return method == null ? "payment gateway" : method.name();
    }

    GatewayPaymentCreationResult createPayment(
            GatewayPaymentCreationCommand command
    );
}
