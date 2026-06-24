package vn.hcmute.edu.dp.nhom10.backend.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCompleteOrderRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PlaceOrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffOrderDetailResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderStatusHistory;
import vn.hcmute.edu.dp.nhom10.backend.entity.Payment;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderCompletionSource;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;
import vn.hcmute.edu.dp.nhom10.backend.event.OrderStatusChangedEvent;
import vn.hcmute.edu.dp.nhom10.backend.exception.OrderStateConflictException;
import vn.hcmute.edu.dp.nhom10.backend.integration.support.PlaceOrderTestDataFactory;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderStatusHistoryRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.PlaceOrderService;
import vn.hcmute.edu.dp.nhom10.backend.service.StaffOrderService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StaffOrderCompleteIT extends AbstractPostgresIntegrationTest {

    @Autowired
    private PlaceOrderService placeOrderService;

    @Autowired
    private StaffOrderService staffOrderService;

    @Autowired
    private PlaceOrderTestDataFactory testDataFactory;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void completeOrder_shippingCodOrder_updatesStatusPaymentLoyaltyHistoryAndEvent() {
        User staff = createStaff();
        Order order = createShippingCodOrder(staff);
        User customerBefore = userRepository.findById(order.getUser().getId()).orElseThrow();
        int previousPoints = customerBefore.getLoyaltyPoints();
        int expectedAwardedPoints = expectedPoints(order.getTotalAmount());
        orderStatusChangedEventProbe.clear();

        StaffOrderDetailResponse response = staffOrderService.completeOrder(
                order.getOrderCode(),
                staff.getId(),
                completeRequest()
        );

        Order updatedOrder = orderRepository.findByOrderCode(order.getOrderCode()).orElseThrow();
        Payment codPayment = paymentRepository.findAllByOrderId(updatedOrder.getId()).stream()
                .filter(payment -> payment.getMethod() == PaymentMethod.cod)
                .findFirst()
                .orElseThrow();
        User customerAfter = userRepository.findById(updatedOrder.getUser().getId()).orElseThrow();
        List<OrderStatusHistory> histories = histories(updatedOrder);
        OrderStatusHistory completeHistory = histories.get(histories.size() - 1);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.completed);
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.completed);
        assertThat(codPayment.getStatus()).isEqualTo(PaymentStatus.completed);
        assertThat(codPayment.getPaidAt()).isNotNull();
        assertThat(customerAfter.getLoyaltyPoints()).isEqualTo(previousPoints + expectedAwardedPoints);
        assertThat(customerAfter.getMembershipTier()).isNotNull();
        assertThat(completeHistory.getFromStatus()).isEqualTo(OrderStatus.shipping);
        assertThat(completeHistory.getToStatus()).isEqualTo(OrderStatus.completed);
        assertThat(completeHistory.getReason()).isEqualTo("GHN confirmed");
        assertThat(completeHistory.getChangedBy().getId()).isEqualTo(staff.getId());
        assertThat(completeHistory.getChangedByRole()).isEqualTo(UserRole.staff);
        assertThat(completeHistory.getMetadata()).containsEntry("confirmationSource", "shipping_partner");
        assertThat(completeHistory.getMetadata()).containsEntry("loyaltyPointsAwarded", expectedAwardedPoints);
        assertThat(response.getTimeline()).extracting("toStatus").contains(OrderStatus.completed);
        assertThat(orderStatusChangedEventProbe.events()).hasSize(1);
        OrderStatusChangedEvent event = orderStatusChangedEventProbe.events().get(0);
        assertThat(event.orderId()).isEqualTo(updatedOrder.getId());
        assertThat(event.orderCode()).isEqualTo(updatedOrder.getOrderCode());
        assertThat(event.changedByStaffId()).isEqualTo(staff.getId());
        assertThat(event.changedByStaffEmail()).isEqualTo(staff.getEmail());
        assertThat(event.fromStatus()).isEqualTo(OrderStatus.shipping);
        assertThat(event.toStatus()).isEqualTo(OrderStatus.completed);
        assertThat(event.reason()).isEqualTo("GHN confirmed");
    }

    @ParameterizedTest
    @EnumSource(value = PaymentMethod.class, names = {"vnpay", "momo"})
    void completeOrder_onlineCompletedPayment_doesNotMutatePayment(PaymentMethod paymentMethod) {
        User staff = createStaff();
        Order order = createOnlineShippingOrder(paymentMethod, PaymentStatus.completed);
        Payment onlinePaymentBefore = paymentRepository.findAllByOrderId(order.getId()).get(0);
        OffsetDateTime originalPaidAt = onlinePaymentBefore.getPaidAt();
        String originalTransactionId = onlinePaymentBefore.getTransactionId();
        orderStatusChangedEventProbe.clear();

        staffOrderService.completeOrder(order.getOrderCode(), staff.getId(), completeRequest());

        Payment onlinePaymentAfter = paymentRepository.findById(onlinePaymentBefore.getId()).orElseThrow();
        assertThat(onlinePaymentAfter.getMethod()).isEqualTo(paymentMethod);
        assertThat(onlinePaymentAfter.getStatus()).isEqualTo(PaymentStatus.completed);
        assertThat(onlinePaymentAfter.getPaidAt()).isEqualTo(originalPaidAt);
        assertThat(onlinePaymentAfter.getTransactionId()).isEqualTo(originalTransactionId);
    }

    @Test
    void completeOrder_wrongStatusDoesNotChangePaymentLoyaltyHistoryOrEvent() {
        User staff = createStaff();
        Order order = createPendingCodOrder();
        Payment codPayment = paymentRepository.findAllByOrderId(order.getId()).get(0);
        User customerBefore = userRepository.findById(order.getUser().getId()).orElseThrow();
        int historyCount = histories(order).size();
        orderStatusChangedEventProbe.clear();

        assertThatThrownBy(() -> staffOrderService.completeOrder(order.getOrderCode(), staff.getId(), completeRequest()))
                .isInstanceOf(OrderStateConflictException.class);

        Order updatedOrder = orderRepository.findByOrderCode(order.getOrderCode()).orElseThrow();
        Payment updatedPayment = paymentRepository.findById(codPayment.getId()).orElseThrow();
        User customerAfter = userRepository.findById(updatedOrder.getUser().getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.pending);
        assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.pending);
        assertThat(customerAfter.getLoyaltyPoints()).isEqualTo(customerBefore.getLoyaltyPoints());
        assertThat(histories(updatedOrder)).hasSize(historyCount);
        assertThat(orderStatusChangedEventProbe.events()).isEmpty();
    }

    @Test
    void completeOrder_secondCallConflictsWithoutAwardingTwice() {
        User staff = createStaff();
        Order order = createShippingCodOrder(staff);
        staffOrderService.completeOrder(order.getOrderCode(), staff.getId(), completeRequest());
        Order completedOrder = orderRepository.findByOrderCode(order.getOrderCode()).orElseThrow();
        User customerAfterFirstComplete = userRepository.findById(completedOrder.getUser().getId()).orElseThrow();
        int pointsAfterFirstComplete = customerAfterFirstComplete.getLoyaltyPoints();
        int historyCountAfterFirstComplete = histories(completedOrder).size();
        orderStatusChangedEventProbe.clear();

        assertThatThrownBy(() -> staffOrderService.completeOrder(order.getOrderCode(), staff.getId(), completeRequest()))
                .isInstanceOf(OrderStateConflictException.class);

        User customerAfterSecondComplete = userRepository.findById(completedOrder.getUser().getId()).orElseThrow();
        assertThat(customerAfterSecondComplete.getLoyaltyPoints()).isEqualTo(pointsAfterFirstComplete);
        assertThat(histories(completedOrder)).hasSize(historyCountAfterFirstComplete);
        assertThat(orderStatusChangedEventProbe.events()).isEmpty();
    }

    @Test
    void completeOrder_loyaltyFailureRollsBackStatusPaymentAndHistory() {
        User staff = createStaff();
        Order order = createShippingCodOrder(staff);
        Payment codPayment = paymentRepository.findAllByOrderId(order.getId()).get(0);
        User customerBefore = userRepository.findById(order.getUser().getId()).orElseThrow();
        int historyCount = histories(order).size();
        orderStatusChangedEventProbe.clear();
        makeLoyaltyAwardFail();

        assertThatThrownBy(() -> staffOrderService.completeOrder(order.getOrderCode(), staff.getId(), completeRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot award points");

        Order updatedOrder = orderRepository.findByOrderCode(order.getOrderCode()).orElseThrow();
        Payment updatedPayment = paymentRepository.findById(codPayment.getId()).orElseThrow();
        User customerAfter = userRepository.findById(updatedOrder.getUser().getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.shipping);
        assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.pending);
        assertThat(customerAfter.getLoyaltyPoints()).isEqualTo(customerBefore.getLoyaltyPoints());
        assertThat(histories(updatedOrder)).hasSize(historyCount);
        assertThat(orderStatusChangedEventProbe.events()).isEmpty();
    }

    @Test
    void completeOrder_historyFailureRollsBackStatusPaymentAndLoyalty() {
        User staff = createStaff();
        Order order = createShippingCodOrder(staff);
        Payment codPayment = paymentRepository.findAllByOrderId(order.getId()).get(0);
        User customerBefore = userRepository.findById(order.getUser().getId()).orElseThrow();
        int historyCount = histories(order).size();
        orderStatusChangedEventProbe.clear();
        makeHistoryTransitionFail();

        assertThatThrownBy(() -> staffOrderService.completeOrder(order.getOrderCode(), staff.getId(), completeRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot write history");

        Order updatedOrder = orderRepository.findByOrderCode(order.getOrderCode()).orElseThrow();
        Payment updatedPayment = paymentRepository.findById(codPayment.getId()).orElseThrow();
        User customerAfter = userRepository.findById(updatedOrder.getUser().getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.shipping);
        assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.pending);
        assertThat(customerAfter.getLoyaltyPoints()).isEqualTo(customerBefore.getLoyaltyPoints());
        assertThat(histories(updatedOrder)).hasSize(historyCount);
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
        return orderRepository.findByOrderCode(response.order().getOrderCode()).orElseThrow();
    }

    private Order createShippingCodOrder(User staff) {
        Order order = createPendingCodOrder();
        staffOrderService.confirmOrder(order.getOrderCode(), staff.getId());
        staffOrderService.shipOrder(order.getOrderCode(), staff.getId());
        return orderRepository.findByOrderCode(order.getOrderCode()).orElseThrow();
    }

    private Order createOnlineShippingOrder(PaymentMethod paymentMethod, PaymentStatus paymentStatus) {
        User customer = createCustomer();
        Order order = orderRepository.save(Order.builder()
                .user(customer)
                .orderCode("IT-ONLINE-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10))
                .shippingName("Integration Receiver")
                .shippingPhone("0900000001")
                .shippingProvince("Ho Chi Minh")
                .shippingDistrict("Thu Duc")
                .shippingWard("Linh Trung")
                .shippingAddress("1 Vo Van Ngan")
                .subtotal(new BigDecimal("120000.00"))
                .shippingFee(new BigDecimal("30000.00"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("150000.00"))
                .status(OrderStatus.shipping)
                .build());
        paymentRepository.save(Payment.builder()
                .order(order)
                .method(paymentMethod)
                .amount(order.getTotalAmount())
                .status(paymentStatus)
                .paidAt(paymentStatus == PaymentStatus.completed
                        ? OffsetDateTime.parse("2026-01-10T08:00:00+07:00")
                        : null)
                .transactionId("IT-" + paymentMethod.name())
                .build());
        return orderRepository.findByOrderCode(order.getOrderCode()).orElseThrow();
    }

    private User createCustomer() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return userRepository.save(User.builder()
                .email("customer-" + suffix + "@example.test")
                .passwordHash("password-hash")
                .fullName("Customer " + suffix)
                .phone("0900000000")
                .role(UserRole.customer)
                .loyaltyPoints(0)
                .authProvider("email")
                .emailVerified(true)
                .isActive(true)
                .build());
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

    private StaffCompleteOrderRequest completeRequest() {
        return new StaffCompleteOrderRequest(OrderCompletionSource.shipping_partner, "GHN confirmed");
    }

    private int expectedPoints(BigDecimal totalAmount) {
        return totalAmount.divide(BigDecimal.valueOf(1000), 0, RoundingMode.DOWN).intValue();
    }
}
