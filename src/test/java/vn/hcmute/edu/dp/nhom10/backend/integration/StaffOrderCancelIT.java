package vn.hcmute.edu.dp.nhom10.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCancelOrderRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PlaceOrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffOrderDetailResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderStatusHistory;
import vn.hcmute.edu.dp.nhom10.backend.entity.Payment;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
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
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.PlaceOrderService;
import vn.hcmute.edu.dp.nhom10.backend.service.StaffOrderService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StaffOrderCancelIT extends AbstractPostgresIntegrationTest {

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
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void cancelPendingCodOrder_restoresInventoryVoucherRecordsHistoryAndEventWithoutPaymentOrLoyaltyChanges() {
        User staff = createStaff();
        PlaceOrderTestDataFactory.CheckoutFixture fixture =
                testDataFactory.createCheckoutFixture(5, 2, true);
        PlaceOrderResponseDTO placeOrderResponse = placeOrderService.confirmCheckout(
                fixture.request(PaymentMethod.cod),
                fixture.userId(),
                "203.0.113.10"
        );
        Order order = orderRepository.findByOrderCode(placeOrderResponse.order().getOrderCode()).orElseThrow();
        Payment codPayment = paymentRepository.findAllByOrderId(order.getId()).get(0);
        Voucher voucherBefore = voucherRepository.findByCode(fixture.voucherCode()).orElseThrow();
        User customerBefore = userRepository.findById(fixture.userId()).orElseThrow();
        int historyCountBefore = histories(order).size();
        orderStatusChangedEventProbe.clear();

        StaffOrderDetailResponse response = staffOrderService.cancelOrder(
                order.getOrderCode(),
                staff.getId(),
                cancelRequest()
        );

        Order updatedOrder = orderRepository.findByOrderCode(order.getOrderCode()).orElseThrow();
        Payment paymentAfter = paymentRepository.findById(codPayment.getId()).orElseThrow();
        ProductVariant variantAfter = productVariantRepository.findById(fixture.productVariantId()).orElseThrow();
        Voucher voucherAfter = voucherRepository.findById(voucherBefore.getId()).orElseThrow();
        User customerAfter = userRepository.findById(fixture.userId()).orElseThrow();
        List<OrderStatusHistory> histories = histories(updatedOrder);
        OrderStatusHistory cancelHistory = histories.get(histories.size() - 1);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.cancelled);
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.cancelled);
        assertThat(variantAfter.getStockQuantity()).isEqualTo(5);
        assertThat(voucherAfter.getTimesUsed()).isZero();
        assertThat(paymentAfter.getStatus()).isEqualTo(PaymentStatus.pending);
        assertThat(paymentAfter.getPaidAt()).isNull();
        assertThat(customerAfter.getLoyaltyPoints()).isEqualTo(customerBefore.getLoyaltyPoints());
        assertThat(histories).hasSize(historyCountBefore + 1);
        assertThat(cancelHistory.getFromStatus()).isEqualTo(OrderStatus.pending);
        assertThat(cancelHistory.getToStatus()).isEqualTo(OrderStatus.cancelled);
        assertThat(cancelHistory.getReason()).isEqualTo("Customer requested cancellation");
        assertThat(cancelHistory.getChangedBy().getId()).isEqualTo(staff.getId());
        assertThat(cancelHistory.getChangedByRole()).isEqualTo(UserRole.staff);
        assertThat(cancelHistory.getMetadata()).containsEntry("inventoryRestored", true);
        assertThat(cancelHistory.getMetadata()).containsEntry("voucherUsageRestored", true);
        assertThat(cancelHistory.getMetadata()).containsEntry("refundPerformed", false);
        assertThat(cancelHistory.getMetadata()).containsEntry("requiresManualRefundReview", false);
        assertThat(orderStatusChangedEventProbe.events()).hasSize(1);
        OrderStatusChangedEvent event = orderStatusChangedEventProbe.events().get(0);
        assertThat(event.orderCode()).isEqualTo(updatedOrder.getOrderCode());
        assertThat(event.fromStatus()).isEqualTo(OrderStatus.pending);
        assertThat(event.toStatus()).isEqualTo(OrderStatus.cancelled);
        assertThat(event.paymentMethod()).isEqualTo(PaymentMethod.cod);
        assertThat(event.paymentStatus()).isEqualTo(PaymentStatus.pending);
        assertThat(event.requiresManualRefundReview()).isFalse();
    }

    @Test
    void cancelProcessingOnlineCompletedOrder_requiresManualRefundReviewAndDoesNotMutatePayment() {
        User staff = createStaff();
        PlaceOrderTestDataFactory.CheckoutFixture fixture =
                testDataFactory.createCheckoutFixture(4, 1, false);
        PlaceOrderResponseDTO placeOrderResponse = placeOrderService.confirmCheckout(
                fixture.request(PaymentMethod.cod),
                fixture.userId(),
                "203.0.113.10"
        );
        Order order = orderRepository.findByOrderCode(placeOrderResponse.order().getOrderCode()).orElseThrow();
        Payment onlinePayment = paymentRepository.findAllByOrderId(order.getId()).get(0);
        OffsetDateTime paidAt = OffsetDateTime.parse("2026-01-10T08:00:00+07:00");
        onlinePayment.setMethod(PaymentMethod.vnpay);
        onlinePayment.setStatus(PaymentStatus.completed);
        onlinePayment.setPaidAt(paidAt);
        onlinePayment.setTransactionId("IT-ONLINE-CANCEL");
        paymentRepository.save(onlinePayment);
        staffOrderService.confirmOrder(order.getOrderCode(), staff.getId());
        orderStatusChangedEventProbe.clear();

        staffOrderService.cancelOrder(order.getOrderCode(), staff.getId(), cancelRequest());

        Order updatedOrder = orderRepository.findByOrderCode(order.getOrderCode()).orElseThrow();
        Payment paymentAfter = paymentRepository.findById(onlinePayment.getId()).orElseThrow();
        ProductVariant variantAfter = productVariantRepository.findById(fixture.productVariantId()).orElseThrow();
        OrderStatusHistory cancelHistory = histories(updatedOrder).get(histories(updatedOrder).size() - 1);
        OrderStatusChangedEvent event = orderStatusChangedEventProbe.events().get(0);

        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.cancelled);
        assertThat(variantAfter.getStockQuantity()).isEqualTo(4);
        assertThat(paymentAfter.getStatus()).isEqualTo(PaymentStatus.completed);
        assertThat(paymentAfter.getPaidAt()).isEqualTo(paidAt);
        assertThat(paymentAfter.getTransactionId()).isEqualTo("IT-ONLINE-CANCEL");
        assertThat(cancelHistory.getFromStatus()).isEqualTo(OrderStatus.processing);
        assertThat(cancelHistory.getMetadata()).containsEntry("requiresManualRefundReview", true);
        assertThat(event.paymentMethod()).isEqualTo(PaymentMethod.vnpay);
        assertThat(event.paymentStatus()).isEqualTo(PaymentStatus.completed);
        assertThat(event.requiresManualRefundReview()).isTrue();
    }

    @Test
    void cancelShippingOrder_conflictsWithoutRestoringInventoryVoucherHistoryOrEvent() {
        User staff = createStaff();
        PlaceOrderTestDataFactory.CheckoutFixture fixture =
                testDataFactory.createCheckoutFixture(5, 2, true);
        PlaceOrderResponseDTO placeOrderResponse = placeOrderService.confirmCheckout(
                fixture.request(PaymentMethod.cod),
                fixture.userId(),
                "203.0.113.10"
        );
        Order order = orderRepository.findByOrderCode(placeOrderResponse.order().getOrderCode()).orElseThrow();
        staffOrderService.confirmOrder(order.getOrderCode(), staff.getId());
        staffOrderService.shipOrder(order.getOrderCode(), staff.getId());
        Order shippingOrder = orderRepository.findByOrderCode(order.getOrderCode()).orElseThrow();
        int historyCountBefore = histories(shippingOrder).size();
        orderStatusChangedEventProbe.clear();

        assertThatThrownBy(() -> staffOrderService.cancelOrder(shippingOrder.getOrderCode(), staff.getId(), cancelRequest()))
                .isInstanceOf(OrderStateConflictException.class);

        Order updatedOrder = orderRepository.findByOrderCode(order.getOrderCode()).orElseThrow();
        ProductVariant variantAfter = productVariantRepository.findById(fixture.productVariantId()).orElseThrow();
        Voucher voucherAfter = voucherRepository.findByCode(fixture.voucherCode()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.shipping);
        assertThat(variantAfter.getStockQuantity()).isEqualTo(3);
        assertThat(voucherAfter.getTimesUsed()).isEqualTo(1);
        assertThat(histories(updatedOrder)).hasSize(historyCountBefore);
        assertThat(orderStatusChangedEventProbe.events()).isEmpty();
    }

    @Test
    void cancelHistoryFailure_rollsBackStatusInventoryVoucherAndEvent() {
        User staff = createStaff();
        PlaceOrderTestDataFactory.CheckoutFixture fixture =
                testDataFactory.createCheckoutFixture(5, 2, true);
        PlaceOrderResponseDTO placeOrderResponse = placeOrderService.confirmCheckout(
                fixture.request(PaymentMethod.cod),
                fixture.userId(),
                "203.0.113.10"
        );
        Order order = orderRepository.findByOrderCode(placeOrderResponse.order().getOrderCode()).orElseThrow();
        int historyCountBefore = histories(order).size();
        orderStatusChangedEventProbe.clear();
        makeHistoryTransitionFail();

        assertThatThrownBy(() -> staffOrderService.cancelOrder(order.getOrderCode(), staff.getId(), cancelRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot write history");

        Order updatedOrder = orderRepository.findByOrderCode(order.getOrderCode()).orElseThrow();
        ProductVariant variantAfter = productVariantRepository.findById(fixture.productVariantId()).orElseThrow();
        Voucher voucherAfter = voucherRepository.findByCode(fixture.voucherCode()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.pending);
        assertThat(variantAfter.getStockQuantity()).isEqualTo(3);
        assertThat(voucherAfter.getTimesUsed()).isEqualTo(1);
        assertThat(histories(updatedOrder)).hasSize(historyCountBefore);
        assertThat(orderStatusChangedEventProbe.events()).isEmpty();
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

    private StaffCancelOrderRequest cancelRequest() {
        return new StaffCancelOrderRequest("Customer requested cancellation");
    }

    private List<OrderStatusHistory> histories(Order order) {
        return orderStatusHistoryRepository.findAllByOrder_IdOrderByCreatedAtAscIdAsc(order.getId());
    }
}
