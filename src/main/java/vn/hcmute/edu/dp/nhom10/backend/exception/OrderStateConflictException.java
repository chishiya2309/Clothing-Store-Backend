package vn.hcmute.edu.dp.nhom10.backend.exception;

public class OrderStateConflictException extends RuntimeException {
    public OrderStateConflictException(String message) {
        super(message);
    }
}
