package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoCreatePaymentRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoCreatePaymentResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoIpnRequest;

import static org.assertj.core.api.Assertions.assertThat;

class MomoSignatureServiceTest {

    private final MomoSignatureService signatureService = new MomoSignatureService();

    @Test
    void sign_createPaymentRawSignature_usesMomoOrderingAndLowercaseHmacSha256() {
        MomoCreatePaymentRequest request = new MomoCreatePaymentRequest(
                "TEST_PARTNER",
                "PAY-1",
                100000,
                "PAY-1",
                "Thanh toan don hang CHK-1",
                "https://example.test/api/payments/momo/return",
                "https://example.test/api/payments/momo/ipn",
                "captureWallet",
                "",
                "vi",
                true,
                null
        );

        String rawSignature = signatureService.createPaymentRawSignature("test-access", request);

        assertThat(signatureService.sign("test-secret", rawSignature))
                .isEqualTo("7549052e11df58c0e8cae47234b479f8562d4ddccf3e2b74965b1c54c8c31aae");
    }

    @Test
    void sign_ipnRawSignature_usesMomoOrdering() {
        MomoIpnRequest request = new MomoIpnRequest(
                "TEST_PARTNER",
                "PAY-1",
                "PAY-1",
                100000,
                "Thanh toan don hang CHK-1",
                "momo_wallet",
                "TRANS-1",
                0,
                "Successful.",
                "qr",
                1718770000000L,
                "",
                null
        );

        String rawSignature = signatureService.ipnRawSignature("test-access", request);

        assertThat(signatureService.sign("test-secret", rawSignature))
                .isEqualTo("ee88980197a744808b7bad36854c063937858386309ddfd8db2dda38d2b4fa62");
    }

    @Test
    void matches_acceptsUppercaseGatewaySignatureWithConstantTimeComparison() {
        assertThat(signatureService.matches("abcdef", "ABCDEF")).isTrue();
        assertThat(signatureService.matches("abcdef", "abcdee")).isFalse();
    }

    @Test
    void sign_createResponseRawSignature_usesMomoOrdering() {
        MomoCreatePaymentResponse response = new MomoCreatePaymentResponse(
                "TEST_PARTNER",
                "PAY-1",
                100000,
                "PAY-1",
                "Successful.",
                0,
                "https://test-payment.momo.vn/pay/PAY-1",
                1718770000000L,
                null
        );

        String rawSignature = signatureService.createResponseRawSignature("test-access", response);

        assertThat(signatureService.sign("test-secret", rawSignature))
                .isEqualTo("fcce6589eb22d88927fe741adf5fdd435359a3e23c4b9480540237f10498e400");
    }
}
