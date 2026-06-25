package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.config.payment.MomoProperties;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoCreatePaymentRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoCreatePaymentResponse;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.exception.PaymentGatewayUncertainException;
import vn.hcmute.edu.dp.nhom10.backend.exception.PaymentInitializationException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "MOMO-ADAPTER")
public class MomoAdapter implements PaymentGatewayAdapter {

    private final MomoProperties properties;
    private final MomoCreatePaymentRequestFactory requestFactory;
    private final MomoCreateResponseVerifier responseVerifier;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PaymentMethod supportMethod() {
        return PaymentMethod.momo;
    }

    @Override
    public boolean isAvailable() {
        return properties.isAvailable();
    }

    @Override
    public String unavailableReason() {
        return properties.unavailableReason();
    }

    @Override
    public GatewayPaymentCreationResult createPayment(GatewayPaymentCreationCommand command) {
        if (!isAvailable()) {
            throw new PaymentInitializationException(
                    "MoMo adapter is not configured: " + properties.unavailableReason()
            );
        }

        MomoCreatePaymentRequest request = requestFactory.create(properties, command);
        MomoCreatePaymentResponse response = sendCreatePaymentRequest(request, command.paymentReference());
        if (!responseVerifier.isValidSuccess(properties, request, response)) {
            throw new PaymentInitializationException("MoMo create payment response is invalid: "
                    + responseSummary(response));
        }

        return new GatewayPaymentCreationResult(
                response.payUrl(),
                null,
                sanitizedPayload(request, response)
        );
    }

    private MomoCreatePaymentResponse sendCreatePaymentRequest(
            MomoCreatePaymentRequest request,
            String paymentReference
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getConnectTimeoutSeconds()))
                .build();
        String body = serializeRequest(request);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(properties.getCreateUrl()))
                .timeout(Duration.ofSeconds(properties.getReadTimeoutSeconds()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            log.info("MoMo create payment response: paymentReference={}, httpStatus={}, body={}",
                    paymentReference, response.statusCode(), response.body());
            if (response.statusCode() >= 500) {
                log.warn("MoMo create payment uncertain failure: paymentReference={}, httpStatus={}",
                        paymentReference, response.statusCode());
                throw new PaymentGatewayUncertainException("MoMo create payment HTTP " + response.statusCode());
            }
            if (response.statusCode() >= 400) {
                throw new PaymentInitializationException("MoMo create payment HTTP " + response.statusCode());
            }
            if (response.body() == null || response.body().isBlank()) {
                throw new PaymentInitializationException("MoMo create payment response body is empty");
            }
            return objectMapper.readValue(response.body(), MomoCreatePaymentResponse.class);
        } catch (java.net.http.HttpTimeoutException e) {
            throw new PaymentGatewayUncertainException("MoMo create payment timed out", e);
        } catch (IOException e) {
            throw new PaymentGatewayUncertainException("MoMo create payment connection failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentGatewayUncertainException("MoMo create payment was interrupted", e);
        }
    }

    private String responseSummary(MomoCreatePaymentResponse response) {
        if (response == null) {
            return "empty response";
        }
        return "resultCode=" + response.resultCode()
                + ", message=" + response.message()
                + ", orderId=" + response.orderId()
                + ", requestId=" + response.requestId();
    }

    private String serializeRequest(MomoCreatePaymentRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new PaymentInitializationException("Unable to serialize MoMo create payment request", e);
        }
    }

    private Map<String, Object> sanitizedPayload(
            MomoCreatePaymentRequest request,
            MomoCreatePaymentResponse response
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("gateway", supportMethod().name());
        payload.put("partnerCode", request.partnerCode());
        payload.put("orderId", request.orderId());
        payload.put("requestId", request.requestId());
        payload.put("amount", request.amount());
        payload.put("requestType", request.requestType());
        payload.put("resultCode", response.resultCode());
        payload.put("message", response.message());
        payload.put("responseTime", response.responseTime());
        return payload;
    }
}
