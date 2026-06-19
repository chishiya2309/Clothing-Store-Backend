package vn.hcmute.edu.dp.nhom10.backend.exception;

public class PaymentGatewayUncertainException extends RuntimeException {

    public PaymentGatewayUncertainException(String message) {
        super(message);
    }

    public PaymentGatewayUncertainException(String message, Throwable cause) {
        super(message, cause);
    }
}
