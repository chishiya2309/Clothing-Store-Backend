package vn.hcmute.edu.dp.nhom10.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.ConfirmCheckoutRequestDTO;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.OnlinePaymentResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.OrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PlaceOrderResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentMethod;
import vn.hcmute.edu.dp.nhom10.backend.exception.GlobalExceptionHandling;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.PaymentGatewayUnavailableException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.security.AuthenticatedUserProvider;
import vn.hcmute.edu.dp.nhom10.backend.security.ClientIpResolver;
import vn.hcmute.edu.dp.nhom10.backend.service.PlaceOrderService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

    private static final String CLIENT_IP = "203.0.113.10";

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PlaceOrderService placeOrderService;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private ClientIpResolver clientIpResolver;

    @InjectMocks
    private CheckoutController checkoutController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(checkoutController)
                .setControllerAdvice(new GlobalExceptionHandling())
                .setValidator(validator)
                .build();
    }

    @Test
    void confirmCheckout_authenticatedCod_returnsSuccess() throws Exception {
        Authentication authentication = authentication();
        ConfirmCheckoutRequestDTO request = request(PaymentMethod.cod);
        when(authenticatedUserProvider.getCurrentUserId(authentication)).thenReturn(10L);
        when(clientIpResolver.resolve(org.mockito.ArgumentMatchers.any())).thenReturn(CLIENT_IP);
        when(placeOrderService.confirmCheckout(eq(request), eq(10L), eq(CLIENT_IP))).thenReturn(codResponse());

        mockMvc.perform(post("/api/checkouts/confirm")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.checkoutCode").value("CHK-1"))
                .andExpect(jsonPath("$.data.paymentMethod").value("cod"))
                .andExpect(jsonPath("$.data.order.orderCode").value("ORD-1"))
                .andExpect(jsonPath("$.data.onlinePayment").value(nullValue()));
    }

    @Test
    void confirmCheckout_authenticatedOnline_returnsPaymentUrl() throws Exception {
        Authentication authentication = authentication();
        ConfirmCheckoutRequestDTO request = request(PaymentMethod.vnpay);
        when(authenticatedUserProvider.getCurrentUserId(authentication)).thenReturn(10L);
        when(clientIpResolver.resolve(org.mockito.ArgumentMatchers.any())).thenReturn(CLIENT_IP);
        when(placeOrderService.confirmCheckout(eq(request), eq(10L), eq(CLIENT_IP))).thenReturn(onlineResponse());

        mockMvc.perform(post("/api/checkouts/confirm")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentMethod").value("vnpay"))
                .andExpect(jsonPath("$.data.order").value(nullValue()))
                .andExpect(jsonPath("$.data.onlinePayment.paymentReference").value("PAY-1"))
                .andExpect(jsonPath("$.data.onlinePayment.paymentUrl").value("https://pay.test/checkout"));
    }

    @Test
    void confirmCheckout_usesAuthenticationUserId() throws Exception {
        Authentication authentication = authentication();
        ConfirmCheckoutRequestDTO request = request(PaymentMethod.cod);
        when(authenticatedUserProvider.getCurrentUserId(authentication)).thenReturn(10L);
        when(clientIpResolver.resolve(org.mockito.ArgumentMatchers.any())).thenReturn(CLIENT_IP);
        when(placeOrderService.confirmCheckout(eq(request), eq(10L), eq(CLIENT_IP))).thenReturn(codResponse());

        mockMvc.perform(post("/api/checkouts/confirm")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authenticatedUserProvider).getCurrentUserId(authentication);
        verify(placeOrderService).confirmCheckout(request, 10L, CLIENT_IP);
        verifyNoMoreInteractions(placeOrderService);
    }

    @Test
    void confirmCheckout_passesResolvedClientIpToService() throws Exception {
        Authentication authentication = authentication();
        ConfirmCheckoutRequestDTO request = request(PaymentMethod.vnpay);
        when(authenticatedUserProvider.getCurrentUserId(authentication)).thenReturn(10L);
        when(clientIpResolver.resolve(org.mockito.ArgumentMatchers.any())).thenReturn(CLIENT_IP);
        when(placeOrderService.confirmCheckout(eq(request), eq(10L), eq(CLIENT_IP))).thenReturn(onlineResponse());

        mockMvc.perform(post("/api/checkouts/confirm")
                        .principal(authentication)
                        .header("X-Forwarded-For", "203.0.113.10, 10.0.0.1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(placeOrderService).confirmCheckout(request, 10L, CLIENT_IP);
    }

    @Test
    void confirmCheckout_missingAddressId_returnsBadRequest() throws Exception {
        ConfirmCheckoutRequestDTO request = new ConfirmCheckoutRequestDTO(null, null, PaymentMethod.cod);

        mockMvc.perform(post("/api/checkouts/confirm")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmCheckout_missingPaymentMethod_returnsBadRequest() throws Exception {
        ConfirmCheckoutRequestDTO request = new ConfirmCheckoutRequestDTO(1L, null, null);

        mockMvc.perform(post("/api/checkouts/confirm")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmCheckout_gatewayUnavailable_returnsServiceUnavailable() throws Exception {
        Authentication authentication = authentication();
        ConfirmCheckoutRequestDTO request = request(PaymentMethod.vnpay);
        when(authenticatedUserProvider.getCurrentUserId(authentication)).thenReturn(10L);
        when(clientIpResolver.resolve(org.mockito.ArgumentMatchers.any())).thenReturn(CLIENT_IP);
        when(placeOrderService.confirmCheckout(eq(request), eq(10L), eq(CLIENT_IP)))
                .thenThrow(new PaymentGatewayUnavailableException("Gateway unavailable"));

        mockMvc.perform(post("/api/checkouts/confirm")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void confirmCheckout_serviceConflict_returnsConflict() throws Exception {
        Authentication authentication = authentication();
        ConfirmCheckoutRequestDTO request = request(PaymentMethod.cod);
        when(authenticatedUserProvider.getCurrentUserId(authentication)).thenReturn(10L);
        when(clientIpResolver.resolve(org.mockito.ArgumentMatchers.any())).thenReturn(CLIENT_IP);
        when(placeOrderService.confirmCheckout(eq(request), eq(10L), eq(CLIENT_IP)))
                .thenThrow(new InvalidDataException("Checkout conflict"));

        mockMvc.perform(post("/api/checkouts/confirm")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void confirmCheckout_resourceNotFound_returnsNotFound() throws Exception {
        Authentication authentication = authentication();
        ConfirmCheckoutRequestDTO request = request(PaymentMethod.cod);
        when(authenticatedUserProvider.getCurrentUserId(authentication)).thenReturn(10L);
        when(clientIpResolver.resolve(org.mockito.ArgumentMatchers.any())).thenReturn(CLIENT_IP);
        when(placeOrderService.confirmCheckout(eq(request), eq(10L), eq(CLIENT_IP)))
                .thenThrow(new ResourceNotFoundException("Checkout not found"));

        mockMvc.perform(post("/api/checkouts/confirm")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void confirmCheckout_controllerCallsOnlyPlaceOrderServiceForBusinessFlow() throws Exception {
        Authentication authentication = authentication();
        ConfirmCheckoutRequestDTO request = request(PaymentMethod.cod);
        when(authenticatedUserProvider.getCurrentUserId(authentication)).thenReturn(10L);
        when(clientIpResolver.resolve(org.mockito.ArgumentMatchers.any())).thenReturn(CLIENT_IP);
        when(placeOrderService.confirmCheckout(eq(request), eq(10L), eq(CLIENT_IP))).thenReturn(codResponse());

        mockMvc.perform(post("/api/checkouts/confirm")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(placeOrderService).confirmCheckout(request, 10L, CLIENT_IP);
    }

    private Authentication authentication() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("customer@test.com", null);
        authentication.setAuthenticated(true);
        return authentication;
    }

    private ConfirmCheckoutRequestDTO request(PaymentMethod paymentMethod) {
        return new ConfirmCheckoutRequestDTO(1L, null, paymentMethod);
    }

    private PlaceOrderResponseDTO codResponse() {
        return PlaceOrderResponseDTO.forCod(
                "CHK-1",
                OrderResponseDTO.builder()
                        .orderCode("ORD-1")
                        .subtotal(money("100000.00"))
                        .shippingFee(money("20000.00"))
                        .discountAmount(BigDecimal.ZERO)
                        .totalAmount(money("120000.00"))
                        .status(OrderStatus.pending)
                        .build()
        );
    }

    private PlaceOrderResponseDTO onlineResponse() {
        return PlaceOrderResponseDTO.forOnline(
                "CHK-1",
                PaymentMethod.vnpay,
                new OnlinePaymentResponseDTO(
                        "PAY-1",
                        "https://pay.test/checkout",
                        money("120000.00"),
                        OffsetDateTime.now().plusMinutes(15)
                )
        );
    }

    private BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
