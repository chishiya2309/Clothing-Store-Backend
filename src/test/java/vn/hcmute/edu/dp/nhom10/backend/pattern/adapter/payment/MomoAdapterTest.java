package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.config.payment.MomoProperties;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoCreatePaymentResponse;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.exception.PaymentGatewayUncertainException;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MomoAdapterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MomoSignatureService signatureService = new MomoSignatureService();
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void createPayment_postsJsonToMomoCreateApiAndReturnsVerifiedPayUrl() throws Exception {
        AtomicReference<String> receivedBody = new AtomicReference<>();
        MomoProperties properties = properties(200, receivedBody);
        MomoAdapter adapter = adapter(properties);

        GatewayPaymentCreationResult result = adapter.createPayment(command());

        assertThat(result.paymentUrl()).isEqualTo("https://test-payment.momo.vn/pay/PAY-1");
        assertThat(result.gatewayPayload()).containsEntry("gateway", "momo");
        assertThat(result.gatewayPayload()).containsEntry("orderId", "PAY-1");
        assertThat(result.gatewayPayload()).doesNotContainKeys("accessKey", "secretKey", "signature");
        assertThat(receivedBody.get()).contains("\"partnerCode\":\"TEST_PARTNER\"");
        assertThat(receivedBody.get()).contains("\"orderId\":\"PAY-1\"");
        assertThat(receivedBody.get()).contains("\"signature\":");
    }

    @Test
    void createPayment_http5xxIsUncertainAndDoesNotExposeSecret() throws Exception {
        MomoProperties properties = properties(500, new AtomicReference<>());
        MomoAdapter adapter = adapter(properties);

        assertThatThrownBy(() -> adapter.createPayment(command()))
                .isInstanceOf(PaymentGatewayUncertainException.class)
                .hasMessageContaining("HTTP 500")
                .hasMessageNotContaining("test-secret")
                .hasMessageNotContaining("test-access");
    }

    @Test
    void supportMethod_returnsMomo() throws Exception {
        MomoProperties properties = properties(200, new AtomicReference<>());
        assertThat(adapter(properties).supportMethod()).isEqualTo(PaymentMethod.momo);
    }

    private MomoAdapter adapter(MomoProperties properties) {
        MomoAmountConverter amountConverter = new MomoAmountConverter();
        return new MomoAdapter(
                properties,
                new MomoCreatePaymentRequestFactory(amountConverter, signatureService),
                new MomoCreateResponseVerifier(signatureService)
        );
    }

    private MomoProperties properties(int statusCode, AtomicReference<String> receivedBody) throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/create", exchange -> {
            receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String body = statusCode >= 500 ? "temporary failure" : objectMapper.writeValueAsString(signedResponse());
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, body.getBytes(StandardCharsets.UTF_8).length);
            exchange.getResponseBody().write(body.getBytes(StandardCharsets.UTF_8));
            exchange.close();
        });
        server.start();

        MomoProperties properties = new MomoProperties();
        properties.setEnabled(true);
        properties.setPartnerCode("TEST_PARTNER");
        properties.setAccessKey("test-access");
        properties.setSecretKey("test-secret");
        properties.setCreateUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/create");
        properties.setRedirectUrl("https://example.test/api/payments/momo/return");
        properties.setIpnUrl("https://example.test/api/payments/momo/ipn");
        properties.setConnectTimeoutSeconds(2);
        properties.setReadTimeoutSeconds(2);
        return properties;
    }

    private MomoCreatePaymentResponse signedResponse() {
        MomoCreatePaymentResponse unsigned = new MomoCreatePaymentResponse(
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
        String signature = signatureService.sign(
                "test-secret",
                signatureService.createResponseRawSignature("test-access", unsigned)
        );
        return new MomoCreatePaymentResponse(
                unsigned.partnerCode(),
                unsigned.requestId(),
                unsigned.amount(),
                unsigned.orderId(),
                unsigned.message(),
                unsigned.resultCode(),
                unsigned.payUrl(),
                unsigned.responseTime(),
                signature
        );
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
}
