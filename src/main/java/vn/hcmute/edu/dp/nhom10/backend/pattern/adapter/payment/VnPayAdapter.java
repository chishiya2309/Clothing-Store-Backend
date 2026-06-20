package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.config.payment.VnPayProperties;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.exception.PaymentInitializationException;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class VnPayAdapter implements PaymentGatewayAdapter {

    private final VnPayProperties properties;
    private final VnPayPaymentUrlBuilder paymentUrlBuilder;

    @Override
    public PaymentMethod supportMethod() {
        return PaymentMethod.vnpay;
    }

    @Override
    public boolean isAvailable() {
        return properties.isAvailable();
    }

    @Override
    public String unavailableReason() {
        return properties.unavailableReason();
    }

    @Override
    public GatewayPaymentCreationResult createPayment(GatewayPaymentCreationCommand command) {
        if (!isAvailable()) {
            throw new PaymentInitializationException(
                    "VNPay adapter is not configured: " + properties.unavailableReason()
            );
        }
        VnPayPaymentUrlBuilder.VnPayPaymentUrl paymentUrl = paymentUrlBuilder.build(properties, command);
        Map<String, Object> gatewayPayload = sanitizedPayload(paymentUrl.parameters());
        return new GatewayPaymentCreationResult(
                paymentUrl.paymentUrl(),
                null,
                gatewayPayload
        );
    }

    private Map<String, Object> sanitizedPayload(Map<String, String> parameters) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("gateway", supportMethod().name());
        payload.put("version", parameters.get("vnp_Version"));
        payload.put("command", parameters.get("vnp_Command"));
        payload.put("txnRef", parameters.get("vnp_TxnRef"));
        payload.put("amount", parameters.get("vnp_Amount"));
        payload.put("createDate", parameters.get("vnp_CreateDate"));
        payload.put("expireDate", parameters.get("vnp_ExpireDate"));
        payload.put("returnUrl", parameters.get("vnp_ReturnUrl"));
        return payload;
    }
}
