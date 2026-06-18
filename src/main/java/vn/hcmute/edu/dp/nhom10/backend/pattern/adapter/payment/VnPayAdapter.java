package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.exception.PaymentInitializationException;

@Component
public class VnPayAdapter implements PaymentGatewayAdapter {

    @Value("${payment.vnpay.enabled:false}")
    private boolean enabled;

    @Override
    public PaymentMethod supportMethod() {
        return PaymentMethod.vnpay;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public GatewayPaymentCreationResult createPayment(GatewayPaymentCreationCommand command) {
        if (!enabled || !isAvailable()) {
            throw new PaymentInitializationException("VNPay adapter is not configured");
        }
        throw new PaymentInitializationException("VNPay sandbox URL creation is not implemented without signed gateway configuration");
    }
}
