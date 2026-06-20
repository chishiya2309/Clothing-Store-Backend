package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.VnPayCallbackData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VnPayCallbackParserTest {

    private final VnPayCallbackParser parser = new VnPayCallbackParser();

    @Test
    void parse_mapsSupportedFieldsAndIgnoresNonVnpParameters() {
        MultiValueMap<String, String> parameters = validParameters();
        parameters.add("Authorization", "Bearer secret");

        VnPayCallbackData data = parser.parse(parameters);

        assertEquals("12000000", data.amount());
        assertEquals("00", data.responseCode());
        assertEquals("TEST_TMN_CODE", data.terminalCode());
        assertEquals("GTW-1", data.transactionNumber());
        assertEquals("PAY-1", data.paymentReference());
        assertFalse(data.parameters().containsKey("Authorization"));
    }

    @Test
    void parse_missingRequiredParameter_throwsException() {
        MultiValueMap<String, String> parameters = validParameters();
        parameters.remove("vnp_Amount");

        assertThrows(IllegalArgumentException.class, () -> parser.parse(parameters));
    }

    @Test
    void parse_duplicateRequiredParameter_throwsException() {
        MultiValueMap<String, String> parameters = validParameters();
        parameters.add("vnp_TxnRef", "PAY-2");

        assertThrows(IllegalArgumentException.class, () -> parser.parse(parameters));
    }

    private MultiValueMap<String, String> validParameters() {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("vnp_Amount", "12000000");
        parameters.add("vnp_ResponseCode", "00");
        parameters.add("vnp_TmnCode", "TEST_TMN_CODE");
        parameters.add("vnp_TransactionNo", "GTW-1");
        parameters.add("vnp_TransactionStatus", "00");
        parameters.add("vnp_TxnRef", "PAY-1");
        parameters.add("vnp_SecureHash", "abc123");
        return parameters;
    }
}
