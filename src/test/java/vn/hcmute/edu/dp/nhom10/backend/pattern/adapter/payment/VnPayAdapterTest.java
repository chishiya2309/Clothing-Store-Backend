package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.config.payment.VnPayProperties;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.exception.PaymentInitializationException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VnPayAdapterTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-06-19T03:20:30Z"), ZoneOffset.UTC);

    @Test
    void supportMethod_returnsVnpay() {
        assertEquals(PaymentMethod.vnpay, adapter(validProperties()).supportMethod());
    }

    @Test
    void isAvailable_reflectsProperties() {
        assertTrue(adapter(validProperties()).isAvailable());

        VnPayProperties disabled = validProperties();
        disabled.setEnabled(false);
        assertFalse(adapter(disabled).isAvailable());
    }

    @Test
    void createPayment_returnsSignedUrlAndSanitizedPayload() {
        GatewayPaymentCreationResult result = adapter(validProperties()).createPayment(command());

        assertTrue(result.paymentUrl().startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?"));
        assertTrue(result.paymentUrl().contains("vnp_SecureHash="));
        assertFalse(result.paymentUrl().contains("test-only-secret"));
        assertNull(result.gatewayTransactionId());
        assertEquals("vnpay", result.gatewayPayload().get("gateway"));
        assertEquals("PAY-1", result.gatewayPayload().get("txnRef"));
        assertEquals("10000000", result.gatewayPayload().get("amount"));
        assertEquals("20260619102030", result.gatewayPayload().get("createDate"));
        assertEquals("20260619103530", result.gatewayPayload().get("expireDate"));
        assertFalse(result.gatewayPayload().containsKey("hashSecret"));
        assertFalse(result.gatewayPayload().containsKey("vnp_SecureHash"));
    }

    @Test
    void createPayment_unavailableConfigurationThrowsWithoutSecretValue() {
        VnPayProperties properties = validProperties();
        properties.setHashSecret("");

        PaymentInitializationException thrown = assertThrows(
                PaymentInitializationException.class,
                () -> adapter(properties).createPayment(command())
        );

        assertTrue(thrown.getMessage().contains("payment.vnpay.hash-secret"));
        assertFalse(thrown.getMessage().contains("test-only-secret"));
    }

    private VnPayAdapter adapter(VnPayProperties properties) {
        return new VnPayAdapter(
                properties,
                new VnPayPaymentUrlBuilder(new VnPaySignatureService(), FIXED_CLOCK)
        );
    }

    private GatewayPaymentCreationCommand command() {
        return new GatewayPaymentCreationCommand(
                "PAY-1",
                "CHK-1",
                new BigDecimal("100000.00"),
                OffsetDateTime.parse("2026-06-19T03:50:30Z"),
                null,
                null,
                "203.0.113.10"
        );
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
