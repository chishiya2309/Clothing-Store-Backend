package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.OrderDetailResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.OrderItemResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.OrderSummaryResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.event.OrderCancelledEvent;
import vn.hcmute.edu.dp.nhom10.backend.exception.OrderCancellationException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.CancellationPolicy;
import vn.hcmute.edu.dp.nhom10.backend.pattern.strategy.CancellationPolicyFactory;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.OrderService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CancellationPolicyFactory cancellationPolicyFactory;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getOrderHistory(String email, OrderStatus status, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Page<Order> ordersPage;
        if (status != null) {
            ordersPage = orderRepository.findByUserIdAndStatus(user.getId(), status, pageable);
        } else {
            ordersPage = orderRepository.findByUserId(user.getId(), pageable);
        }

        return ordersPage.map(order -> OrderSummaryResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .createdAt(order.getCreatedAt())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // IDOR Check
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Bạn không có quyền truy cập đơn hàng này");
        }

        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .variantId(item.getProductVariant().getId())
                        .productName(item.getProductName())
                        .variantInfo(item.getVariantInfo())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        String addressFull = String.format("%s, %s, %s, %s",
                order.getShippingAddress(),
                order.getShippingWard(),
                order.getShippingDistrict(),
                order.getShippingProvince());

        return OrderDetailResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .createdAt(order.getCreatedAt())
                .status(order.getStatus().name())
                .note(order.getNote())
                .shippingName(order.getShippingName())
                .shippingPhone(order.getShippingPhone())
                .shippingAddressFull(addressFull)
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .items(itemResponses)
                .build();
    }

    @Override
    @Transactional
    public void cancelOrder(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // IDOR Check
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Bạn không có quyền hủy đơn hàng này");
        }

        CancellationPolicy policy = cancellationPolicyFactory.getPolicy(order.getStatus());
        if (!policy.canCancel(order)) {
            throw new OrderCancellationException("Đơn hàng không thể hủy ở trạng thái hiện tại: " + order.getStatus());
        }

        policy.cancel(order);
        orderRepository.save(order);

        log.info("Đơn hàng {} đã được hủy bởi user {}. Phát sự kiện OrderCancelledEvent.", order.getOrderCode(), email);
        eventPublisher.publishEvent(new OrderCancelledEvent(order));
    }
}
