package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoIpnRequest;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;

@Component
public class MomoCallbackParser {

    public MomoIpnRequest parse(MultiValueMap<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            throw new InvalidDataException("MoMo callback parameters are required");
        }
        return new MomoIpnRequest(
                value(parameters, "partnerCode"),
                value(parameters, "orderId"),
                value(parameters, "requestId"),
                longValue(parameters, "amount"),
                value(parameters, "orderInfo"),
                value(parameters, "orderType"),
                value(parameters, "transId"),
                intValue(parameters, "resultCode"),
                value(parameters, "message"),
                value(parameters, "payType"),
                longValue(parameters, "responseTime"),
                value(parameters, "extraData"),
                value(parameters, "signature")
        );
    }

    private String value(MultiValueMap<String, String> parameters, String name) {
        String value = parameters.getFirst(name);
        return value == null ? "" : value;
    }

    private int intValue(MultiValueMap<String, String> parameters, String name) {
        try {
            return Integer.parseInt(value(parameters, name));
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Invalid MoMo integer field: " + name);
        }
    }

    private long longValue(MultiValueMap<String, String> parameters, String name) {
        try {
            return Long.parseLong(value(parameters, name));
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Invalid MoMo long field: " + name);
        }
    }
}
