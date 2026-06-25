package vn.hcmute.edu.dp.nhom10.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.VnPayIpnResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.VnPayReturnResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.VnPayIpnService;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.VnPayReturnService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VnPayCallbackControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VnPayReturnService returnService;

    @Mock
    private VnPayIpnService ipnService;

    @InjectMocks
    private VnPayCallbackController controller;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "frontendUrl", "http://localhost:5173");
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void handleReturn_redirectsToCheckoutResult() throws Exception {
        when(returnService.handleReturn(any())).thenReturn(new VnPayReturnResponseDTO(
                true, "PAY-1", "00", "00", "GTW-1", "CHK-1", null, "processing", "Payment status loaded"
        ));

        mockMvc.perform(get("/api/payments/vnpay/return").params(parameters()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/checkout/result")))
                .andExpect(redirectedUrlPattern("http://localhost:5173/checkout/result?status=pending&paymentMethod=vnpay&checkoutCode=CHK-1&paymentReference=PAY-1&gatewayTransactionId=GTW-1&message=*"));
    }

    @Test
    void handleIpn_returnsVnPayResponseDirectly() throws Exception {
        when(ipnService.handleIpn(any())).thenReturn(VnPayIpnResponse.confirmSuccess());

        mockMvc.perform(get("/api/payments/vnpay/ipn").params(parameters()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("00"))
                .andExpect(jsonPath("$.Message").value("Confirm Success"))
                .andExpect(jsonPath("$.status").doesNotExist())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    private MultiValueMap<String, String> parameters() {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("vnp_TxnRef", "PAY-1");
        return parameters;
    }
}
