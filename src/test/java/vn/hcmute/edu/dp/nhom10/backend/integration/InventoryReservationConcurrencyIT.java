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
import vn.hcmute.edu.dp.nhom10.backend.exception.InsufficientStockException;
import vn.hcmute.edu.dp.nhom10.backend.integration.support.PlaceOrderTestDataFactory;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.InventoryReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.CheckoutService;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryReservationConcurrencyIT extends AbstractPostgresIntegrationTest {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private PlaceOrderTestDataFactory testDataFactory;

    @Autowired
    private CheckoutSessionRepository checkoutSessionRepository;

    @Autowired
    private InventoryReservationRepository inventoryReservationRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Test
    void prepareCheckout_concurrentReservationsForOneStock_allowsOnlyOneReservation() throws Exception {
        PlaceOrderTestDataFactory.SharedVariantFixture fixture =
                testDataFactory.createSharedVariantFixture(1, 1, 2);
        CyclicBarrier barrier = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            List<Future<ConcurrentCheckoutResult>> futures = fixture.actors().stream()
                    .map(actor -> executor.submit(prepareCheckoutTask(actor, barrier)))
                    .toList();

            List<ConcurrentCheckoutResult> results = futures.stream()
                    .map(this::getResult)
                    .toList();

            assertThat(results).filteredOn(ConcurrentCheckoutResult::success).hasSize(1);
            assertThat(results).filteredOn(result -> !result.success()).hasSize(1);
            assertThat(results)
                    .filteredOn(result -> !result.success())
                    .allSatisfy(result -> assertThat(result.failure()).isInstanceOf(InsufficientStockException.class));

            List<CheckoutSession> sessions = checkoutSessionRepository.findAll();
            assertThat(sessions).hasSize(1);
            assertThat(sessions.get(0).getStatus()).isEqualTo(CheckoutSessionStatus.reserved);

            List<InventoryReservation> reservations = inventoryReservationRepository.findAll();
            assertThat(reservations).hasSize(1);
            assertThat(reservations.get(0).getStatus()).isEqualTo(ReservationStatus.active);
            assertThat(reservations.get(0).getQuantity()).isEqualTo(1);

            ProductVariant variant = productVariantRepository.findById(fixture.productVariantId()).orElseThrow();
            assertThat(variant.getStockQuantity()).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    private Callable<ConcurrentCheckoutResult> prepareCheckoutTask(
            PlaceOrderTestDataFactory.CheckoutActor actor,
            CyclicBarrier barrier
    ) {
        return () -> {
            barrier.await(10, TimeUnit.SECONDS);
            try {
                ReservedCheckoutResult result = checkoutService.prepareCheckout(
                        actor.request(PaymentMethod.cod),
                        actor.userId()
                );
                return new ConcurrentCheckoutResult(true, result.checkoutCode(), null);
            } catch (RuntimeException e) {
                return new ConcurrentCheckoutResult(false, null, e);
            }
        };
    }

    private ConcurrentCheckoutResult getResult(Future<ConcurrentCheckoutResult> future) {
        try {
            return future.get(15, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new AssertionError("Concurrent checkout task did not finish", e);
        }
    }

    private record ConcurrentCheckoutResult(
            boolean success,
            String checkoutCode,
            Throwable failure
    ) {
    }
}
