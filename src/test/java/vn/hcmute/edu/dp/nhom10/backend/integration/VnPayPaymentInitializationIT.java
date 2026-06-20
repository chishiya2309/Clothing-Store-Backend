package vn.hcmute.edu.dp.nhom10.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
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

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
        "test.fake-payment-gateway.enabled=false",
        "payment.vnpay.enabled=true",
        "payment.vnpay.tmn-code=TEST_TMN_CODE",
        "payment.vnpay.hash-secret=test-only-secret",
        "payment.vnpay.pay-url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html",
        "payment.vnpay.return-url=https://example.test/api/payments/vnpay/return"
})
class VnPayPaymentInitializationIT extends AbstractPostgresIntegrationTest {

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
    void confirmCheckout_vnpay_generatesSignedSandboxPaymentUrlWithoutCreatingOnlineOrder() {
        PlaceOrderTestDataFactory.CheckoutFixture fixture =
                testDataFactory.createCheckoutFixture(5, 1, true);

        PlaceOrderResponseDTO response = placeOrderService.confirmCheckout(
                fixture.request(PaymentMethod.vnpay),
                fixture.userId(),
                "203.0.113.10"
        );

        CheckoutSession checkoutSession = checkoutSessionRepository.findByCheckoutCode(response.checkoutCode()).orElseThrow();
        PaymentAttempt paymentAttempt = paymentAttemptRepository
                .findAllByCheckoutSessionId(checkoutSession.getId())
                .get(0);
        URI uri = URI.create(response.onlinePayment().paymentUrl());
        Map<String, String> query = query(uri);

        assertThat(response.paymentMethod()).isEqualTo(PaymentMethod.vnpay);
        assertThat(response.order()).isNull();
        assertThat(response.onlinePayment()).isNotNull();
        assertThat(response.onlinePayment().paymentReference()).isEqualTo(paymentAttempt.getPaymentReference());
        assertThat(response.onlinePayment().paymentUrl()).isEqualTo(paymentAttempt.getPaymentUrl());

        assertThat(uri.getHost()).isEqualTo("sandbox.vnpayment.vn");
        assertThat(uri.getPath()).isEqualTo("/paymentv2/vpcpay.html");
        assertThat(query).containsEntry("vnp_TmnCode", "TEST_TMN_CODE");
        assertThat(query).containsEntry("vnp_TxnRef", paymentAttempt.getPaymentReference());
        assertThat(query).containsEntry("vnp_ReturnUrl", "https://example.test/api/payments/vnpay/return");
        assertThat(query).containsEntry("vnp_IpAddr", "203.0.113.10");
        assertThat(query).containsKey("vnp_SecureHash");
        assertThat(query).doesNotContainKeys("vnp_IpnUrl", "vnp_SecureHashType");
        assertThat(response.onlinePayment().paymentUrl()).doesNotContain("test-only-secret");

        assertThat(checkoutSession.getStatus()).isEqualTo(CheckoutSessionStatus.reserved);
        assertThat(paymentAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.pending);
        assertThat(paymentAttempt.getAmount()).isEqualByComparingTo(checkoutSession.getTotalAmount());
        assertThat(paymentAttempt.getGatewayPayload()).containsEntry("txnRef", paymentAttempt.getPaymentReference());
        assertThat(paymentAttempt.getGatewayPayload()).doesNotContainKeys("hashSecret", "vnp_SecureHash");

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

    private Map<String, String> query(URI uri) {
        return Arrays.stream(uri.getRawQuery().split("&"))
                .map(part -> part.split("=", 2))
                .collect(Collectors.toMap(
                        pair -> URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                        pair -> URLDecoder.decode(pair[1], StandardCharsets.UTF_8)
                ));
    }
}
