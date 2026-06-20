package vn.hcmute.edu.dp.nhom10.backend.config.payment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VnPayPropertiesTest {

    @Test
    void isAvailable_enabledFalse_returnsFalse() {
        VnPayProperties properties = validProperties();
        properties.setEnabled(false);

        assertFalse(properties.isAvailable());
    }

    @Test
    void isAvailable_missingTmnCode_returnsFalse() {
        VnPayProperties properties = validProperties();
        properties.setTmnCode(" ");

        assertFalse(properties.isAvailable());
    }

    @Test
    void isAvailable_missingHashSecret_returnsFalse() {
        VnPayProperties properties = validProperties();
        properties.setHashSecret(" ");

        assertFalse(properties.isAvailable());
    }

    @Test
    void isAvailable_invalidPayUrl_returnsFalse() {
        VnPayProperties properties = validProperties();
        properties.setPayUrl("ftp://sandbox.vnpayment.vn/paymentv2/vpcpay.html");

        assertFalse(properties.isAvailable());
    }

    @Test
    void isAvailable_missingReturnUrl_returnsFalse() {
        VnPayProperties properties = validProperties();
        properties.setReturnUrl(" ");

        assertFalse(properties.isAvailable());
    }

    @Test
    void isAvailable_validConfiguration_returnsTrue() {
        assertTrue(validProperties().isAvailable());
    }

    private VnPayProperties validProperties() {
        VnPayProperties properties = new VnPayProperties();
        properties.setEnabled(true);
        properties.setTmnCode("TEST_TMN_CODE");
        properties.setHashSecret("test-only-secret");
        properties.setPayUrl("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        properties.setReturnUrl("https://example.test/api/payments/vnpay/return");
        return properties;
    }
}
