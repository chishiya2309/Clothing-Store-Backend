package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
public class VnPaySignatureService {

    private static final String HMAC_SHA512 = "HmacSHA512";

    public String sign(String secret, Map<String, String> parameters) {
        validateSecret(secret);
        String signedData = buildSignedData(parameters);
        try {
            Mac hmac = Mac.getInstance(HMAC_SHA512);
            hmac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA512));
            byte[] bytes = hmac.doFinal(signedData.getBytes(StandardCharsets.UTF_8));
            return toLowercaseHex(bytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Unable to create VNPay signature", e);
        }
    }

    String buildSignedData(Map<String, String> parameters) {
        SortedMap<String, String> sortedParameters = sortedNonBlankParameters(parameters);
        StringBuilder signedData = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParameters.entrySet()) {
            if (!signedData.isEmpty()) {
                signedData.append('&');
            }
            signedData.append(VnPayUrlEncoding.encode(entry.getKey()))
                    .append('=')
                    .append(VnPayUrlEncoding.encode(entry.getValue()));
        }
        return signedData.toString();
    }

    SortedMap<String, String> sortedNonBlankParameters(Map<String, String> parameters) {
        SortedMap<String, String> sortedParameters = new TreeMap<>();
        if (parameters == null || parameters.isEmpty()) {
            return sortedParameters;
        }
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()
                    || entry.getValue() == null || entry.getValue().isBlank()
                    || "vnp_SecureHash".equals(entry.getKey())) {
                continue;
            }
            sortedParameters.put(entry.getKey(), entry.getValue());
        }
        return sortedParameters;
    }

    private void validateSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("VNPay hash secret is required");
        }
    }

    private String toLowercaseHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            hex.append(String.format(Locale.ROOT, "%02x", value));
        }
        return hex.toString();
    }
}
