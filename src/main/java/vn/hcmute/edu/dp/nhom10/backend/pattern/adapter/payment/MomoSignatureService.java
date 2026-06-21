package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoCreatePaymentRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoCreatePaymentResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoIpnRequest;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class MomoSignatureService {

    private static final String HMAC_SHA256 = "HmacSHA256";

    public String createPaymentRawSignature(
            String accessKey,
            MomoCreatePaymentRequest request
    ) {
        return "accessKey=" + value(accessKey)
                + "&amount=" + request.amount()
                + "&extraData=" + value(request.extraData())
                + "&ipnUrl=" + value(request.ipnUrl())
                + "&orderId=" + value(request.orderId())
                + "&orderInfo=" + value(request.orderInfo())
                + "&partnerCode=" + value(request.partnerCode())
                + "&redirectUrl=" + value(request.redirectUrl())
                + "&requestId=" + value(request.requestId())
                + "&requestType=" + value(request.requestType());
    }

    public String createResponseRawSignature(
            String accessKey,
            MomoCreatePaymentResponse response
    ) {
        return "accessKey=" + value(accessKey)
                + "&amount=" + response.amount()
                + "&message=" + value(response.message())
                + "&orderId=" + value(response.orderId())
                + "&partnerCode=" + value(response.partnerCode())
                + "&payUrl=" + value(response.payUrl())
                + "&requestId=" + value(response.requestId())
                + "&responseTime=" + value(response.responseTime())
                + "&resultCode=" + response.resultCode();
    }

    public String ipnRawSignature(
            String accessKey,
            MomoIpnRequest request
    ) {
        return "accessKey=" + value(accessKey)
                + "&amount=" + request.amount()
                + "&extraData=" + value(request.extraData())
                + "&message=" + value(request.message())
                + "&orderId=" + value(request.orderId())
                + "&orderInfo=" + value(request.orderInfo())
                + "&orderType=" + value(request.orderType())
                + "&partnerCode=" + value(request.partnerCode())
                + "&payType=" + value(request.payType())
                + "&requestId=" + value(request.requestId())
                + "&responseTime=" + request.responseTime()
                + "&resultCode=" + request.resultCode()
                + "&transId=" + value(request.transId());
    }

    public String sign(String secretKey, String rawSignature) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new InvalidDataException("MoMo secret key is required");
        }
        if (rawSignature == null) {
            throw new InvalidDataException("MoMo raw signature is required");
        }
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            return HexFormat.of().formatHex(mac.doFinal(rawSignature.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new InvalidDataException("Unable to sign MoMo message");
        }
    }

    public boolean matches(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.toLowerCase().getBytes(StandardCharsets.UTF_8)
        );
    }

    private String value(Object value) {
        return value == null ? "" : value.toString();
    }
}
