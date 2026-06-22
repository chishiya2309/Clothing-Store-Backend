package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCompleteOrderRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffOrderDetailResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffOrderItemResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffOrderListItemResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffOrderStatusTimelineResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffPaymentSummaryResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderStatusHistory;
import vn.hcmute.edu.dp.nhom10.backend.entity.Payment;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderCompletionSource;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentStatus;
import vn.hcmute.edu.dp.nhom10.backend.event.OrderStatusChangedEvent;
import vn.hcmute.edu.dp.nhom10.backend.exception.OrderStateConflictException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.specification.OrderSpecification;
import vn.hcmute.edu.dp.nhom10.backend.policy.OrderStatusTransitionPolicy;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderStatusHistoryRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.LoyaltyPointAwardResult;
import vn.hcmute.edu.dp.nhom10.backend.service.LoyaltyPointService;
import vn.hcmute.edu.dp.nhom10.backend.service.OrderStatusHistoryService;
import vn.hcmute.edu.dp.nhom10.backend.service.StaffOrderService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffOrderServiceImpl implements StaffOrderService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String DEFAULT_SORT_BY = "createdAt";
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt",
            "updatedAt",
            "totalAmount",
            "status",
            "orderCode"
    );

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final UserRepository userRepository;
    private final OrderStatusHistoryService orderStatusHistoryService;
    private final OrderStatusTransitionPolicy orderStatusTransitionPolicy;
    private final LoyaltyPointService loyaltyPointService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StaffOrderListItemResponse> getOrders(
            OrderStatus status,
            LocalDate fromDate,
            LocalDate toDate,
            String keyword,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        validatePage(page, size);
        validateDateRange(fromDate, toDate);

        OffsetDateTime fromDateTime = toStartOfDay(fromDate);
        OffsetDateTime toDateTimeExclusive = toStartOfNextDay(toDate);
        Specification<Order> spec = Specification.where(OrderSpecification.hasStatus(status))
                .and(OrderSpecification.createdAtGreaterThanOrEqualTo(fromDateTime))
                .and(OrderSpecification.createdAtLessThan(toDateTimeExclusive))
                .and(OrderSpecification.hasKeyword(normalizeKeyword(keyword)));

        PageRequest pageRequest = PageRequest.of(page, size, resolveSort(sortBy, sortDir));
        Page<Order> orderPage = orderRepository.findAll(spec, pageRequest);

        Map<Long, Payment> representativePayments = representativePayments(orderPage.getContent());
        List<StaffOrderListItemResponse> content = orderPage.getContent().stream()
                .map(order -> toListItemResponse(order, representativePayments.get(order.getId())))
                .toList();

        return PageResponse.<StaffOrderListItemResponse>builder()
                .pageNumber(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .content(content)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StaffOrderDetailResponse getOrderDetail(String orderCode) {
        String normalizedOrderCode = normalizeOrderCode(orderCode);
        Order order = orderRepository.findByOrderCode(normalizedOrderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with code: " + normalizedOrderCode));

        List<OrderItem> orderItems = orderItemRepository.findAllByOrderIdWithVariantOrderById(order.getId());
        List<Payment> payments = paymentRepository.findAllByOrderId(order.getId()).stream()
                .sorted(paymentCreatedAtDesc())
                .toList();
        List<OrderStatusHistory> histories = orderStatusHistoryRepository
                .findAllByOrder_IdOrderByCreatedAtAscIdAsc(order.getId());

        return toDetailResponse(
                order,
                orderItems,
                chooseRepresentativePayment(payments),
                histories
        );
    }

    @Override
    @Transactional
    public StaffOrderDetailResponse confirmOrder(String orderCode, Long staffUserId) {
        return transitionOrder(orderCode, staffUserId, OrderStatus.processing);
    }

    @Override
    @Transactional
    public StaffOrderDetailResponse shipOrder(String orderCode, Long staffUserId) {
        return transitionOrder(orderCode, staffUserId, OrderStatus.shipping);
    }

    @Override
    @Transactional
    public StaffOrderDetailResponse completeOrder(String orderCode, Long staffUserId, StaffCompleteOrderRequest request) {
        validateCompleteRequest(request);

        String normalizedOrderCode = normalizeOrderCode(orderCode);
        User staffUser = findStaffActor(staffUserId);
        Order order = orderRepository.findByOrderCodeForUpdate(normalizedOrderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with code: " + normalizedOrderCode));
        OrderStatus fromStatus = order.getStatus();
        OffsetDateTime completedAt = OffsetDateTime.now(clock);

        orderStatusTransitionPolicy.validate(fromStatus, OrderStatus.completed);
        CodPaymentCompletionResult paymentResult = completeCodPaymentIfPresent(order, completedAt);
        LoyaltyPointAwardResult loyaltyResult = loyaltyPointService.awardForCompletedOrder(order);

        order.setStatus(OrderStatus.completed);

        Map<String, Object> metadata = completionMetadata(request.confirmationSource(), paymentResult, loyaltyResult);
        orderStatusHistoryService.recordTransition(
                order,
                fromStatus,
                OrderStatus.completed,
                staffUser,
                request.note(),
                metadata
        );
        publishStatusChangedEvent(order, staffUser, fromStatus, OrderStatus.completed, request.note(), completedAt);

        return toDetailResponse(order);
    }

    private StaffOrderDetailResponse transitionOrder(String orderCode, Long staffUserId, OrderStatus targetStatus) {
        String normalizedOrderCode = normalizeOrderCode(orderCode);
        User staffUser = findStaffActor(staffUserId);
        Order order = orderRepository.findByOrderCodeForUpdate(normalizedOrderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with code: " + normalizedOrderCode));
        OrderStatus fromStatus = order.getStatus();

        orderStatusTransitionPolicy.validate(fromStatus, targetStatus);
        order.setStatus(targetStatus);

        orderStatusHistoryService.recordTransition(
                order,
                fromStatus,
                targetStatus,
                staffUser,
                null,
                null
        );
        publishStatusChangedEvent(order, staffUser, fromStatus, targetStatus, null, OffsetDateTime.now());

        return toDetailResponse(order);
    }

    private void validateCompleteRequest(StaffCompleteOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Complete order request is required");
        }
        if (request.confirmationSource() == null) {
            throw new IllegalArgumentException("Confirmation source is required");
        }
        if (request.note() == null || request.note().trim().isEmpty()) {
            throw new IllegalArgumentException("Completion note is required");
        }
        if (request.note().length() > 500) {
            throw new IllegalArgumentException("Completion note must not exceed 500 characters");
        }
    }

    private CodPaymentCompletionResult completeCodPaymentIfPresent(Order order, OffsetDateTime completedAt) {
        List<Payment> payments = paymentRepository.findAllByOrderIdForUpdate(order.getId());
        if (payments.isEmpty()) {
            throw new OrderStateConflictException("Order has no payment record");
        }
        List<Payment> codPayments = payments.stream()
                .filter(payment -> payment.getMethod() == PaymentMethod.cod)
                .sorted(paymentCreatedAtDesc())
                .toList();
        if (codPayments.isEmpty()) {
            return CodPaymentCompletionResult.notCod();
        }

        Payment pendingCodPayment = codPayments.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.pending)
                .findFirst()
                .orElse(null);
        if (pendingCodPayment != null) {
            pendingCodPayment.setStatus(PaymentStatus.completed);
            pendingCodPayment.setPaidAt(completedAt);
            return CodPaymentCompletionResult.completed(pendingCodPayment);
        }

        Payment completedCodPayment = codPayments.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.completed)
                .findFirst()
                .orElse(null);
        if (completedCodPayment != null) {
            return CodPaymentCompletionResult.alreadyCompleted(completedCodPayment);
        }

        Payment invalidCodPayment = codPayments.get(0);
        throw new OrderStateConflictException(
                "COD payment is " + invalidCodPayment.getStatus() + " and cannot be completed"
        );
    }

    private Map<String, Object> completionMetadata(
            OrderCompletionSource confirmationSource,
            CodPaymentCompletionResult paymentResult,
            LoyaltyPointAwardResult loyaltyResult
    ) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("confirmationSource", confirmationSource.name());
        metadata.put("codPaymentCompleted", paymentResult.completedNow());
        metadata.put("codPaymentAlreadyCompleted", paymentResult.alreadyCompleted());
        metadata.put("loyaltyPointsAwarded", loyaltyResult.awardedPoints());
        metadata.put("previousMembershipTier", loyaltyResult.previousMembershipTier());
        metadata.put("resultingMembershipTier", loyaltyResult.resultingMembershipTier());
        metadata.put("membershipTierChanged", loyaltyResult.membershipTierChanged());
        return metadata;
    }

    private User findStaffActor(Long staffUserId) {
        if (staffUserId == null) {
            throw new IllegalArgumentException("Staff user ID is required");
        }
        return userRepository.findById(staffUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff user not found with ID: " + staffUserId));
    }

    private void publishStatusChangedEvent(
            Order order,
            User staffUser,
            OrderStatus fromStatus,
            OrderStatus toStatus,
            String reason,
            OffsetDateTime changedAt
    ) {
        User customer = order.getUser();
        eventPublisher.publishEvent(new OrderStatusChangedEvent(
                order.getId(),
                order.getOrderCode(),
                customer == null ? null : customer.getId(),
                customer == null ? null : customer.getEmail(),
                staffUser.getId(),
                staffUser.getEmail(),
                fromStatus,
                toStatus,
                reason,
                changedAt
        ));
    }

    private Map<Long, Payment> representativePayments(List<Order> orders) {
        List<Long> orderIds = orders.stream()
                .map(Order::getId)
                .toList();
        if (orderIds.isEmpty()) {
            return Map.of();
        }

        List<Payment> payments = paymentRepository.findAllByOrder_IdInOrderByCreatedAtDesc(orderIds);
        Map<Long, List<Payment>> paymentsByOrderId = payments.stream()
                .filter(payment -> payment.getOrder() != null && payment.getOrder().getId() != null)
                .collect(Collectors.groupingBy(
                        payment -> payment.getOrder().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        Map<Long, Payment> representativePayments = new LinkedHashMap<>();
        for (Long orderId : orderIds) {
            representativePayments.put(
                    orderId,
                    chooseRepresentativePayment(paymentsByOrderId.getOrDefault(orderId, List.of()))
            );
        }
        return representativePayments;
    }

    private Payment chooseRepresentativePayment(Collection<Payment> payments) {
        if (payments == null || payments.isEmpty()) {
            return null;
        }

        List<Payment> sortedPayments = payments.stream()
                .sorted(paymentCreatedAtDesc())
                .toList();
        return sortedPayments.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.completed)
                .findFirst()
                .orElse(sortedPayments.get(0));
    }

    private Comparator<Payment> paymentCreatedAtDesc() {
        return Comparator.comparing(
                Payment::getCreatedAt,
                Comparator.nullsFirst(Comparator.naturalOrder())
        ).reversed();
    }

    private StaffOrderListItemResponse toListItemResponse(Order order, Payment payment) {
        User customer = order.getUser();
        return StaffOrderListItemResponse.builder()
                .orderCode(order.getOrderCode())
                .customerName(customer == null ? null : customer.getFullName())
                .customerEmail(customer == null ? null : customer.getEmail())
                .customerPhone(customer == null ? order.getShippingPhone() : customer.getPhone())
                .createdAt(order.getCreatedAt())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentMethod(payment == null ? null : payment.getMethod())
                .paymentStatus(payment == null ? null : payment.getStatus())
                .build();
    }

    private StaffOrderDetailResponse toDetailResponse(
            Order order,
            List<OrderItem> orderItems,
            Payment representativePayment,
            List<OrderStatusHistory> histories
    ) {
        User customer = order.getUser();
        Voucher voucher = order.getVoucher();

        return StaffOrderDetailResponse.builder()
                .orderCode(order.getOrderCode())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .customerId(customer == null ? null : customer.getId())
                .customerName(customer == null ? null : customer.getFullName())
                .customerEmail(customer == null ? null : customer.getEmail())
                .customerPhone(customer == null ? null : customer.getPhone())
                .shippingName(order.getShippingName())
                .shippingPhone(order.getShippingPhone())
                .shippingProvince(order.getShippingProvince())
                .shippingDistrict(order.getShippingDistrict())
                .shippingWard(order.getShippingWard())
                .shippingAddress(order.getShippingAddress())
                .voucherId(voucher == null ? null : voucher.getId())
                .voucherCode(voucher == null ? null : voucher.getCode())
                .items(orderItems.stream().map(this::toOrderItemResponse).toList())
                .payment(toPaymentSummaryResponse(representativePayment))
                .timeline(histories.stream().map(this::toTimelineResponse).toList())
                .build();
    }

    private StaffOrderDetailResponse toDetailResponse(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderIdWithVariantOrderById(order.getId());
        List<Payment> payments = paymentRepository.findAllByOrderId(order.getId()).stream()
                .sorted(paymentCreatedAtDesc())
                .toList();
        List<OrderStatusHistory> histories = orderStatusHistoryRepository
                .findAllByOrder_IdOrderByCreatedAtAscIdAsc(order.getId());

        return toDetailResponse(
                order,
                orderItems,
                chooseRepresentativePayment(payments),
                histories
        );
    }

    private StaffOrderItemResponse toOrderItemResponse(OrderItem item) {
        ProductVariant variant = item.getProductVariant();
        return StaffOrderItemResponse.builder()
                .productVariantId(variant == null ? null : variant.getId())
                .sku(variant == null ? null : variant.getSku())
                .productName(item.getProductName())
                .variantInfo(item.getVariantInfo())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .build();
    }

    private StaffPaymentSummaryResponse toPaymentSummaryResponse(Payment payment) {
        if (payment == null) {
            return null;
        }
        return StaffPaymentSummaryResponse.builder()
                .id(payment.getId())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .transactionId(payment.getTransactionId())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private StaffOrderStatusTimelineResponse toTimelineResponse(OrderStatusHistory history) {
        User changedBy = history.getChangedBy();
        return StaffOrderStatusTimelineResponse.builder()
                .id(history.getId())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .changedById(changedBy == null ? null : changedBy.getId())
                .changedByName(changedBy == null ? null : changedBy.getFullName())
                .changedByEmail(changedBy == null ? null : changedBy.getEmail())
                .changedByRole(history.getChangedByRole())
                .actorLabel(actorLabel(history, changedBy))
                .reason(history.getReason())
                .metadata(history.getMetadata())
                .createdAt(history.getCreatedAt())
                .build();
    }

    private String actorLabel(OrderStatusHistory history, User changedBy) {
        if (changedBy == null && history.getChangedByRole() == null) {
            return "SYSTEM";
        }
        if (changedBy != null && changedBy.getFullName() != null && !changedBy.getFullName().isBlank()) {
            return changedBy.getFullName();
        }
        return changedBy == null ? null : changedBy.getEmail();
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be greater than or equal to 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be greater than 0");
        }
        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Size must not exceed " + MAX_PAGE_SIZE);
        }
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate must not be after toDate");
        }
    }

    private Sort resolveSort(String sortBy, String sortDir) {
        String normalizedSortBy = normalizeSortBy(sortBy);
        Sort.Direction direction = resolveDirection(sortDir);
        return Sort.by(direction, normalizedSortBy);
    }

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return DEFAULT_SORT_BY;
        }
        String normalized = sortBy.trim();
        if (!ALLOWED_SORT_FIELDS.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported sort field: " + normalized);
        }
        return normalized;
    }

    private Sort.Direction resolveDirection(String sortDir) {
        if (sortDir == null || sortDir.isBlank()) {
            return Sort.Direction.DESC;
        }
        String normalized = sortDir.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "asc" -> Sort.Direction.ASC;
            case "desc" -> Sort.Direction.DESC;
            default -> throw new IllegalArgumentException("Unsupported sort direction: " + sortDir);
        };
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return keyword.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeOrderCode(String orderCode) {
        if (orderCode == null || orderCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Order code is required");
        }
        return orderCode.trim();
    }

    private OffsetDateTime toStartOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
    }

    private OffsetDateTime toStartOfNextDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
    }

    private record CodPaymentCompletionResult(
            boolean codOrder,
            boolean completedNow,
            boolean alreadyCompleted,
            Long paymentId
    ) {
        static CodPaymentCompletionResult notCod() {
            return new CodPaymentCompletionResult(false, false, false, null);
        }

        static CodPaymentCompletionResult completed(Payment payment) {
            return new CodPaymentCompletionResult(true, true, false, payment.getId());
        }

        static CodPaymentCompletionResult alreadyCompleted(Payment payment) {
            return new CodPaymentCompletionResult(true, false, true, payment.getId());
        }
    }
}
