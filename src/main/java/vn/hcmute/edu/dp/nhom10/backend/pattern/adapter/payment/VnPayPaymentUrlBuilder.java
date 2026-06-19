package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.config.payment.VnPayProperties;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;

@Component
public class VnPayPaymentUrlBuilder {

    private static final ZoneId VNPAY_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter VNPAY_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(VNPAY_ZONE);
    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    private final VnPaySignatureService signatureService;
    private final Clock clock;

    public VnPayPaymentUrlBuilder(VnPaySignatureService signatureService, Clock clock) {
        this.signatureService = signatureService;
        this.clock = clock;
    }

    public VnPayPaymentUrl build(VnPayProperties properties, GatewayPaymentCreationCommand command) {
        validateProperties(properties);
        Map<String, String> parameters = createParameters(properties, command);
        String signature = signatureService.sign(properties.getHashSecret(), parameters);
        String queryString = buildQueryString(parameters) + "&vnp_SecureHash=" + VnPayUrlEncoding.encode(signature);
        return new VnPayPaymentUrl(properties.getPayUrl() + "?" + queryString, parameters, signature);
    }

    Map<String, String> createParameters(VnPayProperties properties, GatewayPaymentCreationCommand command) {
        validateProperties(properties);
        validateCommand(command);
        Instant createInstant = Instant.now(clock);
        String createDate = formatTime(createInstant);
        String expireDate = formatExpireDate(createInstant, command.expiresAt(), properties.getExpireMinutes());
        String amount = toVnPayAmount(command.amount());

        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("vnp_Version", properties.getVersion());
        parameters.put("vnp_Command", properties.getCommand());
        parameters.put("vnp_TmnCode", properties.getTmnCode());
        parameters.put("vnp_Amount", amount);
        parameters.put("vnp_CurrCode", properties.getCurrency());
        parameters.put("vnp_TxnRef", command.paymentReference());
        parameters.put("vnp_OrderInfo", "Thanh toan " + command.paymentReference());
        parameters.put("vnp_OrderType", properties.getOrderType());
        parameters.put("vnp_Locale", properties.getLocale());
        parameters.put("vnp_ReturnUrl", properties.getReturnUrl());
        parameters.put("vnp_IpAddr", command.clientIp());
        parameters.put("vnp_CreateDate", createDate);
        parameters.put("vnp_ExpireDate", expireDate);
        return parameters;
    }

    String toVnPayAmount(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidDataException("Payment amount is required");
        }
        if (amount.signum() <= 0) {
            throw new InvalidDataException("Payment amount must be greater than zero");
        }
        if (amount.stripTrailingZeros().scale() > 0) {
            throw new InvalidDataException("Payment amount must be a whole VND amount");
        }
        BigDecimal scaledAmount = amount.multiply(BigDecimal.valueOf(100));
        try {
            BigInteger exactAmount = scaledAmount.toBigIntegerExact();
            if (exactAmount.compareTo(LONG_MAX) > 0) {
                throw new InvalidDataException("Payment amount is too large");
            }
            return exactAmount.toString();
        } catch (ArithmeticException e) {
            throw new InvalidDataException("Payment amount must not require rounding for VNPay");
        }
    }

    private String formatExpireDate(Instant createInstant, OffsetDateTime checkoutExpiresAt, int expireMinutes) {
        if (checkoutExpiresAt == null) {
            throw new InvalidDataException("Checkout expiration is required");
        }
        if (!checkoutExpiresAt.toInstant().isAfter(createInstant)) {
            throw new InvalidDataException("Checkout session has expired");
        }
        if (expireMinutes <= 0) {
            throw new InvalidDataException("VNPay expire minutes must be greater than zero");
        }
        Instant configuredExpireInstant = createInstant.plusSeconds(expireMinutes * 60L);
        Instant expireInstant = checkoutExpiresAt.toInstant().isBefore(configuredExpireInstant)
                ? checkoutExpiresAt.toInstant()
                : configuredExpireInstant;
        if (!expireInstant.isAfter(createInstant)) {
            throw new InvalidDataException("VNPay expire date must be after create date");
        }
        return formatTime(expireInstant);
    }

    private String formatTime(Instant instant) {
        return VNPAY_TIME_FORMAT.format(ZonedDateTime.ofInstant(instant, VNPAY_ZONE));
    }

    private String buildQueryString(Map<String, String> parameters) {
        SortedMap<String, String> sortedParameters = signatureService.sortedNonBlankParameters(parameters);
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParameters.entrySet()) {
            if (!queryString.isEmpty()) {
                queryString.append('&');
            }
            queryString.append(VnPayUrlEncoding.encode(entry.getKey()))
                    .append('=')
                    .append(VnPayUrlEncoding.encode(entry.getValue()));
        }
        return queryString.toString();
    }

    private void validateProperties(VnPayProperties properties) {
        if (properties == null || !properties.isAvailable()) {
            String propertyName = properties == null ? "payment.vnpay" : properties.unavailableReason();
            throw new InvalidDataException("VNPay configuration is missing: " + propertyName);
        }
    }

    private void validateCommand(GatewayPaymentCreationCommand command) {
        if (command == null) {
            throw new InvalidDataException("VNPay payment command is required");
        }
        validatePaymentReference(command.paymentReference());
        if (command.clientIp() == null || command.clientIp().isBlank()) {
            throw new InvalidDataException("Client IP is required");
        }
        if (command.clientIp().length() > 45) {
            throw new InvalidDataException("Client IP must be at most 45 characters");
        }
    }

    private void validatePaymentReference(String paymentReference) {
        if (paymentReference == null || paymentReference.isBlank()) {
            throw new InvalidDataException("Payment reference is required");
        }
        if (paymentReference.length() > 100) {
            throw new InvalidDataException("Payment reference must be at most 100 characters");
        }
        if (!paymentReference.matches("[A-Za-z0-9._-]+")) {
            throw new InvalidDataException("Payment reference contains unsupported characters");
        }
    }

    public record VnPayPaymentUrl(
            String paymentUrl,
            Map<String, String> parameters,
            String secureHash
    ) {
    }
}
