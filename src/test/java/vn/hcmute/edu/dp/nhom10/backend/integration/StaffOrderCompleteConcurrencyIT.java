package vn.hcmute.edu.dp.nhom10.backend.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ConfirmCheckoutRequestDTO;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCompleteOrderRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PlaceOrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.entity.CartItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderStatusHistory;
import vn.hcmute.edu.dp.nhom10.backend.entity.Payment;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderCompletionSource;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;
import vn.hcmute.edu.dp.nhom10.backend.exception.OrderStateConflictException;
import vn.hcmute.edu.dp.nhom10.backend.integration.support.PlaceOrderTestDataFactory;
import vn.hcmute.edu.dp.nhom10.backend.repository.CartItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderStatusHistoryRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.PlaceOrderService;
import vn.hcmute.edu.dp.nhom10.backend.service.StaffOrderService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class StaffOrderCompleteConcurrencyIT extends AbstractPostgresIntegrationTest {

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

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    @Timeout(30)
    void concurrentCompleteSameOrder_allowsOneSuccessAndOneConflictWithoutDuplicateSideEffects() throws Exception {
        User firstStaff = createStaff();
        User secondStaff = createStaff();
        Order order = createShippingCodOrder(firstStaff);
        User customerBefore = userRepository.findById(order.getUser().getId()).orElseThrow();
        int expectedAwardedPoints = expectedPoints(order.getTotalAmount());
        orderStatusChangedEventProbe.clear();
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);
            Future<CompleteResult> first = executorService.submit(completeTask(order.getOrderCode(), firstStaff.getId(), ready, start));
            Future<CompleteResult> second = executorService.submit(completeTask(order.getOrderCode(), secondStaff.getId(), ready, start));
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<CompleteResult> results = List.of(
                    first.get(20, TimeUnit.SECONDS),
                    second.get(20, TimeUnit.SECONDS)
            );

            long successCount = results.stream()
                    .filter(CompleteResult::success)
                    .count();
            long conflictCount = results.stream()
                    .filter(result -> result.throwable() instanceof OrderStateConflictException)
                    .count();
            assertThat(successCount).isEqualTo(1);
            assertThat(conflictCount).isEqualTo(1);

            Order updatedOrder = orderRepository.findByOrderCode(order.getOrderCode()).orElseThrow();
            Payment codPayment = paymentRepository.findAllByOrderId(updatedOrder.getId()).stream()
                    .filter(payment -> payment.getMethod() == PaymentMethod.cod)
                    .findFirst()
                    .orElseThrow();
            User customerAfter = userRepository.findById(updatedOrder.getUser().getId()).orElseThrow();
            List<OrderStatusHistory> histories =
                    orderStatusHistoryRepository.findAllByOrder_IdOrderByCreatedAtAscIdAsc(updatedOrder.getId());
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.completed);
            assertThat(codPayment.getStatus()).isEqualTo(PaymentStatus.completed);
            assertThat(codPayment.getPaidAt()).isNotNull();
            assertThat(customerAfter.getLoyaltyPoints()).isEqualTo(customerBefore.getLoyaltyPoints() + expectedAwardedPoints);
            assertThat(histories)
                    .filteredOn(history -> history.getFromStatus() == OrderStatus.shipping
                            && history.getToStatus() == OrderStatus.completed)
                    .hasSize(1);
            assertThat(orderStatusChangedEventProbe.events()).hasSize(1);
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    @Timeout(30)
    void concurrentCompleteDifferentOrdersSameCustomer_doesNotLoseLoyaltyUpdates() throws Exception {
        User staff = createStaff();
        List<Order> orders = createTwoShippingCodOrdersForSameCustomer(staff);
        User customerBefore = userRepository.findById(orders.get(0).getUser().getId()).orElseThrow();
        int expectedAwardedPoints = orders.stream()
                .map(Order::getTotalAmount)
                .mapToInt(this::expectedPoints)
                .sum();
        orderStatusChangedEventProbe.clear();
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);
            Future<CompleteResult> first = executorService.submit(completeTask(orders.get(0).getOrderCode(), staff.getId(), ready, start));
            Future<CompleteResult> second = executorService.submit(completeTask(orders.get(1).getOrderCode(), staff.getId(), ready, start));
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<CompleteResult> results = List.of(
                    first.get(20, TimeUnit.SECONDS),
                    second.get(20, TimeUnit.SECONDS)
            );

            long successCount = results.stream()
                    .filter(CompleteResult::success)
                    .count();
            assertThat(successCount).isEqualTo(2);
            User customerAfter = userRepository.findById(customerBefore.getId()).orElseThrow();
            assertThat(customerAfter.getLoyaltyPoints()).isEqualTo(customerBefore.getLoyaltyPoints() + expectedAwardedPoints);
            for (Order order : orders) {
                Order updatedOrder = orderRepository.findByOrderCode(order.getOrderCode()).orElseThrow();
                assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.completed);
            }
            assertThat(orderStatusChangedEventProbe.events()).hasSize(2);
        } finally {
            executorService.shutdownNow();
        }
    }

    private Callable<CompleteResult> completeTask(
            String orderCode,
            Long staffUserId,
            CountDownLatch ready,
            CountDownLatch start
    ) {
        return () -> {
            ready.countDown();
            start.await(10, TimeUnit.SECONDS);
            try {
                staffOrderService.completeOrder(orderCode, staffUserId, completeRequest());
                return CompleteResult.succeeded();
            } catch (Throwable throwable) {
                return CompleteResult.failed(throwable);
            }
        };
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

    private List<Order> createTwoShippingCodOrdersForSameCustomer(User staff) {
        PlaceOrderTestDataFactory.CheckoutFixture firstFixture =
                testDataFactory.createCheckoutFixture(10, 1, false);
        PlaceOrderResponseDTO firstResponse = placeOrderService.confirmCheckout(
                firstFixture.request(PaymentMethod.cod),
                firstFixture.userId(),
                "203.0.113.10"
        );

        PlaceOrderTestDataFactory.ProductVariantFixture secondVariant =
                testDataFactory.createProductVariant(10);
        User sameCustomer = userRepository.findById(firstFixture.userId()).orElseThrow();
        ProductVariant productVariant = productVariantRepository.findById(secondVariant.productVariantId()).orElseThrow();
        cartItemRepository.save(CartItem.builder()
                .user(sameCustomer)
                .productVariant(productVariant)
                .quantity(1)
                .build());

        PlaceOrderResponseDTO secondResponse = placeOrderService.confirmCheckout(
                new ConfirmCheckoutRequestDTO(firstFixture.addressId(), null, PaymentMethod.cod),
                firstFixture.userId(),
                "203.0.113.10"
        );

        Order firstOrder = orderRepository.findByOrderCode(firstResponse.order().getOrderCode()).orElseThrow();
        Order secondOrder = orderRepository.findByOrderCode(secondResponse.order().getOrderCode()).orElseThrow();
        staffOrderService.confirmOrder(firstOrder.getOrderCode(), staff.getId());
        staffOrderService.shipOrder(firstOrder.getOrderCode(), staff.getId());
        staffOrderService.confirmOrder(secondOrder.getOrderCode(), staff.getId());
        staffOrderService.shipOrder(secondOrder.getOrderCode(), staff.getId());
        return List.of(
                orderRepository.findByOrderCode(firstOrder.getOrderCode()).orElseThrow(),
                orderRepository.findByOrderCode(secondOrder.getOrderCode()).orElseThrow()
        );
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

    private StaffCompleteOrderRequest completeRequest() {
        return new StaffCompleteOrderRequest(OrderCompletionSource.shipping_partner, "GHN confirmed");
    }

    private int expectedPoints(BigDecimal totalAmount) {
        return totalAmount.divide(BigDecimal.valueOf(1000), 0, RoundingMode.DOWN).intValue();
    }

    private record CompleteResult(boolean success, Throwable throwable) {
        static CompleteResult succeeded() {
            return new CompleteResult(true, null);
        }

        static CompleteResult failed(Throwable throwable) {
            return new CompleteResult(false, throwable);
        }
    }
}
