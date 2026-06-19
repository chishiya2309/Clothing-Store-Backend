package vn.hcmute.edu.dp.nhom10.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.ReservedCheckoutResult;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.InventoryReservation;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.enums.CheckoutSessionStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReservationStatus;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.integration.support.PlaceOrderTestDataFactory;
import vn.hcmute.edu.dp.nhom10.backend.repository.CartItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.InventoryReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.CheckoutService;
import vn.hcmute.edu.dp.nhom10.backend.service.OrderService;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class CodOrderConcurrencyIT extends AbstractPostgresIntegrationTest {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PlaceOrderTestDataFactory testDataFactory;

    @Autowired
    private CheckoutSessionRepository checkoutSessionRepository;

    @Autowired
    private InventoryReservationRepository inventoryReservationRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    void createCodOrder_concurrentCallsForSameCheckout_createsOnlyOneOrder() throws Exception {
        PlaceOrderTestDataFactory.CheckoutFixture fixture =
                testDataFactory.createCheckoutFixture(3, 1, false);
        ReservedCheckoutResult checkout = checkoutService.prepareCheckout(
                fixture.request(PaymentMethod.cod),
                fixture.userId()
        );

        CyclicBarrier barrier = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<CodOrderResult> task = () -> {
                barrier.await(10, TimeUnit.SECONDS);
                try {
                    orderService.createCodOrder(checkout.checkoutCode(), fixture.userId());
                    return new CodOrderResult(true, null);
                } catch (RuntimeException e) {
                    return new CodOrderResult(false, e);
                }
            };

            List<Future<CodOrderResult>> futures = List.of(
                    executor.submit(task),
                    executor.submit(task)
            );
            List<CodOrderResult> results = futures.stream()
                    .map(this::getResult)
                    .toList();

            assertThat(results).filteredOn(CodOrderResult::success).hasSize(1);
            assertThat(results)
                    .filteredOn(result -> !result.success())
                    .singleElement()
                    .satisfies(result -> assertThat(result.failure()).isInstanceOf(InvalidDataException.class));

            CheckoutSession completedCheckout =
                    checkoutSessionRepository.findByCheckoutCode(checkout.checkoutCode()).orElseThrow();
            assertThat(completedCheckout.getStatus()).isEqualTo(CheckoutSessionStatus.completed);
            assertThat(orderRepository.findAll()).hasSize(1);
            assertThat(paymentRepository.findAll()).hasSize(1);
            assertThat(cartItemRepository.findAllByUserId(fixture.userId())).isEmpty();

            List<InventoryReservation> reservations =
                    inventoryReservationRepository.findAllByCheckoutSessionId(completedCheckout.getId());
            assertThat(reservations).hasSize(1);
            assertThat(reservations.get(0).getStatus()).isEqualTo(ReservationStatus.consumed);

            ProductVariant variant = productVariantRepository.findById(fixture.productVariantId()).orElseThrow();
            assertThat(variant.getStockQuantity()).isEqualTo(2);
        } finally {
            executor.shutdownNow();
        }
    }

    private CodOrderResult getResult(Future<CodOrderResult> future) {
        try {
            return future.get(15, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new AssertionError("Concurrent COD order task did not finish", e);
        }
    }

    private record CodOrderResult(
            boolean success,
            Throwable failure
    ) {
    }
}
