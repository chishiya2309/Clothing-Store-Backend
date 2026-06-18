package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.exception.PaymentInitializationException;

@Component
public class MomoAdapter implements PaymentGatewayAdapter {

    @Value("${payment.momo.enabled:false}")
    private boolean enabled;

    @Override
    public PaymentMethod supportMethod() {
        return PaymentMethod.momo;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public GatewayPaymentCreationResult createPayment(GatewayPaymentCreationCommand command) {
        if (!enabled || !isAvailable()) {
            throw new PaymentInitializationException("MoMo adapter is not configured");
        }
        throw new PaymentInitializationException("MoMo sandbox URL creation is not implemented without signed gateway configuration");
    }
}
