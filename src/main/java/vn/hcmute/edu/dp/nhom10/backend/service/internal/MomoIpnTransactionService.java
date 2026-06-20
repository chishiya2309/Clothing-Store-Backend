package vn.hcmute.edu.dp.nhom10.backend.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoIpnRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoIpnTransactionResult;
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
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.MomoAmountConverter;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.MomoResultStatusClassifier;
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
@Slf4j(topic = "MOMO-IPN-TX")
public class MomoIpnTransactionService {

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
    private final MomoAmountConverter amountConverter;
    private final MomoResultStatusClassifier resultStatusClassifier;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public MomoIpnTransactionResult process(MomoIpnRequest request) {
        Long checkoutSessionId = paymentAttemptRepository
                .findCheckoutSessionIdByPaymentReference(request.orderId())
                .orElse(null);
        if (checkoutSessionId == null) {
            return MomoIpnTransactionResult.notFound();
        }

        CheckoutSession checkoutSession = checkoutSessionRepository
                .findByIdForUpdate(checkoutSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkout session not found for payment attempt"));
        PaymentAttempt paymentAttempt = paymentAttemptRepository
                .findByPaymentReferenceForUpdate(request.orderId())
                .orElse(null);
        if (paymentAttempt == null) {
            return MomoIpnTransactionResult.notFound();
        }
        validateAttemptBelongsToCheckout(paymentAttempt, checkoutSession);

        if (!amountConverter.matches(request.amount(), paymentAttempt.getAmount())) {
            return MomoIpnTransactionResult.invalidAmount();
        }
        if (paymentAttempt.getMethod() != PaymentMethod.momo
                || checkoutSession.getPaymentMethod() != PaymentMethod.momo) {
            return MomoIpnTransactionResult.unknownError();
        }

        PaymentAttemptStatus currentStatus = paymentAttempt.getStatus();
        if (isTerminal(currentStatus)) {
            log.info("MoMo IPN already terminal: paymentReference={}, transId={}, status={}",
                    request.orderId(), request.transId(), currentStatus);
            return MomoIpnTransactionResult.accepted("Transaction already processed");
        }

        MomoResultStatusClassifier.Status resultStatus = resultStatusClassifier.classify(request.resultCode());
        if (resultStatus == MomoResultStatusClassifier.Status.PENDING
                || resultStatus == MomoResultStatusClassifier.Status.AUTHORIZED) {
            paymentAttempt.setGatewayPayload(sanitizedPayload(request));
            paymentAttemptRepository.save(paymentAttempt);
            return MomoIpnTransactionResult.accepted("MoMo transaction is still processing");
        }
        if (resultStatus == MomoResultStatusClassifier.Status.FAILED) {
            return processFailedPayment(request, checkoutSession, paymentAttempt);
        }
        if (currentStatus == PaymentAttemptStatus.failed || currentStatus == PaymentAttemptStatus.expired) {
            markRequiresRefund(paymentAttempt, request, "Paid callback arrived after attempt was " + currentStatus);
            return MomoIpnTransactionResult.accepted("Requires refund");
        }
        if (currentStatus != PaymentAttemptStatus.pending) {
            return MomoIpnTransactionResult.accepted("Transaction already processed");
        }

        return finalizePaidCheckout(request, checkoutSession, paymentAttempt);
    }

    private MomoIpnTransactionResult processFailedPayment(
            MomoIpnRequest request,
            CheckoutSession checkoutSession,
            PaymentAttempt paymentAttempt
    ) {
        PaymentAttemptStatus status = paymentAttempt.getStatus();
        if (status == PaymentAttemptStatus.failed || status == PaymentAttemptStatus.expired) {
            return MomoIpnTransactionResult.accepted("Transaction already processed");
        }
        if (status != PaymentAttemptStatus.pending) {
            return MomoIpnTransactionResult.accepted("Transaction already processed");
        }

        paymentAttempt.setStatus(PaymentAttemptStatus.failed);
        paymentAttempt.setGatewayTransactionId(request.transId());
        paymentAttempt.setGatewayPayload(sanitizedPayload(request));
        paymentAttempt.setFailureReason("MoMo resultCode=" + request.resultCode());
        paymentAttempt.setFailedAt(OffsetDateTime.now());
        paymentAttemptRepository.save(paymentAttempt);

        releaseReservations(checkoutSession);
        if (checkoutSession.getStatus() != CheckoutSessionStatus.completed) {
            checkoutSession.setStatus(CheckoutSessionStatus.failed);
            checkoutSessionRepository.save(checkoutSession);
        }
        return MomoIpnTransactionResult.accepted("MoMo failed payment recorded");
    }

    private MomoIpnTransactionResult finalizePaidCheckout(
            MomoIpnRequest request,
            CheckoutSession checkoutSession,
            PaymentAttempt paymentAttempt
    ) {
        OffsetDateTime now = OffsetDateTime.now();
        List<CheckoutSessionItem> checkoutItems = checkoutSessionItemRepository
                .findAllByCheckoutSessionIdWithVariant(checkoutSession.getId());
        List<InventoryReservation> inventoryReservations = inventoryReservationRepository
                .findAllByCheckoutSessionIdForUpdate(checkoutSession.getId());
        VoucherReservation voucherReservation = voucherReservationRepository
                .findByCheckoutSessionIdForUpdate(checkoutSession.getId())
                .orElse(null);

        String refundReason = refundReasonIfCannotFulfill(
                checkoutSession,
                checkoutItems,
                inventoryReservations,
                voucherReservation,
                now
        );
        if (refundReason != null) {
            markRequiresRefund(paymentAttempt, request, refundReason);
            return MomoIpnTransactionResult.accepted("Requires refund");
        }

        Map<Long, ProductVariant> variantsById = lockVariants(inventoryReservations);
        Voucher lockedVoucher = lockVoucherIfNeeded(checkoutSession, voucherReservation);

        Order savedOrder = orderRepository.save(createOrder(checkoutSession));
        orderItemRepository.saveAll(checkoutItems.stream()
                .map(item -> toOrderItem(savedOrder, item))
                .toList());
        paymentRepository.save(Payment.builder()
                .order(savedOrder)
                .method(PaymentMethod.momo)
                .amount(checkoutSession.getTotalAmount())
                .status(PaymentStatus.completed)
                .transactionId(request.transId())
                .paymentData(sanitizedPayload(request))
                .paidAt(now)
                .build());

        consumeInventoryReservations(inventoryReservations, variantsById);
        consumeVoucherReservation(voucherReservation, lockedVoucher);
        cartItemRepository.deletePurchasedItems(
                checkoutSession.getUser().getId(),
                productVariantIds(checkoutItems)
        );

        checkoutSession.setStatus(CheckoutSessionStatus.completed);
        checkoutSessionRepository.save(checkoutSession);

        paymentAttempt.setStatus(PaymentAttemptStatus.completed);
        paymentAttempt.setGatewayTransactionId(request.transId());
        paymentAttempt.setGatewayPayload(sanitizedPayload(request));
        paymentAttempt.setCompletedAt(now);
        paymentAttemptRepository.save(paymentAttempt);

        publishOrderCreatedEvent(savedOrder);
        log.info("MoMo paid checkout finalized: checkoutCode={}, paymentReference={}, transId={}, orderCode={}",
                checkoutSession.getCheckoutCode(), request.orderId(), request.transId(), savedOrder.getOrderCode());
        return MomoIpnTransactionResult.accepted("MoMo paid checkout finalized");
    }

    private String refundReasonIfCannotFulfill(
            CheckoutSession checkoutSession,
            List<CheckoutSessionItem> checkoutItems,
            List<InventoryReservation> inventoryReservations,
            VoucherReservation voucherReservation,
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
        if (checkoutSession.getVoucher() != null) {
            if (voucherReservation == null) {
                return "Voucher reservation is missing";
            }
            if (voucherReservation.getStatus() != ReservationStatus.active) {
                return "Voucher reservation is " + voucherReservation.getStatus();
            }
            if (voucherReservation.getExpiresAt() == null || !voucherReservation.getExpiresAt().isAfter(now)) {
                return "Voucher reservation expired";
            }
        }
        return null;
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

    private Voucher lockVoucherIfNeeded(CheckoutSession checkoutSession, VoucherReservation voucherReservation) {
        if (checkoutSession.getVoucher() == null) {
            return null;
        }
        Long voucherId = voucherReservation.getVoucher().getId();
        return voucherRepository.findByIdForUpdate(voucherId)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with ID: " + voucherId));
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

    private void consumeVoucherReservation(VoucherReservation voucherReservation, Voucher lockedVoucher) {
        if (voucherReservation == null || lockedVoucher == null) {
            return;
        }
        Integer timesUsed = lockedVoucher.getTimesUsed();
        Integer usageLimit = lockedVoucher.getUsageLimit();
        if (timesUsed == null || usageLimit == null || timesUsed >= usageLimit) {
            throw new InvalidDataException("Voucher usage limit has been reached");
        }
        lockedVoucher.setTimesUsed(timesUsed + 1);
        voucherReservation.setStatus(ReservationStatus.consumed);
        voucherRepository.save(lockedVoucher);
        voucherReservationRepository.save(voucherReservation);
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

        voucherReservationRepository.findByCheckoutSessionIdForUpdate(checkoutSession.getId())
                .filter(reservation -> reservation.getStatus() == ReservationStatus.active)
                .ifPresent(reservation -> {
                    reservation.setStatus(ReservationStatus.released);
                    voucherReservationRepository.save(reservation);
                });
    }

    private void markRequiresRefund(
            PaymentAttempt paymentAttempt,
            MomoIpnRequest request,
            String reason
    ) {
        paymentAttempt.setStatus(PaymentAttemptStatus.requires_refund);
        paymentAttempt.setGatewayTransactionId(request.transId());
        paymentAttempt.setGatewayPayload(sanitizedPayload(request));
        paymentAttempt.setRequiresRefundReason(safeReason(reason));
        paymentAttemptRepository.save(paymentAttempt);
        log.warn("MoMo paid callback requires refund: paymentReference={}, transId={}, reason={}",
                request.orderId(), request.transId(), safeReason(reason));
    }

    private boolean isTerminal(PaymentAttemptStatus status) {
        return status == PaymentAttemptStatus.completed
                || status == PaymentAttemptStatus.requires_refund
                || status == PaymentAttemptStatus.refund_requested
                || status == PaymentAttemptStatus.refunded;
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

    private Map<String, Object> sanitizedPayload(MomoIpnRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("partnerCode", request.partnerCode());
        payload.put("orderId", request.orderId());
        payload.put("requestId", request.requestId());
        payload.put("amount", request.amount());
        payload.put("orderInfo", request.orderInfo());
        payload.put("orderType", request.orderType());
        payload.put("transId", request.transId());
        payload.put("resultCode", request.resultCode());
        payload.put("message", request.message());
        payload.put("payType", request.payType());
        payload.put("responseTime", request.responseTime());
        payload.put("extraData", request.extraData());
        return payload;
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
