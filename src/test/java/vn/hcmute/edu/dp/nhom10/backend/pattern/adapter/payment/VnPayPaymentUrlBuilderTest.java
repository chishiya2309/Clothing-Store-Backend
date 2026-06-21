package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.config.payment.VnPayProperties;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VnPayPaymentUrlBuilderTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-06-19T03:20:30Z"), ZoneOffset.UTC);

    private final VnPayPaymentUrlBuilder builder =
            new VnPayPaymentUrlBuilder(new VnPaySignatureService(), FIXED_CLOCK);

    @Test
    void build_createsSignedSandboxPaymentUrl() {
        VnPayPaymentUrlBuilder.VnPayPaymentUrl paymentUrl = builder.build(
                properties(),
                command(OffsetDateTime.parse("2026-06-19T03:50:30Z"))
        );

        URI uri = URI.create(paymentUrl.paymentUrl());
        Map<String, String> query = query(uri);

        assertEquals("sandbox.vnpayment.vn", uri.getHost());
        assertEquals("/paymentv2/vpcpay.html", uri.getPath());
        assertEquals("2.1.0", query.get("vnp_Version"));
        assertEquals("pay", query.get("vnp_Command"));
        assertEquals("TEST_TMN_CODE", query.get("vnp_TmnCode"));
        assertEquals("10000000", query.get("vnp_Amount"));
        assertEquals("VND", query.get("vnp_CurrCode"));
        assertEquals("PAY-1", query.get("vnp_TxnRef"));
        assertEquals("other", query.get("vnp_OrderType"));
        assertEquals("vn", query.get("vnp_Locale"));
        assertEquals("https://example.test/api/payments/vnpay/return", query.get("vnp_ReturnUrl"));
        assertEquals("203.0.113.10", query.get("vnp_IpAddr"));
        assertEquals("20260619102030", query.get("vnp_CreateDate"));
        assertEquals("20260619103530", query.get("vnp_ExpireDate"));
        assertTrue(query.containsKey("vnp_SecureHash"));
        assertFalse(paymentUrl.paymentUrl().contains("test-only-secret"));
        assertFalse(query.containsKey("vnp_IpnUrl"));
        assertFalse(query.containsKey("vnp_SecureHashType"));
    }

    @Test
    void createParameters_expireDateDoesNotExceedCheckoutExpiry() {
        Map<String, String> parameters = builder.createParameters(
                properties(),
                command(OffsetDateTime.parse("2026-06-19T03:25:30Z"))
        );

        assertEquals("20260619102530", parameters.get("vnp_ExpireDate"));
    }

    @Test
    void toVnPayAmount_convertsWholeVndToVnpayMinorUnit() {
        assertEquals("10000000", builder.toVnPayAmount(new BigDecimal("100000.00")));
    }

    @Test
    void toVnPayAmount_rejectsInvalidAmounts() {
        assertThrows(InvalidDataException.class, () -> builder.toVnPayAmount(null));
        assertThrows(InvalidDataException.class, () -> builder.toVnPayAmount(BigDecimal.ZERO));
        assertThrows(InvalidDataException.class, () -> builder.toVnPayAmount(new BigDecimal("-1.00")));
        assertThrows(InvalidDataException.class, () -> builder.toVnPayAmount(new BigDecimal("100000.50")));
        assertThrows(InvalidDataException.class, () -> builder.toVnPayAmount(new BigDecimal("92233720368547759.00")));
    }

    @Test
    void createParameters_rejectsExpiredCheckout() {
        assertThrows(InvalidDataException.class,
                () -> builder.createParameters(properties(), command(OffsetDateTime.parse("2026-06-19T03:20:30Z"))));
    }

    @Test
    void createParameters_rejectsUnsafePaymentReference() {
        GatewayPaymentCreationCommand command = new GatewayPaymentCreationCommand(
                "PAY 1",
                "CHK-1",
                new BigDecimal("100000.00"),
                OffsetDateTime.parse("2026-06-19T03:50:30Z"),
                null,
                null,
                "203.0.113.10"
        );

        assertThrows(InvalidDataException.class, () -> builder.createParameters(properties(), command));
    }

    private GatewayPaymentCreationCommand command(OffsetDateTime expiresAt) {
        return new GatewayPaymentCreationCommand(
                "PAY-1",
                "CHK-1",
                new BigDecimal("100000.00"),
                expiresAt,
                null,
                null,
                "203.0.113.10"
        );
    }

    private VnPayProperties properties() {
        VnPayProperties properties = new VnPayProperties();
        properties.setEnabled(true);
        properties.setTmnCode("TEST_TMN_CODE");
        properties.setHashSecret("test-only-secret");
        properties.setPayUrl("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        properties.setReturnUrl("https://example.test/api/payments/vnpay/return");
        return properties;
    }

    private Map<String, String> query(URI uri) {
        return Arrays.stream(uri.getRawQuery().split("&"))
                .map(part -> part.split("=", 2))
                .collect(Collectors.toMap(
                        pair -> URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                        pair -> URLDecoder.decode(pair[1], StandardCharsets.UTF_8)
                ));
    }
}
