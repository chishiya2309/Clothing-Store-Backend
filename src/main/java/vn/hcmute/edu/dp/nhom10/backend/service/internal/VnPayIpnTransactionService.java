package vn.hcmute.edu.dp.nhom10.backend.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.VnPayCallbackData;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.VnPayIpnTransactionResult;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSessionItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.InventoryReservation;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.Payment;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.entity.VoucherReservation;
import vn.hcmute.edu.dp.nhom10.backend.enums.CheckoutSessionStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentAttemptStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReservationStatus;
import vn.hcmute.edu.dp.nhom10.backend.event.OrderCreatedEvent;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.VnPayAmountMatcher;
import vn.hcmute.edu.dp.nhom10.backend.repository.CartItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.InventoryReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentAttemptRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.OrderStatusHistoryService;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "VNPAY-IPN-TX")
public class VnPayIpnTransactionService {

    private static final int ORDER_CODE_RETRY_LIMIT = 5;

    private final PaymentAttemptRepository paymentAttemptRepository;
    private final CheckoutSessionRepository checkoutSessionRepository;
    private final CheckoutSessionItemRepository checkoutSessionItemRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final ProductVariantRepository productVariantRepository;
    private final VoucherReservationRepository voucherReservationRepository;
    private final VoucherRepository voucherRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final CartItemRepository cartItemRepository;
    private final VnPayAmountMatcher amountMatcher;
    private final OrderStatusHistoryService orderStatusHistoryService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public VnPayIpnTransactionResult process(VnPayCallbackData callbackData) {
        Long checkoutSessionId = paymentAttemptRepository
                .findCheckoutSessionIdByPaymentReference(callbackData.paymentReference())
                .orElse(null);
        if (checkoutSessionId == null) {
            return VnPayIpnTransactionResult.notFound();
        }

        CheckoutSession checkoutSession = checkoutSessionRepository
                .findByIdForUpdate(checkoutSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkout session not found for payment attempt"));
        PaymentAttempt paymentAttempt = paymentAttemptRepository
                .findByPaymentReferenceForUpdate(callbackData.paymentReference())
                .orElse(null);
        if (paymentAttempt == null) {
            return VnPayIpnTransactionResult.notFound();
        }
        validateAttemptBelongsToCheckout(paymentAttempt, checkoutSession);

        if (!amountMatcher.matches(callbackData.amount(), paymentAttempt.getAmount())) {
            return VnPayIpnTransactionResult.invalidAmount();
        }
        if (paymentAttempt.getMethod() != PaymentMethod.vnpay
                || checkoutSession.getPaymentMethod() != PaymentMethod.vnpay) {
            return VnPayIpnTransactionResult.unknownError();
        }

        PaymentAttemptStatus status = paymentAttempt.getStatus();
        if (isTerminal(status)) {
            log.info("VNPay IPN transaction already terminal: paymentReference={}, gatewayTransactionId={}, status={}",
                    callbackData.paymentReference(), callbackData.transactionNumber(), status);
            return VnPayIpnTransactionResult.alreadyProcessed(alreadyProcessedMessage(status));
        }
        if (!callbackData.isGatewaySuccess()) {
            return processFailedPayment(callbackData, checkoutSession, paymentAttempt);
        }
        if (status == PaymentAttemptStatus.failed || status == PaymentAttemptStatus.expired) {
            markRequiresRefund(paymentAttempt, callbackData, "Paid callback arrived after attempt was " + status);
            return VnPayIpnTransactionResult.confirmed();
        }
        if (status != PaymentAttemptStatus.pending) {
            return VnPayIpnTransactionResult.alreadyProcessed("Transaction already processed");
        }

        return finalizePaidCheckout(callbackData, checkoutSession, paymentAttempt);
    }

    private VnPayIpnTransactionResult processFailedPayment(
            VnPayCallbackData callbackData,
            CheckoutSession checkoutSession,
            PaymentAttempt paymentAttempt
    ) {
        PaymentAttemptStatus status = paymentAttempt.getStatus();
        if (status == PaymentAttemptStatus.failed || status == PaymentAttemptStatus.expired) {
            return VnPayIpnTransactionResult.alreadyProcessed("Transaction already processed");
        }
        if (status != PaymentAttemptStatus.pending) {
            return VnPayIpnTransactionResult.alreadyProcessed(alreadyProcessedMessage(status));
        }

        paymentAttempt.setStatus(PaymentAttemptStatus.failed);
        paymentAttempt.setGatewayTransactionId(callbackData.transactionNumber());
        paymentAttempt.setGatewayPayload(sanitizedPayload(callbackData));
        paymentAttempt.setFailureReason("VNPay responseCode=" + callbackData.responseCode()
                + ", transactionStatus=" + callbackData.transactionStatus());
        paymentAttempt.setFailedAt(OffsetDateTime.now());
        paymentAttemptRepository.save(paymentAttempt);

        releaseReservations(checkoutSession);
        if (checkoutSession.getStatus() != CheckoutSessionStatus.completed) {
            checkoutSession.setStatus(CheckoutSessionStatus.failed);
            checkoutSessionRepository.save(checkoutSession);
        }
        log.info("VNPay failed payment recorded: checkoutCode={}, paymentReference={}, gatewayTransactionId={}, responseCode={}, transactionStatus={}",
                checkoutSession.getCheckoutCode(), callbackData.paymentReference(), callbackData.transactionNumber(),
                callbackData.responseCode(), callbackData.transactionStatus());
        return VnPayIpnTransactionResult.confirmed();
    }

    private VnPayIpnTransactionResult finalizePaidCheckout(
            VnPayCallbackData callbackData,
            CheckoutSession checkoutSession,
            PaymentAttempt paymentAttempt
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        List<CheckoutSessionItem> checkoutItems = checkoutSessionItemRepository
                .findAllByCheckoutSessionIdWithVariant(checkoutSession.getId());
        List<InventoryReservation> inventoryReservations = inventoryReservationRepository
                .findAllByCheckoutSessionIdForUpdate(checkoutSession.getId());
        List<VoucherReservation> voucherReservations = voucherReservationRepository
                .findAllByCheckoutSessionIdForUpdate(checkoutSession.getId());

        String refundReason = refundReasonIfCannotFulfill(
                checkoutSession,
                checkoutItems,
                inventoryReservations,
                voucherReservations,
                now
        );
        if (refundReason != null) {
            markRequiresRefund(paymentAttempt, callbackData, refundReason);
            return VnPayIpnTransactionResult.confirmed();
        }

        Map<Long, ProductVariant> variantsById = lockVariants(inventoryReservations);
        Map<Long, Voucher> lockedVouchers = lockVouchers(voucherReservations);

        Order savedOrder = orderRepository.save(createOrder(checkoutSession));
        orderStatusHistoryService.recordInitialStatus(savedOrder);
        orderItemRepository.saveAll(checkoutItems.stream()
                .map(item -> toOrderItem(savedOrder, item))
                .toList());
        paymentRepository.save(Payment.builder()
                .order(savedOrder)
                .method(PaymentMethod.vnpay)
                .amount(checkoutSession.getTotalAmount())
                .status(PaymentStatus.completed)
                .transactionId(callbackData.transactionNumber())
                .paymentData(sanitizedPayload(callbackData))
                .paidAt(now)
                .build());

        consumeInventoryReservations(inventoryReservations, variantsById);
        consumeVoucherReservations(voucherReservations, lockedVouchers);
        cartItemRepository.deletePurchasedItems(
                checkoutSession.getUser().getId(),
                productVariantIds(checkoutItems)
        );

        checkoutSession.setStatus(CheckoutSessionStatus.completed);
        checkoutSessionRepository.save(checkoutSession);

        paymentAttempt.setStatus(PaymentAttemptStatus.completed);
        paymentAttempt.setGatewayTransactionId(callbackData.transactionNumber());
        paymentAttempt.setGatewayPayload(sanitizedPayload(callbackData));
        paymentAttempt.setCompletedAt(now);
        paymentAttemptRepository.save(paymentAttempt);

        publishOrderCreatedEvent(savedOrder);
        log.info("VNPay paid checkout finalized: checkoutCode={}, paymentReference={}, gatewayTransactionId={}, orderCode={}",
                checkoutSession.getCheckoutCode(), callbackData.paymentReference(),
                callbackData.transactionNumber(), savedOrder.getOrderCode());
        return VnPayIpnTransactionResult.confirmed();
    }

    private String refundReasonIfCannotFulfill(
            CheckoutSession checkoutSession,
            List<CheckoutSessionItem> checkoutItems,
            List<InventoryReservation> inventoryReservations,
            List<VoucherReservation> voucherReservations,
            OffsetDateTime now
    ) {
        if (checkoutSession.getStatus() != CheckoutSessionStatus.reserved) {
            return "Checkout status is " + checkoutSession.getStatus();
        }
        if (checkoutSession.getExpiresAt() == null || !checkoutSession.getExpiresAt().isAfter(now)) {
            return "Checkout session expired";
        }
        if (checkoutItems == null || checkoutItems.isEmpty()) {
            return "Checkout item snapshot is empty";
        }
        if (inventoryReservations == null || inventoryReservations.isEmpty()) {
            return "Inventory reservation is missing";
        }
        for (InventoryReservation reservation : inventoryReservations) {
            if (reservation.getStatus() != ReservationStatus.active) {
                return "Inventory reservation is " + reservation.getStatus();
            }
            if (reservation.getExpiresAt() == null || !reservation.getExpiresAt().isAfter(now)) {
                return "Inventory reservation expired";
            }
        }
        if (checkoutSession.getVoucher() != null && (voucherReservations == null || voucherReservations.isEmpty())) {
            return "Voucher reservation is missing";
        }
        if (voucherReservations != null) {
            for (VoucherReservation voucherReservation : voucherReservations) {
                if (voucherReservation.getStatus() != ReservationStatus.active) {
                    return "Voucher reservation is " + voucherReservation.getStatus();
                }
                if (voucherReservation.getExpiresAt() == null || !voucherReservation.getExpiresAt().isAfter(now)) {
                    return "Voucher reservation expired";
                }
                if (voucherReservation.getVoucher() == null || voucherReservation.getVoucher().getId() == null) {
                    return "Voucher reservation is missing voucher";
                }
            }
        }
        return null;
    }

    private Map<Long, Voucher> lockVouchers(List<VoucherReservation> voucherReservations) {
        if (voucherReservations == null || voucherReservations.isEmpty()) {
            return Map.of();
        }
        List<Long> voucherIds = voucherReservations.stream()
                .map(VoucherReservation::getVoucher)
                .filter(Objects::nonNull)
                .map(Voucher::getId)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
        Map<Long, Voucher> vouchersById = new LinkedHashMap<>();
        for (Long voucherId : voucherIds) {
            Voucher voucher = voucherRepository.findByIdForUpdate(voucherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with ID: " + voucherId));
            vouchersById.put(voucherId, voucher);
        }
        return vouchersById;
    }

    private void consumeVoucherReservations(
            List<VoucherReservation> voucherReservations,
            Map<Long, Voucher> lockedVouchers
    ) {
        if (voucherReservations == null || voucherReservations.isEmpty()) {
            return;
        }
        for (VoucherReservation voucherReservation : voucherReservations) {
            Voucher lockedVoucher = lockedVouchers.get(voucherReservation.getVoucher().getId());
            if (lockedVoucher == null) {
                throw new ResourceNotFoundException("Voucher not found for reservation: " + voucherReservation.getId());
            }
            Integer timesUsed = lockedVoucher.getTimesUsed();
            Integer usageLimit = lockedVoucher.getUsageLimit();
            if (timesUsed == null || usageLimit == null || timesUsed >= usageLimit) {
                throw new InvalidDataException("Voucher usage limit has been reached");
            }
            lockedVoucher.setTimesUsed(timesUsed + 1);
            voucherReservation.setStatus(ReservationStatus.consumed);
        }
        voucherRepository.saveAll(lockedVouchers.values());
        voucherReservationRepository.saveAll(voucherReservations);
    }

    private Map<Long, ProductVariant> lockVariants(List<InventoryReservation> inventoryReservations) {
        List<Long> variantIds = inventoryReservations.stream()
                .map(reservation -> reservation.getProductVariant().getId())
                .distinct()
                .sorted()
                .toList();
        Map<Long, ProductVariant> variantsById = productVariantRepository.findAllByIdInForUpdate(variantIds)
                .stream()
                .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));
        for (Long variantId : variantIds) {
            if (!variantsById.containsKey(variantId)) {
                throw new ResourceNotFoundException("Product variant not found with ID: " + variantId);
            }
        }
        return variantsById;
    }

