package vn.hcmute.edu.dp.nhom10.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoIpnRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.MomoReturnResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PlaceOrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.InventoryReservation;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderStatusHistory;
import vn.hcmute.edu.dp.nhom10.backend.entity.Payment;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.entity.VoucherReservation;
import vn.hcmute.edu.dp.nhom10.backend.enums.CheckoutSessionStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentAttemptStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReservationStatus;
import vn.hcmute.edu.dp.nhom10.backend.integration.support.PlaceOrderTestDataFactory;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.MomoSignatureService;
import vn.hcmute.edu.dp.nhom10.backend.repository.CartItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.CheckoutSessionRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.InventoryReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderItemRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderStatusHistoryRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentAttemptRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.PaymentRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.ProductVariantRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherReservationRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.PlaceOrderService;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.MomoIpnService;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.MomoReturnService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
        "payment.momo.enabled=true",
        "payment.momo.partner-code=TEST_PARTNER",
        "payment.momo.access-key=test-access",
        "payment.momo.secret-key=test-secret",
        "payment.momo.redirect-url=https://example.test/api/payments/momo/return",
        "payment.momo.ipn-url=https://example.test/api/payments/momo/ipn"
})
class MomoIpnIT extends AbstractPostgresIntegrationTest {

    private static final String ACCESS_KEY = "test-access";
    private static final String SECRET = "test-secret";

    @Autowired
    private PlaceOrderService placeOrderService;

    @Autowired
    private MomoIpnService ipnService;

    @Autowired
    private MomoReturnService returnService;

    @Autowired
    private MomoSignatureService signatureService;

    @Autowired
    private PlaceOrderTestDataFactory testDataFactory;

    @Autowired
    private CheckoutSessionRepository checkoutSessionRepository;

    @Autowired
    private PaymentAttemptRepository paymentAttemptRepository;

    @Autowired
    private InventoryReservationRepository inventoryReservationRepository;

    @Autowired
    private VoucherReservationRepository voucherReservationRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void ipn_success_createsOrderConsumesReservationsAndClearsCart() {
        CheckoutContext context = createOnlineCheckout(true);

        assertThat(ipnService.handleIpn(callback(context.paymentAttempt(), 0))).isTrue();

        PaymentAttempt attempt = paymentAttemptRepository
                .findByPaymentReference(context.paymentAttempt().getPaymentReference())
                .orElseThrow();
        CheckoutSession checkoutSession = checkoutSessionRepository
                .findByCheckoutCode(context.checkoutCode())
                .orElseThrow();
        assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.completed);
        assertThat(attempt.getGatewayTransactionId()).isEqualTo("TRANS-1");
        assertThat(attempt.getGatewayPayload()).containsKeys("orderId", "requestId", "transId", "resultCode");
        assertThat(attempt.getGatewayPayload()).doesNotContainKeys("signature", "accessKey", "secretKey");
        assertThat(checkoutSession.getStatus()).isEqualTo(CheckoutSessionStatus.completed);

