package vn.hcmute.edu.dp.nhom10.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.hcmute.edu.dp.nhom10.backend.entity.FlashSaleItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.FlashSaleReservation;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.exception.InsufficientStockException;
import vn.hcmute.edu.dp.nhom10.backend.integration.support.PlaceOrderTestDataFactory;
import vn.hcmute.edu.dp.nhom10.backend.repository.FlashSaleItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.FlashSaleReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.CheckoutService;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class FlashSaleReservationConcurrencyIT extends AbstractPostgresIntegrationTest {
    @Autowired CheckoutService checkoutService;
    @Autowired PlaceOrderTestDataFactory factory;
    @Autowired FlashSaleItemRepository itemRepository;
    @Autowired FlashSaleReservationRepository reservationRepository;

    @Test
    void prepareCheckout_twoCustomersCompeteForOneQuota_onlyOneSucceeds() throws Exception {
        var fixture = factory.createSharedVariantFixture(10, 1, 2);
        Long itemId = factory.createActiveFlashSaleForVariant(
                fixture.productVariantId(), 1, new BigDecimal("50000.00"));
        var barrier = new CyclicBarrier(2);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var futures = fixture.actors().stream().map(actor -> executor.submit(() -> {
                barrier.await(10, TimeUnit.SECONDS);
                try {
                    checkoutService.prepareCheckout(actor.request(PaymentMethod.cod), actor.userId());
                    return (Throwable) null;
                } catch (RuntimeException e) {
                    return e;
                }
            })).toList();
            List<Throwable> results = futures.stream().map(future -> {
                try { return future.get(15, TimeUnit.SECONDS); }
                catch (Exception e) { throw new AssertionError(e); }
            }).toList();

            assertThat(results).filteredOn(result -> result == null).hasSize(1);
            assertThat(results).filteredOn(result -> result != null).singleElement()
                    .isInstanceOf(InsufficientStockException.class);
            FlashSaleItem item = itemRepository.findById(itemId).orElseThrow();
            assertThat(item.getReservedQuantity()).isEqualTo(1);
            List<FlashSaleReservation> reservations = reservationRepository.findAll();
            assertThat(reservations).hasSize(1);
        } finally {
            executor.shutdownNow();
        }
    }
}
