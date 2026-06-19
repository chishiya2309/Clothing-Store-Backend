package vn.hcmute.edu.dp.nhom10.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PlaceOrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.InventoryReservation;
import vn.hcmute.edu.dp.nhom10.backend.entity.Order;
import vn.hcmute.edu.dp.nhom10.backend.entity.OrderItem;
import vn.hcmute.edu.dp.nhom10.backend.entity.Payment;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.enums.CheckoutSessionStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentStatus;
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
import vn.hcmute.edu.dp.nhom10.backend.service.PlaceOrderService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceOrderCodIT extends AbstractPostgresIntegrationTest {

    @Autowired
    private PlaceOrderService placeOrderService;

    @Autowired
    private PlaceOrderTestDataFactory testDataFactory;

    @Autowired
    private CheckoutSessionRepository checkoutSessionRepository;

    @Autowired
    private InventoryReservationRepository inventoryReservationRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentAttemptRepository paymentAttemptRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    void confirmCheckout_cod_createsOrderConsumesReservationAndClearsCart() {
        PlaceOrderTestDataFactory.CheckoutFixture fixture =
                testDataFactory.createCheckoutFixture(5, 2, false);

        PlaceOrderResponseDTO response = placeOrderService.confirmCheckout(
                fixture.request(PaymentMethod.cod),
                fixture.userId()
        );

        assertThat(response.paymentMethod()).isEqualTo(PaymentMethod.cod);
        assertThat(response.order()).isNotNull();
        assertThat(response.onlinePayment()).isNull();
        assertThat(response.order().getStatus()).isEqualTo(OrderStatus.pending);
        assertThat(response.order().getSubtotal()).isEqualByComparingTo(fixture.subtotal());
        assertThat(response.order().getShippingFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.order().getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.order().getTotalAmount()).isEqualByComparingTo(fixture.subtotal());

        CheckoutSession checkoutSession = checkoutSessionRepository.findByCheckoutCode(response.checkoutCode()).orElseThrow();
        assertThat(checkoutSession.getStatus()).isEqualTo(CheckoutSessionStatus.completed);

        List<Order> orders = orderRepository.findAll();
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getOrderCode()).isEqualTo(response.order().getOrderCode());

        List<OrderItem> orderItems = orderItemRepository.findAll();
        assertThat(orderItems).hasSize(1);
        assertThat(orderItems.get(0).getProductVariant().getId()).isEqualTo(fixture.productVariantId());
        assertThat(orderItems.get(0).getQuantity()).isEqualTo(fixture.quantity());
        assertThat(orderItems.get(0).getUnitPrice()).isEqualByComparingTo(fixture.unitPrice());

        List<Payment> payments = paymentRepository.findAll();
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getMethod()).isEqualTo(PaymentMethod.cod);
        assertThat(payments.get(0).getStatus()).isEqualTo(PaymentStatus.pending);
        assertThat(payments.get(0).getAmount()).isEqualByComparingTo(fixture.subtotal());

        List<InventoryReservation> reservations =
                inventoryReservationRepository.findAllByCheckoutSessionId(checkoutSession.getId());
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).getStatus()).isEqualTo(ReservationStatus.consumed);

        ProductVariant variant = productVariantRepository.findById(fixture.productVariantId()).orElseThrow();
        assertThat(variant.getStockQuantity()).isEqualTo(3);
        assertThat(cartItemRepository.findAllByUserId(fixture.userId())).isEmpty();
        assertThat(paymentAttemptRepository.findAll()).isEmpty();
    }
}
