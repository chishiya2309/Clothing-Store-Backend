package vn.hcmute.edu.dp.nhom10.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PlaceOrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.InventoryReservation;
import vn.hcmute.edu.dp.nhom10.backend.entity.Payment;
import vn.hcmute.edu.dp.nhom10.backend.entity.PaymentAttempt;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
import vn.hcmute.edu.dp.nhom10.backend.entity.VoucherReservation;
import vn.hcmute.edu.dp.nhom10.backend.enums.CheckoutSessionStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentAttemptStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.ReservationStatus;
import vn.hcmute.edu.dp.nhom10.backend.integration.support.PlaceOrderTestDataFactory;
import vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment.VnPaySignatureService;
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
import vn.hcmute.edu.dp.nhom10.backend.service.impl.VnPayIpnService;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.VnPayReturnService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
        "payment.vnpay.enabled=true",
        "payment.vnpay.tmn-code=TEST_TMN_CODE",
        "payment.vnpay.hash-secret=test-only-secret",
        "payment.vnpay.pay-url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html",
        "payment.vnpay.return-url=https://example.test/api/payments/vnpay/return"
})
class VnPayIpnIT extends AbstractPostgresIntegrationTest {

    private static final String SECRET = "test-only-secret";

    @Autowired
    private PlaceOrderService placeOrderService;

    @Autowired
    private VnPayIpnService ipnService;

    @Autowired
    private VnPayReturnService returnService;

    @Autowired
    private VnPaySignatureService signatureService;

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
    private PaymentRepository paymentRepository;

    @Test
    void ipn_success_createsOrderConsumesReservationsAndClearsCart() throws Exception {
        CheckoutContext context = createOnlineCheckout(true);

        assertThat(ipnService.handleIpn(successCallback(context.paymentAttempt())).rspCode()).isEqualTo("00");

        PaymentAttempt attempt = paymentAttemptRepository
                .findByPaymentReference(context.paymentAttempt().getPaymentReference())
                .orElseThrow();
        CheckoutSession checkoutSession = checkoutSessionRepository
                .findByCheckoutCode(context.checkoutCode())
                .orElseThrow();
        assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.completed);
        assertThat(attempt.getGatewayTransactionId()).isEqualTo("GTW-1");
        assertThat(attempt.getGatewayPayload()).containsKeys("vnp_Amount", "vnp_TxnRef", "vnp_TransactionNo");
        assertThat(attempt.getGatewayPayload()).doesNotContainKeys("vnp_SecureHash", "vnp_SecureHashType");
        assertThat(checkoutSession.getStatus()).isEqualTo(CheckoutSessionStatus.completed);

