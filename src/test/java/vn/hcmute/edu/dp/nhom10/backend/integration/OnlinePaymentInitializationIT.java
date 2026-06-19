package vn.hcmute.edu.dp.nhom10.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PlaceOrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.InventoryReservation;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;
import vn.hcmute.edu.dp.nhom10.backend.enums.CheckoutSessionStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentAttemptStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReservationStatus;
import vn.hcmute.edu.dp.nhom10.backend.exception.PaymentInitializationException;
import vn.hcmute.edu.dp.nhom10.backend.integration.support.PlaceOrderTestDataFactory;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.InventoryReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentAttemptRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.PlaceOrderService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OnlinePaymentInitializationIT extends AbstractPostgresIntegrationTest {

    @Autowired
    private PlaceOrderService placeOrderService;

    @Autowired
    private PlaceOrderTestDataFactory testDataFactory;

    @Autowired
    private CheckoutSessionRepository checkoutSessionRepository;

    @Autowired
    private InventoryReservationRepository inventoryReservationRepository;

    @Autowired
    private PaymentAttemptRepository paymentAttemptRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void confirmCheckout_vnpay_createsCommittedPendingAttemptBeforeGatewayCall() {
        PlaceOrderTestDataFactory.CheckoutFixture fixture =
                testDataFactory.createCheckoutFixture(5, 1, false);

        PlaceOrderResponseDTO response = placeOrderService.confirmCheckout(
                fixture.request(PaymentMethod.vnpay),
                fixture.userId(),
                "203.0.113.10"
        );

        assertThat(response.paymentMethod()).isEqualTo(PaymentMethod.vnpay);
        assertThat(response.order()).isNull();
        assertThat(response.onlinePayment()).isNotNull();
        assertThat(response.onlinePayment().paymentUrl()).startsWith("https://gateway.test/pay/");
        assertThat(gatewayObservedPendingAttempt()).isTrue();

        CheckoutSession checkoutSession = checkoutSessionRepository.findByCheckoutCode(response.checkoutCode()).orElseThrow();
        assertThat(checkoutSession.getStatus()).isEqualTo(CheckoutSessionStatus.reserved);

        List<PaymentAttempt> attempts = paymentAttemptRepository.findAllByCheckoutSessionId(checkoutSession.getId());
        assertThat(attempts).hasSize(1);
        assertThat(attempts.get(0).getStatus()).isEqualTo(PaymentAttemptStatus.pending);
        assertThat(attempts.get(0).getPaymentUrl()).isEqualTo(response.onlinePayment().paymentUrl());
        assertThat(attempts.get(0).getAmount()).isEqualByComparingTo(fixture.subtotal());

        List<InventoryReservation> reservations =
                inventoryReservationRepository.findAllByCheckoutSessionId(checkoutSession.getId());
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).getStatus()).isEqualTo(ReservationStatus.active);
        assertThat(orderRepository.findAll()).isEmpty();
        assertThat(paymentRepository.findAll()).isEmpty();
    }

    @Test
    void confirmCheckout_whenGatewayFails_marksCheckoutFailedAndReleasesReservation() {
        PlaceOrderTestDataFactory.CheckoutFixture fixture =
                testDataFactory.createCheckoutFixture(5, 1, false);
        makeGatewayFail();

        assertThatThrownBy(() -> placeOrderService.confirmCheckout(
                fixture.request(PaymentMethod.vnpay),
                fixture.userId(),
                "203.0.113.10"
        )).isInstanceOf(PaymentInitializationException.class);

        List<CheckoutSession> sessions = checkoutSessionRepository.findAll();
        assertThat(sessions).hasSize(1);
        assertThat(sessions.get(0).getStatus()).isEqualTo(CheckoutSessionStatus.failed);

        List<PaymentAttempt> attempts = paymentAttemptRepository.findAllByCheckoutSessionId(sessions.get(0).getId());
        assertThat(attempts).hasSize(1);
        assertThat(attempts.get(0).getStatus()).isEqualTo(PaymentAttemptStatus.failed);

        List<InventoryReservation> reservations =
                inventoryReservationRepository.findAllByCheckoutSessionId(sessions.get(0).getId());
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).getStatus()).isEqualTo(ReservationStatus.released);
        assertThat(orderRepository.findAll()).isEmpty();
        assertThat(paymentRepository.findAll()).isEmpty();
    }
}
