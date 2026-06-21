package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import vn.hcmute.edu.dp.nhom10.backend.config.payment.VnPayProperties;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.VnPayCallbackData;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VnPayCallbackVerifierTest {

    private static final String SECRET = "test-only-secret";

    private VnPaySignatureService signatureService;
    private VnPayCallbackParser parser;
    private VnPayCallbackVerifier verifier;

    @BeforeEach
    void setUp() {
        VnPayProperties properties = new VnPayProperties();
        properties.setEnabled(true);
        properties.setTmnCode("TEST_TMN_CODE");
        properties.setHashSecret(SECRET);
        properties.setReturnUrl("https://example.test/api/payments/vnpay/return");
        signatureService = new VnPaySignatureService();
        parser = new VnPayCallbackParser();
        verifier = new VnPayCallbackVerifier(properties, signatureService);
    }

    @Test
    void hasValidSignature_validCallback_returnsTrue() {
        assertTrue(verifier.hasValidSignature(signedData(callbackParameters())));
    }

    @Test
    void hasValidSignature_invalidCallback_returnsFalse() {
        MultiValueMap<String, String> parameters = callbackParameters();
        parameters.set("vnp_SecureHash", "bad-signature");

        assertFalse(verifier.hasValidSignature(parser.parse(parameters)));
    }

    @Test
    void hasValidSignature_ignoresSecureHashTypeAndParameterOrder() {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("vnp_SecureHashType", "HMACSHA512");
        parameters.add("vnp_TxnRef", "PAY-1");
        parameters.add("vnp_TransactionStatus", "00");
        parameters.add("vnp_TmnCode", "TEST_TMN_CODE");
        parameters.add("vnp_ResponseCode", "00");
        parameters.add("vnp_Amount", "12000000");
        parameters.add("vnp_OrderInfo", "Thanh toán PAY-1");
        parameters.add("vnp_SecureHash", signature(parameters));

        assertTrue(verifier.hasValidSignature(parser.parse(parameters)));
    }

    @Test
    void hasValidTerminalCode_invalidTerminal_returnsFalse() {
        MultiValueMap<String, String> parameters = callbackParameters();
        parameters.set("vnp_TmnCode", "OTHER_TMN");
        parameters.set("vnp_SecureHash", signature(parameters));

        VnPayCallbackData data = parser.parse(parameters);

        assertTrue(verifier.hasValidSignature(data));
        assertFalse(verifier.hasValidTerminalCode(data));
    }

    private VnPayCallbackData signedData(MultiValueMap<String, String> parameters) {
        parameters.set("vnp_SecureHash", signature(parameters));
        return parser.parse(parameters);
    }

    private String signature(MultiValueMap<String, String> parameters) {
        Map<String, String> flat = new LinkedHashMap<>();
        for (Map.Entry<String, java.util.List<String>> entry : parameters.entrySet()) {
            if (!"vnp_SecureHash".equals(entry.getKey())) {
                flat.put(entry.getKey(), entry.getValue().get(0));
            }
        }
        return signatureService.sign(SECRET, flat);
    }

    private MultiValueMap<String, String> callbackParameters() {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("vnp_Amount", "12000000");
        parameters.add("vnp_OrderInfo", "Thanh toán PAY-1");
        parameters.add("vnp_ResponseCode", "00");
        parameters.add("vnp_TmnCode", "TEST_TMN_CODE");
        parameters.add("vnp_TransactionNo", "GTW-1");
        parameters.add("vnp_TransactionStatus", "00");
        parameters.add("vnp_TxnRef", "PAY-1");
        parameters.add("vnp_SecureHash", "placeholder");
        return parameters;
    }
}