    private void consumeInventoryReservations(
            List<InventoryReservation> inventoryReservations,
            Map<Long, ProductVariant> variantsById
    ) {
        for (InventoryReservation reservation : inventoryReservations) {
            ProductVariant variant = variantsById.get(reservation.getProductVariant().getId());
            int stockQuantity = Objects.requireNonNullElse(variant.getStockQuantity(), 0);
            Integer quantity = reservation.getQuantity();
            if (quantity == null || quantity <= 0 || stockQuantity < quantity) {
                throw new InvalidDataException("Inventory reservation cannot be consumed safely");
            }
            variant.setStockQuantity(stockQuantity - quantity);
            reservation.setStatus(ReservationStatus.consumed);
        }
        productVariantRepository.saveAll(variantsById.values());
        inventoryReservationRepository.saveAll(inventoryReservations);
    }

    private void releaseReservations(CheckoutSession checkoutSession) {
        List<InventoryReservation> inventoryReservations =
                inventoryReservationRepository.findAllByCheckoutSessionIdForUpdate(checkoutSession.getId());
        List<InventoryReservation> changedInventoryReservations = inventoryReservations.stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.active)
                .peek(reservation -> reservation.setStatus(ReservationStatus.released))
                .toList();
        if (!changedInventoryReservations.isEmpty()) {
            inventoryReservationRepository.saveAll(changedInventoryReservations);
        }

