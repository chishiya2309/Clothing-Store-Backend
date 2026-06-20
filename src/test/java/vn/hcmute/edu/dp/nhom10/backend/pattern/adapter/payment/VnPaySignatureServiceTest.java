package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VnPaySignatureServiceTest {

    private final VnPaySignatureService signatureService = new VnPaySignatureService();

    @Test
    void sign_usesSortedUtf8EncodedNonBlankParameters() {
        String signature = signatureService.sign("test-only-secret", sampleParameters());

        assertEquals(
                "c041d021ed768e3152ce909251087f032b0b0eecbc195570314a9089a68c4e11d7e61ced2a847b55c6b6cc8fdfe2c5e4733b04a7fa028fb96fef388a770d73ed",
                signature
        );
    }

    @Test
    void buildSignedData_sortsFiltersAndEncodesDeterministically() {
        String signedData = signatureService.buildSignedData(sampleParameters());

        assertEquals(
                "vnp_Amount=10000000&vnp_Command=pay&vnp_CreateDate=20260619102030&vnp_CurrCode=VND&vnp_IpAddr=203.0.113.10&vnp_Locale=vn&vnp_OrderInfo=Thanh+toan+PAY-1&vnp_OrderType=other&vnp_ReturnUrl=https%3A%2F%2Fexample.test%2Freturn&vnp_TmnCode=TEST_TMN_CODE&vnp_TxnRef=PAY-1&vnp_Version=2.1.0",
                signedData
        );
        assertFalse(signedData.contains("vnp_Empty"));
        assertFalse(signedData.contains("vnp_Null"));
        assertFalse(signedData.contains("vnp_SecureHash"));
    }

    @Test
    void buildSignedData_unicodeEncodingIsDeterministic() {
        Map<String, String> parameters = Map.of("vnp_OrderInfo", "Thanh toán PAY-1");

        assertEquals("vnp_OrderInfo=Thanh+to%C3%A1n+PAY-1", signatureService.buildSignedData(parameters));
    }

    @Test
    void sign_returnsLowercaseHexAndChangesWhenParameterChanges() {
        Map<String, String> parameters = sampleParameters();
        String signature = signatureService.sign("test-only-secret", parameters);

        Map<String, String> changedParameters = new LinkedHashMap<>(parameters);
        changedParameters.put("vnp_Amount", "10000100");

        assertTrue(signature.matches("[0-9a-f]+"));
        assertNotEquals(signature, signatureService.sign("test-only-secret", changedParameters));
    }

    private Map<String, String> sampleParameters() {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("vnp_Version", "2.1.0");
        parameters.put("vnp_Command", "pay");
        parameters.put("vnp_TmnCode", "TEST_TMN_CODE");
        parameters.put("vnp_Amount", "10000000");
        parameters.put("vnp_CurrCode", "VND");
        parameters.put("vnp_TxnRef", "PAY-1");
        parameters.put("vnp_OrderInfo", "Thanh toan PAY-1");
        parameters.put("vnp_OrderType", "other");
        parameters.put("vnp_Locale", "vn");
        parameters.put("vnp_ReturnUrl", "https://example.test/return");
        parameters.put("vnp_IpAddr", "203.0.113.10");
        parameters.put("vnp_CreateDate", "20260619102030");
        parameters.put("vnp_Empty", " ");
        parameters.put("vnp_Null", null);
        parameters.put("vnp_SecureHash", "must-not-be-signed");
        return parameters;
    }
}
