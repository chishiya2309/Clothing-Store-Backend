package vn.hcmute.edu.dp.nhom10.backend.dto.payment;

import java.util.Map;

public record VnPayCallbackData(
        String amount,
        String bankCode,
        String bankTransactionNumber,
        String cardType,
        String orderInfo,
        String payDate,
        String responseCode,
        String terminalCode,
        String transactionNumber,
        String transactionStatus,
        String paymentReference,
        String secureHash,
        Map<String, String> parameters
) {
    public boolean isGatewaySuccess() {
        return "00".equals(responseCode) && "00".equals(transactionStatus);
    }
}
