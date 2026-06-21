package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VnPayIpnResponse(
        @JsonProperty("RspCode")
        String rspCode,
        @JsonProperty("Message")
        String message
) {
    public static VnPayIpnResponse confirmSuccess() {
        return new VnPayIpnResponse("00", "Confirm Success");
    }

    public static VnPayIpnResponse notFound() {
        return new VnPayIpnResponse("01", "Payment attempt not found");
    }

    public static VnPayIpnResponse alreadyProcessed(String message) {
        return new VnPayIpnResponse("02", message);
    }

    public static VnPayIpnResponse invalidAmount() {
        return new VnPayIpnResponse("04", "Invalid amount");
    }

    public static VnPayIpnResponse invalidSignature() {
        return new VnPayIpnResponse("97", "Invalid signature");
    }

    public static VnPayIpnResponse unknownError() {
        return new VnPayIpnResponse("99", "Unknown error");
    }

    public static VnPayIpnResponse invalidRequest() {
        return new VnPayIpnResponse("99", "Invalid request");
    }
}
