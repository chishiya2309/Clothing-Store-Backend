package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.ReservedCheckoutResult;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.OnlinePaymentInitializationResult;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ConfirmCheckoutRequestDTO;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.OnlinePaymentResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.OrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PlaceOrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.PaymentGatewayAdapterFactory;
import vn.hcmute.edu.dp.nhom10.backend.service.CheckoutService;
import vn.hcmute.edu.dp.nhom10.backend.service.OrderService;
import vn.hcmute.edu.dp.nhom10.backend.service.PaymentInitializationService;
import vn.hcmute.edu.dp.nhom10.backend.service.PlaceOrderService;
import vn.hcmute.edu.dp.nhom10.backend.service.internal.CheckoutFailureTransactionService;

@Service
@RequiredArgsConstructor
public class PlaceOrderServiceImpl implements PlaceOrderService {

    private final CheckoutService checkoutService;
    private final OrderService orderService;
    private final PaymentInitializationService paymentInitializationService;
    private final PaymentGatewayAdapterFactory paymentGatewayAdapterFactory;
    private final CheckoutFailureTransactionService checkoutFailureTransactionService;

    @Override
    public PlaceOrderResponseDTO confirmCheckout(
            ConfirmCheckoutRequestDTO requestDTO,
            Long userId,
            String clientIp
    ) {
        validateRequest(requestDTO, userId);
        preflightGatewayIfNeeded(requestDTO.paymentMethod());

        ReservedCheckoutResult reservedCheckout = checkoutService.prepareCheckout(requestDTO, userId);
        validatePreparedPaymentMethod(requestDTO.paymentMethod(), reservedCheckout.paymentMethod());

        return switch (reservedCheckout.paymentMethod()) {
            case cod -> createCodOrder(reservedCheckout, userId);
            case vnpay, momo -> initializeOnlinePayment(reservedCheckout, userId, clientIp);
        };
    }

    private void validateRequest(ConfirmCheckoutRequestDTO requestDTO, Long userId) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("Checkout request is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (requestDTO.paymentMethod() == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
    }

    private void preflightGatewayIfNeeded(PaymentMethod paymentMethod) {
        if (paymentMethod == PaymentMethod.vnpay || paymentMethod == PaymentMethod.momo) {
            paymentGatewayAdapterFactory.requireAvailable(paymentMethod);
        }
    }

    private void validatePreparedPaymentMethod(
            PaymentMethod requestedPaymentMethod,
            PaymentMethod preparedPaymentMethod
    ) {
        if (preparedPaymentMethod == null || preparedPaymentMethod != requestedPaymentMethod) {
            throw new InvalidDataException("Checkout payment method does not match request");
        }
    }

    private PlaceOrderResponseDTO createCodOrder(
            ReservedCheckoutResult reservedCheckout,
            Long userId
    ) {
        try {
            OrderResponseDTO order = orderService.createCodOrder(
                    reservedCheckout.checkoutCode(),
                    userId
            );
            return PlaceOrderResponseDTO.forCod(
                    reservedCheckout.checkoutCode(),
                    order
            );
        } catch (RuntimeException originalException) {
            compensateCodFailure(reservedCheckout.checkoutCode(), originalException);
            throw originalException;
        }
    }

    private void compensateCodFailure(
            String checkoutCode,
            RuntimeException originalException
    ) {
        try {
            checkoutFailureTransactionService.failAndReleaseReservedCheckout(checkoutCode);
        } catch (RuntimeException compensationException) {
            originalException.addSuppressed(compensationException);
        }
    }

    private PlaceOrderResponseDTO initializeOnlinePayment(
            ReservedCheckoutResult reservedCheckout,
            Long userId,
            String clientIp
    ) {
        OnlinePaymentInitializationResult paymentResult = paymentInitializationService.initializeOnlinePayment(
                reservedCheckout.checkoutCode(),
                userId,
                clientIp
        );
        OnlinePaymentResponseDTO onlinePayment = new OnlinePaymentResponseDTO(
                paymentResult.paymentReference(),
                paymentResult.paymentUrl(),
                paymentResult.amount(),
                paymentResult.expiresAt()
        );
        return PlaceOrderResponseDTO.forOnline(
                reservedCheckout.checkoutCode(),
                reservedCheckout.paymentMethod(),
                onlinePayment
        );
    }
}