        assertThat(orderRepository.findAll()).hasSize(1);
        assertThat(orderItemRepository.findAll()).hasSize(1);
        Payment payment = paymentRepository.findAll().get(0);
        assertThat(payment.getMethod()).isEqualTo(PaymentMethod.vnpay);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.completed);
        assertThat(payment.getTransactionId()).isEqualTo("GTW-1");
        assertThat(payment.getPaymentData()).doesNotContainKeys("vnp_SecureHash", "vnp_SecureHashType");

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

    @Test
    void ipn_failure_marksAttemptFailedAndReleasesReservations() throws Exception {
        CheckoutContext context = createOnlineCheckout(true);

        assertThat(ipnService.handleIpn(failureCallback(context.paymentAttempt())).rspCode()).isEqualTo("00");

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
        assertThat(productVariantRepository.findById(context.fixture().productVariantId()).orElseThrow().getStockQuantity())
                .isEqualTo(5);
        assertThat(voucherRepository.findByCode(context.fixture().voucherCode()).orElseThrow().getTimesUsed())
                .isZero();
        assertThat(cartItemRepository.findAllByUserId(context.fixture().userId())).hasSize(1);
    }

    @Test
    void ipn_invalidSignature_doesNotChangeDatabase() throws Exception {
        CheckoutContext context = createOnlineCheckout(false);
        MultiValueMap<String, String> parameters = successCallback(context.paymentAttempt());
        parameters.set("vnp_SecureHash", "bad-signature");

        assertThat(ipnService.handleIpn(parameters).rspCode()).isEqualTo("97");

        assertUnchangedPendingOnlineCheckout(context);
    }

    @Test
    void ipn_amountMismatch_doesNotChangeDatabase() throws Exception {
        CheckoutContext context = createOnlineCheckout(false);
        MultiValueMap<String, String> parameters = successCallback(context.paymentAttempt());
        parameters.set("vnp_Amount", "99999900");
        parameters.set("vnp_SecureHash", signature(parameters));

        assertThat(ipnService.handleIpn(parameters).rspCode()).isEqualTo("04");

        assertUnchangedPendingOnlineCheckout(context);
    }

    @Test
    void ipn_duplicateSequential_returnsAlreadyProcessedAndCreatesOneOrder() throws Exception {
        CheckoutContext context = createOnlineCheckout(false);
        MultiValueMap<String, String> parameters = successCallback(context.paymentAttempt());

        assertThat(ipnService.handleIpn(parameters).rspCode()).isEqualTo("00");
        assertThat(ipnService.handleIpn(parameters).rspCode()).isEqualTo("02");

        assertThat(orderRepository.findAll()).hasSize(1);
        assertThat(paymentRepository.findAll()).hasSize(1);
    }

    @Test
    void ipn_duplicateConcurrent_createsOneOrderAndOnePayment() throws Exception {
        CheckoutContext context = createOnlineCheckout(false);
        MultiValueMap<String, String> parameters = successCallback(context.paymentAttempt());
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            futures.add(executor.submit(() -> {
                ready.countDown();
                start.await(5, TimeUnit.SECONDS);
                return ipnService.handleIpn(new LinkedMultiValueMap<>(parameters)).rspCode();
            }));
        }

        assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        List<String> responses = List.of(
                futures.get(0).get(10, TimeUnit.SECONDS),
                futures.get(1).get(10, TimeUnit.SECONDS)
        );
        executor.shutdownNow();

        assertThat(responses).containsExactlyInAnyOrder("00", "02");
        assertThat(orderRepository.findAll()).hasSize(1);
        assertThat(paymentRepository.findAll()).hasSize(1);
        ProductVariant variant = productVariantRepository.findById(context.fixture().productVariantId()).orElseThrow();
        assertThat(variant.getStockQuantity()).isEqualTo(4);
    }

    @Test
    void ipn_successAfterExpiration_marksRequiresRefundWithoutOrder() throws Exception {
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

        assertThat(ipnService.handleIpn(successCallback(attempt)).rspCode()).isEqualTo("00");

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
    void returnUrl_isReadOnlyBeforeAndAfterIpn() throws Exception {
        CheckoutContext context = createOnlineCheckout(false);
        MultiValueMap<String, String> parameters = successCallback(context.paymentAttempt());

        assertThat(returnService.handleReturn(parameters).signatureValid()).isTrue();
        assertThat(returnService.handleReturn(parameters).paymentStatus()).isEqualTo("processing");
        assertThat(orderRepository.findAll()).isEmpty();

        assertThat(ipnService.handleIpn(parameters).rspCode()).isEqualTo("00");
        assertThat(returnService.handleReturn(parameters).paymentStatus()).isEqualTo("success");

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
                fixture.request(PaymentMethod.vnpay),
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

    private MultiValueMap<String, String> successCallback(PaymentAttempt paymentAttempt) {
        return signedCallback(paymentAttempt, "00", "00");
    }

    private MultiValueMap<String, String> failureCallback(PaymentAttempt paymentAttempt) {
        return signedCallback(paymentAttempt, "24", "02");
    }

    private MultiValueMap<String, String> signedCallback(
            PaymentAttempt paymentAttempt,
            String responseCode,
            String transactionStatus
    ) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("vnp_Amount", toVnPayAmount(paymentAttempt.getAmount()));
        parameters.add("vnp_BankCode", "NCB");
        parameters.add("vnp_BankTranNo", "BANK-1");
        parameters.add("vnp_CardType", "ATM");
        parameters.add("vnp_OrderInfo", "Thanh toan " + paymentAttempt.getPaymentReference());
        parameters.add("vnp_PayDate", "20260619131500");
        parameters.add("vnp_ResponseCode", responseCode);
        parameters.add("vnp_TmnCode", "TEST_TMN_CODE");
        parameters.add("vnp_TransactionNo", "GTW-1");
        parameters.add("vnp_TransactionStatus", transactionStatus);
        parameters.add("vnp_TxnRef", paymentAttempt.getPaymentReference());
        parameters.add("vnp_SecureHash", signature(parameters));
        return parameters;
    }

    private String signature(MultiValueMap<String, String> parameters) {
        Map<String, String> flat = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            if (!"vnp_SecureHash".equals(entry.getKey())) {
                flat.put(entry.getKey(), entry.getValue().get(0));
            }
        }
        return signatureService.sign(SECRET, flat);
    }

    private String toVnPayAmount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).toBigIntegerExact().toString();
    }

    private record CheckoutContext(
            PlaceOrderTestDataFactory.CheckoutFixture fixture,
            String checkoutCode,
            PaymentAttempt paymentAttempt
    ) {
    }
}