        List<VoucherReservation> voucherReservations =
                voucherReservationRepository.findAllByCheckoutSessionIdForUpdate(checkoutSession.getId());
        List<VoucherReservation> changedVoucherReservations = voucherReservations.stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.active)
                .peek(reservation -> reservation.setStatus(ReservationStatus.released))
                .toList();
        if (!changedVoucherReservations.isEmpty()) {
            voucherReservationRepository.saveAll(changedVoucherReservations);
        }
    }

    private void markRequiresRefund(
            PaymentAttempt paymentAttempt,
            VnPayCallbackData callbackData,
            String reason
    ) {
        paymentAttempt.setStatus(PaymentAttemptStatus.requires_refund);
        paymentAttempt.setGatewayTransactionId(callbackData.transactionNumber());
        paymentAttempt.setGatewayPayload(sanitizedPayload(callbackData));
        paymentAttempt.setRequiresRefundReason(safeReason(reason));
        paymentAttemptRepository.save(paymentAttempt);
        log.warn("VNPay paid callback requires refund: paymentReference={}, gatewayTransactionId={}, reason={}",
                callbackData.paymentReference(), callbackData.transactionNumber(), safeReason(reason));
    }

    private boolean isTerminal(PaymentAttemptStatus status) {
        return status == PaymentAttemptStatus.completed
                || status == PaymentAttemptStatus.requires_refund
                || status == PaymentAttemptStatus.refund_requested
                || status == PaymentAttemptStatus.refunded;
    }

    private String alreadyProcessedMessage(PaymentAttemptStatus status) {
        if (status == PaymentAttemptStatus.completed) {
            return "Order already confirmed";
        }
        return "Transaction already processed";
    }

    private void validateAttemptBelongsToCheckout(
            PaymentAttempt paymentAttempt,
            CheckoutSession checkoutSession
    ) {
        if (paymentAttempt.getCheckoutSession() == null
                || paymentAttempt.getCheckoutSession().getId() == null
                || !paymentAttempt.getCheckoutSession().getId().equals(checkoutSession.getId())) {
            throw new InvalidDataException("Payment attempt does not belong to locked checkout session");
        }
    }

    private Map<String, Object> sanitizedPayload(VnPayCallbackData callbackData) {
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfPresent(payload, "vnp_Amount", callbackData.amount());
        putIfPresent(payload, "vnp_BankCode", callbackData.bankCode());
        putIfPresent(payload, "vnp_BankTranNo", callbackData.bankTransactionNumber());
        putIfPresent(payload, "vnp_CardType", callbackData.cardType());
        putIfPresent(payload, "vnp_OrderInfo", callbackData.orderInfo());
        putIfPresent(payload, "vnp_PayDate", callbackData.payDate());
        putIfPresent(payload, "vnp_ResponseCode", callbackData.responseCode());
        putIfPresent(payload, "vnp_TmnCode", callbackData.terminalCode());
        putIfPresent(payload, "vnp_TransactionNo", callbackData.transactionNumber());
        putIfPresent(payload, "vnp_TransactionStatus", callbackData.transactionStatus());
        putIfPresent(payload, "vnp_TxnRef", callbackData.paymentReference());
        return payload;
    }

    private void putIfPresent(Map<String, Object> payload, String key, String value) {
        if (value != null && !value.isBlank()) {
            payload.put(key, value);
        }
    }

    private Order createOrder(CheckoutSession checkoutSession) {
        return Order.builder()
                .orderCode(generateOrderCode())
                .user(checkoutSession.getUser())
                .shippingName(checkoutSession.getShippingName())
                .shippingPhone(checkoutSession.getShippingPhone())
                .shippingProvince(checkoutSession.getShippingProvince())
                .shippingDistrict(checkoutSession.getShippingDistrict())
                .shippingWard(checkoutSession.getShippingWard())
                .shippingAddress(checkoutSession.getShippingAddress())
                .subtotal(checkoutSession.getSubtotal())
                .shippingFee(checkoutSession.getShippingFee())
                .discountAmount(checkoutSession.getDiscountAmount())
                .totalAmount(checkoutSession.getTotalAmount())
                .voucher(checkoutSession.getVoucher())
                .status(OrderStatus.pending)
                .build();
    }

    private OrderItem toOrderItem(Order order, CheckoutSessionItem checkoutSessionItem) {
        return OrderItem.builder()
                .order(order)
                .productVariant(checkoutSessionItem.getProductVariant())
                .productName(checkoutSessionItem.getProductName())
                .variantInfo(checkoutSessionItem.getVariantInfo())
                .quantity(checkoutSessionItem.getQuantity())
                .unitPrice(checkoutSessionItem.getUnitPrice())
                .subtotal(checkoutSessionItem.getSubtotal())
                .build();
    }

    private Collection<Long> productVariantIds(List<CheckoutSessionItem> checkoutItems) {
        return checkoutItems.stream()
                .map(item -> item.getProductVariant().getId())
                .distinct()
                .toList();
    }

    private void publishOrderCreatedEvent(Order order) {
        eventPublisher.publishEvent(new OrderCreatedEvent(
                order.getId(),
                order.getOrderCode(),
                order.getUser().getId(),
                order.getTotalAmount(),
                OffsetDateTime.now()
        ));
    }

    private String generateOrderCode() {
        for (int i = 0; i < ORDER_CODE_RETRY_LIMIT; i++) {
            String orderCode = "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            if (!orderRepository.existsByOrderCode(orderCode)) {
                return orderCode;
            }
        }
        throw new InvalidDataException("Unable to generate unique order code");
    }

    private String safeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return "Paid callback cannot be fulfilled safely";
        }
        String trimmed = reason.trim();
        return trimmed.length() <= 500 ? trimmed : trimmed.substring(0, 500);
    }
}
