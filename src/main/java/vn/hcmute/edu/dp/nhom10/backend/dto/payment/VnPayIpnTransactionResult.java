package vn.hcmute.edu.dp.nhom10.backend.dto.payment;

public record VnPayIpnTransactionResult(
        Code code,
        String message
) {
    public enum Code {
        CONFIRMED,
        NOT_FOUND,
        ALREADY_PROCESSED,
        INVALID_AMOUNT,
        UNKNOWN_ERROR
    }

    public static VnPayIpnTransactionResult confirmed() {
        return new VnPayIpnTransactionResult(Code.CONFIRMED, "Confirm Success");
    }

    public static VnPayIpnTransactionResult notFound() {
        return new VnPayIpnTransactionResult(Code.NOT_FOUND, "Payment attempt not found");
    }

    public static VnPayIpnTransactionResult alreadyProcessed(String message) {
        return new VnPayIpnTransactionResult(Code.ALREADY_PROCESSED, message);
    }

    public static VnPayIpnTransactionResult invalidAmount() {
        return new VnPayIpnTransactionResult(Code.INVALID_AMOUNT, "Invalid amount");
    }

    public static VnPayIpnTransactionResult unknownError() {
        return new VnPayIpnTransactionResult(Code.UNKNOWN_ERROR, "Unknown error");
    }
}
