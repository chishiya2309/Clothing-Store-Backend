package vn.hcmute.edu.dp.nhom10.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PlaceOrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffOrderDetailResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderStatusHistory;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;
import vn.hcmute.edu.dp.nhom10.backend.event.OrderStatusChangedEvent;
import vn.hcmute.edu.dp.nhom10.backend.exception.OrderStateConflictException;
import vn.hcmute.edu.dp.nhom10.backend.integration.support.PlaceOrderTestDataFactory;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderStatusHistoryRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.PlaceOrderService;
import vn.hcmute.edu.dp.nhom10.backend.service.StaffOrderService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StaffOrderTransitionIT extends AbstractPostgresIntegrationTest {

    @Autowired
    private PlaceOrderService placeOrderService;

    @Autowired
    private StaffOrderService staffOrderService;

    @Autowired
    private PlaceOrderTestDataFactory testDataFactory;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void confirmOrder_pendingOrder_movesToProcessingAndRecordsStaffHistoryAndEvent() {
        User staff = createStaff();
        Order order = createPendingCodOrder();

        StaffOrderDetailResponse response = staffOrderService.confirmOrder(order.getOrderCode(), staff.getId());

        Order updatedOrder = orderRepository.findByOrderCode(order.getOrderCode()).orElseThrow();
        List<OrderStatusHistory> histories = histories(updatedOrder);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.processing);
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.processing);
        assertThat(histories).hasSize(2);
        assertThat(histories.get(0).getFromStatus()).isNull();
        assertThat(histories.get(0).getToStatus()).isEqualTo(OrderStatus.pending);
        assertThat(histories.get(1).getFromStatus()).isEqualTo(OrderStatus.pending);
        assertThat(histories.get(1).getToStatus()).isEqualTo(OrderStatus.processing);
        assertThat(histories.get(1).getChangedBy().getId()).isEqualTo(staff.getId());
        assertThat(histories.get(1).getChangedByRole()).isEqualTo(UserRole.staff);
        assertThat(response.getTimeline()).hasSize(2);
        assertThat(orderStatusChangedEventProbe.events()).hasSize(1);
        OrderStatusChangedEvent event = orderStatusChangedEventProbe.events().get(0);
        assertThat(event.orderId()).isEqualTo(updatedOrder.getId());
        assertThat(event.orderCode()).isEqualTo(updatedOrder.getOrderCode());
        assertThat(event.changedByStaffId()).isEqualTo(staff.getId());
        assertThat(event.changedByStaffEmail()).isEqualTo(staff.getEmail());
        assertThat(event.fromStatus()).isEqualTo(OrderStatus.pending);
        assertThat(event.toStatus()).isEqualTo(OrderStatus.processing);
    }

    @Test
    void shipOrder_processingOrder_movesToShippingAndTimelineContainsAllTransitions() {
        User staff = createStaff();
        Order order = createPendingCodOrder();
        staffOrderService.confirmOrder(order.getOrderCode(), staff.getId());
        orderStatusChangedEventProbe.clear();

        StaffOrderDetailResponse response = staffOrderService.shipOrder(order.getOrderCode(), staff.getId());

        Order updatedOrder = orderRepository.findByOrderCode(order.getOrderCode()).orElseThrow();
        List<OrderStatusHistory> histories = histories(updatedOrder);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.shipping);
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.shipping);
        assertThat(histories).hasSize(3);
        assertThat(histories).extracting(OrderStatusHistory::getFromStatus)
                .containsExactly(null, OrderStatus.pending, OrderStatus.processing);
        assertThat(histories).extracting(OrderStatusHistory::getToStatus)
                .containsExactly(OrderStatus.pending, OrderStatus.processing, OrderStatus.shipping);
        assertThat(response.getTimeline()).hasSize(3);
        assertThat(orderStatusChangedEventProbe.events()).hasSize(1);
        assertThat(orderStatusChangedEventProbe.events().get(0).fromStatus()).isEqualTo(OrderStatus.processing);
        assertThat(orderStatusChangedEventProbe.events().get(0).toStatus()).isEqualTo(OrderStatus.shipping);
    }

    @Test
    void confirmOrder_conflictDoesNotChangeStatusAddHistoryOrPublishEvent() {
        User staff = createStaff();
        Order order = createPendingCodOrder();
        staffOrderService.confirmOrder(order.getOrderCode(), staff.getId());
        orderStatusChangedEventProbe.clear();

        assertThatThrownBy(() -> staffOrderService.confirmOrder(order.getOrderCode(), staff.getId()))
                .isInstanceOf(OrderStateConflictException.class);

        Order updatedOrder = orderRepository.findByOrderCode(order.getOrderCode()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.processing);
        assertThat(histories(updatedOrder)).hasSize(2);
        assertThat(orderStatusChangedEventProbe.events()).isEmpty();
    }

    @Test
    void shipOrder_conflictDoesNotChangeStatusAddHistoryOrPublishEvent() {
        User staff = createStaff();
        Order order = createPendingCodOrder();
        orderStatusChangedEventProbe.clear();

        assertThatThrownBy(() -> staffOrderService.shipOrder(order.getOrderCode(), staff.getId()))
                .isInstanceOf(OrderStateConflictException.class);

        Order updatedOrder = orderRepository.findByOrderCode(order.getOrderCode()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.pending);
        assertThat(histories(updatedOrder)).hasSize(1);
        assertThat(orderStatusChangedEventProbe.events()).isEmpty();
    }

    private Order createPendingCodOrder() {
        PlaceOrderTestDataFactory.CheckoutFixture fixture =
                testDataFactory.createCheckoutFixture(5, 1, false);
        PlaceOrderResponseDTO response = placeOrderService.confirmCheckout(
                fixture.request(PaymentMethod.cod),
                fixture.userId(),
                "203.0.113.10"
        );
        orderStatusChangedEventProbe.clear();
        return orderRepository.findByOrderCode(response.order().getOrderCode()).orElseThrow();
    }

    private User createStaff() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return userRepository.save(User.builder()
                .email("staff-" + suffix + "@example.test")
                .passwordHash("password-hash")
                .fullName("Staff " + suffix)
                .phone("0911111111")
                .role(UserRole.staff)
                .loyaltyPoints(0)
                .authProvider("email")
                .emailVerified(true)
                .isActive(true)
                .build());
    }

    private List<OrderStatusHistory> histories(Order order) {
        return orderStatusHistoryRepository.findAllByOrder_IdOrderByCreatedAtAscIdAsc(order.getId());
    }
}
