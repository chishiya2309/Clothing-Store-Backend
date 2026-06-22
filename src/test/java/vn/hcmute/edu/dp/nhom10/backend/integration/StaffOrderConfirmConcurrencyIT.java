package vn.hcmute.edu.dp.nhom10.backend.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PlaceOrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderStatusHistory;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;
import vn.hcmute.edu.dp.nhom10.backend.exception.OrderStateConflictException;
import vn.hcmute.edu.dp.nhom10.backend.integration.support.PlaceOrderTestDataFactory;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderStatusHistoryRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.PlaceOrderService;
import vn.hcmute.edu.dp.nhom10.backend.service.StaffOrderService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class StaffOrderConfirmConcurrencyIT extends AbstractPostgresIntegrationTest {

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
    @Timeout(30)
    void concurrentConfirm_allowsOneSuccessAndOneConflictWithoutDuplicateHistory() throws Exception {
        User staff = createStaff();
        Order order = createPendingCodOrder();
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Callable<TransitionResult> task = () -> {
                ready.countDown();
                start.await(10, TimeUnit.SECONDS);
                try {
                    staffOrderService.confirmOrder(order.getOrderCode(), staff.getId());
                    return TransitionResult.succeeded();
                } catch (Throwable throwable) {
                    return TransitionResult.failed(throwable);
                }
            };

            Future<TransitionResult> first = executorService.submit(task);
            Future<TransitionResult> second = executorService.submit(task);
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<TransitionResult> results = List.of(
                    first.get(20, TimeUnit.SECONDS),
                    second.get(20, TimeUnit.SECONDS)
            );

            long successCount = results.stream()
                    .filter(TransitionResult::success)
                    .count();
            long conflictCount = results.stream()
                    .filter(result -> result.throwable() instanceof OrderStateConflictException)
                    .count();
            assertThat(successCount).isEqualTo(1);
            assertThat(conflictCount).isEqualTo(1);

            Order updatedOrder = orderRepository.findByOrderCode(order.getOrderCode()).orElseThrow();
            List<OrderStatusHistory> histories =
                    orderStatusHistoryRepository.findAllByOrder_IdOrderByCreatedAtAscIdAsc(updatedOrder.getId());
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.processing);
            assertThat(histories).hasSize(2);
            assertThat(histories)
                    .filteredOn(history -> history.getFromStatus() == null
                            && history.getToStatus() == OrderStatus.pending)
                    .hasSize(1);
            assertThat(histories)
                    .filteredOn(history -> history.getFromStatus() == OrderStatus.pending
                            && history.getToStatus() == OrderStatus.processing)
                    .hasSize(1);
        } finally {
            executorService.shutdownNow();
        }
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

    private record TransitionResult(boolean success, Throwable throwable) {
        static TransitionResult succeeded() {
            return new TransitionResult(true, null);
        }

        static TransitionResult failed(Throwable throwable) {
            return new TransitionResult(false, throwable);
        }
    }
}
