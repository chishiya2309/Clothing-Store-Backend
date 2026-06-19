package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.config.payment.MomoProperties;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoCreatePaymentRequest;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;

@Component
@RequiredArgsConstructor
public class MomoCreatePaymentRequestFactory {

    private static final int MAX_ORDER_INFO_LENGTH = 120;

    private final MomoAmountConverter amountConverter;
    private final MomoSignatureService signatureService;

    public MomoCreatePaymentRequest create(MomoProperties properties, GatewayPaymentCreationCommand command) {
        validatePaymentReference(command.paymentReference());
        long amount = amountConverter.toMomoAmount(command.amount());
        String orderInfo = orderInfo(command.checkoutCode());
        MomoCreatePaymentRequest unsignedRequest = new MomoCreatePaymentRequest(
                properties.getPartnerCode(),
                command.paymentReference(),
                amount,
                command.paymentReference(),
                orderInfo,
                properties.getRedirectUrl(),
                properties.getIpnUrl(),
                properties.getRequestType(),
                "",
                properties.getLang(),
                properties.isAutoCapture(),
                null
        );
        String rawSignature = signatureService.createPaymentRawSignature(properties.getAccessKey(), unsignedRequest);
        String signature = signatureService.sign(properties.getSecretKey(), rawSignature);
        return new MomoCreatePaymentRequest(
                unsignedRequest.partnerCode(),
                unsignedRequest.requestId(),
                unsignedRequest.amount(),
                unsignedRequest.orderId(),
                unsignedRequest.orderInfo(),
                unsignedRequest.redirectUrl(),
                unsignedRequest.ipnUrl(),
                unsignedRequest.requestType(),
                unsignedRequest.extraData(),
                unsignedRequest.lang(),
                unsignedRequest.autoCapture(),
                signature
        );
    }

    private void validatePaymentReference(String paymentReference) {
        if (paymentReference == null || paymentReference.isBlank()) {
            throw new InvalidDataException("MoMo orderId is required");
        }
        if (paymentReference.length() > 50 || !paymentReference.matches("[A-Za-z0-9_-]+")) {
            throw new InvalidDataException("MoMo orderId is invalid");
        }
    }

    private String orderInfo(String checkoutCode) {
        String normalizedCheckoutCode = checkoutCode == null || checkoutCode.isBlank()
                ? "checkout"
                : checkoutCode.trim();
        String orderInfo = "Thanh toan don hang " + normalizedCheckoutCode;
        return orderInfo.length() <= MAX_ORDER_INFO_LENGTH
                ? orderInfo
                : orderInfo.substring(0, MAX_ORDER_INFO_LENGTH);
    }
}
