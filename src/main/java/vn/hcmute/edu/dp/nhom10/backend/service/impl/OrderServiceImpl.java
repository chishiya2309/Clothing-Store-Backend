package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.OrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSessionItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.Payment;
import vn.hcmute.edu.dp.nhom10.backend.enums.CheckoutSessionStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentStatus;
import vn.hcmute.edu.dp.nhom10.backend.event.OrderCreatedEvent;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.CartItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.InventoryReservationService;
import vn.hcmute.edu.dp.nhom10.backend.service.OrderService;
import vn.hcmute.edu.dp.nhom10.backend.service.OrderStatusHistoryService;
import vn.hcmute.edu.dp.nhom10.backend.service.VoucherReservationService;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final int ORDER_CODE_RETRY_LIMIT = 5;

    private final CheckoutSessionRepository checkoutSessionRepository;
    private final CheckoutSessionItemRepository checkoutSessionItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryReservationService inventoryReservationService;
    private final VoucherReservationService voucherService;
    private final OrderStatusHistoryService orderStatusHistoryService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public OrderResponseDTO createCodOrder(String checkoutCode, Long userId) {
        String normalizedCheckoutCode = normalizeCheckoutCode(checkoutCode);
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        CheckoutSession checkoutSession = checkoutSessionRepository.findByCheckoutCodeForUpdate(normalizedCheckoutCode)
                .orElseThrow(() -> new ResourceNotFoundException("Checkout session not found with code: " + normalizedCheckoutCode));
        validateCheckoutSession(checkoutSession, userId);

        List<CheckoutSessionItem> checkoutItems = checkoutSessionItemRepository
                .findAllByCheckoutSessionIdWithVariant(checkoutSession.getId());
        if (checkoutItems.isEmpty()) {
            throw new InvalidDataException("Checkout session item snapshot is empty");
        }

        Order order = createOrder(checkoutSession);
        Order savedOrder = orderRepository.save(order);
        orderStatusHistoryService.recordInitialStatus(savedOrder);

        List<OrderItem> orderItems = checkoutItems.stream()
                .map(item -> toOrderItem(savedOrder, item))
                .toList();
        orderItemRepository.saveAll(orderItems);

        Payment payment = Payment.builder()
                .order(savedOrder)
                .method(PaymentMethod.cod)
                .amount(checkoutSession.getTotalAmount())
                .status(PaymentStatus.pending)
                .build();
        paymentRepository.save(payment);

        inventoryReservationService.consumeStockReservation(normalizedCheckoutCode);
        if (checkoutSession.getVoucher() != null) {
            voucherService.consumeVoucherReservation(normalizedCheckoutCode);
        }

        cartItemRepository.deletePurchasedItems(userId, getProductVariantIds(checkoutItems));

        checkoutSession.setStatus(CheckoutSessionStatus.completed);
        checkoutSessionRepository.save(checkoutSession);
        publishOrderCreatedEvent(savedOrder);

        return toOrderResponse(savedOrder);
    }

    private void validateCheckoutSession(CheckoutSession checkoutSession, Long userId) {
        if (checkoutSession.getUser() == null || checkoutSession.getUser().getId() == null
                || !checkoutSession.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Checkout session not found");
        }
        if (checkoutSession.getPaymentMethod() != PaymentMethod.cod) {
            throw new InvalidDataException("Checkout session payment method is not COD");
        }
        if (checkoutSession.getStatus() != CheckoutSessionStatus.reserved) {
            throw new InvalidDataException("Checkout session status must be reserved");
        }
        if (checkoutSession.getExpiresAt() == null || !checkoutSession.getExpiresAt().isAfter(OffsetDateTime.now())) {
            throw new InvalidDataException("Checkout session has expired");
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

    private OrderItem toOrderItem(Order savedOrder, CheckoutSessionItem checkoutSessionItem) {
        return OrderItem.builder()
                .order(savedOrder)
                .productVariant(checkoutSessionItem.getProductVariant())
                .productName(checkoutSessionItem.getProductName())
                .variantInfo(checkoutSessionItem.getVariantInfo())
                .quantity(checkoutSessionItem.getQuantity())
                .unitPrice(checkoutSessionItem.getUnitPrice())
                .subtotal(checkoutSessionItem.getSubtotal())
                .build();
    }

    private Collection<Long> getProductVariantIds(List<CheckoutSessionItem> checkoutItems) {
        return checkoutItems.stream()
                .map(item -> item.getProductVariant().getId())
                .distinct()
                .toList();
    }

    private OrderResponseDTO toOrderResponse(Order order) {
        return OrderResponseDTO.builder()
                .orderCode(order.getOrderCode())
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .build();
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

    private String normalizeCheckoutCode(String checkoutCode) {
        if (checkoutCode == null || checkoutCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Checkout code is required");
        }
        return checkoutCode.trim();
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
}