        List<Order> orders = orderRepository.findAll();
        assertThat(orders).hasSize(1);
        assertInitialStatusHistory(orders.get(0));
        assertThat(orderItemRepository.findAll()).hasSize(1);
        Payment payment = paymentRepository.findAll().get(0);
        assertThat(payment.getMethod()).isEqualTo(PaymentMethod.momo);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.completed);
        assertThat(payment.getTransactionId()).isEqualTo("TRANS-1");

        InventoryReservation inventoryReservation = inventoryReservationRepository
                .findAllByCheckoutSessionId(checkoutSession.getId())
                .get(0);
        assertThat(inventoryReservation.getStatus()).isEqualTo(ReservationStatus.consumed);
        ProductVariant variant = productVariantRepository.findById(context.fixture().productVariantId()).orElseThrow();
        assertThat(variant.getStockQuantity()).isEqualTo(4);

        VoucherReservation voucherReservation = voucherReservationRepository
                .findByCheckoutSessionId(checkoutSession.getId())
                .orElseThrow();
        assertThat(voucherReservation.getStatus()).isEqualTo(ReservationStatus.consumed);
        Voucher voucher = voucherRepository.findByCode(context.fixture().voucherCode()).orElseThrow();
        assertThat(voucher.getTimesUsed()).isEqualTo(1);
        assertThat(cartItemRepository.findAllByUserId(context.fixture().userId())).isEmpty();
        assertThat(orderCreatedEventProbe.events()).hasSize(1);
    }

    private void assertInitialStatusHistory(Order order) {
        List<OrderStatusHistory> histories =
                orderStatusHistoryRepository.findAllByOrder_IdOrderByCreatedAtAscIdAsc(order.getId());
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getFromStatus()).isNull();
        assertThat(histories.get(0).getToStatus()).isEqualTo(OrderStatus.pending);
        assertThat(histories.get(0).getChangedBy()).isNull();
        assertThat(histories.get(0).getChangedByRole()).isNull();
    }

    @Test
    void ipn_pending_keepsAttemptAndReservationPending() {
        CheckoutContext context = createOnlineCheckout(false);

        assertThat(ipnService.handleIpn(callback(context.paymentAttempt(), 7000))).isTrue();

        assertUnchangedPendingOnlineCheckout(context);
    }

    @Test
    void ipn_failure_marksAttemptFailedAndReleasesReservations() {
        CheckoutContext context = createOnlineCheckout(true);

        assertThat(ipnService.handleIpn(callback(context.paymentAttempt(), 49))).isTrue();

        PaymentAttempt attempt = paymentAttemptRepository
                .findByPaymentReference(context.paymentAttempt().getPaymentReference())
                .orElseThrow();
        CheckoutSession checkoutSession = checkoutSessionRepository
                .findByCheckoutCode(context.checkoutCode())
                .orElseThrow();
        assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.failed);
        assertThat(checkoutSession.getStatus()).isEqualTo(CheckoutSessionStatus.failed);
        assertThat(orderRepository.findAll()).isEmpty();
        assertThat(paymentRepository.findAll()).isEmpty();
        assertThat(inventoryReservationRepository.findAllByCheckoutSessionId(checkoutSession.getId()).get(0).getStatus())
                .isEqualTo(ReservationStatus.released);
        assertThat(voucherReservationRepository.findByCheckoutSessionId(checkoutSession.getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.released);
    }

    @Test
    void ipn_invalidSignature_doesNotChangeDatabase() {
        CheckoutContext context = createOnlineCheckout(false);
        MomoIpnRequest request = callback(context.paymentAttempt(), 0);
        MomoIpnRequest tampered = withSignature(request, "bad-signature");

        assertThat(ipnService.handleIpn(tampered)).isFalse();

        assertUnchangedPendingOnlineCheckout(context);
    }

    @Test
    void ipn_amountMismatch_doesNotChangeDatabase() {
        CheckoutContext context = createOnlineCheckout(false);
        MomoIpnRequest request = callback(context.paymentAttempt(), 0);
        MomoIpnRequest tampered = signed(new MomoIpnRequest(
                request.partnerCode(),
                request.orderId(),
                request.requestId(),
                999999,
                request.orderInfo(),
                request.orderType(),
                request.transId(),
                request.resultCode(),
                request.message(),
                request.payType(),
                request.responseTime(),
                request.extraData(),
                null
        ));

        assertThat(ipnService.handleIpn(tampered)).isFalse();

        assertUnchangedPendingOnlineCheckout(context);
    }

    @Test
    void ipn_duplicateSequential_createsOneOrderAndOnePayment() {
        CheckoutContext context = createOnlineCheckout(false);
        MomoIpnRequest request = callback(context.paymentAttempt(), 0);

        assertThat(ipnService.handleIpn(request)).isTrue();
        assertThat(ipnService.handleIpn(request)).isTrue();

        assertThat(orderRepository.findAll()).hasSize(1);
        assertThat(paymentRepository.findAll()).hasSize(1);
    }

    @Test
    void ipn_duplicateConcurrent_createsOneOrderAndOnePayment() throws Exception {
        CheckoutContext context = createOnlineCheckout(false);
        MomoIpnRequest request = callback(context.paymentAttempt(), 0);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            futures.add(executor.submit(() -> {
                ready.countDown();
                start.await(5, TimeUnit.SECONDS);
                return ipnService.handleIpn(request);
            }));
        }

        assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        assertThat(futures.get(0).get(10, TimeUnit.SECONDS)).isTrue();
        assertThat(futures.get(1).get(10, TimeUnit.SECONDS)).isTrue();
        executor.shutdownNow();

        assertThat(orderRepository.findAll()).hasSize(1);
        assertThat(paymentRepository.findAll()).hasSize(1);
        ProductVariant variant = productVariantRepository.findById(context.fixture().productVariantId()).orElseThrow();
        assertThat(variant.getStockQuantity()).isEqualTo(4);
    }

    @Test
    void ipn_successAfterExpiration_marksRequiresRefundWithoutOrder() {
        CheckoutContext context = createOnlineCheckout(false);
        CheckoutSession checkoutSession = checkoutSessionRepository
                .findByCheckoutCode(context.checkoutCode())
                .orElseThrow();
        checkoutSession.setStatus(CheckoutSessionStatus.expired);
        checkoutSession.setExpiresAt(OffsetDateTime.now().minusMinutes(1));
        checkoutSessionRepository.save(checkoutSession);
        PaymentAttempt attempt = paymentAttemptRepository
                .findByPaymentReference(context.paymentAttempt().getPaymentReference())
                .orElseThrow();
        attempt.setStatus(PaymentAttemptStatus.expired);
        paymentAttemptRepository.save(attempt);

        assertThat(ipnService.handleIpn(callback(attempt, 0))).isTrue();

        PaymentAttempt updatedAttempt = paymentAttemptRepository
                .findByPaymentReference(attempt.getPaymentReference())
                .orElseThrow();
        assertThat(updatedAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.requires_refund);
        assertThat(orderRepository.findAll()).isEmpty();
        assertThat(paymentRepository.findAll()).isEmpty();
        ProductVariant variant = productVariantRepository.findById(context.fixture().productVariantId()).orElseThrow();
        assertThat(variant.getStockQuantity()).isEqualTo(5);
    }

    @Test
    void returnUrl_isReadOnlyBeforeAndAfterIpn() {
        CheckoutContext context = createOnlineCheckout(false);
        MomoIpnRequest request = callback(context.paymentAttempt(), 0);

        MomoReturnResponseDTO beforeIpn = returnService.handleReturn(parameters(request));
        assertThat(beforeIpn.signatureValid()).isTrue();
        assertThat(beforeIpn.paymentStatus()).isEqualTo("processing");
        assertThat(orderRepository.findAll()).isEmpty();

        assertThat(ipnService.handleIpn(request)).isTrue();

        MomoReturnResponseDTO afterIpn = returnService.handleReturn(parameters(request));
        assertThat(afterIpn.paymentStatus()).isEqualTo("success");
        assertThat(orderRepository.findAll()).hasSize(1);
    }

    private void assertUnchangedPendingOnlineCheckout(CheckoutContext context) {
        PaymentAttempt attempt = paymentAttemptRepository
                .findByPaymentReference(context.paymentAttempt().getPaymentReference())
                .orElseThrow();
        CheckoutSession checkoutSession = checkoutSessionRepository
                .findByCheckoutCode(context.checkoutCode())
                .orElseThrow();
        assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.pending);
        assertThat(checkoutSession.getStatus()).isEqualTo(CheckoutSessionStatus.reserved);
        assertThat(orderRepository.findAll()).isEmpty();
        assertThat(paymentRepository.findAll()).isEmpty();
        assertThat(inventoryReservationRepository.findAllByCheckoutSessionId(checkoutSession.getId()).get(0).getStatus())
                .isEqualTo(ReservationStatus.active);
    }

    private CheckoutContext createOnlineCheckout(boolean withVoucher) {
        PlaceOrderTestDataFactory.CheckoutFixture fixture =
                testDataFactory.createCheckoutFixture(5, 1, withVoucher);
        PlaceOrderResponseDTO response = placeOrderService.confirmCheckout(
                fixture.request(PaymentMethod.momo),
                fixture.userId(),
                "203.0.113.10"
        );
        CheckoutSession checkoutSession = checkoutSessionRepository
                .findByCheckoutCode(response.checkoutCode())
                .orElseThrow();
        PaymentAttempt paymentAttempt = paymentAttemptRepository
                .findAllByCheckoutSessionId(checkoutSession.getId())
                .get(0);
        return new CheckoutContext(fixture, response.checkoutCode(), paymentAttempt);
    }

    private MomoIpnRequest callback(PaymentAttempt paymentAttempt, int resultCode) {
        return signed(new MomoIpnRequest(
                "TEST_PARTNER",
                paymentAttempt.getPaymentReference(),
                paymentAttempt.getPaymentReference(),
                paymentAttempt.getAmount().longValueExact(),
                "Thanh toan don hang",
                "momo_wallet",
                "TRANS-1",
                resultCode,
                resultCode == 0 ? "Successful." : "Result " + resultCode,
                "qr",
                1718770000000L,
                "",
                null
        ));
    }

    private MomoIpnRequest signed(MomoIpnRequest request) {
        String signature = signatureService.sign(
                SECRET,
                signatureService.ipnRawSignature(ACCESS_KEY, request)
        );
        return withSignature(request, signature);
    }

    private MomoIpnRequest withSignature(MomoIpnRequest request, String signature) {
        return new MomoIpnRequest(
                request.partnerCode(),
                request.orderId(),
                request.requestId(),
                request.amount(),
                request.orderInfo(),
                request.orderType(),
                request.transId(),
                request.resultCode(),
                request.message(),
                request.payType(),
                request.responseTime(),
                request.extraData(),
                signature
        );
    }

    private MultiValueMap<String, String> parameters(MomoIpnRequest request) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("partnerCode", request.partnerCode());
        parameters.add("orderId", request.orderId());
        parameters.add("requestId", request.requestId());
        parameters.add("amount", Long.toString(request.amount()));
        parameters.add("orderInfo", request.orderInfo());
        parameters.add("orderType", request.orderType());
        parameters.add("transId", request.transId());
        parameters.add("resultCode", Integer.toString(request.resultCode()));
        parameters.add("message", request.message());
        parameters.add("payType", request.payType());
        parameters.add("responseTime", Long.toString(request.responseTime()));
        parameters.add("extraData", request.extraData());
        parameters.add("signature", request.signature());
        return parameters;
    }

    private record CheckoutContext(
            PlaceOrderTestDataFactory.CheckoutFixture fixture,
            String checkoutCode,
            PaymentAttempt paymentAttempt
    ) {
    }
}
