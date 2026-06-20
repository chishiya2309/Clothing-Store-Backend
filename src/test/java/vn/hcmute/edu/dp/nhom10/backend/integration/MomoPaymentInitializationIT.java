package vn.hcmute.edu.dp.nhom10.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PlaceOrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.InventoryReservation;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.entity.VoucherReservation;
import vn.hcmute.edu.dp.nhom10.backend.enums.CheckoutSessionStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentAttemptStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReservationStatus;
import vn.hcmute.edu.dp.nhom10.backend.integration.support.PlaceOrderTestDataFactory;
import vn.hcmute.edu.dp.nhom10.backend.repository.CartItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.InventoryReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentAttemptRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.PlaceOrderService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MomoPaymentInitializationIT extends AbstractPostgresIntegrationTest {

    @Autowired
    private PlaceOrderService placeOrderService;

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
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    void confirmCheckout_momo_initializesOnlinePaymentWithoutCreatingOrder() {
        PlaceOrderTestDataFactory.CheckoutFixture fixture =
                testDataFactory.createCheckoutFixture(5, 1, true);

        PlaceOrderResponseDTO response = placeOrderService.confirmCheckout(
                fixture.request(PaymentMethod.momo),
                fixture.userId(),
                "203.0.113.10"
        );

        CheckoutSession checkoutSession = checkoutSessionRepository.findByCheckoutCode(response.checkoutCode()).orElseThrow();
        PaymentAttempt paymentAttempt = paymentAttemptRepository
                .findAllByCheckoutSessionId(checkoutSession.getId())
                .get(0);

        assertThat(response.paymentMethod()).isEqualTo(PaymentMethod.momo);
        assertThat(response.order()).isNull();
        assertThat(response.onlinePayment()).isNotNull();
        assertThat(response.onlinePayment().paymentReference()).isEqualTo(paymentAttempt.getPaymentReference());
        assertThat(response.onlinePayment().paymentUrl()).isEqualTo(paymentAttempt.getPaymentUrl());
        assertThat(response.onlinePayment().paymentUrl()).startsWith("https://test-payment.momo.vn/pay/");

        assertThat(checkoutSession.getStatus()).isEqualTo(CheckoutSessionStatus.reserved);
        assertThat(paymentAttempt.getMethod()).isEqualTo(PaymentMethod.momo);
        assertThat(paymentAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.pending);
        assertThat(paymentAttempt.getGatewayPayload()).containsEntry("gateway", "momo");
        assertThat(paymentAttempt.getGatewayPayload()).doesNotContainKeys("accessKey", "secretKey", "signature");
        assertThat(gatewayObservedPendingAttempt()).isTrue();

        List<InventoryReservation> inventoryReservations =
                inventoryReservationRepository.findAllByCheckoutSessionId(checkoutSession.getId());
        assertThat(inventoryReservations).hasSize(1);
        assertThat(inventoryReservations.get(0).getStatus()).isEqualTo(ReservationStatus.active);

        VoucherReservation voucherReservation =
                voucherReservationRepository.findByCheckoutSessionId(checkoutSession.getId()).orElseThrow();
        assertThat(voucherReservation.getStatus()).isEqualTo(ReservationStatus.active);

        ProductVariant variant = productVariantRepository.findById(fixture.productVariantId()).orElseThrow();
        assertThat(variant.getStockQuantity()).isEqualTo(5);

        Voucher voucher = voucherRepository.findByCode(fixture.voucherCode()).orElseThrow();
        assertThat(voucher.getTimesUsed()).isZero();

        assertThat(cartItemRepository.findAllByUserId(fixture.userId())).hasSize(1);
        assertThat(orderRepository.findAll()).isEmpty();
        assertThat(orderItemRepository.findAll()).isEmpty();
        assertThat(paymentRepository.findAll()).isEmpty();
    }
}
