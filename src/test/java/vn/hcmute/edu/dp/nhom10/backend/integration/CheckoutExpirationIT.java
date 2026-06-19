package vn.hcmute.edu.dp.nhom10.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PlaceOrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.InventoryReservation;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.entity.VoucherReservation;
import vn.hcmute.edu.dp.nhom10.backend.enums.CheckoutSessionStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentAttemptStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReservationStatus;
import vn.hcmute.edu.dp.nhom10.backend.integration.support.PlaceOrderTestDataFactory;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.InventoryReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentAttemptRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.CheckoutExpirationService;
import vn.hcmute.edu.dp.nhom10.backend.service.PlaceOrderService;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CheckoutExpirationIT extends AbstractPostgresIntegrationTest {

    @Autowired
    private PlaceOrderService placeOrderService;

    @Autowired
    private CheckoutExpirationService checkoutExpirationService;

    @Autowired
    private PlaceOrderTestDataFactory testDataFactory;

    @Autowired
    private CheckoutSessionRepository checkoutSessionRepository;

    @Autowired
    private InventoryReservationRepository inventoryReservationRepository;

    @Autowired
    private VoucherReservationRepository voucherReservationRepository;

    @Autowired
    private PaymentAttemptRepository paymentAttemptRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void expireDueCheckouts_expiresCheckoutReservationsAndPendingPaymentAttempt() {
        PlaceOrderTestDataFactory.CheckoutFixture fixture =
                testDataFactory.createCheckoutFixture(5, 1, true);
        PlaceOrderResponseDTO response = placeOrderService.confirmCheckout(
                fixture.request(PaymentMethod.vnpay),
                fixture.userId()
        );

        CheckoutSession checkoutSession = checkoutSessionRepository.findByCheckoutCode(response.checkoutCode()).orElseThrow();
        OffsetDateTime now = OffsetDateTime.now();
        checkoutSession.setExpiresAt(now.minusMinutes(1));
        checkoutSessionRepository.saveAndFlush(checkoutSession);

        int expiredCount = checkoutExpirationService.expireDueCheckouts(now);

        assertThat(expiredCount).isEqualTo(1);
        CheckoutSession expiredCheckout = checkoutSessionRepository.findById(checkoutSession.getId()).orElseThrow();
        assertThat(expiredCheckout.getStatus()).isEqualTo(CheckoutSessionStatus.expired);

        List<InventoryReservation> inventoryReservations =
                inventoryReservationRepository.findAllByCheckoutSessionId(checkoutSession.getId());
        assertThat(inventoryReservations).hasSize(1);
        assertThat(inventoryReservations.get(0).getStatus()).isEqualTo(ReservationStatus.expired);

        VoucherReservation voucherReservation =
                voucherReservationRepository.findByCheckoutSessionId(checkoutSession.getId()).orElseThrow();
        assertThat(voucherReservation.getStatus()).isEqualTo(ReservationStatus.expired);

        List<PaymentAttempt> paymentAttempts =
                paymentAttemptRepository.findAllByCheckoutSessionId(checkoutSession.getId());
        assertThat(paymentAttempts).hasSize(1);
        assertThat(paymentAttempts.get(0).getStatus()).isEqualTo(PaymentAttemptStatus.expired);

        ProductVariant variant = productVariantRepository.findById(fixture.productVariantId()).orElseThrow();
        assertThat(variant.getStockQuantity()).isEqualTo(5);
        assertThat(orderRepository.findAll()).isEmpty();
    }
}
