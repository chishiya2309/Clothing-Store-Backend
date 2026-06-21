package vn.hcmute.edu.dp.nhom10.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PlaceOrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.event.OrderCreatedEvent;
import vn.hcmute.edu.dp.nhom10.backend.integration.support.PlaceOrderTestDataFactory;
import vn.hcmute.edu.dp.nhom10.backend.repository.OrderRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.PlaceOrderService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCreatedEventIT extends AbstractPostgresIntegrationTest {

    @Autowired
    private PlaceOrderService placeOrderService;

    @Autowired
    private PlaceOrderTestDataFactory testDataFactory;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void confirmCheckout_cod_publishesOrderCreatedEventAfterCommit() {
        PlaceOrderTestDataFactory.CheckoutFixture fixture =
                testDataFactory.createCheckoutFixture(3, 1, false);

        PlaceOrderResponseDTO response = placeOrderService.confirmCheckout(
                fixture.request(PaymentMethod.cod),
                fixture.userId(),
                "203.0.113.10"
        );

        List<OrderCreatedEvent> events = orderCreatedEventProbe.events();
        assertThat(events).hasSize(1);
        OrderCreatedEvent event = events.get(0);
        assertThat(event.orderCode()).isEqualTo(response.order().getOrderCode());
        assertThat(event.userId()).isEqualTo(fixture.userId());
        assertThat(event.totalAmount()).isEqualByComparingTo(fixture.subtotal());
        assertThat(orderRepository.findById(event.orderId())).isPresent();
    }
}
