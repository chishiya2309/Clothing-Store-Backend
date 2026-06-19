package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.config.payment.MomoProperties;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoCreatePaymentRequest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MomoCreatePaymentRequestFactoryTest {

    private final MomoCreatePaymentRequestFactory factory = new MomoCreatePaymentRequestFactory(
            new MomoAmountConverter(),
            new MomoSignatureService()
    );

    @Test
    void create_usesPaymentReferenceForOrderIdAndDeterministicRequestId() {
        MomoCreatePaymentRequest request = factory.create(properties(), command());

        assertThat(request.partnerCode()).isEqualTo("TEST_PARTNER");
        assertThat(request.orderId()).isEqualTo("PAY-1");
        assertThat(request.requestId()).isEqualTo("PAY-1");
        assertThat(request.amount()).isEqualTo(100000L);
        assertThat(request.extraData()).isEmpty();
        assertThat(request.redirectUrl()).isEqualTo("https://example.test/api/payments/momo/return");
        assertThat(request.ipnUrl()).isEqualTo("https://example.test/api/payments/momo/ipn");
        assertThat(request.signature()).isNotBlank();
    }

    private GatewayPaymentCreationCommand command() {
        return new GatewayPaymentCreationCommand(
                "PAY-1",
                "CHK-1",
                new BigDecimal("100000.00"),
                OffsetDateTime.now().plusMinutes(15),
                null,
                null,
                "203.0.113.10"
        );
    }

    private MomoProperties properties() {
        MomoProperties properties = new MomoProperties();
        properties.setPartnerCode("TEST_PARTNER");
        properties.setAccessKey("test-access");
        properties.setSecretKey("test-secret");
        properties.setRedirectUrl("https://example.test/api/payments/momo/return");
        properties.setIpnUrl("https://example.test/api/payments/momo/ipn");
        return properties;
    }
}
