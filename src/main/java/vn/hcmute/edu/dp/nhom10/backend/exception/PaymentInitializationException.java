package vn.hcmute.edu.dp.nhom10.backend.exception;

public class PaymentInitializationException extends RuntimeException {
    public PaymentInitializationException(String message) {
        super(message);
    }

    public PaymentInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
