package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;

import java.io.Serializable;

public record PlaceOrderResponseDTO(
        String checkoutCode,
        PaymentMethod paymentMethod,
        OrderResponseDTO order,
        OnlinePaymentResponseDTO onlinePayment
) implements Serializable {

    public static PlaceOrderResponseDTO forCod(
            String checkoutCode,
            OrderResponseDTO order
    ) {
        return new PlaceOrderResponseDTO(
                checkoutCode,
                PaymentMethod.cod,
                order,
                null
        );
    }

    public static PlaceOrderResponseDTO forOnline(
            String checkoutCode,
            PaymentMethod paymentMethod,
            OnlinePaymentResponseDTO onlinePayment
    ) {
        return new PlaceOrderResponseDTO(
                checkoutCode,
                paymentMethod,
                null,
                onlinePayment
        );
    }
}
