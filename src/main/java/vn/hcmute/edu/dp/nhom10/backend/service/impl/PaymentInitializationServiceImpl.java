package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
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
public class PaymentInitializationServiceImpl implements PaymentInitializationService {

    private final PaymentAttemptTransactionService paymentAttemptTransactionService;
    private final PaymentGatewayAdapterFactory paymentGatewayAdapterFactory;

    @Value("${payment.return-url:}")
    private String returnUrl;

    @Value("${payment.callback-url:}")
    private String callbackUrl;

    @Override
    public OnlinePaymentInitializationResult initializeOnlinePayment(String checkoutCode, Long userId) {
        PendingPaymentContext pendingContext = paymentAttemptTransactionService.createPendingAttempt(checkoutCode, userId);
        if (pendingContext.hasPaymentUrl()) {
            return toOnlinePaymentInitializationResult(pendingContext);
        }

        try {
            PaymentGatewayAdapter adapter = paymentGatewayAdapterFactory.getAdapter(pendingContext.paymentMethod());
            GatewayPaymentCreationResult gatewayResult = adapter.createPayment(toGatewayCommand(pendingContext));
            validateGatewayResult(gatewayResult);
            return paymentAttemptTransactionService.completeInitialization(
                    pendingContext.paymentReference(),
                    gatewayResult
            );
        } catch (RuntimeException e) {
            paymentAttemptTransactionService.failInitialization(
                    pendingContext.paymentReference(),
                    safeFailureReason(e)
            );
            throw new PaymentInitializationException("Payment initialization failed", e);
        }
    }

    private GatewayPaymentCreationCommand toGatewayCommand(PendingPaymentContext pendingContext) {
        if (returnUrl == null || returnUrl.isBlank()) {
            throw new InvalidDataException("Payment return URL is not configured");
        }
        if (callbackUrl == null || callbackUrl.isBlank()) {
            throw new InvalidDataException("Payment callback URL is not configured");
        }
        return new GatewayPaymentCreationCommand(
                pendingContext.paymentReference(),
                pendingContext.checkoutCode(),
                pendingContext.amount(),
                pendingContext.expiresAt(),
                returnUrl,
                callbackUrl
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
