package vn.hcmute.edu.dp.nhom10.backend.dto.payment;

public record MomoIpnTransactionResult(
        Code code,
        String message
) {
    public enum Code {
        ACCEPTED,
        NOT_FOUND,
        INVALID_AMOUNT,
        UNKNOWN_ERROR
    }

    public static MomoIpnTransactionResult accepted(String message) {
        return new MomoIpnTransactionResult(Code.ACCEPTED, message);
    }

    public static MomoIpnTransactionResult notFound() {
        return new MomoIpnTransactionResult(Code.NOT_FOUND, "Payment attempt not found");
    }

    public static MomoIpnTransactionResult invalidAmount() {
        return new MomoIpnTransactionResult(Code.INVALID_AMOUNT, "Invalid amount");
    }

    public static MomoIpnTransactionResult unknownError() {
        return new MomoIpnTransactionResult(Code.UNKNOWN_ERROR, "Unknown error");
    }
}
