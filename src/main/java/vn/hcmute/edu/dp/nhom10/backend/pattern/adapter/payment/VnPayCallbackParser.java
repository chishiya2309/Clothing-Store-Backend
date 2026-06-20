package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.VnPayCallbackData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class VnPayCallbackParser {

    private static final Set<String> REQUIRED_KEYS = Set.of(
            "vnp_Amount",
            "vnp_ResponseCode",
            "vnp_TmnCode",
            "vnp_TransactionStatus",
            "vnp_TxnRef",
            "vnp_SecureHash"
    );

    public VnPayCallbackData parse(MultiValueMap<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            throw new IllegalArgumentException("VNPay callback parameters are required");
        }

        Map<String, String> vnpParameters = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            String key = entry.getKey();
            if (key == null || !key.startsWith("vnp_")) {
                continue;
            }
            List<String> values = entry.getValue();
            if (values == null || values.size() != 1) {
                throw new IllegalArgumentException("VNPay callback parameter must have exactly one value: " + key);
            }
            vnpParameters.put(key, values.get(0));
        }

        for (String requiredKey : REQUIRED_KEYS) {
            String value = vnpParameters.get(requiredKey);
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Missing VNPay callback parameter: " + requiredKey);
            }
        }

        return new VnPayCallbackData(
                vnpParameters.get("vnp_Amount"),
                vnpParameters.get("vnp_BankCode"),
                vnpParameters.get("vnp_BankTranNo"),
                vnpParameters.get("vnp_CardType"),
                vnpParameters.get("vnp_OrderInfo"),
                vnpParameters.get("vnp_PayDate"),
                vnpParameters.get("vnp_ResponseCode"),
                vnpParameters.get("vnp_TmnCode"),
                vnpParameters.get("vnp_TransactionNo"),
                vnpParameters.get("vnp_TransactionStatus"),
                vnpParameters.get("vnp_TxnRef"),
                vnpParameters.get("vnp_SecureHash"),
                Map.copyOf(vnpParameters)
        );
    }
}
