package vn.hcmute.edu.dp.nhom10.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.hcmute.edu.dp.nhom10.backend.dto.checkout.ReservedCheckoutResult;
import vn.hcmute.edu.dp.nhom10.backend.entity.CheckoutSession;
import vn.hcmute.edu.dp.nhom10.backend.entity.InventoryReservation;
import vn.hcmute.edu.dp.nhom10.backend.entity.ProductVariant;
import vn.hcmute.edu.dp.nhom10.backend.entity.Voucher;
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
import vn.hcmute.edu.dp.nhom10.backend.repository.VoucherRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.CheckoutService;
import vn.hcmute.edu.dp.nhom10.backend.service.OrderService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CodOrderRollbackIT extends AbstractPostgresIntegrationTest {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PlaceOrderTestDataFactory testDataFactory;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private CheckoutSessionRepository checkoutSessionRepository;

    @Autowired
    private InventoryReservationRepository inventoryReservationRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    void createCodOrder_whenVoucherConsumeFails_rollsBackOrderInventoryAndCartChanges() {
        PlaceOrderTestDataFactory.CheckoutFixture fixture =
                testDataFactory.createCheckoutFixture(4, 2, true);
        ReservedCheckoutResult checkout = checkoutService.prepareCheckout(
                fixture.request(PaymentMethod.cod),
                fixture.userId()
        );

        Voucher voucher = voucherRepository.findByCode(fixture.voucherCode()).orElseThrow();
        voucher.setTimesUsed(voucher.getUsageLimit());
        voucherRepository.saveAndFlush(voucher);

        assertThatThrownBy(() -> orderService.createCodOrder(checkout.checkoutCode(), fixture.userId()))
                .isInstanceOf(InvalidDataException.class);

        CheckoutSession checkoutSession =
                checkoutSessionRepository.findByCheckoutCode(checkout.checkoutCode()).orElseThrow();
        assertThat(checkoutSession.getStatus()).isEqualTo(CheckoutSessionStatus.reserved);
        assertThat(orderRepository.findAll()).isEmpty();
        assertThat(paymentRepository.findAll()).isEmpty();
        assertThat(cartItemRepository.findAllByUserId(fixture.userId())).hasSize(1);

        List<InventoryReservation> reservations =
                inventoryReservationRepository.findAllByCheckoutSessionId(checkoutSession.getId());
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).getStatus()).isEqualTo(ReservationStatus.active);

        ProductVariant variant = productVariantRepository.findById(fixture.productVariantId()).orElseThrow();
        assertThat(variant.getStockQuantity()).isEqualTo(4);
    }
}
