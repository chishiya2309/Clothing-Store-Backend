package vn.hcmute.edu.dp.nhom10.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoIpnRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.MomoReturnResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.enums.PaymentAttemptStatus;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.MomoIpnService;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.MomoReturnService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MomoCallbackControllerTest {

    private final MomoReturnService returnService = mock(MomoReturnService.class);
    private final MomoIpnService ipnService = mock(MomoIpnService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new MomoCallbackController(returnService, ipnService))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void handleReturn_redirectsToCheckoutResult() throws Exception {
        when(returnService.handleReturn(any())).thenReturn(new MomoReturnResponseDTO(
                true, "PAY-1", 0, "TRANS-1", "CHK-1", PaymentAttemptStatus.completed, "success", "ok"));

        mockMvc.perform(get("/api/payments/momo/return")
                        .param("orderId", "PAY-1"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("http://localhost:5173/checkout/result?status=success&paymentMethod=momo&checkoutCode=CHK-1&paymentReference=PAY-1&gatewayTransactionId=TRANS-1&message=*"));
    }

    @Test
    void handleIpn_validRequestReturns204WithoutBody() throws Exception {
        MomoIpnRequest request = request();
        when(ipnService.handleIpn(request)).thenReturn(true);

        mockMvc.perform(post("/api/payments/momo/ipn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void handleIpn_invalidRequestReturns400WithoutBody() throws Exception {
        MomoIpnRequest request = request();
        when(ipnService.handleIpn(request)).thenReturn(false);

        mockMvc.perform(post("/api/payments/momo/ipn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    private MomoIpnRequest request() {
        return new MomoIpnRequest(
                "TEST_PARTNER",
                "PAY-1",
                "PAY-1",
                100000,
                "Thanh toan don hang CHK-1",
                "momo_wallet",
                "TRANS-1",
                0,
                "Successful.",
                "qr",
                1718770000000L,
                "",
                "signature"
        );
    }
}
