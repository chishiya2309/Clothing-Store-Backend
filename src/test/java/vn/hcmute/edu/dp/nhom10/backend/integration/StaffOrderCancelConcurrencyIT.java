package vn.hcmute.edu.dp.nhom10.backend.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffCancelOrderRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PlaceOrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderStatusHistory;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.UserRole;
import vn.hcmute.edu.dp.nhom10.backend.exception.OrderStateConflictException;
import vn.hcmute.edu.dp.nhom10.backend.integration.support.PlaceOrderTestDataFactory;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderStatusHistoryRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherRepository;
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

class StaffOrderCancelConcurrencyIT extends AbstractPostgresIntegrationTest {

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
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Timeout(30)
    void concurrentCancelSameOrder_allowsOneSuccessAndOneConflictWithoutDuplicateRestores() throws Exception {
        User firstStaff = createStaff();
        User secondStaff = createStaff();
        PlaceOrderTestDataFactory.CheckoutFixture fixture =
                testDataFactory.createCheckoutFixture(5, 2, true);
        PlaceOrderResponseDTO placeOrderResponse = placeOrderService.confirmCheckout(
                fixture.request(PaymentMethod.cod),
                fixture.userId(),
                "203.0.113.10"
        );
        Order order = orderRepository.findByOrderCode(placeOrderResponse.order().getOrderCode()).orElseThrow();
        Voucher voucher = voucherRepository.findByCode(fixture.voucherCode()).orElseThrow();
        orderStatusChangedEventProbe.clear();
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);
            Future<CancelResult> first = executorService.submit(cancelTask(order.getOrderCode(), firstStaff.getId(), ready, start));
            Future<CancelResult> second = executorService.submit(cancelTask(order.getOrderCode(), secondStaff.getId(), ready, start));
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<CancelResult> results = List.of(
                    first.get(20, TimeUnit.SECONDS),
                    second.get(20, TimeUnit.SECONDS)
            );

            long successCount = results.stream()
                    .filter(CancelResult::success)
                    .count();
            long conflictCount = results.stream()
                    .filter(result -> result.throwable() instanceof OrderStateConflictException)
                    .count();
            assertThat(successCount).isEqualTo(1);
            assertThat(conflictCount).isEqualTo(1);

            Order updatedOrder = orderRepository.findByOrderCode(order.getOrderCode()).orElseThrow();
            ProductVariant variantAfter = productVariantRepository.findById(fixture.productVariantId()).orElseThrow();
            Voucher voucherAfter = voucherRepository.findById(voucher.getId()).orElseThrow();
            List<OrderStatusHistory> histories =
                    orderStatusHistoryRepository.findAllByOrder_IdOrderByCreatedAtAscIdAsc(updatedOrder.getId());
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.cancelled);
            assertThat(variantAfter.getStockQuantity()).isEqualTo(5);
            assertThat(voucherAfter.getTimesUsed()).isZero();
            assertThat(histories)
                    .filteredOn(history -> history.getFromStatus() == OrderStatus.pending
                            && history.getToStatus() == OrderStatus.cancelled)
                    .hasSize(1);
            assertThat(orderStatusChangedEventProbe.events()).hasSize(1);
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    @Timeout(30)
    void concurrentCancelDifferentOrdersSameVariant_restoresAllStockWithoutLostUpdates() throws Exception {
        User staff = createStaff();
        PlaceOrderTestDataFactory.SharedVariantFixture fixture =
                testDataFactory.createSharedVariantFixture(10, 2, 2);
        PlaceOrderResponseDTO firstResponse = placeOrderService.confirmCheckout(
                fixture.actors().get(0).request(PaymentMethod.cod),
                fixture.actors().get(0).userId(),
                "203.0.113.10"
        );
        PlaceOrderResponseDTO secondResponse = placeOrderService.confirmCheckout(
                fixture.actors().get(1).request(PaymentMethod.cod),
                fixture.actors().get(1).userId(),
                "203.0.113.11"
        );
        Order firstOrder = orderRepository.findByOrderCode(firstResponse.order().getOrderCode()).orElseThrow();
        Order secondOrder = orderRepository.findByOrderCode(secondResponse.order().getOrderCode()).orElseThrow();
        ProductVariant variantAfterCheckout = productVariantRepository.findById(fixture.productVariantId()).orElseThrow();
        assertThat(variantAfterCheckout.getStockQuantity()).isEqualTo(6);
        orderStatusChangedEventProbe.clear();
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);
            Future<CancelResult> first = executorService.submit(cancelTask(firstOrder.getOrderCode(), staff.getId(), ready, start));
            Future<CancelResult> second = executorService.submit(cancelTask(secondOrder.getOrderCode(), staff.getId(), ready, start));
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<CancelResult> results = List.of(
                    first.get(20, TimeUnit.SECONDS),
                    second.get(20, TimeUnit.SECONDS)
            );

            assertThat(results).allMatch(CancelResult::success);
            ProductVariant variantAfterCancel = productVariantRepository.findById(fixture.productVariantId()).orElseThrow();
            Order updatedFirstOrder = orderRepository.findByOrderCode(firstOrder.getOrderCode()).orElseThrow();
            Order updatedSecondOrder = orderRepository.findByOrderCode(secondOrder.getOrderCode()).orElseThrow();
            assertThat(variantAfterCancel.getStockQuantity()).isEqualTo(10);
            assertThat(updatedFirstOrder.getStatus()).isEqualTo(OrderStatus.cancelled);
            assertThat(updatedSecondOrder.getStatus()).isEqualTo(OrderStatus.cancelled);
            assertThat(orderStatusChangedEventProbe.events()).hasSize(2);
        } finally {
            executorService.shutdownNow();
        }
    }

    private Callable<CancelResult> cancelTask(
            String orderCode,
            Long staffUserId,
            CountDownLatch ready,
            CountDownLatch start
    ) {
        return () -> {
            ready.countDown();
            start.await(10, TimeUnit.SECONDS);
            try {
                staffOrderService.cancelOrder(orderCode, staffUserId, cancelRequest());
                return CancelResult.succeeded();
            } catch (Throwable throwable) {
                return CancelResult.failed(throwable);
            }
        };
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

    private record CancelResult(boolean success, Throwable throwable) {
        static CancelResult succeeded() {
            return new CancelResult(true, null);
        }

        static CancelResult failed(Throwable throwable) {
            return new CancelResult(false, throwable);
        }
    }
}
