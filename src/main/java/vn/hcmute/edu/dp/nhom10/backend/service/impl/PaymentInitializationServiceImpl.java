package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.OnlinePaymentInitializationResult;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.PendingPaymentContext;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.PaymentInitializationException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.GatewayPaymentCreationCommand;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.GatewayPaymentCreationResult;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.PaymentGatewayAdapter;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.PaymentGatewayAdapterFactory;
import vn.hcmute.edu.dp.nhom10.backend.service.PaymentInitializationService;
import vn.hcmute.edu.dp.nhom10.backend.service.internal.PaymentAttemptTransactionService;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "PAYMENT-INITIALIZATION")
public class PaymentInitializationServiceImpl implements PaymentInitializationService {

    private final PaymentAttemptTransactionService paymentAttemptTransactionService;
    private final PaymentGatewayAdapterFactory paymentGatewayAdapterFactory;

    @Value("${payment.return-url:}")
    private String returnUrl;

    @Value("${payment.callback-url:}")
    private String callbackUrl;

    @Override
    public OnlinePaymentInitializationResult initializeOnlinePayment(String checkoutCode, Long userId, String clientIp) {
        PendingPaymentContext pendingContext = paymentAttemptTransactionService.createPendingAttempt(checkoutCode, userId);
        if (pendingContext.hasPaymentUrl()) {
            log.info("Reusing initialized online payment: checkoutCode={}, paymentReference={}, method={}, expiresAt={}",
                    pendingContext.checkoutCode(), pendingContext.paymentReference(),
                    pendingContext.paymentMethod(), pendingContext.expiresAt());
            return toOnlinePaymentInitializationResult(pendingContext);
        }

        try {
            PaymentGatewayAdapter adapter = paymentGatewayAdapterFactory.getAdapter(pendingContext.paymentMethod());
            GatewayPaymentCreationResult gatewayResult = adapter.createPayment(toGatewayCommand(pendingContext, clientIp));
            validateGatewayResult(gatewayResult);
            OnlinePaymentInitializationResult result = paymentAttemptTransactionService.completeInitialization(
                    pendingContext.paymentReference(),
                    gatewayResult
            );
            log.info("Initialized online payment: checkoutCode={}, paymentReference={}, method={}, expiresAt={}",
                    result.checkoutCode(), result.paymentReference(), result.paymentMethod(), result.expiresAt());
            return result;
        } catch (RuntimeException e) {
            paymentAttemptTransactionService.failInitialization(
                    pendingContext.paymentReference(),
                    safeFailureReason(e)
            );
            log.warn("Online payment initialization failed: checkoutCode={}, paymentReference={}, method={}, reason={}",
                    pendingContext.checkoutCode(), pendingContext.paymentReference(),
                    pendingContext.paymentMethod(), safeFailureReason(e));
            throw new PaymentInitializationException("Payment initialization failed", e);
        }
    }

    private GatewayPaymentCreationCommand toGatewayCommand(PendingPaymentContext pendingContext, String clientIp) {
        return new GatewayPaymentCreationCommand(
                pendingContext.paymentReference(),
                pendingContext.checkoutCode(),
                pendingContext.amount(),
                pendingContext.expiresAt(),
                returnUrl,
                callbackUrl,
                clientIp
        );
    }

    private void validateGatewayResult(GatewayPaymentCreationResult gatewayResult) {
        if (gatewayResult == null || gatewayResult.paymentUrl() == null || gatewayResult.paymentUrl().isBlank()) {
            throw new InvalidDataException("Gateway payment URL is required");
        }
    }

    private OnlinePaymentInitializationResult toOnlinePaymentInitializationResult(PendingPaymentContext pendingContext) {
        return new OnlinePaymentInitializationResult(
                pendingContext.checkoutCode(),
                pendingContext.paymentReference(),
                pendingContext.paymentMethod(),
                pendingContext.paymentUrl(),
                pendingContext.amount(),
                pendingContext.expiresAt()
        );
    }

    private String safeFailureReason(RuntimeException e) {
        String message = e.getMessage();
        if (message == null || message.isBlank()) {
            return e.getClass().getSimpleName();
        }
        return message.length() <= 500 ? message : message.substring(0, 500);
    }
}
