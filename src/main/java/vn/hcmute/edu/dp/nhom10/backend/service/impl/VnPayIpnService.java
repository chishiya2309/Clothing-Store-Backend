package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.VnPayCallbackData;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.VnPayIpnTransactionResult;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.VnPayIpnResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentAttemptStatus;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.VnPayAmountMatcher;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.VnPayCallbackParser;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.VnPayCallbackVerifier;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentAttemptRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.internal.VnPayIpnTransactionService;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "VNPAY-IPN")
public class VnPayIpnService {

    private final VnPayCallbackParser callbackParser;
    private final VnPayCallbackVerifier callbackVerifier;
    private final VnPayAmountMatcher amountMatcher;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final VnPayIpnTransactionService ipnTransactionService;

    public VnPayIpnResponse handleIpn(MultiValueMap<String, String> parameters) {
        VnPayCallbackData callbackData;
        try {
            callbackData = callbackParser.parse(parameters);
        } catch (RuntimeException e) {
            return VnPayIpnResponse.invalidRequest();
        }

        boolean signatureValid = callbackVerifier.hasValidSignature(callbackData)
                && callbackVerifier.hasValidTerminalCode(callbackData);
        if (!signatureValid) {
            log.info("VNPay IPN rejected: paymentReference={}, responseCode={}, transactionStatus={}, gatewayTransactionId={}, signatureValid=false",
                    callbackData.paymentReference(), callbackData.responseCode(),
                    callbackData.transactionStatus(), callbackData.transactionNumber());
            return VnPayIpnResponse.invalidSignature();
        }

        PaymentAttempt paymentAttempt = paymentAttemptRepository
                .findByPaymentReference(callbackData.paymentReference())
                .orElse(null);
        if (paymentAttempt == null) {
            log.info("VNPay IPN rejected: paymentReference={}, gatewayTransactionId={}, reason=attempt_not_found",
                    callbackData.paymentReference(), callbackData.transactionNumber());
            return VnPayIpnResponse.notFound();
        }
        if (!amountMatcher.matches(callbackData.amount(), paymentAttempt.getAmount())) {
            log.warn("VNPay IPN rejected: paymentReference={}, gatewayTransactionId={}, reason=amount_mismatch, vnpAmount={}, expectedAmount={}",
                    callbackData.paymentReference(), callbackData.transactionNumber(),
                    callbackData.amount(), paymentAttempt.getAmount());
            return VnPayIpnResponse.invalidAmount();
        }
        if (isAlreadyProcessedBeforeTransaction(paymentAttempt, callbackData)) {
            log.info("VNPay IPN acknowledged duplicate: paymentReference={}, gatewayTransactionId={}, status={}",
                    callbackData.paymentReference(), callbackData.transactionNumber(), paymentAttempt.getStatus());
            return VnPayIpnResponse.alreadyProcessed(alreadyProcessedMessage(paymentAttempt.getStatus()));
        }

        try {
            VnPayIpnTransactionResult result = ipnTransactionService.process(callbackData);
            return toResponse(result, callbackData);
        } catch (RuntimeException e) {
            log.error("VNPay IPN failed: paymentReference={}, gatewayTransactionId={}",
                    callbackData.paymentReference(), callbackData.transactionNumber(), e);
            return VnPayIpnResponse.unknownError();
        }
    }

    private boolean isAlreadyProcessedBeforeTransaction(
            PaymentAttempt paymentAttempt,
            VnPayCallbackData callbackData
    ) {
        PaymentAttemptStatus status = paymentAttempt.getStatus();
        if (status == PaymentAttemptStatus.completed
                || status == PaymentAttemptStatus.requires_refund
                || status == PaymentAttemptStatus.refund_requested
                || status == PaymentAttemptStatus.refunded) {
            return true;
        }
        return !callbackData.isGatewaySuccess()
                && (status == PaymentAttemptStatus.failed || status == PaymentAttemptStatus.expired);
    }

    private String alreadyProcessedMessage(PaymentAttemptStatus status) {
        if (status == PaymentAttemptStatus.completed) {
            return "Order already confirmed";
        }
        return "Transaction already processed";
    }

    private VnPayIpnResponse toResponse(VnPayIpnTransactionResult result, VnPayCallbackData callbackData) {
        VnPayIpnResponse response = switch (result.code()) {
            case CONFIRMED -> VnPayIpnResponse.confirmSuccess();
            case NOT_FOUND -> VnPayIpnResponse.notFound();
            case ALREADY_PROCESSED -> VnPayIpnResponse.alreadyProcessed(result.message());
            case INVALID_AMOUNT -> VnPayIpnResponse.invalidAmount();
            case UNKNOWN_ERROR -> VnPayIpnResponse.unknownError();
        };
        log.info("VNPay IPN processed: paymentReference={}, gatewayTransactionId={}, resultCode={}, rspCode={}, message={}",
                callbackData.paymentReference(), callbackData.transactionNumber(),
                result.code(), response.rspCode(), response.message());
        return response;
    }
}
