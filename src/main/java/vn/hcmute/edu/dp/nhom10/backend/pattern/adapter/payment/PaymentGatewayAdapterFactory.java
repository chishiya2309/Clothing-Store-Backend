package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentGatewayAdapterFactory {

    private final Map<PaymentMethod, PaymentGatewayAdapter> adapters;

    public PaymentGatewayAdapterFactory(List<PaymentGatewayAdapter> adapters) {
        this.adapters = new EnumMap<>(PaymentMethod.class);
        for (PaymentGatewayAdapter adapter : adapters) {
            PaymentMethod method = adapter.supportMethod();
            if (method == null) {
                throw new InvalidDataException("Payment gateway adapter method is required");
            }
            if (this.adapters.putIfAbsent(method, adapter) != null) {
                throw new InvalidDataException("Duplicate payment gateway adapter for method: " + method);
            }
        }
    }

    public PaymentGatewayAdapter getAdapter(PaymentMethod method) {
        if (method == null) {
            throw new InvalidDataException("Payment method is required");
        }
        if (method == PaymentMethod.cod) {
            throw new InvalidDataException("COD does not use online payment gateway");
        }
        PaymentGatewayAdapter adapter = adapters.get(method);
        if (adapter == null) {
            throw new InvalidDataException("Payment method is not supported by any gateway adapter: " + method);
        }
        return adapter;
    }
}
