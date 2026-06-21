package vn.hcmute.edu.dp.nhom10.backend.service.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.OnlinePaymentInitializationResult;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.PendingPaymentContext;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;
import vn.hcmute.edu.dp.nhom10.backend.enums.CheckoutSessionStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentAttemptStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.GatewayPaymentCreationResult;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentAttemptRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.InventoryReservationService;
import vn.hcmute.edu.dp.nhom10.backend.service.VoucherReservationService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentAttemptTransactionService {

    private static final int PAYMENT_REFERENCE_RETRY_LIMIT = 5;

    private final CheckoutSessionRepository checkoutSessionRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final InventoryReservationService inventoryReservationService;
    private final VoucherReservationService voucherService;

    @Transactional
    public PendingPaymentContext createPendingAttempt(String checkoutCode, Long userId) {
        String normalizedCheckoutCode = normalizeCheckoutCode(checkoutCode);
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        CheckoutSession checkoutSession = checkoutSessionRepository.findByCheckoutCodeForUpdate(normalizedCheckoutCode)
                .orElseThrow(() -> new ResourceNotFoundException("Checkout session not found with code: " + normalizedCheckoutCode));
        validateCheckoutForOnlinePayment(checkoutSession, userId);

        PaymentAttempt latestAttempt = paymentAttemptRepository
                .findTopByCheckoutSession_IdOrderByCreatedAtDesc(checkoutSession.getId())
                .orElse(null);
        if (latestAttempt != null) {
            PendingPaymentContext reusableContext = handleLatestAttempt(checkoutSession, latestAttempt);
            if (reusableContext != null) {
                return reusableContext;
            }
        }

        PaymentAttempt paymentAttempt = PaymentAttempt.builder()
                .paymentReference(generatePaymentReference())
                .checkoutSession(checkoutSession)
                .method(checkoutSession.getPaymentMethod())
                .amount(checkoutSession.getTotalAmount())
                .status(PaymentAttemptStatus.pending)
                .paymentUrl(null)
                .gatewayTransactionId(null)
                .gatewayPayload(null)
                .expiresAt(checkoutSession.getExpiresAt())
                .build();
        PaymentAttempt savedAttempt = paymentAttemptRepository.save(paymentAttempt);

        return toPendingPaymentContext(savedAttempt);
    }

    @Transactional
    public OnlinePaymentInitializationResult completeInitialization(
            String paymentReference,
            GatewayPaymentCreationResult gatewayResult
    ) {
        PaymentAttempt existingAttempt = paymentAttemptRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new ResourceNotFoundException("Payment attempt not found with reference: " + paymentReference));
        CheckoutSession checkoutSession = checkoutSessionRepository.findByIdForUpdate(existingAttempt.getCheckoutSession().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Checkout session not found for payment attempt: " + paymentReference));
        PaymentAttempt paymentAttempt = paymentAttemptRepository.findByPaymentReferenceForUpdate(paymentReference)
                .orElseThrow(() -> new ResourceNotFoundException("Payment attempt not found with reference: " + paymentReference));

        validateAttemptBelongsToCheckout(paymentAttempt, checkoutSession);
        validateCheckoutStillReservable(checkoutSession);
        if (paymentAttempt.getStatus() != PaymentAttemptStatus.pending) {
            throw new InvalidDataException("Payment attempt status must be pending");
        }
        if (paymentAttempt.getAmount() == null || paymentAttempt.getAmount().compareTo(checkoutSession.getTotalAmount()) != 0
                || paymentAttempt.getMethod() != checkoutSession.getPaymentMethod()) {
            throw new InvalidDataException("Payment attempt data no longer matches checkout session");
        }
        validatePaymentUrl(gatewayResult == null ? null : gatewayResult.paymentUrl());

        paymentAttempt.setPaymentUrl(gatewayResult.paymentUrl());
        paymentAttempt.setGatewayTransactionId(gatewayResult.gatewayTransactionId());
        paymentAttempt.setGatewayPayload(sanitizeGatewayPayload(gatewayResult.gatewayPayload()));
        paymentAttempt.setStatus(PaymentAttemptStatus.pending);
        PaymentAttempt savedAttempt = paymentAttemptRepository.save(paymentAttempt);

        return toOnlinePaymentInitializationResult(savedAttempt);
    }

    @Transactional
    public void failInitialization(String paymentReference, String failureReason) {
        PaymentAttempt existingAttempt = paymentAttemptRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new ResourceNotFoundException("Payment attempt not found with reference: " + paymentReference));
        CheckoutSession checkoutSession = checkoutSessionRepository.findByIdForUpdate(existingAttempt.getCheckoutSession().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Checkout session not found for payment attempt: " + paymentReference));
        PaymentAttempt paymentAttempt = paymentAttemptRepository.findByPaymentReferenceForUpdate(paymentReference)
                .orElseThrow(() -> new ResourceNotFoundException("Payment attempt not found with reference: " + paymentReference));

        validateAttemptBelongsToCheckout(paymentAttempt, checkoutSession);
        if (paymentAttempt.getStatus() == PaymentAttemptStatus.completed
                || paymentAttempt.getStatus() == PaymentAttemptStatus.requires_refund) {
            return;
        }
        if (paymentAttempt.getStatus() == PaymentAttemptStatus.pending) {
            paymentAttempt.setStatus(PaymentAttemptStatus.failed);
            paymentAttempt.setFailureReason(safeFailureReason(failureReason));
            paymentAttempt.setFailedAt(OffsetDateTime.now());
            paymentAttemptRepository.save(paymentAttempt);
        }

        inventoryReservationService.releaseStockReservation(checkoutSession.getCheckoutCode());
        if (checkoutSession.getVoucher() != null) {
            voucherService.releaseVoucherReservation(checkoutSession.getCheckoutCode());
        }
        if (checkoutSession.getStatus() != CheckoutSessionStatus.failed) {
            checkoutSession.setStatus(CheckoutSessionStatus.failed);
            checkoutSessionRepository.save(checkoutSession);
        }
    }

    private PendingPaymentContext handleLatestAttempt(CheckoutSession checkoutSession, PaymentAttempt latestAttempt) {
        if (latestAttempt.getStatus() == PaymentAttemptStatus.pending) {
            if (latestAttempt.getExpiresAt() != null && !latestAttempt.getExpiresAt().isAfter(OffsetDateTime.now())) {
                latestAttempt.setStatus(PaymentAttemptStatus.expired);
                paymentAttemptRepository.save(latestAttempt);
                return null;
            }
            if (latestAttempt.getPaymentUrl() != null && !latestAttempt.getPaymentUrl().isBlank()) {
                return toPendingPaymentContext(latestAttempt);
            }
            throw new InvalidDataException("Payment initialization is already in progress");
        }
        if (latestAttempt.getStatus() == PaymentAttemptStatus.completed
                || latestAttempt.getStatus() == PaymentAttemptStatus.requires_refund
                || latestAttempt.getStatus() == PaymentAttemptStatus.refund_requested
                || latestAttempt.getStatus() == PaymentAttemptStatus.refunded) {
            throw new InvalidDataException("Payment attempt cannot be initialized again because latest status is " + latestAttempt.getStatus());
        }
        return null;
    }

    private void validateCheckoutForOnlinePayment(CheckoutSession checkoutSession, Long userId) {
        if (checkoutSession.getUser() == null || checkoutSession.getUser().getId() == null
                || !checkoutSession.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Checkout session not found");
        }
        validateCheckoutStillReservable(checkoutSession);
        if (checkoutSession.getPaymentMethod() != PaymentMethod.vnpay
                && checkoutSession.getPaymentMethod() != PaymentMethod.momo) {
            throw new InvalidDataException("Checkout session payment method is not supported for online initialization");
        }
        BigDecimal totalAmount = checkoutSession.getTotalAmount();
        if (totalAmount == null || totalAmount.signum() <= 0) {
            throw new InvalidDataException("Checkout total amount must be greater than zero");
        }
    }

    private void validateCheckoutStillReservable(CheckoutSession checkoutSession) {
        if (checkoutSession.getStatus() != CheckoutSessionStatus.reserved) {
            throw new InvalidDataException("Checkout session status must be reserved");
        }
        if (checkoutSession.getExpiresAt() == null || !checkoutSession.getExpiresAt().isAfter(OffsetDateTime.now())) {
            throw new InvalidDataException("Checkout session has expired");
        }
    }

    private void validateAttemptBelongsToCheckout(PaymentAttempt paymentAttempt, CheckoutSession checkoutSession) {
        if (paymentAttempt.getCheckoutSession() == null
                || paymentAttempt.getCheckoutSession().getId() == null
                || !paymentAttempt.getCheckoutSession().getId().equals(checkoutSession.getId())) {
            throw new InvalidDataException("Payment attempt does not belong to locked checkout session");
        }
    }

    private PendingPaymentContext toPendingPaymentContext(PaymentAttempt paymentAttempt) {
        CheckoutSession checkoutSession = paymentAttempt.getCheckoutSession();
        return new PendingPaymentContext(
                checkoutSession.getId(),
                checkoutSession.getCheckoutCode(),
                paymentAttempt.getPaymentReference(),
                paymentAttempt.getMethod(),
                paymentAttempt.getAmount(),
                paymentAttempt.getExpiresAt(),
                paymentAttempt.getPaymentUrl()
        );
    }

    private OnlinePaymentInitializationResult toOnlinePaymentInitializationResult(PaymentAttempt paymentAttempt) {
        CheckoutSession checkoutSession = paymentAttempt.getCheckoutSession();
        return new OnlinePaymentInitializationResult(
                checkoutSession.getCheckoutCode(),
                paymentAttempt.getPaymentReference(),
                paymentAttempt.getMethod(),
                paymentAttempt.getPaymentUrl(),
                paymentAttempt.getAmount(),
                paymentAttempt.getExpiresAt()
        );
    }

    private void validatePaymentUrl(String paymentUrl) {
        if (paymentUrl == null || paymentUrl.isBlank()) {
            throw new InvalidDataException("Gateway payment URL is required");
        }
    }

    private Map<String, Object> sanitizeGatewayPayload(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return null;
        }
        Map<String, Object> sanitized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String key = entry.getKey();
            if (key == null || isSensitiveKey(key)) {
                continue;
            }
            sanitized.put(key, entry.getValue());
        }
        return sanitized.isEmpty() ? null : sanitized;
    }

    private boolean isSensitiveKey(String key) {
        String normalizedKey = key.toLowerCase();
        return normalizedKey.contains("secret")
                || normalizedKey.contains("hash")
                || normalizedKey.contains("signature")
                || normalizedKey.contains("accesskey")
                || normalizedKey.contains("token")
                || normalizedKey.contains("password");
    }

    private String safeFailureReason(String failureReason) {
        if (failureReason == null || failureReason.isBlank()) {
            return "Payment initialization failed";
        }
        String trimmed = failureReason.trim();
        return trimmed.length() <= 500 ? trimmed : trimmed.substring(0, 500);
    }

    private String normalizeCheckoutCode(String checkoutCode) {
        if (checkoutCode == null || checkoutCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Checkout code is required");
        }
        return checkoutCode.trim();
    }

    private String generatePaymentReference() {
        for (int i = 0; i < PAYMENT_REFERENCE_RETRY_LIMIT; i++) {
            String paymentReference = "PAY-" + UUID.randomUUID().toString().replace("-", "");
            if (!paymentAttemptRepository.existsByPaymentReference(paymentReference)) {
                return paymentReference;
            }
        }
        throw new InvalidDataException("Unable to generate unique payment reference");
    }
}
