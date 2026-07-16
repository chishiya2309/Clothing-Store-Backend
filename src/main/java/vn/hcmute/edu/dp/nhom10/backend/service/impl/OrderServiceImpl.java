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
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherRepository;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.entity.Payment;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentStatus;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.order.CustomerOrderCancellationStrategy;
import vn.hcmute.edu.dp.nhom10.backend.pattern.observer.order.OrderCancellationManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.OrderDetailResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.OrderHistoryItemResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductImage;
import vn.hcmute.edu.dp.nhom10.backend.enums.ImageType;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


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
    private final ProductVariantRepository productVariantRepository;
    private final VoucherRepository voucherRepository;
    private final CustomerOrderCancellationStrategy customerOrderCancellationStrategy;
    private final OrderCancellationManager orderCancellationManager;

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
                .flashSaleItem(checkoutSessionItem.getFlashSaleItem())
                .priceSource(checkoutSessionItem.getPriceSource())
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

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderHistoryItemResponse> getOrderHistory(String email, OrderStatus status, int page, int size) {
        Page<Order> orderPage = (status == null)
                ? orderRepository.findByUserEmail(email, PageRequest.of(page, size))
                : orderRepository.findByUserEmailAndStatus(email, status, PageRequest.of(page, size));

        List<OrderHistoryItemResponse> items = orderPage.getContent().stream().map(order -> {
            // Lấy tối đa 3 ảnh thumbnail từ các sản phẩm trong đơn
            List<OrderHistoryItemResponse.OrderHistoryProductImage> images = order.getOrderItems().stream()
                    .limit(3)
                    .map(oi -> {
                        // Lấy ảnh thumbnail đầu tiên của sản phẩm
                        String imageUrl = oi.getProductVariant().getProduct().getImages().stream()
                                .filter(img -> ImageType.thumbnail.equals(img.getImageType()))
                                .findFirst()
                                .or(() -> oi.getProductVariant().getProduct().getImages().stream().findFirst())
                                .map(ProductImage::getImageUrl)
                                .orElse(null);
                        return OrderHistoryItemResponse.OrderHistoryProductImage.builder()
                                .imageUrl(imageUrl)
                                .productName(oi.getProductName())
                                .build();
                    })
                    .collect(Collectors.toList());

            return OrderHistoryItemResponse.builder()
                    .id(order.getId())
                    .orderCode(order.getOrderCode())
                    .totalAmount(order.getTotalAmount())
                    .discountAmount(order.getDiscountAmount())
                    .status(order.getStatus())
                    .createdAt(order.getCreatedAt())
                    .itemCount(order.getOrderItems().size())
                    .productImages(images)
                    .build();
        }).collect(Collectors.toList());

        return PageResponse.<OrderHistoryItemResponse>builder()
                .pageNumber(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalPages(orderPage.getTotalPages())
                .totalElements(orderPage.getTotalElements())
                .content(items)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(String orderCode, String email) {
        Order order = orderRepository.findByOrderCodeAndUserEmail(orderCode, email)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderCode));

        List<OrderDetailResponse.OrderDetailItemResponse> itemResponses = order.getOrderItems().stream()
                .map(oi -> {
                    String imageUrl = oi.getProductVariant().getProduct().getImages().stream()
                            .filter(img -> ImageType.thumbnail.equals(img.getImageType()))
                            .findFirst()
                            .or(() -> oi.getProductVariant().getProduct().getImages().stream().findFirst())
                            .map(ProductImage::getImageUrl)
                            .orElse(null);
                    String productSlug = oi.getProductVariant().getProduct().getSlug();
                    return OrderDetailResponse.OrderDetailItemResponse.builder()
                            .id(oi.getId())
                            .productName(oi.getProductName())
                            .variantInfo(oi.getVariantInfo())
                            .quantity(oi.getQuantity())
                            .unitPrice(oi.getUnitPrice())
                            .subtotal(oi.getSubtotal())
                            .imageUrl(imageUrl)
                            .productSlug(productSlug)
                            .build();
                })
                .collect(Collectors.toList());

        PaymentMethod paymentMethod = order.getPayments().stream()
                .map(Payment::getMethod)
                .findFirst()
                .orElse(null);
        PaymentStatus paymentStatus = order.getPayments().stream()
                .map(Payment::getStatus)
                .findFirst()
                .orElse(null);

        return OrderDetailResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .note(order.getNote())
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .shippingName(order.getShippingName())
                .shippingPhone(order.getShippingPhone())
                .shippingProvince(order.getShippingProvince())
                .shippingDistrict(order.getShippingDistrict())
                .shippingWard(order.getShippingWard())
                .shippingAddress(order.getShippingAddress())
                .items(itemResponses)
                .build();
    }

    @Override
    @Transactional
    public void cancelOrder(String orderCode, String email) {
        Order order = orderRepository.findByOrderCodeAndUserEmail(orderCode, email)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderCode));

        // Use Strategy pattern to check cancellation rules and set status to cancelled
        customerOrderCancellationStrategy.cancel(order);

        // Notify observers to handle post-cancellation events (stock restoration, voucher restoration, email notifications)
        orderCancellationManager.notifyObservers(order);

        orderRepository.save(order);
    }
}
