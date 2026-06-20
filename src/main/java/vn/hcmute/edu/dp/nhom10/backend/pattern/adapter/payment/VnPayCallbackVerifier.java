package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.config.payment.VnPayProperties;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.VnPayCallbackData;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class VnPayCallbackVerifier {

    private final VnPayProperties properties;
    private final VnPaySignatureService signatureService;

    public boolean hasValidSignature(VnPayCallbackData callbackData) {
        if (callbackData == null || callbackData.secureHash() == null || callbackData.secureHash().isBlank()) {
            return false;
        }
        try {
            String expectedSignature = signatureService.sign(
                    properties.getHashSecret(),
                    signingParameters(callbackData)
            );
            byte[] expected = expectedSignature.getBytes(StandardCharsets.UTF_8);
            byte[] actual = callbackData.secureHash()
                    .toLowerCase(Locale.ROOT)
                    .getBytes(StandardCharsets.UTF_8);
            return MessageDigest.isEqual(expected, actual);
        } catch (RuntimeException e) {
            return false;
        }
    }

    public boolean hasValidTerminalCode(VnPayCallbackData callbackData) {
        return callbackData != null
                && hasText(properties.getTmnCode())
                && properties.getTmnCode().equals(callbackData.terminalCode());
    }

    private Map<String, String> signingParameters(VnPayCallbackData callbackData) {
        Map<String, String> signingParameters = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : callbackData.parameters().entrySet()) {
            String key = entry.getKey();
            if (key == null || !key.startsWith("vnp_")
                    || "vnp_SecureHash".equals(key)
                    || "vnp_SecureHashType".equals(key)) {
                continue;
            }
            signingParameters.put(key, entry.getValue());
        }
        return signingParameters;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
